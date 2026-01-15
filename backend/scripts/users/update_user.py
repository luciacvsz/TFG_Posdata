import boto3
import os
import json
from common.responses import create_response
from common.utils import create_user_id
import common.preferences as preferences
from common.database import check_user_exists
from common.validators import is_valid_phone

# Environment variables
TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

# Initialize DynamoDB resource and table
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(TABLE_NAME)

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
        
        phone_number = body.get('phone_number', '')
        if phone_number != '' and not is_valid_phone(phone_number):
            raise ValueError("Phone number must be valid.")
        
        updates = {
            'PHONE_NUMBER': phone_number,
            'FONT_SIZE': body.get('font_size', ''),
            'NOTIFICATION_SOUND': body.get('notification_sound', ''),
            'COLOR_SCHEME': body.get('color_scheme', ''),
            'EXHAUSTIVITY': body.get('exhaustivity', ''),
            'EXPLANATION_MODE': body.get('explanation_mode', ''),
            'EXTRA_ALERT': body.get('extra_alert', '')
        }
        
        print(f"Updating user in DynamoDB.")
        dynamodb_update(user_id, updates)
        
        print(f"User {user_id} updated successfully.")
        return create_response({'message': 'User updated successfully'})
    
    except ValueError as ve:
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        return create_response({'error': 'Internal server error'}, status_code=500)