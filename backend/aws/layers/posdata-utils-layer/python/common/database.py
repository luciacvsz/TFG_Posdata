from botocore.exceptions import ClientError

def check_user_exists(user_id: str, user_table: object) -> bool:
    '''
    Check if a user with the given primary key exists in the DynamoDB table.

    Parameters
    ----------
        user_id : str
            The primary key of the user to check.
        user_table : boto3.resources.factory.dynamodb.Table
            The DynamoDB table resource where users are stored.

    Returns
    -------
        bool
            True if the user exists, False otherwise.

    Raises
    ------
        ValueError
            If the primary key is not a string.
    '''
    if not isinstance(user_id, str):
        raise ValueError("Primary key must be a string.")
    response = user_table.get_item(Key={'PK': user_id})
    item = response.get('Item')
    if item and item.get('ACTIVE') is True:
        return True
    return False

def get_item_by_pk_sk(table: object, pk: str, sk: str=None) -> dict:
    '''
    Retrieve an item from a DynamoDB table using primary key and optional sort key.

    Parameters
    ----------
        table : boto3.resources.factory.dynamodb.Table
            The DynamoDB table resource to query.
        pk : str
            The primary key of the item to retrieve.
        sk : str, optional
            The sort key of the item to retrieve (default is None).
    Returns
    -------
        dict or None
            The retrieved item, or None if not found.
    '''
    key = {'PK': pk}
    if sk:
        key['SK'] = sk
    return table.get_item(Key=key).get('Item')

def update_active_user(table, user_id: str, update_expression: str,
                       expression_names: dict, expression_values: dict) -> None:
    '''
    Update an active user's attributes in the DynamoDB table.

    Parameters
    ----------
    table : boto3.resource
        The DynamoDB table resource.
    user_id : str
        The primary key of the user.
    update_expression : str
        The DynamoDB update expression string.
    expression_names : dict
        Additional expression attribute name aliases.
    expression_values : dict
        Additional expression attribute value aliases.

    Raises
    ------
    ValueError
        If the user does not exist or is inactive.
    ClientError
        If there is an error updating the item in DynamoDB.
    '''
    base_names = {'#active': 'ACTIVE', **expression_names}
    base_values = {':true_val': True, **expression_values}
    try:
        table.update_item(
            Key={'PK': user_id},
            UpdateExpression=update_expression,
            ConditionExpression='attribute_exists(PK) AND #active = :true_val',
            ExpressionAttributeNames=base_names,
            ExpressionAttributeValues=base_values
        )
    except ClientError as e:
        if e.response['Error']['Code'] == 'ConditionalCheckFailedException':
            raise ValueError(f"User with ID {user_id} does not exist.")
        raise