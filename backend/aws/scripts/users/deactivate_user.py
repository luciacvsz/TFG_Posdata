import boto3
import json
import os
from common.database import check_user_exists
from common.responses import create_response

# Environment variables
USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

if not USERS_TABLE_NAME:
    raise ValueError("USERS_TABLE_NAME environment variable not set")
if not REGION_NAME:
    raise ValueError("REGION_NAME environment variable not set")

# Initialize DynamoDB resource and table
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
users_table = dynamodb.Table(USERS_TABLE_NAME)

#Auxiliary functions
def dynamodb_deactivation(user_id):
    '''
    Deactivate a user in the DynamoDB tables.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
    '''
    users_table.put_item(
        Item={
            'PK': user_id,
            'ACTIVE': False
        }
    )

# Lambda handler
def lambda_handler(event, context):
    '''
    AWS Lambda handler to deactivate a user.

    Parameters
    ----------
        event : dict
            The event data from the Lambda invocation.
        context : object
            The runtime information of the Lambda function.
    '''
    try:
        print("Processing deactivate user request.")

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
        if not user_id or not check_user_exists(user_id, users_table):
            raise ValueError("User ID is required and must exist.")
        
        print(f"Deactivating user {user_id} in DynamoDB.")
        dynamodb_deactivation(user_id)
        print(f"User {user_id} deactivated successfully.")

        return create_response({'message': 'User deactivated successfully'})
    
    except ValueError as ve:
        return create_response({'ValueError': str(ve)}, status_code=400)
    except Exception as e:
        return create_response({'error': 'Internal server error'}, status_code=500)