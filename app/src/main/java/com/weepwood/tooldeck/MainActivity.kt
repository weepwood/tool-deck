package com.weepwood.tooldeck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.weepwood.tooldeck.data.ToolPreferences
import com.weepwood.tooldeck.ui.ToolDeckApp
import com.weepwood.tooldeck.ui.ToolDeckState
import com.weepwood.tooldeck.ui.theme.ToolDeckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state = remember { ToolDeckState(ToolPreferences(applicationContext)) }
            ToolDeckTheme(themeMode = state.themeMode) {
                ToolDeckApp(state)
            }
        }
    }
}
