import boto3
import json
import logging
import os
from botocore.exceptions import ClientError
from common.database import get_item_by_pk_sk
from common.notification import sms_emergency_contact_notification_message, email_emergency_contact_notification_message, Verdict
from datetime import datetime, timezone

# Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Environment variables
REQUIRED_VARS = ['RESULTS_BUCKET_NAME', 'USERS_TABLE_NAME', 'SOURCE_EMAIL', 'SMS_SENDER_ID']
for var in REQUIRED_VARS:
    if not os.environ.get(var):
        raise RuntimeError(f"Missing required environment variable: {var}")

RESULTS_BUCKET_NAME = os.environ.get('RESULTS_BUCKET_NAME')
USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME', 'eu-west-3')
SOURCE_EMAIL = os.environ.get('SOURCE_EMAIL')
SMS_SENDER_ID = os.environ.get('SMS_SENDER_ID')

# Initialize resources
s3 = boto3.client('s3', region_name=REGION_NAME)
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
users_table = dynamodb.Table(USERS_TABLE_NAME)
sns = boto3.client('sns', region_name=REGION_NAME)
ses = boto3.client('ses', region_name=REGION_NAME)

def store_notification_in_s3(info, user_id):
    '''
    Stores the notification information in S3.

    Parameters
    ----------
    info : dict
        The notification information to store.
    user_id : str
        The user ID associated with the notification.

    Returns
    -------
    str
        The S3 file key where the notification is stored.

    Raises
    ------
    ClientError
        If there is an error storing the object in S3.
    '''
    now = datetime.now(timezone.utc)
    output_payload = {**info, 'processed_at': now.isoformat()}
    file_key = f"notifications/{user_id}/{now.strftime('%Y/%m/%d/%H%M%S')}.json"

    try:
        s3.put_object(
            Bucket=RESULTS_BUCKET_NAME,
            Key=file_key,
            Body=json.dumps(output_payload),
            ContentType='application/json'
        )
        return file_key
    except ClientError:
        logger.error(f"Failed to store notification in S3 for user: {user_id}")
        raise

def notify_emergency_contacts(info, user_id):
    '''
    Notifies the emergency contacts of a user via SMS and email.

    Parameters
    ----------
    info : dict
        The notification information containing the verdict.
    user_id : str
        The user ID whose emergency contacts will be notified.

    Raises
    ------
    Exception
        If there is an error sending notifications.
    '''
    user_info = get_item_by_pk_sk(users_table, user_id)
    if not user_info:
        logger.error(f"User not found: {user_id}")
        return
    
    full_name = user_info.get('FULL_NAME', 'User')
    verdict = info.get('verdict')
    emergency_contacts = user_info.get('EMERGENCY_CONTACTS', {})

    phone_numbers = emergency_contacts.get('phone_numbers')
    if phone_numbers and phone_numbers != "NONE":
        sms_body = sms_emergency_contact_notification_message(full_name, verdict)
        for number in phone_numbers:
            try:
                sns.publish(
                    PhoneNumber=number,
                    Message=sms_body,
                    MessageAttributes={
                        'AWS.SNS.SMS.SMSType': {
                            'DataType': 'String',
                            'StringValue': 'Transactional'
                        },
                        'AWS.SNS.SMS.SenderID': {
                            'DataType': 'String',
                            'StringValue': SMS_SENDER_ID
                        }
                    }
                )
                logger.info(f"SMS sent successfully to {number}")
            except Exception as e:
                logger.error(f"Failed to send SMS to {number}: {e}")
    
    emails = emergency_contacts.get('emails')
    if emails and emails != "NONE":
        email_content = email_emergency_contact_notification_message(full_name, verdict)
        for email in emails:
            try:
                ses.send_email(
                    Source=SOURCE_EMAIL,
                    Destination={'ToAddresses': [email]},
                    Message={
                        'Subject': {'Data': email_content['subject']},
                        'Body': {
                            'Html': {'Data': email_content['html']},
                            'Text': {'Data': email_content['text']}
                        }
                    }
                )
                logger.info(f"Email sent successfully to {email}")
            except Exception as e:
                logger.error(f"Failed to send Email to {email}: {e}")

def lambda_handler(event, context):
    '''
    AWS Lambda handler to process SMS check notifications.

    Parameters
    ----------
    event : dict
        The event data from the Lambda invocation.
    context : object
        The runtime information of the Lambda function.

    Raises
    ------
        ValueError
            If required fields are missing in the event.
        Exception
            For any other errors during processing.
    '''
    try:
        logger.info("Processing notification request.")

        required = ['user_id', 'sender', 'message', 'verdict', 'reason']
        missing = [f for f in required if f not in event]
        if missing:
            raise ValueError(f"Missing required fields in event: {', '.join(missing)}")
        
        user_id = event['user_id']

        path = store_notification_in_s3(event, user_id)
        if event['verdict'] in [Verdict.SUSPICIOUS.value, Verdict.MALICIOUS.value]:
            notify_emergency_contacts(event, user_id)

        return {
            "status": "success",
            "s3_path": path,
            "user_id": user_id
        }

    except ValueError as ve:
        logger.warning(f"Validation error: {ve}")
        raise
    except Exception as e:
        logger.error(f"System failure: {e}", exc_info=True)
        raise