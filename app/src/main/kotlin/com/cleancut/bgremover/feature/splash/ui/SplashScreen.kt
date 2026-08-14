package com.cleancut.bgremover.feature.splash.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleancut.bgremover.core.designsystem.CyberIndigo
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.core.designsystem.ElectricViolet
import com.cleancut.bgremover.core.designsystem.PrimaryGradient
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }
    val badgeAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseGlow",
    )

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(500, easing = LinearEasing))
        scale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 300f,
            ),
        )
        badgeAlpha.animateTo(1f, animationSpec = tween(400))
        delay(1400)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Glowing Ambient Orb Behind Logo
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(pulseScale.value)
                .alpha(0.25f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricViolet, ElectricCyan, Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp),
        ) {
            // Animated Icon Box
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(PrimaryGradient)
                    .border(
                        2.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.8f), ElectricCyan),
                        ),
                        RoundedCornerShape(28.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCut,
                    contentDescription = "CleanCut Logo",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CleanCut Title
            Text(
                text = "CleanCut",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(alpha.value),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Studio AI Background Isolation",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(alpha.value),
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Animated "Powered by LEMINNO" Pill
            Box(
                modifier = Modifier
                    .alpha(badgeAlpha.value)
                    .clip(RoundedCornerShape(100.dp))
                    .background(CyberIndigo.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(ElectricViolet, ElectricCyan),
                        ),
                        RoundedCornerShape(100.dp),
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "POWERED BY LEMINNO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 2.sp,
                    )
                }
            }
        }
    }
}
