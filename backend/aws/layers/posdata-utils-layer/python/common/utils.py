import json
import uuid
from common.database import check_user_exists

def create_user_id(user_table: object) -> str:
    '''
    Create a unique user identifier, ensuring it does not already exist in DynamoDB.

    Parameters
    ----------
    user_table : boto3.resources.factory.dynamodb.Table
        The DynamoDB table resource used to check for ID conflicts.

    Returns
    -------
    str
        A unique identifier in hexadecimal format.
    '''
    user_id = uuid.uuid4().hex
    while check_user_exists(user_id, user_table):
        user_id = uuid.uuid4().hex
    return user_id

def extract_body(event: dict) -> dict:
    '''
    Extract and validate the JSON body from a Lambda event.

    Parameters
    ----------
    event : dict
        The Lambda event dictionary containing the request body as a JSON string.

    Returns
    -------
    dict
        The parsed request body.

    Raises
    ------
    ValueError
        If the request body is missing or empty.
    '''
    body = json.loads(event.get('body', '{}'))
    if not body:
        raise ValueError("Request body is required.")
    return body