package com.cleancut.bgremover.compositing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import kotlin.math.min

/**
 * Turns a low-resolution segmenter confidence mask into a full-resolution cutout,
 * staying well under the memory a naive full-size ARGB_8888-everywhere pipeline
 * would need: peak usage is roughly one full-size ARGB_8888 source (~183MB at
 * 48MP) plus one full-size ALPHA_8 mask (~46MB), never a second full-size
 * ARGB_8888 buffer for "the output" - the source bitmap becomes the output.
 */
object BitmapCompositor {

    private const val DEFAULT_BAND_HEIGHT = 256

    /**
     * Converts the segmenter's native-resolution confidence mask into a full-size
     * ALPHA_8 bitmap via bilinear upsampling. ALPHA_8 is 1 byte/px, a quarter the
     * memory of an ARGB_8888 mask at the same dimensions. The bilinear-upsampled
     * edge is the accepted v1 quality tradeoff (soft edges, not guided-filter sharp).
     */
    fun buildFullResolutionMask(
        confidenceMask: FloatBuffer,
        maskWidth: Int,
        maskHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap {
        confidenceMask.rewind()
        val smallMask = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ALPHA_8)
        val rowBytes = smallMask.rowBytes
        val maskBytes = ByteArray(rowBytes * maskHeight)
        for (y in 0 until maskHeight) {
            val rowStart = y * rowBytes
            for (x in 0 until maskWidth) {
                val alpha = (confidenceMask.get(y * maskWidth + x) * 255f).toInt().coerceIn(0, 255)
                maskBytes[rowStart + x] = alpha.toByte()
            }
        }
        smallMask.copyPixelsFromBuffer(ByteBuffer.wrap(maskBytes))

        val fullMask = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(fullMask)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(smallMask, null, Rect(0, 0, targetWidth, targetHeight), paint)
        smallMask.recycle()
        return fullMask
    }

    /**
     * Streams [source] in horizontal bands, replacing only the alpha channel with
     * values read from [mask]. RGB bits pass through [getPixels]/[setPixels]
     * completely untouched, so kept foreground pixels stay byte-identical to the
     * source. Mutates and returns [source] in place; no second full-size ARGB_8888
     * buffer is ever allocated. Recycles [mask] once compositing is done.
     *
     * [bandHeight] is the tuning knob if a device OOMs on a very large image -
     * lower it to shrink per-band scratch allocations.
     */
    fun compositeInPlace(
        source: Bitmap,
        mask: Bitmap,
        bandHeight: Int = DEFAULT_BAND_HEIGHT,
    ): Bitmap {
        require(source.width == mask.width && source.height == mask.height) {
            "Mask dimensions (${mask.width}x${mask.height}) must match source " +
                "dimensions (${source.width}x${source.height}) exactly"
        }

        val width = source.width
        val height = source.height
        val pixelBuf = IntArray(width * bandHeight)

        var y = 0
        while (y < height) {
            val rows = min(bandHeight, height - y)

            source.getPixels(pixelBuf, 0, width, 0, y, width, rows)

            val maskBand = Bitmap.createBitmap(mask, 0, y, width, rows)
            val bandRowBytes = maskBand.rowBytes
            val bandBytes = ByteArray(bandRowBytes * rows)
            maskBand.copyPixelsToBuffer(ByteBuffer.wrap(bandBytes))
            maskBand.recycle()

            for (row in 0 until rows) {
                val maskRowOffset = row * bandRowBytes
                val pixelRowOffset = row * width
                for (x in 0 until width) {
                    val alpha = bandBytes[maskRowOffset + x].toInt() and 0xFF
                    val argb = pixelBuf[pixelRowOffset + x]
                    pixelBuf[pixelRowOffset + x] = (alpha shl 24) or (argb and 0x00FFFFFF)
                }
            }

            source.setPixels(pixelBuf, 0, width, 0, y, width, rows)
            y += rows
        }

        mask.recycle()
        return source
    }
}
