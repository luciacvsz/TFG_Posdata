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
    - LIGHT: Light color scheme with bright backgrounds and dark text.
    - DARK: Dark color scheme with dark backgrounds and light text.
    - HIGH_CONTRAST: High contrast color scheme for better visibility.
    - RED_GREEN_SAFE: Color scheme that is safe for red-green color blindness.
    - BLUE_YELLOW_SAFE: Color scheme that is safe for blue-yellow color blindness.
    - GRAYSCALE: Grayscale color scheme for users with color vision deficiencies.
    '''
    LIGHT = 'light'
    DARK = 'dark'
    HIGH_CONTRAST = 'high_contrast'
    RED_GREEN_SAFE = 'red_green_safe'
    BLUE_YELLOW_SAFE = 'blue_yellow_safe'
    GRAYSCALE = 'grayscale'

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

DEFAULT_PREFERENCES = {
    'font_size': FontSize.REGULAR.value,
    'notification_sound': NotificationSound.ON.value,
    'color_scheme': ColorScheme.LIGHT.value,
    'exhaustivity': Exhaustivity.REGULAR.value,
    'explanation_mode': ExplanationMode.ON.value
}
