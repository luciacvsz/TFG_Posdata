import boto3
import json
import logging
import os
import sentencepiece
import sys
from common.notification import Verdict
import numpy as np
import onnxruntime as ort
from transformers import XLMRobertaTokenizer

#Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Environment variables
REQUIRED_VARS = ['MODEL_BUCKET_NAME']
for var in REQUIRED_VARS:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")

MODEL_BUCKET_NAME = os.environ.get('MODEL_BUCKET_NAME')

# Constants
LOCAL_PATH = '/tmp/model'
MODEL_FILE = 'model_quantized.onnx'

# Initialize resources
s3 = boto3.client('s3')
tokenizer = None
ort_session = None

def download_model_from_s3():
    '''
    Downloads the model files from the specified S3 bucket to the local path.

    Raises
    ------
    Exception
        If there is an error during download.
    '''
    if not os.path.exists(LOCAL_PATH):
        os.makedirs(LOCAL_PATH)
    
    files = [MODEL_FILE, 'config.json', 'tokenizer.json',
             'tokenizer_config.json', 'special_tokens_map.json', 'sentencepiece.bpe.model']
    
    for f in files:
        dest = os.path.join(LOCAL_PATH, f)
        if not os.path.exists(dest):
            try:
                logger.info(f"Downloading {f} from S3 bucket {MODEL_BUCKET_NAME}...")
                s3.download_file(MODEL_BUCKET_NAME, f, dest)
            except Exception as e:
                logger.error(f"Failed to download '{f}'")
                raise

def init_inference_engine():
    '''
    Initializes the tokenizer and ONNX inference session.

    Raises
    ------
    Exception
        If there is an error during initialization.
    '''
    global tokenizer, ort_session

    if tokenizer is None or ort_session is None:
        download_model_from_s3()

        logger.info("Loading tokenizer and model...")
        try:
            tokenizer = XLMRobertaTokenizer.from_pretrained(LOCAL_PATH)
            model_path = os.path.join(LOCAL_PATH, MODEL_FILE)
            sess_options = ort.SessionOptions()
            sess_options.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_EXTENDED
            ort_session = ort.InferenceSession(model_path, sess_options, providers=["CPUExecutionProvider"])
            logger.info("Inference engine ready.")
        except Exception as e:
            logger.error(f"Error initializing inference engine.")
            raise

try:
    init_inference_engine()
except Exception as e:
    logger.error(f"Initial model load failure. Handler will attempt retry.")

def softmax(x):
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

        if not text:
            return output_payload

        inputs = tokenizer(text, return_tensors="np", padding=True, truncation=True)
        onnx_inputs = {k: v.astype(np.int64) for k, v in inputs.items()}

        logits = ort_session.run(None, onnx_inputs)[0][0]
        probs = softmax(logits)
        spam_prob = float(probs[1])

        if spam_prob > 0.8:
            output_payload['verdict'] = Verdict.MALICIOUS.value
            output_payload['reason'] = "AI detected high confidence malicious content"
        elif spam_prob > 0.5:
            output_payload['verdict'] = Verdict.SUSPICIOUS.value
            output_payload['reason'] = "Content may be malicious"
        else:
            output_payload['verdict'] = Verdict.SAFE.value
            output_payload['reason'] = "Content appears safe"

        output_payload['details'] = f"Confidence: {spam_prob*100:.2f}%"

        logger.info(f"AI SMS check result for user {user_id}: {output_payload['verdict']} ({spam_prob*100:.2f}%)")

        return output_payload

    except Exception as e:
        logger.error(f"Inference failure", exc_info=True)
        return {"verdict": Verdict.UNKNOWN.value, "reason": "Internal AI inference error"}