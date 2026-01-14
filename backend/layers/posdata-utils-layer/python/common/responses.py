import json

def create_response(body, status_code=200):
    '''
    Crea una respuesta HTTP con el cuerpo y el código de estado especificados.

    Parameters
    ----------
        body : dict
            El cuerpo de la respuesta.
        status_code : int, optional
            El código de estado HTTP (default es 200).
    
    Returns
    -------
        dict
            Un diccionario formateado como respuesta HTTP.
    '''
    return {
        'statusCode': status_code,
        'body': json.dumps(body)
    }

def create_detailed_response(status, reason, details='', status_code=200):
    '''
    Crea una respuesta detallada con estado, razón y detalles adicionales.

    Parameters
    ----------
        status : str
            El estado de la respuesta (e.g., "success", "error").
        reason : str
            La razón principal de la respuesta.
        details : str, optional
            Detalles adicionales sobre la respuesta (default es una cadena vacía).
        status_code : int, optional
            El código de estado HTTP (default es 200).

    Returns
    -------
        dict
            Un diccionario formateado como respuesta HTTP.
    '''
    body = {
        'status': status,
        'reason': reason,
        'details': details
    }
    return create_response(body, status_code)