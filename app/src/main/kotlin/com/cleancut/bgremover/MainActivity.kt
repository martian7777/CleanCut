package com.cleancut.bgremover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cleancut.bgremover.ui.screens.CleanCutScreen
import com.cleancut.bgremover.ui.theme.CleanCutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanCutTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CleanCutScreen()
                }
            }
        }
    }
}
