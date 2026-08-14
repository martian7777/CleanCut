package com.cleancut.bgremover.core.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Studio Dark Midnight Palette
val BackgroundDark = Color(0xFF090D16)
val BackgroundDarkElevated = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF131C31)
val SurfaceDarkGlass = Color(0xCC131C31)
val SurfaceDarkElevated = Color(0xFF1E293B)
val BorderDark = Color(0xFF26334D)
val BorderGlow = Color(0xFF4F46E5)

// Studio Light Palette
val BackgroundLight = Color(0xFFF8FAFC)
val BackgroundLightElevated = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceLightGlass = Color(0xE6FFFFFF)
val SurfaceLightElevated = Color(0xFFF1F5F9)
val BorderLight = Color(0xFFE2E8F0)

// Vibrant Cyberpunk & Studio Accents
val ElectricViolet = Color(0xFF8B5CF6)
val CyberIndigo = Color(0xFF6366F1)
val ElectricCyan = Color(0xFF06B6D4)
val NeonPink = Color(0xFFEC4899)
val EmeraldGreen = Color(0xFF10B981)
val SunsetOrange = Color(0xFFF97316)
val PureWhite = Color(0xFFFFFFFF)
val PureBlack = Color(0xFF000000)

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(CyberIndigo, ElectricViolet, ElectricCyan),
)

val GlowGradient = Brush.radialGradient(
    colors = listOf(ElectricViolet.copy(alpha = 0.35f), Color.Transparent),
)

val CardGlowBorderGradient = Brush.linearGradient(
    colors = listOf(ElectricViolet.copy(alpha = 0.6f), ElectricCyan.copy(alpha = 0.2f), BorderDark),
)

val ScannerLaserGradient = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        ElectricCyan.copy(alpha = 0.3f),
        ElectricCyan,
        ElectricCyan.copy(alpha = 0.3f),
        Color.Transparent,
    ),
)

// Legacy / M3 Compatibility Colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6366F1)
val PurpleGrey40 = Color(0xFF475569)
val Pink40 = Color(0xFF7D5260)

// Checkerboard Transparency Matrix
val CheckerLight = Color(0xFFE2E8F0)
val CheckerDark = Color(0xFFCBD5E1)
val CheckerDarkThemeLight = Color(0xFF1E293B)
val CheckerDarkThemeDark = Color(0xFF0F172A)
