package com.posdata.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.posdata.app.model.AppColorScheme
import com.posdata.app.model.AppFontSize

@Composable
fun PosdataAppTheme(
    colorScheme: AppColorScheme = AppColorScheme.LIGHT,
    fontSize: AppFontSize = AppFontSize.REGULAR,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val activeColorScheme = when (colorScheme) {
        AppColorScheme.LIGHT -> LightColorScheme
        AppColorScheme.DARK -> DarkColorScheme
        AppColorScheme.HIGH_CONTRAST -> HighContrastColorScheme
        AppColorScheme.RED_GREEN_SAFE -> RedGreenSafeColorScheme
        AppColorScheme.BLUE_YELLOW_SAFE -> BlueYellowSafeColorScheme
        AppColorScheme.GRAYSCALE -> GrayscaleColorScheme
    }

    val activeTypography = when (fontSize) {
        AppFontSize.REGULAR -> RegularTypography
        AppFontSize.LARGE -> LargeTypography
    }

    MaterialTheme(
        colorScheme = activeColorScheme,
        typography = activeTypography,
        content = content
    )
}