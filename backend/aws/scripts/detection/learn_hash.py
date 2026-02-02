import boto3
import json
import logging
import os
from botocore.exceptions import ClientError
from common.security import get_sha512_hash
from datetime import datetime , timezone
from typing import List, Dict

#Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Environment variables
# Environment variables
REQUIRED_VARS = ['LISTS_TABLE_NAME']
for var in REQUIRED_VARS:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")

LISTS_TABLE_NAME = os.environ.get('LISTS_TABLE_NAME') 
REGION_NAME = os.environ.get('REGION_NAME')

# Constants
PARTITION_KEY = 'BLACKLIST_HASH'

# Initialize resources
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(LISTS_TABLE_NAME)

def store_hash(message_hash, reason):
    '''
    Store the SHA-512 hash of a message into the DynamoDB table.

    Parameters
    ----------
    message_hash : str
        The SHA-512 hash of the message.
    reason : str
        The reason for storing the hash.

    Returns
    -------
    bool
        True if the hash was stored successfully, False otherwise.
    '''
    try:
        table.put_item(
            Item={
                'PK': PARTITION_KEY,
                'SK': message_hash,
                'DESCRIPTION': f'Learned: {reason}',
                'UPLOAD_DATE': datetime.now(timezone.utc).isoformat()
            }
        )
        return True
    except ClientError as ce:
        logger.error(f"Failed to store hash: {ce}")
        return False

def process_message(payload):
    '''
    Process the incoming payload to store the SHA-512 hash of the message in DynamoDB.

    Parameters
    ----------
    payload : dict
        The payload dictionary containing 'message' and optional 'reason'.

    Returns
    -------
    bool
        True if the hash was stored successfully, False otherwise.
    '''
    message = payload.get('message')
    if not isinstance(message, str) or not message.strip():
        logger.warning(f"Invalid empty message received: {message}")
        return False
    
    reason = str(payload.get('reason', 'No reason provided')).strip()
    message_hash = get_sha512_hash(message.strip())

    return store_hash(message_hash, reason)

def lambda_handler(event, context):
    '''
    AWS Lambda handler to process incoming messages and store their SHA-512 hashes in DynamoDB.
    
    Parameters
    ----------
    event : dict
        The event dictionary containing records to process.
    context : object
        The context in which the Lambda function is called.

    Returns
    -------
    dict
        A dictionary containing any batch item failures.
    '''
    records = event.get('Records', [])
    logger.info(f"Processing {len(records)} records.")

    failed_items_ids: List[Dict[str, str]] = []

    for record in records:
        mid = record.get('messageId', 'Unknown')
        try:
            body_str = record.get('body')
            if not body_str:
                logger.error(f"Record with messageId {mid} has no body. Skipping.")
                continue

            payload = json.loads(body_str)
            success = process_message(payload)

            if not success:
                failed_items_ids.append({'itemIdentifier': record.get('messageId', '')})

        except (json.JSONDecodeError, TypeError) as e:
            logger.error(f"Malformed JSON in record {mid}: {e}")
            failed_items_ids.append({'itemIdentifier': record.get('messageId', '')})
        except Exception as e:
            logger.error(f"System failure in record {mid}: {e}", exc_info=True)
            failed_items_ids.append({'itemIdentifier': record.get('messageId', '')})

    return {'batchItemFailures': failed_items_ids}