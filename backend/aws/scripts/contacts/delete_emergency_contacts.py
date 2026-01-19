import boto3
import json
import os
from common.database import check_user_exists
from common.validators import is_valid_email, is_valid_phone_number
from common.responses import create_response

# Environment variables
USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
CONTACTS_TABLE_NAME = os.environ.get('CONTACTS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME', 'eu-west-3')

# Initialize DynamoDB resource and tables
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
users_table = dynamodb.Table(USERS_TABLE_NAME)
contacts_table = dynamodb.Table(CONTACTS_TABLE_NAME)

#Auxiliary functions
def dynamodb_deletion(user_id, contacts):
    '''
    Delete emergency contacts for a user from the DynamoDB table using batch writer.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
        contacts : list
            A list of dictionaries containing emergency contact data to be deleted from DynamoDB.
    '''
    with contacts_table.batch_writer() as batch:
        for contact in contacts:
            if not isinstance(contact, str):
                raise ValueError("Each contact must be a string.")
            if not (is_valid_email(contact) or is_valid_phone_number(contact)):
                raise ValueError(f"Invalid contact format: {contact}")
            key = {
                'PK': user_id,
                'SK': contact
            }
            batch.delete_item(Key=key)
    
# Lambda handler
def lambda_handler(event, context):
    '''
    Delete emergency contacts for a user.

    Parameters
    ----------
        event : dict
            The event data from the Lambda invocation.
        context : object
            The runtime information of the Lambda function.

    Returns
    -------
        dict
            A dictionary containing the status code and message of the operation.

    Raises
    ------
        ValueError
            If there are issues with the event data or contact formats.
        Exception
            If there is an error during processing.
    '''
    try:
        print("Starting the emergency contacts deletion process.")

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
        if not user_id or not isinstance(user_id, str) or not check_user_exists(user_id, users_table):
            raise ValueError("User ID is required and must exist.")
        
        contacts = body.get('emergency_contacts')
        if not contacts or not isinstance(contacts, list):
            raise ValueError("Invalid or missing emergency_contacts.")
        
        exists = check_user_exists(user_id, users_table)
        if not exists:
            raise ValueError(f"User with user_id {user_id} does not exist.")
        
        print("Deleting emergency contacts from DynamoDB.")
        dynamodb_deletion(user_id, contacts)

        print("Emergency contacts deleted successfully.")
        return create_response({'message': 'Emergency contacts deleted successfully.'}) 
       
    except ValueError as ve:
        return create_response({'ValueError': str(ve)}, status_code=400)
    except Exception as e:
        return create_response({'error': 'Internal server error'}, status_code=500)
