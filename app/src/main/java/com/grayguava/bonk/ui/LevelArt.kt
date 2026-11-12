package com.grayguava.bonk.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * The artwork for a level: one character per level, in the assets.
 *
 * The files are webp squares, decoded down to what each one is drawn at and no
 * further. The pictures are kept, one per level and size, for as long as the app
 * runs.
 */
@Composable
fun LevelArt(
    level: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val context = LocalContext.current
    val pixels = with(LocalDensity.current) { size.roundToPx() }
    val art by produceState<ImageBitmap?>(null, level, pixels) {
        value = withContext(Dispatchers.IO) { LevelArtCache.load(context, level, pixels) }
    }
    val bitmap = art
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alpha = alpha,
            modifier = modifier.size(size),
        )
    }
}

/** The decoded pictures, kept per level and per size. */
private object LevelArtCache {
    private val bitmaps = ConcurrentHashMap<Pair<Int, Int>, ImageBitmap>()

    fun load(context: Context, level: Int, pixels: Int): ImageBitmap? {
        val key = level to pixels
        bitmaps[key]?.let { return it }
        val bitmap = decode(context, "levels/$level.webp", pixels) ?: return null
        bitmaps[key] = bitmap
        return bitmap
    }

    /**
     * Decode the picture at [pixels] across. The sample size has to be a power of
     * two, so the picture lands on the next size down that still covers what it is
     * drawn at, and never larger than the source.
     */
    private fun decode(context: Context, path: String, pixels: Int): ImageBitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        runCatching {
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        }
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= pixels) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return runCatching {
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
        }.getOrNull()?.asImageBitmap()
    }
}
