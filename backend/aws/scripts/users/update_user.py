import boto3
import os
import json
from common.responses import create_response
import common.preferences as preferences
from common.database import check_user_exists
from common.validators import is_valid_phone, is_valid_email

# Environment variables
USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

# Initialize DynamoDB resource and table
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(USERS_TABLE_NAME)

#Auxiliary functions
def dynamodb_update(user_id, updates):
    '''
    Update an existing user in the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
        updates : dict
            Dictionary with fields to update.
    '''
    update_expression = []
    expression_values = {}
    
    for key, value in updates.items():
        if value != '':
            placeholder = f':{key.lower()[:2]}'
            update_expression.append(f'{key} = {placeholder}')
            expression_values[placeholder] = value
    
    if update_expression:
        table.update_item(
            Key={'PK': user_id},
            UpdateExpression='SET ' + ', '.join(update_expression),
            ExpressionAttributeValues=expression_values
        )

# Lambda handler
def lambda_handler(event, context):
    try:
        print("Processing update user request.")

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
        
        user_id = body.get('user_id')
        if not user_id or not check_user_exists(user_id, table):
            raise ValueError("User ID is required and must exist.")
        
        updates = {}
        
        full_name = body.get('full_name')
        if full_name:
            updates['FULL_NAME'] = full_name
        
        contact = body.get('contact')
        if contact and (not 'phone_number' in contact or not 'email' in contact):
            raise ValueError("Contact information must include phone number and email.")
        if contact and contact['phone_number'] != 'NONE' and not is_valid_phone(contact['phone_number']):
            raise ValueError("Phone number must be valid.")
        if contact and contact['email'] != 'NONE' and not is_valid_email(contact['email']):
            raise ValueError("Email must be valid.")
        if contact:
            updates['CONTACT'] = contact
        
        prefs = body.get('preferences')
        if prefs and (not prefs['font_size'] or not prefs['notification_sound'] or not prefs['color_scheme'] or not prefs['exhaustivity'] or not prefs['explanation_mode']):
            raise ValueError("All preferences must be provided.")
        if prefs and (prefs['font_size'] not in [e.value for e in preferences.FontSize]):
            raise ValueError("Font size preference is invalid.")
        if prefs and (prefs['notification_sound'] not in [e.value for e in preferences.NotificationSound]):
            raise ValueError("Notification sound preference is invalid.")
        if prefs and (prefs['color_scheme'] not in [e.value for e in preferences.ColorScheme]):
            raise ValueError("Color scheme preference is invalid.")
        if prefs and (prefs['exhaustivity'] not in [e.value for e in preferences.Exhaustivity]):
            raise ValueError("Exhaustivity preference is invalid.")
        if prefs and (prefs['explanation_mode'] not in [e.value for e in preferences.ExplanationMode]):
            raise ValueError("Explanation mode preference is invalid.")
        if prefs:
            updates['PREFERENCES'] = prefs
    
        emergency_contacts = body.get('emergency_contacts')
        if emergency_contacts and (not 'phone_numbers' in emergency_contacts or not 'emails' in emergency_contacts):
            raise ValueError("Emergency contacts must include phone numbers and emails.")
        if 'phone_numbers' in emergency_contacts and emergency_contacts['phone_numbers'] != 'NONE':
            for phone_number in emergency_contacts['phone_numbers']:
                if not is_valid_phone(phone_number):
                    raise ValueError("Emergency contact phone numbers must be valid.")
        if 'emails' in emergency_contacts and emergency_contacts['emails'] != 'NONE':
            for email in emergency_contacts['emails']:
                if not is_valid_email(email):
                    raise ValueError("Emergency contact emails must be valid.")
        if emergency_contacts:
            updates['EMERGENCY_CONTACTS'] = emergency_contacts
        
        print(f"Updating user in DynamoDB.")
        dynamodb_update(user_id, updates)
        print(f"User {user_id} updated successfully.")
        
        return create_response({'message': 'User updated successfully'})
    
    except ValueError as ve:
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        return create_response({'error': 'Internal server error'}, status_code=500)