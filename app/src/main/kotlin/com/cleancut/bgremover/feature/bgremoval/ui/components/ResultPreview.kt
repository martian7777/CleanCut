package com.cleancut.bgremover.feature.bgremoval.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.Visibility
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleancut.bgremover.core.designsystem.CheckerDarkThemeDark
import com.cleancut.bgremover.core.designsystem.CheckerDarkThemeLight
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.core.designsystem.ElectricViolet
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundOption

@Composable
fun ResultPreview(
    cutoutBitmap: Bitmap,
    originalBitmap: Bitmap? = null,
    backgroundOption: BackgroundOption = BackgroundOption.Transparent,
    modifier: Modifier = Modifier,
) {
    var showOriginal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(ElectricViolet.copy(alpha = 0.5f), ElectricCyan.copy(alpha = 0.3f)),
                ),
                RoundedCornerShape(24.dp),
            ),
    ) {
        // Dynamic Backdrop
        when (backgroundOption) {
            is BackgroundOption.Transparent -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cell = 18.dp.toPx()
                    var y = 0f
                    var row = 0
                    while (y < size.height) {
                        var x = 0f
                        var col = row
                        while (x < size.width) {
                            val color = if (col % 2 == 0) CheckerDarkThemeLight else CheckerDarkThemeDark
                            drawRect(color = color, topLeft = Offset(x, y), size = Size(cell, cell))
                            x += cell
                            col++
                        }
                        y += cell
                        row++
                    }
                }
            }
            is BackgroundOption.SolidColor -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundOption.color),
                )
            }
        }

        // Image Display with Before/After Crossfade
        Crossfade(
            targetState = showOriginal && originalBitmap != null,
            animationSpec = tween(250),
            label = "beforeAfter",
        ) { isOriginal ->
            val displayBitmap = if (isOriginal && originalBitmap != null) originalBitmap else cutoutBitmap
            Image(
                bitmap = displayBitmap.asImageBitmap(),
                contentDescription = if (isOriginal) "Original Image" else "CleanCut Result",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Before / After Compare Button
        if (originalBitmap != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                showOriginal = true
                                tryAwaitRelease()
                                showOriginal = false
                            },
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = if (showOriginal) Icons.Rounded.Visibility else Icons.Rounded.Compare,
                        contentDescription = "Hold to compare",
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = if (showOriginal) "Original" else "Hold to Compare",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}
