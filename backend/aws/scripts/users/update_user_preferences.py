import boto3
import json
import logging
import os
from common.database import update_active_user
from common.responses import create_response
from common.utils import extract_body

# Setup logging
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

def dynamodb_update_preferences(user_id: str, updates: dict) -> None:
    '''
    Update the user preferences data in the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
        updates : dict
            Dictionary with fields to update.
    '''
    update_parts = []
    attribute_names = {'#pref': 'PREFERENCES'}
    attribute_values = {}

    for key, value in updates.items():
        attr_name_alias = f'#key_{key}'
        attr_value_alias = f':val_{key}'
        update_parts.append(f"#pref.{attr_name_alias} = {attr_value_alias}")
        attribute_names[attr_name_alias] = key
        attribute_values[attr_value_alias] = value

    if not update_parts:
        return

    update_active_user(
        table, user_id,
        update_expression=f'SET {", ".join(update_parts)}',
        expression_names=attribute_names,
        expression_values=attribute_values
    )

def lambda_handler(event, context):
    '''
    Lambda function to update the preferences of an existing user in the DynamoDB table.

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

        user_id = event['pathParameters']['user_id']
        body = extract_body(event)
        
        dynamodb_update_preferences(user_id, body['preferences'])        

        logger.info(f"Successfully updated preferences for user ID: {user_id}")
        return create_response({}, status_code=204)
    
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        return create_response({'error': 'Internal server error'}, status_code=500)