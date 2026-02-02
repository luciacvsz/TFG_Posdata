from enum import Enum

class Verdict(Enum):
    '''
    Enum for verdict preferences.

    Values:
    - SAFE: The item is safe.
    - MALICIOUS: The item is malicious.
    - SUSPICIOUS: The item is suspicious.
    - UNKNOWN: The item verdict is unknown.
    '''
    SAFE = 'safe'
    MALICIOUS = 'malicious'
    SUSPICIOUS = 'suspicious'
    UNKNOWN = 'unknown'

class ContactMethod(Enum):
    '''
    Enum for contact method preferences.

    Values:
    - SMS: Contact via SMS.
    - EMAIL: Contact via Email.
    '''
    SMS = 'SMS'
    EMAIL = 'Email'

def sms_emergency_contact_notification_message(full_name, verdict):
    '''
    Generate an SMS notification message for emergency contacts.

    Parameters
    ----------
        full_name : str
            The full name of the user.
        verdict : str
            The verdict of the item.

    Returns
    -------
        str
            The generated notification message.
    '''
    message = None

    if verdict == Verdict.SUSPICIOUS.value:
        message =  f"POSDATA - AVISO: El teléfono de {full_name} ha recibido un mensaje sospechoso. Te sugerimos llamarle para recordarle que no interactúe con él por seguridad."
    elif verdict == Verdict.MALICIOUS.value:
        message = f"POSDATA - URGENTE: El dispositivo de {full_name} acaba de recibir un mensaje malicioso. Por favor, contacta con él/ella inmediatamente para evitar que acceda."

    return message

def email_emergency_contact_notification_message(full_name, verdict):
    '''
    Generate an Email notification content (Subject and HTML Body) for emergency contacts.

    Parameters
    ----------
        full_name : str
            The full name of the user.
        verdict : str
            The verdict of the item (e.g., Verdict.SUSPICIOUS.value).

    Returns
    -------
        dict
            A dictionary containing:
            - 'subject': The email subject line.
            - 'html': The HTML body of the email.
            - 'text': A plain text version (fallback).
    '''
    message = None
    
    if verdict == Verdict.SUSPICIOUS.value:
        subject = f"⚠️ Aviso de Seguridad: Actividad sospechosa - {full_name}"
        color_hex = "#F59E0B"
        title_text = "Actividad Sospechosa Detectada"
        body_text = (
            f"El sistema de seguridad de POSDATA ha detectado un mensaje inusual "
            f"en el dispositivo de <strong>{full_name}</strong>.<br><br>"
            f"No es una emergencia crítica, pero el mensaje tiene características de spam o estafa. "
            f"Te sugerimos contactar con el usuario para recordarle que no comparta datos personales."
        )
        action_text = "Nivel de Riesgo: MEDIO - PRECAUCIÓN"

    elif verdict == Verdict.MALICIOUS.value:
        subject = f"🛑 URGENTE: Amenaza bloqueada en el dispositivo de {full_name}"
        color_hex = "#DC2626"
        title_text = "AMENAZA MALICIOSA DETECTADA"
        body_text = (
            f"<strong>ACCIÓN REQUERIDA:</strong> El sistema POSDATA ha identificado un intento de ataque "
            f"(phishing o malware) en el teléfono de <strong>{full_name}</strong>.<br><br>"
            f"Por favor, <strong>contacta con él/ella inmediatamente</strong> y asegúrate de que no abra "
            f"ningún enlace ni descargue archivos recibidos recientemente."
        )
        action_text = "Nivel de Riesgo: ALTO - ACCIÓN INMEDIATA"
    
    html_content = f"""
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
    </head>
    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333333; margin: 0; padding: 0;">
        <div style="max-width: 600px; margin: 0 auto; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;">
            
            <div style="background-color: {color_hex}; padding: 20px; text-align: center;">
                <h2 style="color: #ffffff; margin: 0; font-size: 24px;">{title_text}</h2>
            </div>
            
            <div style="padding: 20px; background-color: #ffffff;">
                <p style="font-size: 16px; margin-bottom: 20px;">
                    Hola,
                </p>
                <p style="font-size: 16px; margin-bottom: 20px;">
                    {body_text}
                </p>
                
                <div style="background-color: #f3f4f6; padding: 15px; border-left: 5px solid {color_hex}; margin: 20px 0;">
                    <strong style="color: {color_hex};">{action_text}</strong>
                </div>
                
                <p style="font-size: 14px; color: #6b7280; margin-top: 30px; border-top: 1px solid #e5e7eb; padding-top: 10px;">
                    Este es un mensaje automático del sistema de seguridad POSDATA.
                </p>
            </div>
        </div>
    </body>
    </html>
    """

    plain_text = f"""
    {title_text}
    --------------------------------------------------
    {body_text.replace('<br>', '\n').replace('<strong>', '').replace('</strong>', '')}
    
    {action_text}
    
    POSDATA Security Team
    """

    return {
        'subject': subject,
        'html': html_content,
        'text': plain_text
    }