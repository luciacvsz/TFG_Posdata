import boto3
import os
from common.responses import create_response
from common.utils import create_user_id

# Environment variables

TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

# Initialize DynamoDB resource and table

dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(TABLE_NAME)

#Auxiliary functions

def dynamodb_insertion(user_id):
    '''
    Insert a new user into the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
    '''

    table.put_item(
        Item={
            'PK': user_id,
            'ACTIVE': True
        }
    )

# Lambda handler

def lambda_handler(event, context):
    try:
        print("Creating new user.")

        user_id = create_user_id(table)
        print(f"Generated user ID: {user_id}. Inserting into DynamoDB.")

        dynamodb_insertion(user_id)
        print(f"User {user_id} inserted successfully.")

        return create_response({'user_id': user_id})
    except Exception as e:
        return create_response({'error': 'Internal server error'}, status_code=500)