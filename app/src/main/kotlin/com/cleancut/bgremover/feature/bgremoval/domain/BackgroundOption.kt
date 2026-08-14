package com.cleancut.bgremover.feature.bgremoval.domain

import androidx.compose.ui.graphics.Color
import com.cleancut.bgremover.core.designsystem.CyberIndigo
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.core.designsystem.ElectricViolet
import com.cleancut.bgremover.core.designsystem.EmeraldGreen
import com.cleancut.bgremover.core.designsystem.NeonPink
import com.cleancut.bgremover.core.designsystem.PureBlack
import com.cleancut.bgremover.core.designsystem.PureWhite
import com.cleancut.bgremover.core.designsystem.SunsetOrange

sealed interface BackgroundOption {
    val id: String
    val name: String

    data object Transparent : BackgroundOption {
        override val id = "transparent"
        override val name = "Transparent"
    }

    data class SolidColor(
        override val id: String,
        override val name: String,
        val color: Color,
        val argbInt: Int,
    ) : BackgroundOption

    companion object {
        val PRESETS: List<BackgroundOption> = listOf(
            Transparent,
            SolidColor("white", "Studio White", PureWhite, 0xFFFFFFFF.toInt()),
            SolidColor("black", "Deep Onyx", PureBlack, 0xFF0B0F19.toInt()),
            SolidColor("cyber_indigo", "Indigo", CyberIndigo, 0xFF6366F1.toInt()),
            SolidColor("electric_violet", "Violet", ElectricViolet, 0xFF8B5CF6.toInt()),
            SolidColor("electric_cyan", "Cyan", ElectricCyan, 0xFF06B6D4.toInt()),
            SolidColor("sunset_orange", "Sunset", SunsetOrange, 0xFFF97316.toInt()),
            SolidColor("emerald", "Emerald", EmeraldGreen, 0xFF10B981.toInt()),
            SolidColor("neon_pink", "Neon Pink", NeonPink, 0xFFEC4899.toInt()),
        )
    }
}
