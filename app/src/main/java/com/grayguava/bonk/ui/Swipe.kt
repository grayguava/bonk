package com.grayguava.bonk.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Swipe left or right to change tab.
 *
 * A child that claims the gesture first keeps it: a scrolling list takes vertical
 * drags, so only an unclaimed horizontal drag moves the page. The swipe has to travel
 * [threshold] to count, so a stray finger does not flick the app.
 */
@Composable
fun Modifier.swipeToTab(
    tabCount: Int,
    current: Int,
    threshold: androidx.compose.ui.unit.Dp = 64.dp,
    onTab: (Int) -> Unit,
): Modifier {
    val travelNeeded = with(LocalDensity.current) { threshold.toPx() }

    return this.pointerInput(tabCount, current, travelNeeded) {
        var travel = 0f
        detectHorizontalDragGestures(
            onDragStart = { travel = 0f },
            onHorizontalDrag = { change, dragAmount ->
                travel += dragAmount
                change.consume()
            },
            onDragEnd = {
                if (abs(travel) > travelNeeded) {
                    val next = if (travel < 0) current + 1 else current - 1
                    if (next in 0 until tabCount) onTab(next)
                }
                travel = 0f
            },
            onDragCancel = { travel = 0f },
        )
    }
}
