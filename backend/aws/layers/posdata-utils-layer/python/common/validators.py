import re

_EMAIL_PATTERN = re.compile(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$')
_PHONE_PATTERN = re.compile(r'^(\+?[1-9]\d{1,14}|[0-9]{9,15})$')
_HTTP = r"https?://[^\s]+" # Simplified URL pattern for http and https
_WWW = r"www\.[^\s]+" # Simplified URL pattern for www
_DOMAIN = r"((?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}(?:/[^\s]*)?)" # Simplified domain pattern with optional path
_URL_REGEX = re.compile(f"({_HTTP})|({_WWW})|({_DOMAIN})") # Combined regex to match URLs in different formats

def is_valid_email(email: str) -> bool:
    '''
    Validate if the provided email address is in a correct format.

    Parameters
    ----------
        email : str
            The email address to validate.
    
    Returns
    -------
        bool
            True if the email is valid, False otherwise.
    '''
    if not isinstance(email, str):
        return False
    return bool(_EMAIL_PATTERN.match(email))

def is_valid_phone(phone: str) -> bool:
    '''
    Validate if the provided phone number is in a correct format.

    Parameters
    ----------
        phone : str
            The phone number to validate.

    Returns
    -------
        bool
            True if the phone number is valid, False otherwise.
    '''
    if not isinstance(phone, str):
        return False
    return bool(_PHONE_PATTERN.match(phone))

def extract_urls(message: str) -> list:
    '''
    Extract all URLs from a given text message.

    Parameters
    ----------
        message : str
            The text message from which to extract URLs.
    
    Returns
    -------
        list
            A list of extracted URLs.
    '''
    if not message or not isinstance(message, str):
        return []

    urls = _URL_REGEX.findall(message)

    extracted_urls = []
    for match in urls:
        url_raw = next((group for group in match if group), '')
        url_clean = re.sub(r'[.,;:|?)\]\'\"]+$', '', url_raw.strip())
        extracted_urls.append(url_clean)

    return extracted_urls