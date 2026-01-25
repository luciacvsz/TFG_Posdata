import boto3
import os
import json
from datetime import datetime
from common.responses import create_response
from common.utils import create_user_id
import common.preferences as preferences
from common.validators import is_valid_phone, is_valid_email

# Environment variables
USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

# Initialize DynamoDB resource and table
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(USERS_TABLE_NAME)

#Auxiliary functions
def dynamodb_insertion(user_id, full_name, contact, prefs, emergency_contacts):
    '''
    Insert a new user into the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
        full_name : str
            The full name of the user.
        contact : dict
            The contact information of the user.
        prefs : dict
            The preferences of the user.
        emergency_contacts : list
            List of emergency contacts.
    '''
    table.put_item(
        Item={
            'PK': user_id,
            'ACTIVE': True,
            'FULL_NAME': full_name,
            'CONTACT': contact,
            'PREFERENCES': prefs,
            'EMERGENCY_CONTACTS': emergency_contacts,
            'CREATED_AT': datetime.now().isoformat()
        }
    )

# Lambda handler
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
        print("Processing create user request.")

        if 'body' in event:
            if isinstance(event['body'], str):
                try:
                    body = json.loads(event['body'])
                except json.JSONDecodeError:
                    raise ValueError("Invalid JSON in body")
            else:
                body = event['body']
        else:
            body = {}

        full_name = body.get('full_name')
        if not full_name:
            raise ValueError("Full name is required.")

        contact = body.get('contact')
        if not contact:
            raise ValueError("Contact information is required.")
        if not 'phone_number' in contact or not 'email' in contact:
            raise ValueError("Contact must include phone number and email.")
        if contact['phone_number'] != 'NONE' and not is_valid_phone(contact['phone_number']):
            raise ValueError("Phone number must be valid.")
        if contact['email'] != 'NONE' and not is_valid_email(contact['email']):
            raise ValueError("Email must be valid.")
        
        prefs = body.get('preferences')
        if not prefs:
            raise ValueError("Preferences information is required.")
        if not 'font_size' in prefs or not 'notification_sound' in prefs or not 'color_scheme' in prefs or not 'exhaustivity' in prefs or not 'explanation_mode' in prefs:
            raise ValueError("All preferences must be provided.")
        if prefs['font_size'] not in [e.value for e in preferences.FontSize]:
            raise ValueError("Font size preference is invalid.")
        if prefs['notification_sound'] not in [e.value for e in preferences.NotificationSound]:
            raise ValueError("Notification sound preference is invalid.")
        if prefs['color_scheme'] not in [e.value for e in preferences.ColorScheme]:
            raise ValueError("Color scheme preference is invalid.")
        if prefs['exhaustivity'] not in [e.value for e in preferences.Exhaustivity]:
            raise ValueError("Exhaustivity preference is invalid.")
        if prefs['explanation_mode'] not in [e.value for e in preferences.ExplanationMode]:
            raise ValueError("Explanation mode preference is invalid.")
        
        emergency_contacts = body.get('emergency_contacts')
        if not emergency_contacts:
            raise ValueError("Emergency contacts information is required.")
        if not 'phone_numbers' in emergency_contacts or not 'emails' in emergency_contacts:
            raise ValueError("Emergency contacts must include phone numbers and emails.")
        if emergency_contacts['phone_numbers'] != 'NONE':
            for phone_number in emergency_contacts['phone_numbers']:
                if not is_valid_phone(phone_number):
                    raise ValueError("Emergency contact phone numbers must be valid.")
        if emergency_contacts['emails'] != 'NONE':
            for email in emergency_contacts['emails']:
                if not is_valid_email(email):
                    raise ValueError("Emergency contact emails must be valid.")

        print("Creating new user ID.")
        user_id = create_user_id(table)

        print(f"Inserting new user into DynamoDB.")
        dynamodb_insertion(user_id, full_name, contact, prefs, emergency_contacts)
        print(f"User {user_id} inserted successfully.")

        return create_response({'user_id': user_id})
    
    except ValueError as ve:
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        return create_response({'error': 'Internal server error'}, status_code=500)