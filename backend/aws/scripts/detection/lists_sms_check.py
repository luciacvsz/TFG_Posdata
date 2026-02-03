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

def extract_and_validate_payload(event):
    '''
    Extracts and validates the payload from the event.

    Parameters
    ----------
    event : dict
        The event data containing SMS details.
    
    Returns
    -------
    dict
        The validated payload with user_id, message, and sender.

    Raises
    ------
    ValueError
        If required fields are missing or invalid.
    '''
    raw_body = event.get('body')
    body = json.loads(raw_body) if raw_body and isinstance(raw_body, str) else event

    data = {
        'user_id': body.get('user_id'),
        'message': body.get('message'),
        'sender': str(body.get('sender', UNKNOWN_SENDER)).strip()
    }

    if not data['user_id'] or not data['message']:
        raise ValueError("Missing required fields: user_id or message.")
    
    return data

def hash_check(message):
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
            "reason": "Message hash is blacklisted",
            "details": hash_data.get('DESCRIPTION', '')
        }
    return None

    

def url_check(urls, message):
    '''
    Checks URLs against blacklist and whitelist in the database.

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
                "reason": "Message contains blacklisted URL",
                "details": f"Blacklisted URL found: {url}"
            }
        
        if get_item_by_pk_sk(table, 'WHITELIST_URL', url):
            whitelisted_count += 1
        
    if whitelisted_count == len(urls) and len(urls) > 0:
        return {
            "verdict": Verdict.SAFE.value,
            "reason": "All URLs are whitelisted"
        }
    
    return None

def trigger_hash_learning_async(message, reason):
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

def sender_check(sender):
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
            "reason": "Sender is whitelisted",
            "details": sender_data.get('DESCRIPTION', '')
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

        # 1. Parsing & validation
        data = extract_and_validate_payload(event)
        logger.info(f"Processing SMS check for user: {data['user_id']}") 
                
        response = { 
            "user_id": data['user_id'],
            "sender": data['sender'],
            "message": data['message'],
            "verdict": Verdict.UNKNOWN.value,
            "reason": "Not found in any list",
        }
        
        # 2. Content Hash Check
        if hash_result := hash_check(data['message']):
            response.update(hash_result)
            return response

        # 3. URL Analysis
        urls = list(set(extract_urls(data['message'])))
        if url_result := url_check(urls, data['message']):
            response.update(url_result)
            return response

        # 4. Sender Check
        if sender_result := sender_check(data['sender']):
            response.update(sender_result)
            return response
            
        return response
            
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        raise
    except Exception as e:
        logger.error(f"System Failure: {e}", exc_info=True)
        raise