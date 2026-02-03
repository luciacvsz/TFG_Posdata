import boto3
import csv
import io
import json
import logging
import os
from botocore.exceptions import ClientError
from common.responses import create_response
from datetime import datetime, timezone

# Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Environment variables
REQUIRED_VARS = ['LISTS_TABLE_NAME']
for var in REQUIRED_VARS:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")
    
LISTS_TABLE_NAME = os.environ.get('LISTS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME', 'eu-west-3')

# Initialize resources
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(LISTS_TABLE_NAME)
s3 = boto3.client('s3', region_name=REGION_NAME)

def process_csv_and_upload(bucket, key):
    '''
    Processes a CSV file from S3 and uploads its contents to DynamoDB.

    Parameters
    ----------
        bucket : str
            The S3 bucket name.
        key : str
            The S3 object key.
    
    Returns
    -------
        int
            The number of items successfully processed and uploaded.

    Raises
    ------
        ClientError
            If there is an error accessing the S3 object.
        Exception
            If there is an error parsing the CSV or uploading to DynamoDB.
    '''
    try:
        response = s3.get_object(Bucket=bucket, Key=key)

        stream = io.TextIOWrapper(response['Body'], encoding='utf-8')
        csv_reader = csv.DictReader(stream)

        count = 0
        with table.batch_writer() as batch:
            for row in csv_reader:
                if not all(k in row for k in ['PK', 'SK', 'DESCRIPTION']):
                    continue

                item = {k: v for k, v in row.items() if v}
                item['UPLOAD_DATE'] = datetime.now(timezone.utc).isoformat()

                batch.put_item(Item=item)
                count += 1

        return count
    
    except ClientError as e:
        logger.error(f"ClientError while processing S3 object: {e}")
        raise
    except Exception as e:
        logger.error(f"Error parsing CSV and uploading to DynamoDB: {e}")
        raise

def lambda_handler(event, context):
    '''
    AWS Lambda handler to process S3 event, read CSV file, and insert data into DynamoDB.

    Parameters
    ----------
        event : dict
            The event data from S3.
        context : object
            The runtime information of the Lambda function.

    Returns
    -------
        dict
            A dictionary containing the status code and message of the operation.
    '''
    try:
        logger.info(f"Received S3 event: {json.dumps(event)}")

        if 'Records' not in event:
            raise ValueError("Event does not contain 'Records' key.")

        total_processed = 0
        for record in event['Records']:
            bucket = record['s3']['bucket']['name']
            key = record['s3']['object']['key']

            if not key.lower().endswith('.csv'):
                logger.warning(f"Skipping non-CSV file: {key}")
                continue

            logger.info(f"Processing file {key} from bucket {bucket}")
            total_processed += process_csv_and_upload(bucket, key)
        
        return create_response({'message': f'Successfully processed {total_processed} records from the provided files.'}, status_code=200)
    
    except ValueError as ve:
        logger.error(f"Validation error: {ve}")
        return create_response({'ValueError': str(ve)}, status_code=400)
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        return create_response({'error': 'Internal server error'}, status_code=500)