import boto3
import json
import logging
import os
from common.database import get_item_by_pk_sk
from common.responses import create_response

#Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Environment variables
REQUIRED_VARS = ['USERS_TABLE_NAME']
for var in REQUIRED_VARS:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")

USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME', 'eu-west-3')

# Initialize resources
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(USERS_TABLE_NAME)

def dynamodb_get(user_id):
    '''
    Get an existing user from the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
    Returns
    -------
        dict
            The retrieved user data.
    Raises
    ------
        ValueError
            If the user does not exist.
        ClientError
            If there is an error retrieving the item from DynamoDB.
    '''

    user = get_item_by_pk_sk(table, user_id)
    if not user:
        raise ValueError(f"User with ID {user_id} does not exist.")
    
    return user

def lambda_handler(event, context):
    '''
    Lambda function to get an existing user from the DynamoDB table.

    Parameters
    ----------
        event : dict
            The event dictionary containing request data.
        context : object
            The context object containing runtime information.
    
    Returns
    -------
        dict
            The response dictionary.
    '''
    try:
        logger.info(f"Received event: {json.dumps(event)}")

        query_params = event.get('queryStringParameters', {})
        user_id = query_params.get('user_id') if query_params else None

        if not user_id:
            raise ValueError("Missing required query parameter: user_id")
        
        user = dynamodb_get(user_id)

        response = {
            'full_name': user['FULL_NAME'],
            'contact': user['CONTACT'],
            'preferences': user['PREFERENCES'],
            'trusted_contacts': user['TRUSTED_CONTACTS']
        }     

        logger.info(f"Successfully retrieved user ID: {user_id}")
        return create_response(response, status_code=200)
    
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        return create_response({'error': 'Internal server error'}, status_code=500)