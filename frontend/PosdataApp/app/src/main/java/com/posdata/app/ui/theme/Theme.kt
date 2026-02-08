package com.posdata.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val StandardColorScheme = lightColorScheme(
    primary = PosdataBlue,
    secondary = PosdataDarkBlue,
    tertiary = PosdataLightBlue,
    background = PosdataBackground,
    surface = PosdataSurface,
    onPrimary = PosdataWhite,
    onBackground = PosdataBlackText,
    onSurface = PosdataBlackText,
    error = PosdataRed,
    outline = PosdataGreyBorder
)

@Composable
fun PosdataAppTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = StandardColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}