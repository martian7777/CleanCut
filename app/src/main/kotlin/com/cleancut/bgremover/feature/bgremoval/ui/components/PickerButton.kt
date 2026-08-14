package com.cleancut.bgremover.feature.bgremoval.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PickerButton(
    onPickClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "idleFloat")
    val floatOffset = infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float",
    )
    val glowAlpha = infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Glowing Ambient Backdrop
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .alpha(glowAlpha.value)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(ElectricViolet, ElectricCyan, Color.Transparent),
                        ),
                        shape = CircleShape,
                    ),
            )

            // Main Interactive Hero Upload Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                ElectricViolet.copy(alpha = 0.7f),
                                ElectricCyan.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            ),
                        ),
                        RoundedCornerShape(32.dp),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPickClicked,
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Floating Animated Icon
                    Box(
                        modifier = Modifier
                            .offset(y = floatOffset.value.dp)
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CyberIndigo.copy(alpha = 0.3f), ElectricCyan.copy(alpha = 0.2f)),
                                ),
                            )
                            .border(
                                2.dp,
                                Brush.horizontalGradient(
                                    listOf(ElectricViolet, ElectricCyan),
                                ),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Pick photo",
                            tint = ElectricCyan,
                            modifier = Modifier.size(42.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Isolate Subjects Instantly",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap to pick any photo from your gallery. On-device neural segmentation extracts foregrounds cleanly in milliseconds.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Gradient CTA Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(PrimaryGradient)
                            .clickable(onClick = onPickClicked)
                            .padding(horizontal = 32.dp, vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Select Photo",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Feature Highlights
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FeatureChip(icon = Icons.Rounded.Speed, text = "Sub-second AI")
            Spacer(modifier = Modifier.size(8.dp))
            FeatureChip(icon = Icons.Rounded.Lock, text = "100% Offline")
            Spacer(modifier = Modifier.size(8.dp))
            FeatureChip(icon = Icons.Rounded.CheckCircle, text = "Lossless Resolution")
        }
    }
}

@Composable
private fun FeatureChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(100.dp),
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricViolet,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
