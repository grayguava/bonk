package com.grayguava.bonk.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grayguava.bonk.data.StreakRecord
import com.grayguava.bonk.data.StreakStore
import com.grayguava.bonk.data.UserStore
import com.grayguava.bonk.data.streakDays
import kotlinx.coroutines.delay
import java.time.LocalTime

private val TABS = listOf(
    DockTab("Home", Icons.Outlined.Home, Icons.Filled.Home),
    DockTab("History", Icons.Outlined.DateRange, Icons.Filled.DateRange),
    DockTab("Levels", Icons.Outlined.Star, Icons.Filled.Star),
)

/**
 * The push between pages, as OpenTune does it: the new page comes in the whole
 * width while the old one edges away by a quarter, both on the same critically
 * damped spring, so neither of them bounces.
 */
private val PageSpring = spring(
    dampingRatio = 1f,
    stiffness = 520f,
    visibilityThreshold = IntOffset.VisibilityThreshold,
)

@Composable
fun BonkScreen() {
    val context = LocalContext.current
    val userStore = remember { UserStore(context) }
    var storedName by remember { mutableStateOf(userStore.name) }
    val name = storedName

    if (name == null) {
        WelcomeScreen(
            onContinue = { entered ->
                userStore.name = entered
                storedName = entered
            }
        )
    } else {
        // A restore can bring a name with it, or take the one here away, so the
        // greeting has to be told that the name changed.
        BonkHome(name = name, onNameChanged = { storedName = userStore.name })
    }
}

@Composable
private fun BonkHome(name: String, onNameChanged: () -> Unit) {
    val context = LocalContext.current
    val store = remember { StreakStore(context) }
    val userStore = remember { UserStore(context) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var streakStart by remember { mutableStateOf(store.currentStart) }
    var history by remember { mutableStateOf(store.history()) }
    var showAbout by remember { mutableStateOf(false) }
    val greeting = rememberGreeting()

    val buttonLabel = when {
        streakStart != null -> "Reset streak"
        history.isEmpty() -> "Start streak"
        else -> "Restart streak"
    }

    // The phone's own back button has to leave the About page too, not exit the app.
    BackHandler(enabled = showAbout) { showAbout = false }

    if (showAbout) {
        AboutScreen(
            onBack = { showAbout = false },
            streakStore = store,
            userStore = userStore,
            onRestore = {
                streakStart = store.currentStart
                history = store.history()
                onNameChanged()
            },
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = screenTopGap(), bottom = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        // The greeting belongs to the home screen alone.
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                            color = Color.White.copy(alpha = 0.55f),
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(
                            text = "$name.",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    } else {
                        Text(
                            text = TABS[selectedTab].label,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
                IconButton(onClick = { showAbout = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 26.dp),
                contentAlignment = Alignment.Center,
            ) {
                BottomDock(
                    tabs = TABS,
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .swipeToTab(tabCount = TABS.size, current = selectedTab) { selectedTab = it }
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    // The way you came decides which way the page comes in.
                    val shift = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(PageSpring) { it * shift } + fadeIn(tween(120)))
                        .togetherWith(
                            slideOutHorizontally(PageSpring) { -it / 4 * shift } +
                                fadeOut(tween(260))
                        )
                },
                label = "pages",
            ) { tab ->
                // The pages are opaque, so the one coming in covers the one leaving.
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    when (tab) {
                        0 -> HomeScreen(
                            streakStart = streakStart,
                            buttonLabel = buttonLabel,
                            onStart = {
                                val now = System.currentTimeMillis()
                                store.currentStart = now
                                streakStart = now
                            },
                            onReset = { reason ->
                                val start = streakStart ?: return@HomeScreen
                                val now = System.currentTimeMillis()
                                store.add(
                                    StreakRecord(
                                        startTime = start,
                                        resetTime = now,
                                        days = streakDays(start, now),
                                        reason = reason.trim(),
                                    )
                                )
                                store.currentStart = null
                                streakStart = null
                                history = store.history()
                            },
                        )
                        1 -> HistoryScreen(records = history)
                        else -> LevelsScreen(streakStart = streakStart)
                    }
                }
            }
        }
    }
}

/** The greeting for this hour, refreshed as the day moves on. */
@Composable
private fun rememberGreeting(): String {
    var greeting by remember { mutableStateOf(greetingFor(LocalTime.now().hour)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            greeting = greetingFor(LocalTime.now().hour)
        }
    }
    return greeting
}

private fun greetingFor(hour: Int): String = when (hour) {
    in 0..4 -> "Late night,"
    in 5..11 -> "Good morning,"
    in 12..16 -> "Good afternoon,"
    in 17..21 -> "Good evening,"
    else -> "Good night,"
}
