import boto3
import json
import logging
import os
from botocore.exceptions import ClientError
import common.preferences as preferences
from common.responses import create_response
from common.validators import is_valid_phone, is_valid_email

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
    Validates the user data from the request body and generates updates.

    Parameters
    ----------
    body : dict
        The request body containing user data.

    Returns
    -------
    str
        The user_id of the user to update.
    dict
        The validated user data.

    Raises
    ------
    ValueError
        If any required field is missing or invalid.
    '''
    if 'user_id' not in body or not body['user_id']:
        raise ValueError("Missing required field: user_id")
    user_id = body['user_id']
    body.pop('user_id')

    if'contact' in body:
        contact = body['contact']
        if not all(c in contact for c in ['phone_number', 'email']):
            raise ValueError("Contact must include phone number and email.")
        if contact['phone_number'] != 'NONE' and not is_valid_phone(contact['phone_number']):
            raise ValueError("Phone number must be valid.")
        if contact['email'] != 'NONE' and not is_valid_email(contact['email']):
            raise ValueError("Email must be valid.")
        
    if 'preferences' in body:
        prefs = body['preferences']
        for key, valid_values in VALID_PREFERENCES.items():
            if key not in prefs:
                raise ValueError(f"Missing preference: {key}")
            if prefs[key] not in valid_values:
                raise ValueError(f"Invalid value for {key}: {prefs[key]}")
            
    if 'emergency_contacts' in body:
        emergency_contacts = body['emergency_contacts']
        for list_key, validator in [('phone_numbers', is_valid_phone), ('emails', is_valid_email)]:
            items = emergency_contacts.get(list_key)
            if not items:
                raise ValueError(f"Missing emergency contact list: {list_key}")
            if items and items != 'NONE':
                if not all(validator(item) for item in items):
                    raise ValueError(f"All {list_key} must be valid.")

    return user_id, body

def dynamodb_update(user_id, updates):
    '''
    Update an existing user in the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
        updates : dict
            Dictionary with fields to update.

    Raises
    ------
        ValueError
            If the user does not exist.
        ClientError
            If there is an error updating the item in DynamoDB.
    '''
    update_parts = []
    attribute_names = {}
    attribute_values = {}

    allowed_fields = {'full_name', 'contact', 'preferences', 'emergency_contacts'}
    
    for key, value in updates.items():
        if key in allowed_fields and value is not None:
            attr_name_alias = f'#attr_{key}'
            attr_value_alias = f':val_{key}'

            update_parts.append(f"{attr_name_alias} = {attr_value_alias}")
            attribute_names[attr_name_alias] = key.upper()
            attribute_values[attr_value_alias] = value

    if not update_parts:
        return
    
    try:
        table.update_item(
            Key={'PK': user_id},
            UpdateExpression=f'SET {", ".join(update_parts)}',
            ConditionExpression='attribute_exists(PK) AND #active = :true_val',
            ExpressionAttributeNames={
                '#active': 'ACTIVE',
                **attribute_names
            },
            ExpressionAttributeValues={
                ':true_val': True,
                **attribute_values
            }
        )
    except ClientError as e:
        if e.response['Error']['Code'] == 'ConditionalCheckFailedException':
            raise ValueError(f"User with ID {user_id} does not exist.")
        raise

def lambda_handler(event, context):
    '''
    Lambda function to update an existing user in the DynamoDB table.

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

        raw_body = event.get('body', '{}')
        body = json.loads(raw_body) if isinstance(raw_body, str) else raw_body
        if not body:
            raise ValueError("Request body is required.")

        user_id, updates = validate_user_data(body)
        
        dynamodb_update(user_id, updates)        

        logger.info(f"Successfully updated user ID: {user_id}")
        return create_response({'user_id': user_id}, status_code=200)
    
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        return create_response({'error': 'Internal server error'}, status_code=500)