import boto3
import json
import logging
import os
from botocore.exceptions import ClientError
from common.database import get_item_by_pk_sk
from common.notification import Verdict
from common.security import get_sha512_hash
from common.validators import extract_urls

# Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Environment variables
REQUIRED_VARS = ['LISTS_TABLE_NAME', 'HASHING_QUEUE_URL']
for var in REQUIRED_VARS:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")
    
LISTS_TABLE_NAME = os.environ.get('LISTS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME', 'eu-west-3')
HASHING_QUEUE_URL = os.environ.get('HASHING_QUEUE_URL')

# Constants
UNKNOWN_SENDER = 'Unknown'

# Initialize resources
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(LISTS_TABLE_NAME)
sqs = boto3.client('sqs', region_name=REGION_NAME)

def hash_check(message: str) -> dict | None:
    '''
    Check if the SHA-512 hash of the message exists in the blacklist.

    Parameters
    ----------
    message : str
        The SMS message content.

    Returns
    -------
    dict or None
        The blacklist entry if found, otherwise None.
    '''
    message_hash = get_sha512_hash(message)
    if hash_data := get_item_by_pk_sk(table, 'BLACKLIST_HASH', message_hash):
        return {
            "verdict": Verdict.MALICIOUS.value,
            "reason": "Este mensaje ya ha sido identificado como peligroso",
            "details": f"Este mensaje ha sido marcado como fraudulento. Fuente: {hash_data.get('DESCRIPTION', 'desconocida')}"
        }
    return None

def url_check(urls: list, message: str) -> dict | None:
    '''
    Checks URLs against blacklist and whitelist in the database.
    Triggers asynchronous hash learning if a blacklisted URL is found.

    Parameters
    ----------
    urls : list
        List of URLs extracted from the message.
    message : str
        The SMS message content.

    Returns
    -------
    dict or None
        The result of the URL check or None if no conclusive result.
    '''
    whitelisted_count = 0
    for url in urls:
        if get_item_by_pk_sk(table, 'BLACKLIST_URL', url):
            trigger_hash_learning_async(message, f"Blocked by blacklisted URL: {url}")
            return {
                "verdict": Verdict.MALICIOUS.value,
                "reason": "El mensaje contiene un enlace peligroso",
                "details": f"Enlace peligroso detectado: {url}"
            }
        
        if get_item_by_pk_sk(table, 'WHITELIST_URL', url):
            whitelisted_count += 1
        
    if whitelisted_count == len(urls) and len(urls) > 0:
        return {
            "verdict": Verdict.SAFE.value,
            "reason": "Todos los enlaces del mensaje son seguros",
            "details": "No se encontraron enlaces peligrosos"
        }
    
    return None

def trigger_hash_learning_async(message: str, reason: str) -> None:
    '''
    Trigger the asynchronous learning of a message hash by sending a message to the SQS queue.

    Parameters
    ----------
    message : str
        The SMS message content.
    reason : str
        The reason for learning the hash.
    '''
    payload = {'message': message, 'reason': reason}
    try:
        sqs.send_message(
            QueueUrl=HASHING_QUEUE_URL,
            MessageBody=json.dumps(payload)
        )
    except ClientError as ce:
        logger.error(f"Failed to send message to SQS for hash learning: {ce}")

def sender_check(sender: str) -> dict | None:
    '''
    Checks the sender against whitelist in the database.

    Parameters
    ----------
    sender : str
        The sender identifier.

    Returns
    -------
    dict or None
        The result of the sender check or None if not whitelisted.
    '''
    if sender == UNKNOWN_SENDER:
        return None
    
    if sender_data := get_item_by_pk_sk(table, 'WHITELIST_SENDER', sender):
        return {
            "verdict": Verdict.SAFE.value,
            "reason": "El remitente es de confianza",
            "details": f"Este remitente ha sido verificado como seguro. Fuente: {sender_data.get('DESCRIPTION', 'desconocida')}"
        }
    
    return None

def lambda_handler(event, context):
    '''
    Lambda function to check incoming SMS messages against blacklists and whitelists.

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
    ValueError
        If required fields are missing or invalid in the event data.
    Exception
        For any other errors during processing.
    '''
    try:

        user_id = event.get('user_id')
        message = event.get('message')
        sender = str(event.get('sender', UNKNOWN_SENDER)).strip()
        execution_id = event.get('execution_id', 'N/A')
        logger.info(f"Processing SMS check for user: {user_id} | Execution ID: {execution_id}") 
                
        response = { 
            "user_id": user_id,
            "execution_id": execution_id,
            "sender": sender,
            "message": message,
            "verdict": Verdict.UNKNOWN.value,
            "reason": "No se ha podido determinar si el mensaje es seguro",
        }
        
        if hash_result := hash_check(message):
            response.update(hash_result)
            return response

        urls = list(set(extract_urls(message)))
        if url_result := url_check(urls, message):
            response.update(url_result)
            return response

        if sender_result := sender_check(sender):
            response.update(sender_result)
            return response
        
        logger.info(f"Successfully completed SMS list check for user: {user_id} | Execution ID: {execution_id} | Verdict: {response['verdict']}")
        return response
            
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        raise
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        raise