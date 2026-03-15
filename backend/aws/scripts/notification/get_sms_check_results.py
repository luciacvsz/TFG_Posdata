import boto3
import json
import logging
import os
from common.responses import create_response

# Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

#Environment variables
REQUIRED_VARS = ['RESULTS_BUCKET_NAME', 'USERS_TABLE_NAME']
for var in REQUIRED_VARS:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")
    
USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
RESULTS_BUCKET_NAME = os.environ.get('RESULTS_BUCKET_NAME')
REGION_NAME = os.environ.get('REGION_NAME', 'eu-west-3')

# Initialize DynamoDB resource and table
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(USERS_TABLE_NAME)

#Initialze S3 client
s3 = boto3.client('s3', region_name=REGION_NAME)

def get_user_sms_results(user_id: str, execution_id: str) -> tuple:
    '''
    Retrieve the SMS check results for a given user and execution ID from the S3 bucket.  
      
    Parameters
    ----------
    user_id : str
        The ID of the user.
    execution_id : str
        The execution ID associated with the SMS check results.

    Returns
    -------
    tuple
        A tuple containing the SMS check results (dict) and the S3 file key (str).
    '''
    file_key = f"notifications/{user_id}/{execution_id}.json"

    obj = s3.get_object(Bucket=RESULTS_BUCKET_NAME, Key=file_key)
    content = json.loads(obj['Body'].read().decode('utf-8'))

    return content, file_key

def delete_notification(file_key: str) -> None:
    '''
    Delete a notification from the S3 bucket.

    Parameters
    ----------
    file_key : str
        The S3 file key of the notification to be deleted.
    '''
    s3.delete_object(Bucket=RESULTS_BUCKET_NAME, Key=file_key)

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
        logger.info(f"Received event: {json.dumps(event)}")

        user_id = event['pathParameters']['user_id']
        execution_id = event['pathParameters']['execution_id']
        
        results, file_key = get_user_sms_results(user_id, execution_id)
        if results is None:
            raise ValueError("No SMS check results found for the user.")

        delete_notification(file_key)

        logger.info("SMS check results retrieved and notification deleted successfully.")
        return create_response({'results': results}, status_code=200)
    
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        return create_response({'ValueError': str(ve)}, status_code=400)
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        return create_response({'error': 'Internal server error'}, status_code=500)