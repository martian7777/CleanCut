package com.cleancut.bgremover.feature.studio.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cleancut.bgremover.core.designsystem.PrimaryGradient
import com.cleancut.bgremover.feature.bgremoval.ui.CleanCutScreen
import com.cleancut.bgremover.feature.magicbrush.ui.MagicBrushScreen

enum class StudioMode { BACKGROUND_REMOVER, MAGIC_ERASER }

/**
 * Thin shell over CleanCutScreen (Remove BG) and MagicBrushScreen (Magic Eraser), sharing a
 * single picked photo between them so switching tools doesn't force a re-pick. Switching
 * modes re-runs the newly-selected tool fresh from that shared Uri - it does not chain one
 * tool's output into the other; each keeps its own independent working state and undo
 * history. Only the initial picked photo is shared, not intermediate edits.
 */
@Composable
fun StudioScreen(onNavigateToSettings: () -> Unit = {}) {
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var mode by remember { mutableStateOf(StudioMode.BACKGROUND_REMOVER) }

    val toggle: @Composable () -> Unit = {
        if (pickedUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                ModeToggle(mode = mode, onModeSelected = { mode = it })
            }
        }
    }

    when (mode) {
        StudioMode.BACKGROUND_REMOVER -> CleanCutScreen(
            initialUri = pickedUri,
            onNavigateToSettings = onNavigateToSettings,
            onImagePicked = { pickedUri = it },
            modeToggle = toggle,
        )
        StudioMode.MAGIC_ERASER -> MagicBrushScreen(
            initialUri = pickedUri,
            onNavigateToSettings = onNavigateToSettings,
            onImagePicked = { pickedUri = it },
            modeToggle = toggle,
        )
    }
}

@Composable
private fun ModeToggle(mode: StudioMode, onModeSelected: (StudioMode) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(100.dp))
            .padding(4.dp),
    ) {
        ModeToggleOption(
            label = "Remove BG",
            icon = Icons.Rounded.ContentCut,
            selected = mode == StudioMode.BACKGROUND_REMOVER,
            onClick = { onModeSelected(StudioMode.BACKGROUND_REMOVER) },
        )
        ModeToggleOption(
            label = "Magic Eraser",
            icon = Icons.Rounded.AutoFixHigh,
            selected = mode == StudioMode.MAGIC_ERASER,
            onClick = { onModeSelected(StudioMode.MAGIC_ERASER) },
        )
    }
}

@Composable
private fun ModeToggleOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .then(if (selected) Modifier.background(PrimaryGradient) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
