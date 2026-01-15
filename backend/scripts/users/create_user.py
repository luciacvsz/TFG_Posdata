import boto3
import os
import json
from common.responses import create_response
from common.utils import create_user_id
import common.preferences as preferences
from common.validators import is_valid_phone 

# Environment variables
TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

# Initialize DynamoDB resource and table
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(TABLE_NAME)

#Auxiliary functions
def dynamodb_insertion(user_id, phone_number, font_size, notification_sound, color_scheme, exhaustivity, explanation_mode, extra_alert):
    '''
    Insert a new user into the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
        phone_number : str
            The phone number of the user.
        font_size : str
            The font size preferenc of the user.
        notification_sound : str
            The notification sound preference of the user.
        color_scheme : str
            The color scheme preference of the user.
        exhaustivity : str
            The exhaustivity preference of the user.
        explanation_mode : str
            The explanation mode preference of the user.
        extra_alert : str
            The extra alert preference of the user.
    '''
    table.put_item(
        Item={
            'PK': user_id,
            'ACTIVE': True,
            'PHONE_NUMBER': phone_number,
            'FONT_SIZE': font_size,
            'NOTIFICATION_SOUND': notification_sound,
            'COLOR_SCHEME': color_scheme,
            'EXHAUSTIVITY': exhaustivity,
            'EXPLANATION_MODE': explanation_mode,
            'EXTRA_ALERT': extra_alert
        }
    )

# Lambda handler
def lambda_handler(event, context):
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
        
        phone_number = body.get('phone_number')
        if not phone_number or not is_valid_phone(phone_number):
            raise ValueError("Phone number is required and must be valid.")
        
        font_size = body.get('font_size', preferences.FontSize.LARGE.value)
        notification_sound = body.get('notification_sound', preferences.NotificationSound.ON.value)
        color_scheme = body.get('color_scheme', preferences.ColorScheme.STANDARD.value)
        exhaustivity = body.get('exhaustivity', preferences.Exhaustivity.ENHANCED.value)
        explanation_mode = body.get('explanation_mode', preferences.ExplanationMode.ON.value)
        extra_alert = body.get('extra_alert', preferences.ExtraAlert.OFF.value)

        print("Creating new user ID.")
        user_id = create_user_id(table)

        print(f"Inserting new user into DynamoDB.")
        dynamodb_insertion(user_id, phone_number, font_size, notification_sound, color_scheme, exhaustivity, explanation_mode, extra_alert)
        
        print(f"User {user_id} inserted successfully.")
        return create_response({'user_id': user_id})
    
    except ValueError as ve:
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        return create_response({'error': 'Internal server error'}, status_code=500)