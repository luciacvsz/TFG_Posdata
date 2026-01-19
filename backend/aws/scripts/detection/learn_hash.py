import boto3
import os
from datetime import datetime , timezone
from common.security import get_sha512_hash
import json

# Environment variables

TABLE_NAME = os.environ.get('LISTS_TABLE_NAME') 
REGION_NAME = os.environ.get('REGION_NAME')

# Constants

PARTITION_KEY = 'BLACKLIST_HASH'

# Initialize DynamoDB resource and table

dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(TABLE_NAME)

#Auxiliary functions

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

    if not message or not isinstance(message, str):
        print(f"Skipping invalid message: {message}")
        return False
    
    reason = payload.get('reason', 'No reason provided')

    message = message.strip()
    reason = reason.strip()

    message_hash = get_sha512_hash(message)

    table.put_item(
        Item={
            'PK': PARTITION_KEY,
            'SK': message_hash,
            'DESCRIPTION': f'Learned: {reason}',
            'UPLOAD_DATE': datetime.now(timezone.utc).isoformat()
        }
    )
    print(f"Success: Hash stored for reason: {reason}")

    return True

# Lambda handler

def lambda_handler(event, context):
    '''
    AWS Lambda handler to process incoming messages and store their SHA-512 hashes in DynamoDB.
    
    Parameters
    ----------
    event : dict
        The event dictionary containing records to process.
    context : object
        The context in which the Lambda function is called.
    '''
    print(f"Received {len(event.get('Records', []))} records.")

    for record in event.get('Records', []):
        try:
            body = record.get('body')
            if not body:
                print("Skipping record with no body.")
                continue
            payload = json.loads(body)
            process_message(payload)
        except Exception as e:
            print(f"Error processing record: {e}")
