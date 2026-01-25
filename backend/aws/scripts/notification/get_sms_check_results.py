import json
import boto3
import os
from common.database import check_user_exists
from common.response import create_response

#Environment variables
USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
RESULTS_BUCKET_NAME = os.environ.get('RESULTS_BUCKET_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

# Initialize DynamoDB resource and table
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(USERS_TABLE_NAME)

#Initialze S3 client
s3 = boto3.client('s3', region_name=REGION_NAME)

# Auxiliary functions
def get_user_sms_results(user_id):
    '''
    Retrieve the oldest SMS check results for a given user from the S3 bucket.
    
    Parameters
    ----------
    user_id : str
        The ID of the user.

    Returns
    -------
    tuple
        A tuple containing the SMS check results (dict) and the S3 file key (str).
    '''
    response = s3.list_objects_v2(
        Bucket=RESULTS_BUCKET_NAME,
        Prefix=f"notifications/{user_id}/"
    )
    if 'Contents' not in response:
        return None, None

    oldest_file = min(response['Contents'], key=lambda x: x['Key'])
    file_key = oldest_file['Key']

    obj = s3.get_object(Bucket=RESULTS_BUCKET_NAME, Key=file_key)
    content = json.loads(obj['Body'].read().decode('utf-8'))

    return content, file_key

def delete_notification(file_key):
    '''
    Delete a notification from the S3 bucket.

    Parameters
    ----------
    file_key : str
        The S3 file key of the notification to be deleted.
    '''
    s3.delete_object(Bucket=RESULTS_BUCKET_NAME, Key=file_key)

# Lambda handler
def lambda_handler(event, context):
    '''
    AWS Lambda handler to get SMS check results for a user and delete the notification from S3.
    Parameters
    ----------
        event : dict
            The event data from the Lambda invocation.
        context : object
            The runtime information of the Lambda function.
    Returns
    -------
        dict
            The SMS check results for the user.
    '''
    try:
        print("Processing get SMS check results request.")

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
        
        results, file_key = get_user_sms_results(user_id)
        if results is None:
            raise ValueError("No SMS check results found for the user.")
        print("SMS check results retrieved successfully. Deleting notification from S3.")

        delete_notification(file_key)
        print("Notification deleted successfully.")

        return create_response({'results': results})
    
    except ValueError as ve:
        return create_response({'ValueError': str(ve)}, status_code=400)
    except Exception as e:
        return create_response({'error': 'Internal server error'}, status_code=500)