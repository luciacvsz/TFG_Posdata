package com.posdata.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.posdata.app.ui.theme.PosdataBlackText
import com.posdata.app.ui.theme.PosdataBlue
import com.posdata.app.ui.theme.PosdataGreyBorder
import com.posdata.app.ui.theme.PosdataLightBlue
@Composable
fun PosdataPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    colorOverride: Color? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        enabled = enabled && !isLoading,
    ) {
        val backgroundModifier = when {
            !enabled || isLoading -> Modifier.background(SolidColor(PosdataGreyBorder))
            colorOverride != null -> Modifier.background(SolidColor(colorOverride))
            else -> Modifier.background(
                Brush.horizontalGradient(colors = listOf(PosdataLightBlue, PosdataBlue))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (colorOverride != null && colorOverride != Color.Transparent) Color.White
                    else if (enabled) PosdataBlackText
                    else Color.Gray
                )
            }
        }
    }
}