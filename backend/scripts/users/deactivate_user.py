import boto3
import json
import os
from common.database import check_user_exists
from common.responses import create_response

# Environment variables

TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

# Initialize DynamoDB resource and table

dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(TABLE_NAME)

#Auxiliary functions

def dynamodb_deletion(user_id):
    '''
    Delete a user from the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
    '''

    table.put_item(
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

        exists = check_user_exists(user_id, table)

        if not exists:
            return create_response({'error': 'User does not exist'}, status_code=404)
        
        dynamodb_deletion(user_id)

        return create_response({'message': 'User deactivated successfully'})
    except Exception as e:
        return create_response({'error': 'Internal server error'}, status_code=500)