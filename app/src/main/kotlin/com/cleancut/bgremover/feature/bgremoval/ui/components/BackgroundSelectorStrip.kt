package com.cleancut.bgremover.feature.bgremoval.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleancut.bgremover.core.designsystem.CyberIndigo
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundOption

@Composable
fun BackgroundSelectorStrip(
    selectedOption: BackgroundOption,
    onOptionSelected: (BackgroundOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Backdrop",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )
            Text(
                text = selectedOption.name,
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                fontWeight = FontWeight.SemiBold,
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(BackgroundOption.PRESETS, key = { it.id }) { option ->
                val isSelected = option.id == selectedOption.id
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    label = "border",
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) CyberIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clickable { onOptionSelected(option) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        when (option) {
                            is BackgroundOption.Transparent -> {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.DarkGray)
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GridOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                            is BackgroundOption.SolidColor -> {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(option.color)
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = if (option.color == Color.White) Color.Black else Color.White,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
