import boto3

def get_dynamodb_table(table_name, region_name='eu-west-3'):

    """
    Get a DynamoDB table resource.
    
    Parameters:
    
    table_name (str): The name of the DynamoDB table.
    region_name (str): The AWS region where the table is located. Default is 'eu-west-3'.
    
    Returns:
    
    table (boto3.resources.factory.dynamodb.Table): The DynamoDB table resource.
    
    """

    dynamodb = boto3.resource('dynamodb', region_name=region_name)
    table = dynamodb.Table(table_name)
    return table

def prepare_items(data_list):

    """
    Prepare a list of items for batch insertion into DynamoDB.
    
    Parameters:

    Returns:
    
    """

   