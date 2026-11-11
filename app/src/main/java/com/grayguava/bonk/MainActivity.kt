package com.grayguava.bonk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.grayguava.bonk.ui.BonkScreen
import com.grayguava.bonk.ui.theme.BonkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            BonkTheme {
                BonkScreen()
            }
        }
    }
}
