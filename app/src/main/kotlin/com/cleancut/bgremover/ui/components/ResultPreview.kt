package com.cleancut.bgremover.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.cleancut.bgremover.ui.theme.CheckerDark
import com.cleancut.bgremover.ui.theme.CheckerLight

/** Shows the cutout over a checkerboard so transparency is visibly obvious. */
@Composable
fun ResultPreview(bitmap: Bitmap, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cell = 24.dp.toPx()
            var y = 0f
            var row = 0
            while (y < size.height) {
                var x = 0f
                var col = row
                while (x < size.width) {
                    val color = if (col % 2 == 0) CheckerLight else CheckerDark
                    drawRect(color = color, topLeft = Offset(x, y), size = Size(cell, cell))
                    x += cell
                    col++
                }
                y += cell
                row++
            }
        }
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Background-removed result",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
