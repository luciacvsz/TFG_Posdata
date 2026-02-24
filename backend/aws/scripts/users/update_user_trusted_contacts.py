import boto3
import json
import logging
import os
from common.database import update_active_user
from common.responses import create_response
from common.utils import extract_body, extract_query_param
from common.validators import is_valid_email, is_valid_phone

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

def validate_trusted_contacts_data(body: dict) -> None:
    '''
    Validate the user trusted contacts data.

    Parameters
    ----------
    body : dict
        The request body containing user trusted contacts data.

    Raises
    ------
    ValueError
        If any required field is missing or invalid.
    '''

    trusted_contacts = body['trusted_contacts']
    for contact in trusted_contacts:
        if 'phone_number' in contact and not is_valid_phone(contact['phone_number']):
            raise ValueError("All phone numbers in trusted contacts must be valid.")
        if 'email' in contact and not is_valid_email(contact['email']):
            raise ValueError("All emails in trusted contacts must be valid.")

def dynamodb_update_trusted_contacts(user_id: str, updates: dict) -> None:
    '''
    Update the user trusted contacts data in the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
        updates : dict
            Dictionary with fields to update.
    '''
    update_active_user(
        table, user_id,
        update_expression='SET TRUSTED_CONTACTS = :val_list',
        expression_names={},
        expression_values={':val_list': updates['trusted_contacts']}
    )

def lambda_handler(event, context):
    '''
    Lambda function to update the trusted contacts of an existing user in the DynamoDB table.

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

        user_id = extract_query_param(event, 'user_id')
        body = extract_body(event)

        validate_trusted_contacts_data(body)
        
        dynamodb_update_trusted_contacts(user_id, body)        

        logger.info(f"Successfully updated trusted contacts for user ID: {user_id}")
        return create_response({}, status_code=204)
    
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        return create_response({'error': 'Internal server error'}, status_code=500)