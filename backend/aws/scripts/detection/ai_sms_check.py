import boto3
import json
import joblib
import logging
import numpy as np
import onnxruntime as ort
import os
from common.notification import Verdict
from transformers import AutoTokenizer

# Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Environment variables
REQUIRED_VARS = ['MODEL_BUCKET_NAME']
for var in REQUIRED_VARS:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")

MODEL_BUCKET_NAME = os.environ.get('MODEL_BUCKET_NAME')
REGION_NAME = os.environ.get('REGION_NAME', 'eu-west-3')

# Constants
LOCAL_PATH = '/tmp/model'
LGBM_MODEL_FILE = 'lgbm_model.pkl'
LGBM_CONFIG_FILE = 'lgbm_config.json'
DISTILBERT_MODEL_FILE = 'distilbert_model.onnx'
DISTILBERT_TOKENIZER_FILES = ['tokenizer.json', 'tokenizer_config.json', 'special_tokens_map.json', 'vocab.txt']
META_MODEL_FILE = 'meta_model.pkl'
META_CONFIG_FILE = 'meta_config.json'

# Initialize resources
s3 = boto3.client('s3', region_name=REGION_NAME)
lgbm_model = None
lgbm_config = None
ort_session = None
distilbert_tokenizer = None
meta_model = None
meta_config = None

def download_models_from_s3() -> None:
    '''
    Downloads the model files from the specified S3 bucket to the local path.

    Raises
    ------
    Exception
        If there is an error during download.
    '''
    if not os.path.exists(LOCAL_PATH):
        os.makedirs(LOCAL_PATH)
    
    files = [LGBM_MODEL_FILE, LGBM_CONFIG_FILE, DISTILBERT_MODEL_FILE] + DISTILBERT_TOKENIZER_FILES + [META_MODEL_FILE, META_CONFIG_FILE]
    
    for f in files:
        dest = os.path.join(LOCAL_PATH, f)
        if not os.path.exists(dest):
            try:
                logger.info(f"Downloading {f} from S3 bucket {MODEL_BUCKET_NAME}...")
                s3.download_file(MODEL_BUCKET_NAME, f, dest)
            except Exception as e:
                logger.error(f"Failed to download '{f}': {e}", exc_info=True)
                raise

def init_inference_engine() -> None:
    '''
    Initializes the tokenizer and ONNX inference session.

    Raises
    ------
    Exception
        If there is an error during initialization.
    '''
    global lgbm_model, lgbm_config, ort_session, distilbert_tokenizer, meta_model, meta_config

    download_models_from_s3()

    if lgbm_model is None:
        logger.info("Loading LightGBM model...")
        try:
            lgbm_model = joblib.load(os.path.join(LOCAL_PATH, LGBM_MODEL_FILE))
            logger.info("LightGBM model loaded successfully.")
        except Exception as e:
            logger.error(f"Error loading LightGBM model: {e}", exc_info=True)
            raise

    if lgbm_config is None:
        logger.info("Loading LightGBM config...")
        try:
            with open(os.path.join(LOCAL_PATH, LGBM_CONFIG_FILE), 'r') as f:
                lgbm_config = json.load(f)
            logger.info("LightGBM config loaded successfully.")
        except Exception as e:
            logger.error(f"Error loading LightGBM config: {e}", exc_info=True)
            raise


    if ort_session is None:
        try:
            logger.info("Initializing ONNX inference session...")
            sess_options = ort.SessionOptions()
            sess_options.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_EXTENDED
            ort_session = ort.InferenceSession(os.path.join(LOCAL_PATH, DISTILBERT_MODEL_FILE), sess_options, providers=["CPUExecutionProvider"])
            logger.info("ONNX inference session initialized successfully.")
        except Exception as e:
            logger.error(f"Error initializing ONNX inference session: {e}", exc_info=True)
            raise

    if distilbert_tokenizer is None:
        logger.info("Loading DistilBERT tokenizer...")
        try:
            distilbert_tokenizer = AutoTokenizer.from_pretrained(LOCAL_PATH)
            logger.info("DistilBERT tokenizer loaded successfully.")
        except Exception as e:
            logger.error(f"Error loading DistilBERT tokenizer: {e}", exc_info=True)
            raise    

    
    if meta_model is None:
        logger.info("Loading meta model...")
        try:
            meta_model = joblib.load(os.path.join(LOCAL_PATH, META_MODEL_FILE))
            logger.info("Meta model loaded successfully.")
        except Exception as e:
            logger.error(f"Error loading meta model: {e}", exc_info=True)
            raise

    if meta_config is None:
        logger.info("Loading meta config...")
        try:
            with open(os.path.join(LOCAL_PATH, META_CONFIG_FILE), 'r') as f:
                meta_config = json.load(f)
            logger.info("Meta config loaded successfully.")
        except Exception as e:
            logger.error(f"Error loading meta config: {e}", exc_info=True)
            raise

