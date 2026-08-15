package com.cleancut.bgremover.feature.magicbrush.data

import android.graphics.Rect

/**
 * Ports the bundled ONNX pipeline's own internal crop math (MIGAN_Pipeline.get_masked_bbox,
 * Picsart-AI-Research/MI-GAN scripts/create_onnx_pipeline.py) so the region we crop out of
 * the working bitmap - and the region we snapshot for undo/redo - matches what the model
 * itself treats as "touched": a square, context-padded crop centered on the painted mask,
 * at least [MODEL_RESOLUTION_PX] on a side, shifted (not shrunk) inward at photo edges.
 *
 * Feeding this crop (rather than the full photo) to the model keeps ONNX tensor allocations
 * small regardless of source photo resolution - see MiGanInpaintingEngine.
 */
object MaskedBoundsCalculator {

    /** The pipeline's internal GAN input resolution - confirmed via graph inspection (b512 synthesis blocks). */
    private const val MODEL_RESOLUTION_PX = 512

    /** The pipeline's default context-padding in source pixels, per MIGAN_Pipeline(padding=128). */
    private const val CONTEXT_PADDING_PX = 128

    /**
     * Extra margin beyond the ported formula, hedging against the bundled export having been
     * built with a padding value other than the assumed default of 128px - cheap (tens of KB)
     * insurance that undo/redo never misses a pixel the model actually touched.
     */
    private const val SAFETY_MARGIN_PX = 64

    fun computeCropRect(maskBoundingBox: Rect, sourceWidth: Int, sourceHeight: Int): Rect {
        require(!maskBoundingBox.isEmpty) { "maskBoundingBox must not be empty" }

        val centerX = maskBoundingBox.centerX()
        val centerY = maskBoundingBox.centerY()
        val maskedSize = maxOf(maskBoundingBox.width(), maskBoundingBox.height())
        val cropSize = maxOf(maskedSize + 2 * CONTEXT_PADDING_PX, MODEL_RESOLUTION_PX) + 2 * SAFETY_MARGIN_PX
        val offset = cropSize / 2

        var left = (centerX - offset).coerceAtLeast(0)
        var right = (centerX + offset).coerceAtMost(sourceWidth)
        var top = (centerY - offset).coerceAtLeast(0)
        var bottom = (centerY + offset).coerceAtMost(sourceHeight)

        // Shift (don't shrink) inward at photo edges so the crop keeps its full context size
        // whenever the source is large enough to hold it.
        val xShortfall = (cropSize - (right - left)).coerceAtLeast(0)
        val yShortfall = (cropSize - (bottom - top)).coerceAtLeast(0)
        left = (left - xShortfall).coerceAtLeast(0)
        right = (right + xShortfall).coerceAtMost(sourceWidth)
        top = (top - yShortfall).coerceAtLeast(0)
        bottom = (bottom + yShortfall).coerceAtMost(sourceHeight)

        return Rect(left, top, right, bottom)
    }
}
