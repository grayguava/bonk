package com.grayguava.bonk.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grayguava.bonk.data.Level
import com.grayguava.bonk.data.TIERS
import com.grayguava.bonk.data.Tier
import com.grayguava.bonk.data.levelFor
import com.grayguava.bonk.data.levelsIn
import com.grayguava.bonk.data.nextLevel
import com.grayguava.bonk.data.plural
import com.grayguava.bonk.ui.theme.CardSurface

/** The Levels page: the tiers, the level art, and how far the streak has come. */
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
        LevelArt(
            level = level.number,
            size = 40.dp,
            alpha = if (reached) 1f else 0.3f,
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

