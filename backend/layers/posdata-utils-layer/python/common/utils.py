import uuid
from common.database import check_user_exists

def create_user_id(user_table):
    '''
    Creates a unique user identifier.
    
    Returns
    -------
        str
            A unique identifier in hexadecimal format.
    '''
    user_id = uuid.uuid4().hex
    while check_user_exists(user_id, user_table):
        user_id = uuid.uuid4().hex
    return user_id