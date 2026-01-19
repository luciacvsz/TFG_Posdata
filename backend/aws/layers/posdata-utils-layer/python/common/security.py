import hashlib

def get_sha512_hash(text):
    """
    Get SHA-512 hash of the given text.

    Parameters
    ----------
    text : str
        The input text to hash.

    Returns
    -------
    str
        The SHA-512 hash of the input text in hexadecimal format.
    """
    sha512_hash = hashlib.sha512(text.encode('utf-8')).hexdigest()
    return sha512_hash