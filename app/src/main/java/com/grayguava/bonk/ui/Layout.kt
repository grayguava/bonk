package com.grayguava.bonk.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The gap every screen leaves at the top. One value for all of them, so the app's
 * header line always sits at the same height on every page.
 */
@Composable
fun screenTopGap(): Dp = (LocalConfiguration.current.screenHeightDp * 0.01f).dp
