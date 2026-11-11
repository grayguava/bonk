package com.grayguava.bonk.ui

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Smoked glass panel: translucent fill, gradient hairline edge, soft drop shadow.
 * Whatever sits behind it stays visible through the fill, so no blur shader is needed.
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    fillAlpha: Float = 1f,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val fill = if (dark) {
        listOf(
            FROST_TOP.copy(alpha = FROST_TOP.alpha * fillAlpha),
            FROST_BOTTOM.copy(alpha = FROST_BOTTOM.alpha * fillAlpha),
        )
    } else {
        listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0.45f))
    }
    val edge = if (dark) {
        listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.06f))
    } else {
        listOf(Color.Black.copy(alpha = 0.22f), Color.Black.copy(alpha = 0.05f))
    }
    val grain = rememberFrostGrain()

    Surface(
        modifier = modifier.shadow(18.dp, shape),
        shape = shape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, Brush.verticalGradient(edge)),
    ) {
        Column(
            modifier = Modifier
                .clip(shape)
                .background(Brush.verticalGradient(fill))
                .drawBehind { drawRect(grain) }
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick, role = Role.Button)
                    } else {
                        Modifier
                    }
                )
                .padding(contentPadding),
            content = content,
        )
    }
}

/** Glass pill button, same material as the panels. */
@Composable
fun GlassButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    GlassPanel(
        modifier = modifier,
        fillAlpha = if (enabled) 1f else 0.45f,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        onClick = if (enabled) onClick else null,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.35f),
        )
    }
}

/**
 * A tile of fine white speckle, laid over the glass so it reads as frost rather than
 * smoke. OpenTune's frosted style carries 6% noise for the same reason.
 */
@Composable
fun rememberFrostGrain(): Brush {
    val bitmap = remember { frostNoiseBitmap() }
    return remember(bitmap) {
        ShaderBrush(ImageShader(bitmap, TileMode.Repeated, TileMode.Repeated))
    }
}

private fun frostNoiseBitmap(size: Int = 96): ImageBitmap {
    val random = Random(11)
    val pixels = IntArray(size * size) {
        val alpha = (3 + random.nextInt(13)) shl 24
        alpha or 0x00FFFFFF
    }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
}

/** The glass material, shared by the dock and the floating buttons. */
internal val GlassFilm = Color.White.copy(alpha = 0.05f)
internal val GlassRimTop = Color.White.copy(alpha = 0.26f)
internal val GlassRimBottom = Color.White.copy(alpha = 0.04f)

/** Frost: a milky film, brighter and flatter than a smoked one, with grain over it. */
private val FROST_TOP = Color.White.copy(alpha = 0.11f)
private val FROST_BOTTOM = Color.White.copy(alpha = 0.06f)

/** A soft drop shadow so the glass floats over the page. */
internal fun Modifier.glassShadow(shape: Shape): Modifier =
    shadow(
        18.dp,
        shape,
        clip = false,
        ambientColor = Color.Black.copy(alpha = 0.35f),
        spotColor = Color.Black.copy(alpha = 0.45f),
    )

/** Round glass button holding one icon, in the dock's material. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val grain = rememberFrostGrain()
    Box(
        modifier
            .size(size)
            .glassShadow(CircleShape)
            .background(GlassFilm, CircleShape)
            .border(1.dp, Brush.verticalGradient(listOf(GlassRimTop, GlassRimBottom, GlassRimTop)), CircleShape)
            .clip(CircleShape)
            .drawBehind { drawRect(grain) }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(size * 0.45f),
        )
    }
}

/** Text field in the app's material: near-black fill, hairline white border. */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        placeholder = { Text(placeholder) },
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
            errorContainerColor = Color.White.copy(alpha = 0.03f),
            focusedBorderColor = Color.White.copy(alpha = 0.6f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            errorTextColor = Color.White,
            cursorColor = Color.White,
            focusedPlaceholderColor = Color.White.copy(alpha = 0.35f),
            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.35f),
        ),
        modifier = modifier,
    )
}
