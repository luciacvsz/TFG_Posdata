import boto3
import json
import logging
import os
from botocore.exceptions import ClientError
from common.responses import create_response

#Setup logging
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
table = dynamodb.Table(USERS_TABLE_NAME)

def dynamodb_update_preferences(user_id, updates):
    '''
    Update the user preferences data in the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user.
        updates : dict
            Dictionary with fields to update.

    Raises
    ------
        ValueError
            If the user does not exist.
        ClientError
            If there is an error updating the item in DynamoDB.
    '''
    update_parts = []
    attribute_names = {}
    attribute_values = {}

    attribute_names['#pref'] = 'PREFERENCES'
    
    for key, value in updates.items():
        attr_name_alias = f'#key_{key}'
        attr_value_alias = f':val_{key}'

        update_parts.append(f"#pref.{attr_name_alias} = {attr_value_alias}")
        attribute_names[attr_name_alias] = key
        attribute_values[attr_value_alias] = value
           
    if not update_parts:
        return
    
    try:
        table.update_item(
            Key={'PK': user_id},
            UpdateExpression=f'SET {", ".join(update_parts)}',
            ConditionExpression='attribute_exists(PK) AND #active = :true_val',
            ExpressionAttributeNames={
                '#active': 'ACTIVE',
                **attribute_names
            },
            ExpressionAttributeValues={
                ':true_val': True,
                **attribute_values
            }
        )
    except ClientError as e:
        if e.response['Error']['Code'] == 'ConditionalCheckFailedException':
            raise ValueError(f"User with ID {user_id} does not exist.")
        raise

def lambda_handler(event, context):
    '''
    Lambda function to update the preferences of an existing user in the DynamoDB table.

    Parameters
    ----------
        event : dict
            The event dictionary containing request data.
        context : object
            The context object containing runtime information.
    
    Returns
    -------
        dict
            The response dictionary.
    '''
    try:
        logger.info(f"Received event: {json.dumps(event)}")

        query_params = event.get('queryStringparameters', {})
        user_id = query_params.get('user_id') if query_params else None

        if not user_id:
            raise ValueError("Missing required query parameter: user_id")

        body = json.loads(event.get('body', '{}'))
        if not body:
            raise ValueError("Request body is required.")
        
        dynamodb_update_preferences(user_id, body)        

        logger.info(f"Successfully updated preferences for user ID: {user_id}")
        return create_response({}, status_code=200)
    
    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        return create_response({'ValueError': str(ve)}, status_code=400)    
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        return create_response({'error': 'Internal server error'}, status_code=500)