package com.cleancut.bgremover.feature.bgremoval.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleancut.bgremover.core.designsystem.CyberIndigo
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.core.designsystem.ElectricViolet
import com.cleancut.bgremover.core.designsystem.EmeraldGreen
import com.cleancut.bgremover.core.designsystem.PrimaryGradient

@Composable
fun ExportBar(
    savedMessage: String?,
    onSaveClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onPickAnotherClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Saved feedback notification pill
        AnimatedVisibility(
            visible = savedMessage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            savedMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(EmeraldGreen.copy(alpha = 0.15f))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen,
                        )
                    }
                }
            }
        }

        // Primary Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Save Button (Gradient Primary)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryGradient)
                    .clickable(onClick = onSaveClicked)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "Save",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            // Share Button (Frosted Secondary)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp),
                    )
                    .clickable(onClick = onShareClicked)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Pick Another Icon Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp),
                    )
                    .clickable(onClick = onPickAnotherClicked)
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddPhotoAlternate,
                    contentDescription = "Pick Another",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
