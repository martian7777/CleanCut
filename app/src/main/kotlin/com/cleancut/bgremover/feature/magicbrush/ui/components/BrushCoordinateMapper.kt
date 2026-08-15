package com.cleancut.bgremover.feature.magicbrush.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

/**
 * Live pinch/pan state, matching [androidx.compose.ui.graphics.graphicsLayer]'s semantics
 * exactly (scale applied around the composable's center, then translation) - BrushCanvas
 * renders by applying this as a graphicsLayer on its content, so the math here must mirror
 * that composition or touch input and rendering will drift apart as the user zooms.
 */
data class BrushTransform(
    val scale: Float = 1f,
    val offsetPx: Offset = Offset.Zero,
)

/**
 * Pure, Compose-drawing-independent coordinate math shared by BrushCanvas's pointer-input
 * handling (screen -> source, to record strokes in photo-pixel space) and its stroke-preview
 * rendering (source -> screen, to draw them back). Keeping both directions in one file avoids
 * the classic bug where a brush paints in the wrong place after zooming because the two call
 * sites' scale math quietly drifted apart.
 */
object BrushCoordinateMapper {

    /** The ContentScale.Fit letterbox scale mapping bitmap-intrinsic pixels to canvas pixels. */
    fun fitScale(canvasSizePx: IntSize, bitmapSize: IntSize): Float {
        if (bitmapSize.width <= 0 || bitmapSize.height <= 0) return 1f
        return min(
            canvasSizePx.width / bitmapSize.width.toFloat(),
            canvasSizePx.height / bitmapSize.height.toFloat(),
        )
    }

    /**
     * Maps a source-pixel photo coordinate to screen space. Passing [BrushTransform] with its
     * default identity value yields "base screen" space - the position before the live
     * pinch/pan graphicsLayer is applied - which is what BrushCanvas's stroke-preview Canvas
     * should draw in, since that Canvas is itself a child of the transformed layer.
     */
    fun sourceToScreen(
        sourcePoint: Offset,
        canvasSizePx: IntSize,
        bitmapSize: IntSize,
        transform: BrushTransform = BrushTransform(),
    ): Offset {
        val scale = fitScale(canvasSizePx, bitmapSize)
        val fitOffsetX = (canvasSizePx.width - bitmapSize.width * scale) / 2f
        val fitOffsetY = (canvasSizePx.height - bitmapSize.height * scale) / 2f
        val baseX = fitOffsetX + sourcePoint.x * scale
        val baseY = fitOffsetY + sourcePoint.y * scale

        val centerX = canvasSizePx.width / 2f
        val centerY = canvasSizePx.height / 2f
        val finalX = centerX + transform.scale * (baseX - centerX) + transform.offsetPx.x
        val finalY = centerY + transform.scale * (baseY - centerY) + transform.offsetPx.y
        return Offset(finalX, finalY)
    }

    /**
     * Inverse of [sourceToScreen] with a live [transform] - maps a true screen-space pointer
     * position (as delivered by Compose pointer input, unaffected by any child graphicsLayer)
     * back to source-pixel photo coordinates.
     */
    fun screenToSource(
        screenPoint: Offset,
        canvasSizePx: IntSize,
        bitmapSize: IntSize,
        transform: BrushTransform,
    ): Offset {
        val scale = fitScale(canvasSizePx, bitmapSize)
        if (scale <= 0f || transform.scale == 0f) return Offset.Zero
        val fitOffsetX = (canvasSizePx.width - bitmapSize.width * scale) / 2f
        val fitOffsetY = (canvasSizePx.height - bitmapSize.height * scale) / 2f

        val centerX = canvasSizePx.width / 2f
        val centerY = canvasSizePx.height / 2f
        val baseX = (screenPoint.x - transform.offsetPx.x - centerX) / transform.scale + centerX
        val baseY = (screenPoint.y - transform.offsetPx.y - centerY) / transform.scale + centerY

        return Offset((baseX - fitOffsetX) / scale, (baseY - fitOffsetY) / scale)
    }
}
