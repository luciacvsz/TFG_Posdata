import re

# Key words for smishing detection
urgency_words = ['urgente','urgently','urgent','inmediatamente','immediately','ahora','now','hoy','today','bloquea','blocked','suspendida','suspended','cancel','cancela','verifique','verify']
action_words = ['haga clic','click','acceda','access','llame','call','responda','reply','confirme','confirm','descargue','download','ingrese','enter']
financial_words = ['cuenta','account','banco','bank','tarjeta','card','pago','payment','transferencia','transfer','bizum','credito','credit','débito','debit']
prize_words = ['gratis','free','premio','prize','ganador','winner','regalo','gift','oferta','offer','descuento','discount','gana','win']
threat_words = ['amenaza','threat','peligro','danger','dangerous','peligroso','cuidado','beware','attention','atencion','careful']

# Detection patterns for suspicious content
url_pattern       = re.compile(r"(https?://[^\s]+)|(www\.[^\s]+)")
shortener_pattern = re.compile(r'\b(bit\.ly|t\.co|tinyurl\.com|goo\.gl|ow\.ly|rb\.gy|cutt\.ly)\b')
phone_pattern     = re.compile(r'(\+?[1-9]\d{1,14}|[0-9]{9,15})')