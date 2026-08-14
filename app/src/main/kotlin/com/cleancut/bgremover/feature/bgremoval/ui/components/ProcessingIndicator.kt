package com.cleancut.bgremover.feature.bgremoval.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleancut.bgremover.core.designsystem.CyberIndigo
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.core.designsystem.ElectricViolet
import com.cleancut.bgremover.core.designsystem.PrimaryGradient
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemovalStage

@Composable
fun ProcessingIndicator(
    stage: BackgroundRemovalStage,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")

    // Laser scanline animation: traverses 0f to 1f vertically
    val scanProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "laserScan",
    )

    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // High-Tech Holographic Scanner Box
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(ElectricViolet.copy(alpha = 0.8f), ElectricCyan.copy(alpha = 0.4f)),
                    ),
                    RoundedCornerShape(28.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Scanner Grid Lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 20.dp.toPx()
                for (x in 0..(size.width / step).toInt()) {
                    drawLine(
                        color = ElectricCyan.copy(alpha = 0.06f),
                        start = Offset(x * step, 0f),
                        end = Offset(x * step, size.height),
                        strokeWidth = 1f,
                    )
                }
                for (y in 0..(size.height / step).toInt()) {
                    drawLine(
                        color = ElectricCyan.copy(alpha = 0.06f),
                        start = Offset(0f, y * step),
                        end = Offset(size.width, y * step),
                        strokeWidth = 1f,
                    )
                }

                // Laser Scan Line
                val currentY = size.height * scanProgress.value
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ElectricCyan.copy(alpha = 0.8f),
                            ElectricCyan,
                            ElectricCyan.copy(alpha = 0.8f),
                            Color.Transparent,
                        ),
                    ),
                    start = Offset(0f, currentY),
                    end = Offset(size.width, currentY),
                    strokeWidth = 4.dp.toPx(),
                )
            }

            // Central Pulsing Neural Core
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(CyberIndigo.copy(alpha = 0.5f), ElectricCyan.copy(alpha = 0.1f), Color.Transparent),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = stageIcon(stage),
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(34.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Animated Stage Label
        AnimatedContent(
            targetState = stage,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "stageText",
        ) { currentStage ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stageTitle(currentStage),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stageDetail(currentStage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Smooth Progress Tracker Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(100.dp),
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = ElectricCyan,
                )
                Text(
                    text = "Neural Segmentation in progress…",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun stageIcon(stage: BackgroundRemovalStage): ImageVector = when (stage) {
    BackgroundRemovalStage.DECODING -> Icons.Rounded.Image
    BackgroundRemovalStage.CHECKING_MODEL -> Icons.Rounded.Psychology
    BackgroundRemovalStage.DOWNLOADING_MODEL -> Icons.Rounded.CloudDownload
    BackgroundRemovalStage.SEGMENTING -> Icons.Rounded.AutoAwesome
    BackgroundRemovalStage.COMPOSITING -> Icons.Rounded.BlurOn
    BackgroundRemovalStage.DONE -> Icons.Rounded.AutoAwesome
}

private fun stageTitle(stage: BackgroundRemovalStage): String = when (stage) {
    BackgroundRemovalStage.DECODING -> "Decoding Image"
    BackgroundRemovalStage.CHECKING_MODEL -> "Initializing Engine"
    BackgroundRemovalStage.DOWNLOADING_MODEL -> "Downloading AI Weights"
    BackgroundRemovalStage.SEGMENTING -> "Isolating Subject"
    BackgroundRemovalStage.COMPOSITING -> "Rendering Clean Matte"
    BackgroundRemovalStage.DONE -> "Subject Isolated!"
}

private fun stageDetail(stage: BackgroundRemovalStage): String = when (stage) {
    BackgroundRemovalStage.DECODING -> "Reading full-resolution EXIF & pixel matrix"
    BackgroundRemovalStage.CHECKING_MODEL -> "Validating Google Play Services Vision model"
    BackgroundRemovalStage.DOWNLOADING_MODEL -> "First time setup — downloading ML model"
    BackgroundRemovalStage.SEGMENTING -> "Running deep neural segmentation on subject"
    BackgroundRemovalStage.COMPOSITING -> "Upsampling confidence mask & creating alpha cutout"
    BackgroundRemovalStage.DONE -> "Preparing studio workspace"
}
