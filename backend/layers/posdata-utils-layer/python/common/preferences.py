from enum import Enum

class FontSize(Enum):
    '''
    Enum for font size preferences.

    Values:
    - REGULAR: Standard font size.
    - LARGE: Larger font size for better readability.
    '''
    REGULAR = 'regular'
    LARGE = 'large'

class NotificationSound(Enum):
    '''
    Enum for notification sound preferences.

    Values:
    - ON: Notifications sound is on.
    - OFF: Notifications sound is off.
    '''
    ON = 'on'
    OFF = 'off'

class ColorScheme(Enum):
    '''
    Enum for color scheme preferences.

    Values:
    - STANDARD: Standard color scheme.
    - HIGH_CONTRAST: High contrast color scheme for better readability.
    - PROTANOPIA: Color scheme for users with protanopia (red-green color blindness).
    - DEUTERANOPIA: Color scheme for users with deuteranopia (red-green color blindness).
    - TRITANOPIA: Color scheme for users with tritanopia (blue-yellow color blindness).
    - ACHROMATOPSIA: Color scheme for users with achromatopsia (no color vision).
    '''
    STANDARD = 'standard'
    HIGH_CONTRAST = 'high_contrast'
    PROTANOPIA = 'protanopia'
    DEUTERANOPIA = 'deuteranopia'
    TRITANOPIA = 'tritanopia'
    ACHROMATOPSIA = 'achromatopsia'

class Exhaustivity(Enum):
    '''
    Enum for exhaustivity preferences.

    Values:     
    - REGULAR: Regular exhaustivity. Only MALICIOUS and SUSPICIOUS are included.
    - ENHANCED: Enhanced exhaustivity. Includes ALL categories.
    '''
    REGULAR = 'regular'
    ENHANCED = 'enhanced'

class ExplanationMode(Enum):
    '''
    Enum for explanation mode preferences.

    Values:
    - OFF: Explanations are turned off.
    - ON: Explanations are turned on.
    '''
    OFF = 'off'
    ON = 'on'

class ExtraAlert(Enum):
    '''
    Enum for extra alert preferences.

    Values:
    - OFF: Extra alerts are turned off.
    - ON: Extra alerts are turned on. If the same MALICIOUS or SUSPICIOUS SMS is received by multiple
    users, an extra alert will be sent to all users with this preference enabled.
    '''
    OFF = 'off'
    ON = 'on'