try:
    init_inference_engine()
except Exception as e:
    logger.error(f"Initial model load failure. Handler will attempt retry.", exc_info=True)

def predict_distilbert_onnx(text: str) -> float:
    '''
    Performs inference using the DistilBERT ONNX model.

    Parameters
    ----------
    text : str
        The input text message to classify.

    Returns
    -------
    float
        The probability of the message being malicious.

    Raises
    ------
    Exception
        If there is an error during inference.
    '''
    try:
        inputs = distilbert_tokenizer(text, return_tensors="np", padding=True, truncation=True, max_length=128)
        onnx_inputs = {k: v.astype(np.int64) for k, v in inputs.items()}

        logits = ort_session.run(None, onnx_inputs)[0][0]
        probs = softmax(logits)
        spam_prob = float(probs[1])
        return spam_prob

    except Exception as e:
        logger.error(f"Error during DistilBERT ONNX inference: {e}", exc_info=True)
        raise

def softmax(x: np.ndarray) -> np.ndarray:
    '''
    Computes the softmax function for the given input array.

    Parameters
    ----------
    x : np.ndarray
        The input array.

    Returns
    -------
    np.ndarray
        The softmax probabilities.
    '''
    e_x = np.exp(x - np.max(x))
    return e_x / e_x.sum()

def build_explanation(features_dict, spam_prob):
    signals = []
    
    if features_dict['has_impersonation']:
        signals.append("suplantación de entidad")
    if features_dict['has_urgency']:
        signals.append("lenguaje urgente")
    if features_dict['has_action']:
        signals.append("acción requerida")
    if features_dict['has_url']:
        signals.append("URL sospechosa")
    if features_dict['has_phone']:
        signals.append("número de teléfono")
    if features_dict['has_prize']:
        signals.append("oferta o premio")
    if features_dict['has_financial']:
        signals.append("términos financieros")
    if features_dict['has_threat']:
        signals.append("amenaza o consecuencia")
    
    if not signals:
        signals.append("patrón lingüístico sospechoso")
    
    reason = f"Detectado: {', '.join(signals)}"
    details = f"Confianza: {spam_prob*100:.1f}% | Señales: {', '.join(signals)}"
    
    return reason, details

def extract_features(text: str) -> dict:
    t = str(text).lower()
    
    features = {}
    
    # Estructurales
    features['text_len'] = len(text)
    features['word_count'] = len(text.split())
    features['avg_word_len'] = features['text_len'] / max(features['word_count'], 1)
    
    # Semánticas
    features['has_urgency'] = any(w in t for w in urgency_words)
    
    # URL
    m = url_pattern.search(text)
    features['has_url'] = bool(m)
    features['url_len'] = len(m.group(0)) if m else 0
    # ... resto
    
    return features

def lambda_handler(event, context):
    '''
    Lambda function to perform AI-based SMS content checking.

    Parameters
    ----------
    event : dict
        The event data containing SMS details.
    context : object
        The runtime information of the Lambda function.

    Returns
    -------
    dict
        The result of the SMS check with verdict and reason.

    Raises
    ------
    Exception
        If there is an error during inference.
    '''
    try:
        init_inference_engine()

        text = event.get('message')
        user_id = event.get('user_id')

        output_payload = {
            "user_id": user_id,
            "execution_id": event.get('execution_id'),
            "sender": event.get('sender'),
            "message": text,
            "verdict": Verdict.UNKNOWN.value,
            "reason": "Unable to determine message safety"
        }

        features = extract_features(text)
        features_dict = {feat: features[i] for i, feat in enumerate(lgbm_config['features'])}
        feature_order = lgbm_config['features']
        features_ordered = np.array([features_dict[feat] for feat in feature_order])
        score_lgbm = lgbm_model.predict_proba([features_ordered])[:, 1][0]
        score_distilbert = predict_distilbert_onnx(text)
        X_meta = np.array([[score_lgbm, score_distilbert]])
        spam_prob = meta_model.predict_proba(X_meta)[:, 1][0]

        output_payload['reason'], output_payload['details'] = build_explanation(features_dict, spam_prob)

        if spam_prob > 0.8:
            output_payload['verdict'] = Verdict.MALICIOUS.value
        elif spam_prob > 0.5:
            output_payload['verdict'] = Verdict.SUSPICIOUS.value
        else:
            output_payload['verdict'] = Verdict.SAFE.value

        logger.info(f"AI SMS check result for user {user_id}: {output_payload['verdict']} ({spam_prob*100:.2f}%)")

        return output_payload

    except Exception as e:
        logger.error(f"Inference failure for user {user_id} | Execution ID: {event.get('execution_id')}", exc_info=True)
        return {"verdict": Verdict.UNKNOWN.value, "reason": "Internal AI inference error"}