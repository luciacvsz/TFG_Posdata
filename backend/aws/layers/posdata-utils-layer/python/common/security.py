import hashlib
import json
from botocore.exceptions import ClientError

def get_sha512_hash(text: str) -> str:
    '''
    Get SHA-512 hash of the given text.

    Parameters
    ----------
    text : str
        The input text to hash.

    Returns
    -------
    str
        The SHA-512 hash of the input text in hexadecimal format.
    '''
    sha512_hash = hashlib.sha512(text.encode('utf-8')).hexdigest()
    return sha512_hash

def trigger_hash_learning_async(sqs: Object, hashing_queue_url:str, message: str, reason: str) -> None:
    '''
    Trigger the asynchronous learning of a message hash by sending a message to the SQS queue.

    Parameters
    ----------
    sqs : Object
        The boto3 SQS client.
    hashing_queue_url : str
        The URL of the SQS queue.
    message : str
        The SMS message content.
    reason : str
        The reason for learning the hash.

    Raises
    ------
    RuntimeError
        If the message could not be sent to the SQS queue.
    '''
    payload = {'message': message, 'reason': reason}
    try:
        sqs.send_message(
            QueueUrl=hashing_queue_url,
            MessageBody=json.dumps(payload)
        )
    except ClientError as ce:
        raise RuntimeError(f"Failed to send message to SQS for hash learning: {ce}")