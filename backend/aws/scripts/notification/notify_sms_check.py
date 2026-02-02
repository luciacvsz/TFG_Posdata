import json
import os
import boto3
from datetime import datetime
from common.database import get_item_by_pk_sk
from common.notification import ContactMethod, sms_emergency_contact_notification_message, email_emergency_contact_notification_message

# Environment variables
RESULTS_BUCKET_NAME = os.environ.get('RESULTS_BUCKET_NAME')
USERS_TABLE_NAME = os.environ.get('USERS_TABLE_NAME')
REGION_NAME = os.environ.get('REGION_NAME')

# Constants
SOURCE_EMAIL = "alertas_posdata@gmail.com"
SMS_SENDER_ID = "POSDATA"

# Initialize S3 client
s3_client = boto3.client('s3', region_name=REGION_NAME)

# Initialize DynamoDB resource and table
dynamodb = boto3.resource('dynamodb', region_name=REGION_NAME)
users_table = dynamodb.Table(USERS_TABLE_NAME)

# Initialize SNS client
sns_client = boto3.client('sns', region_name=REGION_NAME)

#Initialize SES client
ses_client = boto3.client('ses', region_name=REGION_NAME)

# Auxiliary functions
def user_notification(info, user_id):
    '''
    Store the notification data in an S3 bucket.

    Parameters
    ----------
    info : dict
        The information to be stored in the S3 bucket.
    user_id : str
        The ID of the user.

    Returns
    -------
    str
        The key of the stored object in S3.
    '''
    output_payload = info.copy()
    output_payload.update({
        'processed_at': datetime.now().isoformat()
    }) 

    file_key = f"notifications/{user_id}/{datetime.now().strftime('%Y/%m/%d/%H%M%S')}.json"

    s3_client.put_object(
        Bucket=RESULTS_BUCKET_NAME,
        Key=file_key,
        Body=json.dumps(output_payload),
        ContentType='application/json'
    )

    return file_key

def emergency_contacts_notification(info, user_id):
    '''
    Send notifications to the emergency contacts of a user.

    Parameters
    ----------
    info : dict
        The information to be sent in the notification.
    user_id : str
        The ID of the user.
    '''
    user_info = get_item_by_pk_sk(users_table, user_id)
    full_name = user_info.get('FULL_NAME')
    veredict = info.get('veredict')
    emergency_contacts = user_info.get('EMERGENCY_CONTACTS')
    phone_numbers = emergency_contacts.get('PHONE_NUMBERS')
    if phone_numbers != "NONE":
        sms_body = sms_emergency_contact_notification_message(full_name, veredict)
        for number in phone_numbers:
            print(f"Attempting SMS to emergency contact: {number}")
            try:
                sns_client.publish(
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
                print(f"SMS sent successfully to {number}")
            except Exception as e:
                print(f"Failed to send SMS to {number}: {e}")
            
    emails = emergency_contacts.get('EMAILS')
    if emails != "NONE":
        email_content = email_emergency_contact_notification_message(full_name, veredict)
        for email in emails:
            print(f"Attempting Email to emergency contact: {email}")
            try:
                ses_client.send_email(
                    Source=SOURCE_EMAIL,
                    Destination={
                        'ToAddresses': [email]
                    },
                    Message={
                        'Subject': {
                            'Data': email_content['subject']
                        },
                        'Body': {
                            'Text': {
                                'Html': {'Data': email_content['html']},
                                'Text': {'Data': email_content['text']}
                            }
                        }
                    }
                )
                print(f"Email sent successfully to {email}")
            except Exception as e:
                print(f"Failed to send Email to {email}: {e}")

#  Lambda handler
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
        print("Received veredict. Starting the notification preparation.")

        if not 'user_id' in event:
            raise ValueError("User ID is required.")
        user_id = event['user_id']
        if not 'sender' in event:
            raise ValueError("Sender is required.")
        if not 'message' in event:
            raise ValueError("Message content is required.")
        if not 'veredict' in event:
            raise ValueError("Veredict is required.")
        if not 'reason' in event:
            raise ValueError("Reason is required.")

        file_key = user_notification(event, user_id)
        print(f"Notification data stored in S3 at {file_key}.")

        emergency_contacts_notification(event, user_id)
        print("Emergency contacts notified.")
        
    except ValueError as ve:
        print(f"ValueError: {ve}")
        raise ve
    except Exception as e:
        print(f"Error processing SMS: {e}")
        raise e