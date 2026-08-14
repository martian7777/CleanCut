package com.cleancut.bgremover.core.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyberIndigo,
    onPrimary = PureWhite,
    primaryContainer = SurfaceDarkElevated,
    onPrimaryContainer = PureWhite,
    secondary = ElectricViolet,
    onSecondary = PureWhite,
    secondaryContainer = SurfaceDarkElevated,
    onSecondaryContainer = PureWhite,
    tertiary = ElectricCyan,
    onTertiary = PureBlack,
    background = BackgroundDark,
    onBackground = PureWhite,
    surface = SurfaceDark,
    onSurface = PureWhite,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = BorderDark,
    outlineVariant = BorderDark.copy(alpha = 0.5f),
)

private val LightColorScheme = lightColorScheme(
    primary = CyberIndigo,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = CyberIndigo,
    secondary = ElectricViolet,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = ElectricViolet,
    tertiary = ElectricCyan,
    onTertiary = PureBlack,
    background = BackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = SurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = SurfaceLightElevated,
    onSurfaceVariant = Color(0xFF64748B),
    outline = BorderLight,
    outlineVariant = BorderLight.copy(alpha = 0.7f),
)

@Composable
fun CleanCutTheme(
    darkTheme: Boolean = true, // Default to sleek studio dark
    dynamicColor: Boolean = false, // Keep brand aesthetics consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
