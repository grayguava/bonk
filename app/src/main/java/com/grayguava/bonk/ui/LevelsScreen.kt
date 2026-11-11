package com.grayguava.bonk.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import com.grayguava.bonk.data.Level
import com.grayguava.bonk.data.TIERS
import com.grayguava.bonk.data.Tier
import com.grayguava.bonk.data.levelFor
import com.grayguava.bonk.data.levelsIn
import com.grayguava.bonk.data.nextLevel
import com.grayguava.bonk.data.plural
import com.grayguava.bonk.ui.theme.CardSurface

/** The Levels page: the tiers, their badges, and how far the streak has come. */
@Composable
fun LevelsScreen(streakStart: Long?) {
    // Ticks every second, so the bar creeps forward with the day rather than jumping.
    val elapsed = rememberElapsedSince(streakStart)
    val streakMillis = elapsed?.toMillis() ?: 0L
    val daysExact = streakMillis / 86_400_000.0
    val current = levelFor((streakMillis / 86_400_000L).toInt())
    val next = nextLevel(current)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Level ${current.number} · ${current.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (next != null) {
                        "${plural(next.days - (streakMillis / 86_400_000L).toInt(), "day")} until ${next.name}"
                    } else {
                        "Top level reached"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.55f),
                )
                if (next != null) {
                    Spacer(Modifier.height(14.dp))
                    // How far through this level the streak is, to the day and the second.
                    val progress = (
                        (daysExact - current.days) / (next.days - current.days)
                        ).coerceIn(0.0, 1.0).toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = Color(current.tier.color),
                        trackColor = Color.White.copy(alpha = 0.08f),
                        strokeCap = StrokeCap.Round,
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        items(TIERS) { tier ->
            TierCard(tier = tier, currentDays = (streakMillis / 86_400_000L).toInt(), current = current)
        }
    }
}

@Composable
private fun TierCard(tier: Tier, currentDays: Int, current: Level) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TierBadge(tier = tier, reached = currentDays >= levelsIn(tier).first().days)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = tier.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Levels ${levelsIn(tier).first().number}–${levelsIn(tier).last().number}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.45f),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            levelsIn(tier).forEach { level ->
                LevelRow(level = level, reached = currentDays >= level.days, current = level == current)
            }
        }
    }
}

@Composable
private fun LevelRow(level: Level, reached: Boolean, current: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(
                    color = if (reached) Color(level.tier.color) else Color.White.copy(alpha = 0.18f),
                    shape = CircleShape,
                )
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "${level.number}. ${level.name}",
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                current -> Color.White
                reached -> Color.White.copy(alpha = 0.8f)
                else -> Color.White.copy(alpha = 0.4f)
            },
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${level.days} ${if (level.days == 1) "day" else "days"}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = if (reached) 0.6f else 0.3f),
        )
    }
}

/** A tier's mark on its own, in the tier's colour. */
@Composable
fun TierBadge(
    tier: Tier,
    reached: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val color = Color(tier.color).copy(alpha = if (reached) 1f else 0.28f)
    Canvas(modifier.size(size)) {
        drawTierGlyph(tier, color)
    }
}

/** The tier's mark in a thin round frame, the way a profile picture sits in its ring. */
@Composable
fun TierAvatar(
    tier: Tier,
    modifier: Modifier = Modifier,
    size: Dp = 101.dp,
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(color = Color.White.copy(alpha = 0.04f))
            drawCircle(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.06f))
                ),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
        TierBadge(tier = tier, reached = true, size = size * 0.5f)
    }
}

/** A leaf: two arcs from the stem to the tip, bulging to one side. */
private fun leafPath(from: Offset, to: Offset, bulge: Float): Path {
    val mid = Offset((from.x + to.x) / 2f, (from.y + to.y) / 2f)
    val dx = to.x - from.x
    val dy = to.y - from.y
    val length = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
    val normal = Offset(-dy / length, dx / length)
    return Path().apply {
        moveTo(from.x, from.y)
        quadraticTo(mid.x + normal.x * bulge, mid.y + normal.y * bulge, to.x, to.y)
        quadraticTo(mid.x - normal.x * bulge, mid.y - normal.y * bulge, from.x, from.y)
        close()
    }
}

/** Each tier's own mark: a sapling, blocks, a peak, a gem, a star. */
private fun DrawScope.drawTierGlyph(tier: Tier, color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val u = min(size.width, size.height) * 0.38f

    when (tier) {
        // Awakening: a sapling, two leaves off a stem.
        Tier.I -> {
            drawLine(
                color = color,
                start = Offset(cx, cy + u * 1.05f),
                end = Offset(cx, cy - u * 0.3f),
                strokeWidth = u * 0.16f,
                cap = StrokeCap.Round,
            )
            drawPath(
                path = leafPath(
                    from = Offset(cx, cy + u * 0.05f),
                    to = Offset(cx + u * 0.92f, cy - u * 0.6f),
                    bulge = u * 0.42f,
                ),
                color = color,
            )
            drawPath(
                path = leafPath(
                    from = Offset(cx, cy + u * 0.5f),
                    to = Offset(cx - u * 0.88f, cy - u * 0.05f),
                    bulge = u * 0.4f,
                ),
                color = color,
            )
        }

        // Building: three blocks, each higher than the last.
        Tier.II -> {
            val w = u * 0.46f
            val base = cy + u * 0.8f
            listOf(0.8f, 1.2f, 1.65f).forEachIndexed { index, height ->
                drawRoundRect(
                    color = color,
                    topLeft = Offset(cx - u * 1.02f + index * u * 0.79f, base - u * height),
                    size = Size(w, u * height),
                    cornerRadius = CornerRadius(u * 0.1f),
                )
            }
        }

        // Strength: a mountain.
        Tier.III -> {
            val path = Path().apply {
                moveTo(cx - u * 1.05f, cy + u * 0.78f)
                lineTo(cx - u * 0.2f, cy - u * 0.78f)
                lineTo(cx + u * 0.22f, cy + u * 0.05f)
                lineTo(cx + u * 0.52f, cy - u * 0.42f)
                lineTo(cx + u * 1.05f, cy + u * 0.78f)
                close()
            }
            drawPath(path, color)
        }

        // Mastery: a cut gem.
        Tier.IV -> {
            val path = Path().apply {
                moveTo(cx, cy - u * 1.02f)
                lineTo(cx + u * 0.95f, cy)
                lineTo(cx, cy + u * 1.02f)
                lineTo(cx - u * 0.95f, cy)
                close()
            }
            drawPath(path, color)
            drawLine(
                color = color,
                start = Offset(cx, cy - u * 1.02f),
                end = Offset(cx - u * 0.4f, cy + u * 0.16f),
                strokeWidth = u * 0.13f,
            )
            drawLine(
                color = color,
                start = Offset(cx, cy - u * 1.02f),
                end = Offset(cx + u * 0.4f, cy + u * 0.16f),
                strokeWidth = u * 0.13f,
            )
        }

        // Transcendence: a star.
        Tier.V -> {
            val path = Path()
            val points = 8
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) u * 1.15f else u * 0.48f
                val angle = Math.toRadians(i * 180.0 / points - 90.0)
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color)
        }
    }
}
