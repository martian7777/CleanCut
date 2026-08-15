package com.cleancut.bgremover.feature.magicbrush.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.feature.magicbrush.domain.InpaintingStage

/**
 * A small inline overlay for in-flight erase steps, rather than reusing ProcessingIndicator
 * (which is a full-screen component hard-typed to BackgroundRemovalStage). Magic Brush stays
 * on the same canvas across many erase steps, so a full-screen swap would lose zoom/pan
 * continuity - see MagicBrushUiState.Editing's doc comment.
 */
@Composable
fun InpaintingProgressOverlay(stage: InpaintingStage, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = ElectricCyan,
                )
                Text(
                    text = stageLabel(stage),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun stageLabel(stage: InpaintingStage): String = when (stage) {
    InpaintingStage.LOADING_MODEL -> "Initializing engine…"
    InpaintingStage.PREPARING_MASK -> "Preparing selection…"
    InpaintingStage.INPAINTING -> "Erasing…"
    InpaintingStage.DONE -> "Done"
}
