import json

def create_response(body: dict, status_code: int = 200) -> dict:
    '''
    Create an HTTP response with the specified body and status code.

    Parameters
    ----------
    body : dict
        The response body.
    status_code : int, optional
        The HTTP status code (default is 200).

    Returns
    -------
    dict
        A dictionary formatted as an HTTP response.
    '''
    return {
        'statusCode': status_code,
        'body': json.dumps(body)
    }