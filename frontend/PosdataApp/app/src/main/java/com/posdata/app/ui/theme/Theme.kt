package com.posdata.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.posdata.app.model.AppColorScheme
import com.posdata.app.model.AppFontSize

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = LightScrim
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val HighContrastColorScheme = lightColorScheme(
    primary = HighContrastPrimary,
    onPrimary = HighContrastOnPrimary,
    primaryContainer = HighContrastPrimaryContainer,
    onPrimaryContainer = HighContrastOnPrimaryContainer,
    secondary = HighContrastSecondary,
    onSecondary = HighContrastOnSecondary,
    secondaryContainer = HighContrastSecondaryContainer,
    onSecondaryContainer = HighContrastOnSecondaryContainer,
    tertiary = HighContrastTertiary,
    onTertiary = HighContrastOnTertiary,
    tertiaryContainer = HighContrastTertiaryContainer,
    onTertiaryContainer = HighContrastOnTertiaryContainer,
    background = HighContrastBackground,
    onBackground = HighContrastOnBackground,
    surface = HighContrastSurface,
    onSurface = HighContrastOnSurface,
    surfaceVariant = HighContrastSurfaceVariant,
    onSurfaceVariant = HighContrastOnSurfaceVariant,
    error = HighContrastError,
    onError = HighContrastOnError,
    errorContainer = HighContrastErrorContainer,
    onErrorContainer = HighContrastOnErrorContainer,
    outline = HighContrastOutline,
    outlineVariant = HighContrastOutlineVariant
)

private val RedGreenSafeColorScheme = lightColorScheme(
    primary = RedGreenSafePrimary,
    onPrimary = RedGreenSafeOnPrimary,
    primaryContainer = RedGreenSafePrimaryContainer,
    onPrimaryContainer = RedGreenSafeOnPrimaryContainer,
    secondary = RedGreenSafeSecondary,
    onSecondary = RedGreenSafeOnSecondary,
    secondaryContainer = RedGreenSafeSecondaryContainer,
    onSecondaryContainer = RedGreenSafeOnSecondaryContainer,
    tertiary = RedGreenSafeTertiary,
    onTertiary = RedGreenSafeOnTertiary,
    tertiaryContainer = RedGreenSafeTertiaryContainer,
    onTertiaryContainer = RedGreenSafeOnTertiaryContainer,
    background = RedGreenSafeBackground,
    onBackground = RedGreenSafeOnBackground,
    surface = RedGreenSafeSurface,
    onSurface = RedGreenSafeOnSurface,
    surfaceVariant = RedGreenSafeSurfaceVariant,
    onSurfaceVariant = RedGreenSafeOnSurfaceVariant,
    error = RedGreenSafeError,
    onError = RedGreenSafeOnError,
    errorContainer = RedGreenSafeErrorContainer,
    onErrorContainer = RedGreenSafeOnErrorContainer,
    outline = RedGreenSafeOutline,
    outlineVariant = RedGreenSafeOutlineVariant
)

private val BlueYellowSafeColorScheme = lightColorScheme(
    primary = BlueYellowSafePrimary,
    onPrimary = BlueYellowSafeOnPrimary,
    primaryContainer = BlueYellowSafePrimaryContainer,
    onPrimaryContainer = BlueYellowSafeOnPrimaryContainer,
    secondary = BlueYellowSafeSecondary,
    onSecondary = BlueYellowSafeOnSecondary,
    secondaryContainer = BlueYellowSafeSecondaryContainer,
    onSecondaryContainer = BlueYellowSafeOnSecondaryContainer,
    tertiary = BlueYellowSafeTertiary,
    onTertiary = BlueYellowSafeOnTertiary,
    tertiaryContainer = BlueYellowSafeTertiaryContainer,
    onTertiaryContainer = BlueYellowSafeOnTertiaryContainer,
    background = BlueYellowSafeBackground,
    onBackground = BlueYellowSafeOnBackground,
    surface = BlueYellowSafeSurface,
    onSurface = BlueYellowSafeOnSurface,
    surfaceVariant = BlueYellowSafeSurfaceVariant,
    onSurfaceVariant = BlueYellowSafeOnSurfaceVariant,
    error = BlueYellowSafeError,
    onError = BlueYellowSafeOnError,
    errorContainer = BlueYellowSafeErrorContainer,
    onErrorContainer = BlueYellowSafeOnErrorContainer,
    outline = BlueYellowSafeOutline,
    outlineVariant = BlueYellowSafeOutlineVariant
)

private val GrayscaleColorScheme = lightColorScheme(
    primary = GrayscalePrimary,
    onPrimary = GrayscaleOnPrimary,
    primaryContainer = GrayscalePrimaryContainer,
    onPrimaryContainer = GrayscaleOnPrimaryContainer,
    secondary = GrayscaleSecondary,
    onSecondary = GrayscaleOnSecondary,
    secondaryContainer = GrayscaleSecondaryContainer,
    onSecondaryContainer = GrayscaleOnSecondaryContainer,
    tertiary = GrayscaleTertiary,
    onTertiary = GrayscaleOnTertiary,
    tertiaryContainer = GrayscaleTertiaryContainer,
    onTertiaryContainer = GrayscaleOnTertiaryContainer,
    background = GrayscaleBackground,
    onBackground = GrayscaleOnBackground,
    surface = GrayscaleSurface,
    onSurface = GrayscaleOnSurface,
    surfaceVariant = GrayscaleSurfaceVariant,
    onSurfaceVariant = GrayscaleOnSurfaceVariant,
    error = GrayscaleError,
    onError = GrayscaleOnError,
    errorContainer = GrayscaleErrorContainer,
    onErrorContainer = GrayscaleOnErrorContainer,
    outline = GrayscaleOutline,
    outlineVariant = GrayscaleOutlineVariant
)

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