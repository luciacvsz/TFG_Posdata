import boto3
import json
import logging
import os
from botocore.exceptions import ClientError
from common.responses import create_response

# Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Environment variables
REQUIRED_VARS = ['USERS_TABLE_NAME']
for var in REQUIRED_VARS:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")

USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME', 'eu-west-3')

# Initialize resources
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
users_table = dynamodb.Table(USERS_TABLE_NAME)

def dynamodb_deactivation(user_id: str) -> None:
    '''
    Deactivate a user in the DynamoDB tables.

    Parameters
    ----------
        user_id : str
            The primary key of the user.

    Raises
    ------
        ValueError
            If the user does not exist.
        ClientError
            If there is an error updating the item in DynamoDB.
    '''
    try:
        users_table.update_item(
            Key={
                'PK': user_id
            },
            UpdateExpression="SET ACTIVE = :val",
            ConditionExpression='attribute_exists(PK) AND #active = :true_val',
            ExpressionAttributeNames={
                '#active': 'ACTIVE'
            }, 
            ExpressionAttributeValues={
                ':val': False,
                ':true_val': True
            },
        )
    except ClientError as e:
        if e.response['Error']['Code'] == 'ConditionalCheckFailedException':
            raise ValueError(f"User with ID {user_id} does not exist.")
        raise

def lambda_handler(event, context):
    '''
    AWS Lambda handler to deactivate a user.

    Parameters
    ----------
        event : dict
            The event data from the Lambda invocation.
        context : object
            The runtime information of the Lambda function.
    
    Returns
    -------
        dict
            The response object.
    '''
    try:
        logger.info(f"Received event: {json.dumps(event)}")

        user_id = event['pathParameters']['user_id']
        
        dynamodb_deactivation(user_id)

        logger.info(f"Successfully deactivated user: {user_id}")
        return create_response({}, status_code=204)
    
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        return create_response({'ValueError': str(ve)}, status_code=400)
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        return create_response({'error': 'Internal server error'}, status_code=500)