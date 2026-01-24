import json
import time

# ESTO ES UN DUMMY!!!!!!!!!!!!!!!!!!!!!!!

#Lambda function
def lambda_handler(event, context):
    try:
        print("Starting AI SMS Check.")

        user_id = event.get('user_id')
        message = event.get('message', '')
        sender = event.get('sender', 'UNKNOWN')
        
        time.sleep(2)

        message_lower = message.lower()

        if "virus" in message_lower or "malware" in message_lower:
            veredict = "MALICIOUS"
            reason = "Message contains suspicious content"
            details = "Contains words like 'virus' or 'malware'"
            confidence = 0.95
        else:
            veredict = "SAFE"
            reason = "No suspicious content detected"
            details = "Message content appears normal"
            confidence = 0.90
        
        output_payload = {
            "user_id": user_id,
            "sender": sender,
            "message": message,
            "veredict": veredict,
            "reason": reason,
            "details": details,
            "confidence": confidence
        }

        print(f"AI SMS Check completed successfully. Veredict: {veredict}")

        return output_payload
    except Exception as e:
        print(f"Error during AI SMS Check: {str(e)}")
        raise e