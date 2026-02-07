import boto3
import json
import logging
import os
from botocore.exceptions import ClientError
import common.preferences as preferences
from common.responses import create_response
from common.utils import create_user_id
from common.validators import is_valid_phone, is_valid_email
from datetime import datetime, timezone

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

# Constants
VALID_PREFERENCES = {
    'font_size': {e.value for e in preferences.FontSize},
    'notification_sound': {e.value for e in preferences.NotificationSound},
    'color_scheme': {e.value for e in preferences.ColorScheme},
    'exhaustivity': {e.value for e in preferences.Exhaustivity},
    'explanation_mode': {e.value for e in preferences.ExplanationMode}
}

# Initialize resources
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(USERS_TABLE_NAME)

def validate_user_data(body):
    '''
    Validates the user data from the request body.

    Parameters
    ----------
    body : dict
        The request body containing user data.

    Returns
    -------
    dict
        The validated user data.

    Raises
    ------
    ValueError
        If any required field is missing or invalid.
    '''        
    contact = body['contact']
    if contact['phone_number'] != 'NONE' and not is_valid_phone(contact['phone_number']):
        raise ValueError("Phone number must be valid.")
    
    if contact['email'] != 'NONE' and not is_valid_email(contact['email']):
        raise ValueError("Email must be valid.")
    
    prefs = body['preferences']
    for key, valid_values in VALID_PREFERENCES.items():
        if key not in prefs:
            raise ValueError(f"Missing required preference: {key}")
        if prefs[key] not in valid_values:
            raise ValueError(f"Invalid value for {key}: {prefs[key]}")
        
    emergency_contacts = body['emergency_contacts']
    for list_key, validator in [('phone_numbers', is_valid_phone), ('emails', is_valid_email)]:
        items = emergency_contacts.get(list_key)
        if not all(validator(item) for item in items):
                raise ValueError(f"All {list_key} must be valid.")

    return body

def dynamodb_insertion(user_id, data):
    '''
    Insert a new user into the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
        data : dict
            The user data containing full_name, contact, preferences, and emergency_contacts.

    Raises
    ------
        ClientError
            If there is an error inserting the item into DynamoDB.
    '''
    try:
        table.put_item(
            Item={
                'PK': user_id,
                'ACTIVE': True,
                'FULL_NAME': data['full_name'],
                'CONTACT': data['contact'],
                'PREFERENCES': data['preferences'],
                'EMERGENCY_CONTACTS': data['emergency_contacts'],
                'CREATED_AT': datetime.now(timezone.utc).isoformat()
            },
            ConditionExpression='attribute_not_exists(PK)'
        )
    except ClientError as e:
        if e.response['Error']['Code'] == 'ConditionalCheckFailedException':
            logger.error(f"User ID {user_id} already exists.")
            raise ValueError("Internal ID generation conflict.")
        raise

def lambda_handler(event, context):
    '''
    AWS Lambda handler to create a new user.

    Parameters
    ----------
        event : dict
            The event data passed to the Lambda function.
        context : object
            The context in which the Lambda function is called.
    
    Returns
    -------
        dict
            The response object containing the new user ID or error message.
    '''
    try:
        logger.info(f"Received event: {json.dumps(event)}")

        body = json.loads(event.get('body', '{}'))
        if not body:
            raise ValueError("Request body is missing.")
    
        validated_data = validate_user_data(body)
        user_id = create_user_id(table)

        dynamodb_insertion(user_id, validated_data)

        logger.info(f"Successfully created user ID: {user_id}")
        return create_response({'user_id': user_id}, status_code=201)
    
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        return create_response({'error': 'Internal server error'}, status_code=500)