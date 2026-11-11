package com.grayguava.bonk.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grayguava.bonk.data.easterEgg
import com.grayguava.bonk.data.levelFor
import com.grayguava.bonk.data.levelUnlockMessage
import com.grayguava.bonk.data.milestoneMessage
import com.grayguava.bonk.data.resetMessage
import com.grayguava.bonk.data.plural
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

/**
 * Home screen: how long the running streak has lasted. The big number is whole days
 * elapsed, the line under it is the full elapsed time.
 */
@Composable
fun HomeScreen(
    streakStart: Long?,
    buttonLabel: String,
    onStart: () -> Unit,
    onReset: (String) -> Unit,
) {
    val elapsed = rememberElapsedSince(streakStart)
    val isRunning = streakStart != null
    val days = elapsed?.toDays()?.toInt() ?: 0
    val level = levelFor(days)
    var showResetDialog by remember { mutableStateOf(false) }

    // The app's voice: one line at a time, under the button, then gone.
    var note by remember { mutableStateOf<String?>(null) }
    val avatarPop = remember { Animatable(1f) }
    val counterPop = remember { Animatable(1f) }
    var lastDays by remember { mutableIntStateOf(days) }
    var lastLevel by remember { mutableIntStateOf(level.number) }

    LaunchedEffect(days, level.number, isRunning) {
        if (!isRunning) {
            lastDays = days
            lastLevel = level.number
            return@LaunchedEffect
        }
        val milestone = milestoneMessage(days)
        val levelChanged = level.number != lastLevel

        if (levelChanged) {
            avatarPop.snapTo(1.16f)
            avatarPop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 500f))
        }
        if (milestone != null) {
            counterPop.snapTo(0.92f)
            counterPop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 600f))
        }

        note = when {
            milestone != null -> milestone
            levelChanged -> levelUnlockMessage(level)
            else -> easterEgg(days)
        }

        lastDays = days
        lastLevel = level.number
    }

    LaunchedEffect(note) {
        if (note != null) {
            delay(4_000)
            note = null
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TierAvatar(
                tier = level.tier,
                modifier = Modifier.graphicsLayer {
                    scaleX = avatarPop.value
                    scaleY = avatarPop.value
                },
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = level.tier.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 23.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Level ${level.number} · ${level.name}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = Color.White.copy(alpha = 0.55f),
            )
            Spacer(Modifier.height(36.dp))
            Text(
                text = days.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    scaleX = counterPop.value
                    scaleY = counterPop.value
                },
            )
            Text(
                text = if (isRunning) "days" else "no streak",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.55f),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = elapsed?.let { formatElapsed(it) } ?: "-- Hr. -- Min. -- Sec.",
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                color = Color.White.copy(alpha = 0.45f),
            )
            Spacer(Modifier.height(44.dp))
            GlassPanel(
                modifier = Modifier.width(220.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                onClick = { if (isRunning) showResetDialog = true else onStart() },
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = buttonLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            AnimatedVisibility(visible = note != null) {
                Text(
                    text = note.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }
        }
    }

    if (showResetDialog) {
        ResetReasonDialog(
            onConfirm = { reason ->
                showResetDialog = false
                val started = streakStart
                onReset(reason)
                if (started != null) {
                    val now = System.currentTimeMillis()
                    note = resetMessage(now - started, LocalTime.now().hour)
                }
            },
            onDismiss = { showResetDialog = false },
        )
    }
}

@Composable
private fun ResetReasonDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }
    val canReset = reason.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0A0A),
        shape = RoundedCornerShape(28.dp),
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.7f),
        title = {
            Text(
                text = "Reset streak",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        },
        text = {
            Column {
                Text(
                    text = "A reason is required to reset the streak.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(14.dp))
                GlassTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = "e.g. travel, illness, forgot",
                    isError = reason.isNotEmpty() && !canReset,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            GlassButton(
                label = "Reset",
                enabled = canReset,
                onClick = { onConfirm(reason.trim()) },
            )
        },
        dismissButton = {
            GlassButton(
                label = "Cancel",
                onClick = onDismiss,
            )
        },
    )
}

/** Time since [start], ticking every second. Null when no streak is running. */
@Composable
internal fun rememberElapsedSince(start: Long?): Duration? {
    var elapsed by remember(start) { mutableStateOf(start?.let { timeSince(it) }) }
    LaunchedEffect(start) {
        while (true) {
            elapsed = start?.let { timeSince(it) }
            delay(1_000)
        }
    }
    return elapsed
}

private fun timeSince(start: Long): Duration =
    Duration.between(Instant.ofEpochMilli(start), Instant.now())

private fun formatElapsed(duration: Duration): String {
    val total = duration.seconds.coerceAtLeast(0)
    return "${plural((total / 3600).toInt(), "hour")} " +
        "${plural(((total % 3600) / 60).toInt(), "min", "mins")} " +
        plural((total % 60).toInt(), "sec", "secs")
}
