package com.grayguava.bonk.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Material's own surfaces: the filled card and the tonal card, on the app's black.
 * Neutral greys — the Material dark scheme's own surfaceContainer roles carry a blue cast.
 */
internal val CardSurface = Color(0xFF141414)
internal val TonalSurface = Color(0xFF242424)

private val BonkDark = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Color(0xFFBDBDBD),
    onSecondary = Color.Black,
    tertiary = Color(0xFFBDBDBD),
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF1E1E1E),
)

@Composable
fun BonkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BonkDark,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}
