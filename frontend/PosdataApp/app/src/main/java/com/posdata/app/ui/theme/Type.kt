package com.posdata.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.posdata.app.R

val SoraFontFamily = FontFamily(
    Font(R.font.sorasemibold, FontWeight.SemiBold),
    Font(R.font.sorabold, FontWeight.Bold),
    Font(R.font.soraextrabold, FontWeight.ExtraBold) // Archivo específico para ExtraBold
)

val DMSansFontFamily = FontFamily(
    Font(R.font.dmsansregular, FontWeight.Normal),
    Font(R.font.dmsansmedium, FontWeight.Medium),
    Font(R.font.dmsansbold, FontWeight.Bold),
    Font(R.font.dmsansextrabold, FontWeight.ExtraBold) // Al usar este archivo, se verá grueso de verdad
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 50.sp,
        lineHeight = 40.sp
    ),

    titleLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),

    labelLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 20.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = DMSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = DMSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
)