def check_user_exists(user_id, user_table):
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

def get_item_by_pk_sk(table, pk, sk=None):
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