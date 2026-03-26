import ctypes
import glob
import os

libs = glob.glob('/var/task/libgomp.so*')
if libs:
    ctypes.CDLL(libs[0])

import boto3
import joblib
import logging
import numpy as np
import onnxruntime as ort
import re
from common.notification import Verdict
from tokenizers import Tokenizer

logger = logging.getLogger()
logger.setLevel(logging.INFO)

for var in ['MODEL_BUCKET_NAME']:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")

MODEL_BUCKET_NAME = os.environ.get('MODEL_BUCKET_NAME')
REGION_NAME = os.environ.get('REGION_NAME', 'eu-west-3')
LOCAL_PATH = '/tmp/models'

urgency_words = ['urgente','urgently','urgent','inmediatamente','immediately','ahora','now','hoy','today','bloquea','blocked','suspendida','suspended','cancel','cancela','verifique','verify']
action_words = ['haga clic','click','acceda','access','llame','call','responda','reply','confirme','confirm','descargue','download','ingrese','enter']
financial_words = ['cuenta','account','banco','bank','tarjeta','card','pago','payment','transferencia','transfer','bizum','credito','credit','débito','debit']
prize_words = ['gratis','free','premio','prize','ganador','winner','regalo','gift','oferta','offer','descuento','discount','gana','win']
threat_words = ['amenaza','threat','peligro','danger','dangerous','peligroso','cuidado','beware','attention','atencion','careful']
impersonation_words = ["santander","bbva","caixabank","bankinter","sabadell","bankia","ing","kutxabank","ibercaja","unicaja","abanca","cajamar","openbank","bizum","movistar","vodafone","orange","masmovil","yoigo","jazztel","lowi","correos","seur","mrw","dhl","fedex","ups","gls","celeritas","hacienda","tributaria","dgt","sepe","ministerio","ayuntamiento","policia","guardia civil","seguridad social","apple","google","microsoft","netflix","spotify","whatsapp","facebook","instagram","icloud","mercadona","lidl","carrefour","alcampo","aldi","zara","inditex","repsol","bp","iberdrola","endesa","naturgy"]

url_pattern = re.compile(r"(https?://[^\\s]+)|(www\\.[^\\s]+)")
shortener_pattern = re.compile(r'\\b(bit\\.ly|t\\.co|tinyurl\\.com|goo\\.gl|ow\\.ly|rb\\.gy|cutt\\.ly)\\b')
phone_pattern = re.compile(r'(\\+?[1-9]\\d{1,14}|[0-9]{9,15})')

s3 = boto3.client('s3', region_name=REGION_NAME)
lgbm_model = None
ort_session = None
distilbert_tokenizer = None
meta_model = None

def download_model_files():
    files = {
        'lgbm_v1': ['model.pkl', 'config.json'],
        'distilbert_v1': ['model.onnx', 'model.onnx.data', 'tokenizer.json'],
        'meta_v1': ['model.pkl', 'config.json'],
    }
    for folder, filenames in files.items():
        local_folder = os.path.join(LOCAL_PATH, folder)
        os.makedirs(local_folder, exist_ok=True)
        for fname in filenames:
            dest = os.path.join(local_folder, fname)
            if not os.path.exists(dest):
                s3_key = f"{folder}/{fname}"
                logger.info(f"Downloading {s3_key}...")
                try:
                    s3.download_file(MODEL_BUCKET_NAME, s3_key, dest)
                except Exception:
                    logger.error(f"Failed to download {s3_key}")
                    raise

def init_inference_engine() -> None:
    global lgbm_model, ort_session, distilbert_tokenizer, meta_model
    if lgbm_model is not None and ort_session is not None: 
        return
    download_model_files()
    logger.info("Loading LightGBM...")
    try:
        lgbm_model = joblib.load(os.path.join(LOCAL_PATH, 'lgbm_v1', 'model.pkl'))
        logger.info("LightGBM loaded OK")
    except Exception as e:
        logger.error(f"LightGBM load error: {e}", exc_info=True)
        raise
    logger.info("Loading DistilBERT ONNX session...")
    sess_options = ort.SessionOptions()
    sess_options.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_EXTENDED
    ort_session = ort.InferenceSession(
        os.path.join(LOCAL_PATH, 'distilbert_v1', 'model.onnx'),
        sess_options,
        providers=['CPUExecutionProvider']
    )
    distilbert_tokenizer = Tokenizer.from_file(
        os.path.join(LOCAL_PATH, 'distilbert_v1', 'tokenizer.json')
    )
    logger.info("Loading meta-model...")
    try:
        meta_model = joblib.load(os.path.join(LOCAL_PATH, 'meta_v1', 'model.pkl'))
        logger.info(f"Meta-model loaded OK: {type(meta_model)}")
    except Exception as e:
        logger.error(f"Meta-model load error: {e}", exc_info=True)
        raise
        logger.info("Inference engine ready.")

