package com.cleancut.bgremover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cleancut.bgremover.core.designsystem.CleanCutTheme
import com.cleancut.bgremover.core.settings.AppThemeMode
import com.cleancut.bgremover.core.settings.SettingsManager
import com.cleancut.bgremover.feature.bgremoval.ui.CleanCutScreen
import com.cleancut.bgremover.feature.settings.ui.SettingsScreen
import com.cleancut.bgremover.feature.splash.ui.SplashScreen

enum class AppDestination {
    SPLASH,
    STUDIO,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsManager = SettingsManager.getInstance(applicationContext)

        setContent {
            val settings by settingsManager.settings.collectAsStateWithLifecycle()
            val isDarkTheme = when (settings.themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            CleanCutTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CleanCutAppRoot()
                }
            }
        }
    }
}

@Composable
fun CleanCutAppRoot() {
    var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }

    when (currentDestination) {
        AppDestination.SETTINGS -> {
            BackHandler {
                currentDestination = AppDestination.STUDIO
            }
        }
        else -> Unit
    }

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            when {
                initialState == AppDestination.SPLASH && targetState == AppDestination.STUDIO -> {
                    fadeIn(animationSpec = tween(450)) togetherWith fadeOut(animationSpec = tween(300))
                }
                targetState == AppDestination.SETTINGS -> {
                    (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn()) togetherWith
                        (slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeOut())
                }
                initialState == AppDestination.SETTINGS -> {
                    (slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeIn()) togetherWith
                        (slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut())
                }
                else -> {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }
            }
        },
        label = "appNavigation",
        modifier = Modifier.fillMaxSize(),
    ) { destination ->
        when (destination) {
            AppDestination.SPLASH -> {
                SplashScreen(
                    onSplashFinished = {
                        currentDestination = AppDestination.STUDIO
                    },
                )
            }
            AppDestination.STUDIO -> {
                CleanCutScreen(
                    onNavigateToSettings = {
                        currentDestination = AppDestination.SETTINGS
                    },
                )
            }
            AppDestination.SETTINGS -> {
                SettingsScreen(
                    onNavigateBack = {
                        currentDestination = AppDestination.STUDIO
                    },
                )
            }
        }
    }
}
