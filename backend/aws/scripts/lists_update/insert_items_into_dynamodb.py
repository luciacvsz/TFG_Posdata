import boto3
import csv
import io
import os
from datetime import datetime, timezone
from common.responses import create_response

# Environment variables

TABLE_NAME = os.environ.get('LISTS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

#  Initialize DynamoDB resource and table

dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
table = dynamodb.Table(TABLE_NAME)

# Initialize S3 client

s3 = boto3.client('s3', region_name=REGION_NAME)

# Auxiliary functions

def read_csv_from_s3(content):
    """
    Parse CSV content and return a list of valid data dictionaries.
    
    Parameters
    ----------
        content : str
            The content of the CSV file as a string.

    Returns
    -------
        list
            A list of dictionaries containing valid data from the CSV. Each dictionary contains 'PK', 'SK', and 'DESCRIPTION' keys.
    """
    csv_file = io.StringIO(content)
    reader = csv.DictReader(csv_file)
    data_list = [row for row in reader if 
                    row.get('PK') and row.get('SK') and row.get('DESCRIPTION')]

    return data_list

def dynamodb_insertion(data_list):
    '''
    Insert a list of data dictionaries into the DynamoDB table using batch writer.

    Parameters
    ----------
        data_list : list
            A list of dictionaries containing data to be inserted into DynamoDB.
    '''
    with table.batch_writer() as batch:
            for data in data_list:
                item = data.copy()
                item['UPLOAD_DATE'] = datetime.now(timezone.utc).isoformat()
                batch.put_item(Item=item)

# Lambda handler

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
    
    Raises
    ------
        ValueError
            If there are issues with the event data or file content.
        Exception
            If there is an error during processing.
    '''
    try:
        if not event.get('Records'):
            raise ValueError("No records found in the event.")
        
        print("Event received successfully. Processing records.")
        
        record = event['Records'][0]
        try:
            bucket_name = record['s3']['bucket']['name']
            file_key = record['s3']['object']['key']
        except KeyError as ke:
            raise ValueError(f"Invalid S3 event structure : {ke}")

        if not file_key.lower().endswith('.csv'):
            raise ValueError(f"File {file_key} is not a CSV file.")

        print(f"Fetching file {file_key} from bucket {bucket_name}")

        response = s3.get_object(Bucket=bucket_name, Key=file_key)
        body = response['Body'].read().decode('utf-8')

        if not body.strip():
            raise ValueError(f"The file {file_key} is empty.")

        print("Fetched file content successfully. Reading CSV data.")

        data_list = read_csv_from_s3(body)
        if not data_list:
            raise ValueError("No valid data found in the CSV file.")
        
        print(f"Read {len(data_list)} valid records from the CSV file. Inserting into DynamoDB.")

        dynamodb_insertion(data_list)

        print("All records inserted successfully.")

        return create_response({'message': f'Successfully processed {len(data_list)} records from {file_key}'})
    
    except ValueError as ve:
        print(f"ValueError: {ve}")
        return create_response({'error': str(ve)}, status_code=400)
    except Exception as e:
        print(f"Error processing file: {e}")
        return create_response({'error': 'Internal server error'}, status_code=500)