try:
    init_inference_engine()
except Exception:
    logger.error("Failure in initial load. Will retry in the handler.")
    lgbm_model = None
    ort_session = None
    distilbert_tokenizer = None
    meta_model = None

def extract_features(text):
    t = text.lower()
    url_match = url_pattern.search(text)
    features = {
        'text_len': len(text),
        'word_count': len(text.split()),
        'avg_word_len': len(text) / max(len(text.split()), 1),
        'caps_ratio': sum(1 for c in text if c.isupper()) / max(len(text), 1),
        'digit_ratio': sum(1 for c in text if c.isdigit()) / max(len(text), 1),
        'excl_count': text.count('!'),
        'ques_count': text.count('?'),
        'num_count': sum(1 for c in text if c.isdigit()),
        'has_urgency': int(any(w in t for w in urgency_words)),
        'has_action': int(any(w in t for w in action_words)),
        'has_financial': int(any(w in t for w in financial_words)),
        'has_prize': int(any(w in t for w in prize_words)),
        'has_threat': int(any(w in t for w in threat_words)),
        'has_impersonation': int(any(w in t for w in impersonation_words)),
        'has_url': int(bool(url_match)),
        'has_phone': int(bool(phone_pattern.search(text))),
        'url_len': len(url_match.group(0)) if url_match else 0,
        'has_shortener': int(bool(shortener_pattern.search(text))),
    }
    return features

def predict_distilbert(text):
    encoding = distilbert_tokenizer.encode(text)
    input_ids = encoding.ids[:128] + [0] * max(0, 128 - len(encoding.ids))
    attention_mask = encoding.attention_mask[:128] + [0] * max(0, 128 - len(encoding.attention_mask))
    onnx_inputs = {
        'input_ids': np.array([input_ids], dtype=np.int64),
        'attention_mask': np.array([attention_mask], dtype=np.int64),
    }
    logits = ort_session.run(None, onnx_inputs)[0][0]
    e_x = np.exp(logits - np.max(logits))
    probs = e_x / e_x.sum()
    return float(probs[1])

def build_explanation(features_dict, spam_prob):
    signals = []
    if features_dict['has_impersonation']: signals.append("suplantación de entidad")
    if features_dict['has_urgency']: signals.append("lenguaje urgente")
    if features_dict['has_action']: signals.append("acción requerida")
    if features_dict['has_url']: signals.append("URL sospechosa")
    if features_dict['has_phone']: signals.append("número de teléfono")
    if features_dict['has_prize']: signals.append("oferta o premio")
    if features_dict['has_financial']: signals.append("términos financieros")
    if features_dict['has_threat']: signals.append("amenaza o consecuencia")
    if not signals: signals.append("patrón lingüístico sospechoso")
    if spam_prob > 0.8:
        reason = "Este mensaje parece un intento de fraude."
        details = f"Se han detectado {len(signals)} indicador{'es' if len(signals) > 1 else ''} de fraude: {', '.join(signals)}." if signals else "El análisis de IA ha detectado contenido fraudulento."
    elif spam_prob > 0.5:
        reason = "Este mensaje podría ser sospechoso. Proceda con cautela."
        details = f"Se han detectado algunos indicadores de riesgo: {', '.join(signals)}." if signals else "El análisis de IA ha detectado cierta probabilidad de fraude."
    else:
        reason = "Este mensaje parece seguro."
        details = "No se han detectado indicadores de fraude significativos."

    return reason, details

def lambda_handler(event, context):
    try:
        init_inference_engine()
        text    = event.get('message')
        user_id = event.get('user_id')
        response = {
            "user_id": user_id,
            "execution_id": event.get('execution_id'),
            "sender": event.get('sender'),
            "message": text,
            "verdict": Verdict.UNKNOWN.value,
            "reason": "No se pudo analizar el mensaje",
        }
        if not text:
            return response
        features = extract_features(text)
        score_lgbm = lgbm_model.predict_proba([list(features.values())])[0][1]
        score_distilbert = predict_distilbert(text)
        X_meta = np.array([[score_lgbm, score_distilbert]])
        spam_prob = float(meta_model.predict_proba(X_meta)[0][1])
        reason, details = build_explanation(features, spam_prob)
        if spam_prob > 0.8:
            response['verdict'] = Verdict.MALICIOUS.value
        elif spam_prob > 0.5:
            response['verdict'] = Verdict.SUSPICIOUS.value
        else:
            response['verdict'] = Verdict.SAFE.value
        response['reason']  = reason
        response['details'] = details
        logger.info(f"AI SMS check for user {user_id}: {response['verdict']} ({spam_prob*100:.2f}%)")
        return response
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        raise