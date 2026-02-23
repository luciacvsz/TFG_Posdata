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

def extract_query_param(event: dict, param: str) -> str:
    '''
    Extract and validate a query string parameter from a Lambda event.

    Parameters
    ----------
    event : dict
        The Lambda event dictionary containing the query string parameters.
    param : str
        The name of the query string parameter to extract.

    Returns
    -------
    str
        The value of the requested query string parameter.

    Raises
    ------
    ValueError
        If the query string parameter is missing or empty.
    '''
    query_params = event.get('queryStringParameters', {})
    value = query_params.get(param) if query_params else None
    if not value:
        raise ValueError(f"Missing required query parameter: {param}")
    return value