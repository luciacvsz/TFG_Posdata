import boto3
import json
import os
from common.security import get_sha512_hash
from common.validators import extract_urls
from common.database import get_item_by_pk_sk, check_user_exists

# Environment variables
TABLE_NAME = os.environ.get('LISTS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')
HASHING_QUEUE_URL = os.environ.get('HASHING_QUEUE_URL')

# Constants
UNKNOWN_SENDER = 'Unknown'

# Initialize DynamoDB resource and table
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(TABLE_NAME)

# Initialize Lambda client
sqs_client = boto3.client('sqs', region_name=REGION_NAME)

# Auxiliary functions
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
    payload = {
        'message': message,
        'reason': reason
    }
    sqs_client.send_message(
        QueueUrl=HASHING_QUEUE_URL,
        MessageBody=json.dumps(payload)
    )

#  Lambda handler
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
        The result of the SMS check with veredict and reason.
    
    Raises
    ------
    ValueError
        If required fields are missing or invalid in the event data.
    Exception
        For any other errors during processing.
    '''
    try:        
        print("Received SMS. Starting Step Function List Check.")

        if 'body' in event and isinstance(event['body'], (str)):
            body = json.loads(event['body'])
        else:
            body = event
        
        user_id = body.get('user_id')
        if not user_id or not check_user_exists(user_id, table):
            raise ValueError("User ID is required and must exist.")
        
        sender = body.get('sender', UNKNOWN_SENDER)

        message = body.get('message', '')
        if not message:
            raise ValueError("Message content is empty")
        
        output_payload = {
            "user_id": user_id,
            "sender": sender,
            "message": message,
            "veredict": "UNKNOWN",
            "reason": "Not found in any list",
        }
        
        print("Checking message hash against blacklist.")
        message_hash = get_sha512_hash(message)
        hash_data = get_item_by_pk_sk(table, 'BLACKLIST_HASH', message_hash)
        if hash_data:
            print("Message hash found in blacklist.")
            output_payload.update({
                "veredict": "MALICIOUS",
                "reason": "Message hash is blacklisted"
                "details": hash_data.get('DESCRIPTION', '')
            })
            return output_payload

        print("Checking sender against whitelist.")
        if sender != UNKNOWN_SENDER:
            if isinstance(sender, str):
                sender = sender.strip()
                sender_data = get_item_by_pk_sk(table, 'WHITELIST_SENDER', sender)
                if sender_data:
                    print("Sender found in whitelist.")
                    output_payload.update({
                        "veredict": "SAFE",
                        "reason": "Sender is whitelisted",
                        "details": sender_data.get('DESCRIPTION', '')
                    })
                    return output_payload
            else:
                raise ValueError("Sender must be a string")  
        
        print("Extracting URLs from message and checking against blacklist.")
        urls = extract_urls(message)
        if urls:
            for url in urls:
                url_data = get_item_by_pk_sk(table, 'BLACKLIST_URL', url)
                if url_data:
                    print(f"URL found in blacklist: {url}. Learning hash.")
                    trigger_hash_learning_async(message, f"Blocked by blacklisted URL: {url}")
                    output_payload.update({
                        "veredict": "MALICIOUS",
                        "reason": "Message contains blacklisted URL",
                        "details": url_data.get('DESCRIPTION', '')
                    })
                    return output_payload
        
        print("No matches found in any list. Veredict is UNKNOWN. Forwarding to AI for further analysis.")
        return output_payload

    except ValueError as ve:
        print(f"ValueError: {ve}")
        raise ve
    except Exception as e:
        print(f"Error processing SMS: {e}")
        raise e