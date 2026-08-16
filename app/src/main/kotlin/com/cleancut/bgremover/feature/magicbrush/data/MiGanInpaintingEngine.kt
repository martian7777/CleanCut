package com.cleancut.bgremover.feature.magicbrush.data

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.cleancut.bgremover.feature.magicbrush.domain.ErasePatch
import com.cleancut.bgremover.feature.magicbrush.domain.InpaintingEngine
import com.cleancut.bgremover.feature.magicbrush.domain.InpaintingError
import com.cleancut.bgremover.feature.magicbrush.domain.InpaintingResult
import com.cleancut.bgremover.feature.magicbrush.domain.InpaintingStage
import com.cleancut.bgremover.feature.magicbrush.domain.MaskRegion
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Verified against andraniksargsyan/migan's migan_pipeline_v2.onnx (downloaded 2026-08-15,
// 28,079,181 bytes) via onnxruntime session inspection: inputs "image" [batch,3,height,width]
// uint8 and "mask" [batch,1,height,width] uint8, dynamic height/width (not fixed 512x512);
// output "result" [batch,3,height,width] uint8, same H/W as the input. Mask polarity
// confirmed against the model repo's own read_mask()/create_onnx_pipeline.py source
// (Picsart-AI-Research/MI-GAN): 255 = known (keep), 0 = hole (erase).
//
// The graph's internal MIGAN_Pipeline.forward already computes its own context-padded crop
// from the mask (padding=128px, min 512px - ported in MaskedBoundsCalculator), resizes to
// its 512x512 synthesis network, feather-blends the result (max-pool mask dilation +
// Gaussian blur) and scatters it back into the input tensor - pixels outside that internal
// crop pass through byte-identical. Pre-cropping to MaskedBoundsCalculator's rect before
// calling the model (rather than feeding the whole photo) keeps ONNX tensor allocations
// small regardless of source resolution, and produces the same effective result since our
// crop is already centered/sized to match what the model would derive on its own.
private const val MODEL_ASSET_PATH = "migan_pipeline_v2.onnx"
private const val INPUT_IMAGE_NAME = "image"
private const val INPUT_MASK_NAME = "mask"
private const val KEEP_BYTE: Byte = 0xFF.toByte()

class MiGanInpaintingEngine(private val context: Context) : InpaintingEngine {

    private val ortEnvironmentLazy = lazy { OrtEnvironment.getEnvironment() }
    private val ortEnvironment: OrtEnvironment by ortEnvironmentLazy

    private val sessionLazy = lazy {
        val modelBytes = context.assets.open(MODEL_ASSET_PATH).use { it.readBytes() }
        val sessionOptions = OrtSession.SessionOptions().apply {
            // Mirrors RmbgBackgroundRemover's CPU-only configuration: ORT's default arena
            // allocator can abort the process natively (no catchable exception) on
            // memory-constrained real devices.
            setMemoryPatternOptimization(false)
            setCPUArenaAllocator(false)
            setIntraOpNumThreads(2)
        }
        ortEnvironment.createSession(modelBytes, sessionOptions)
    }
    private val session: OrtSession by sessionLazy

    override fun close() {
        if (sessionLazy.isInitialized()) session.close()
        if (ortEnvironmentLazy.isInitialized()) ortEnvironment.close()
    }

    override suspend fun inpaint(
        source: Bitmap,
        mask: MaskRegion,
        onProgress: (InpaintingStage) -> Unit,
    ): Result<InpaintingResult> {
        if (mask.boundingBox.isEmpty) {
            return Result.failure(InpaintingError.EmptyMask)
        }

        return try {
            onProgress(InpaintingStage.LOADING_MODEL)
            val activeSession = try {
                session
            } catch (t: Throwable) {
                return Result.failure(InpaintingError.ModelLoadFailed(t))
            }
            check(INPUT_IMAGE_NAME in activeSession.inputNames && INPUT_MASK_NAME in activeSession.inputNames) {
                "Unexpected model input names: ${activeSession.inputNames} " +
                    "(expected \"$INPUT_IMAGE_NAME\" and \"$INPUT_MASK_NAME\")"
            }

            onProgress(InpaintingStage.PREPARING_MASK)
            val cropRect = MaskedBoundsCalculator.computeCropRect(mask.boundingBox, source.width, source.height)
            val width = cropRect.width()
            val height = cropRect.height()

            val beforePixels = IntArray(width * height)
            source.getPixels(beforePixels, 0, width, cropRect.left, cropRect.top, width, height)

            val imageBuffer = directBuffer(buildImageBytes(beforePixels, width, height))
            val maskBuffer = directBuffer(buildModelMaskBytes(mask, cropRect, width, height))

            val afterPixels = OnnxTensor.createTensor(
                ortEnvironment,
                imageBuffer,
                longArrayOf(1, 3, height.toLong(), width.toLong()),
                OnnxJavaType.UINT8,
            ).use { imageTensor ->
                OnnxTensor.createTensor(
                    ortEnvironment,
                    maskBuffer,
                    longArrayOf(1, 1, height.toLong(), width.toLong()),
                    OnnxJavaType.UINT8,
                ).use { maskTensor ->
                    onProgress(InpaintingStage.INPAINTING)
                    activeSession.run(
                        mapOf(INPUT_IMAGE_NAME to imageTensor, INPUT_MASK_NAME to maskTensor),
                    ).use { result ->
                        val outputTensor = result.get(0) as OnnxTensor
                        decodeOutputPixels(outputTensor.byteBuffer, width, height)
                    }
                }
            }

            source.setPixels(afterPixels, 0, width, cropRect.left, cropRect.top, width, height)

            onProgress(InpaintingStage.DONE)
            Result.success(InpaintingResult(source, ErasePatch(cropRect, beforePixels, afterPixels)))
        } catch (oom: OutOfMemoryError) {
            Result.failure(InpaintingError.OutOfMemory)
        } catch (t: Throwable) {
            Result.failure(InpaintingError.InferenceFailed(t))
        }
    }

    private fun directBuffer(bytes: ByteArray): ByteBuffer =
        ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).apply {
            put(bytes)
            rewind()
        }

    /** NCHW planar uint8: the R plane in full, then G, then B - matches the model's export layout. */
    private fun buildImageBytes(pixels: IntArray, width: Int, height: Int): ByteArray {
        val size = width * height
        val bytes = ByteArray(3 * size)
        for (i in 0 until size) {
            val argb = pixels[i]
            bytes[i] = ((argb shr 16) and 0xFF).toByte()
            bytes[size + i] = ((argb shr 8) and 0xFF).toByte()
            bytes[2 * size + i] = (argb and 0xFF).toByte()
        }
        return bytes
    }

    /**
     * Builds the model's uint8 mask tensor bytes at crop size, inverting MaskRegion's
     * domain polarity (255 = erase, 0 = keep) to the model's (255 = keep, 0 = erase) - the
     * one place this flip happens. Getting it backwards here erases the *background*
     * instead of the painted object.
     *
     * The brush is anti-aliased, so [MaskRegion.maskBitmap] carries graduated alpha at
     * stroke edges and at round-cap overlap seams - but the model was trained on strictly
     * binary hole masks (see the model repo's read_mask()) and has no notion of "half
     * erased". Left graduated, those pixels get told "mostly keep this", so the model
     * preserves a faint trace of the painted-over content right where the mask is softest.
     * Thresholding here keeps that binary contract intact.
     */
    private fun buildModelMaskBytes(mask: MaskRegion, cropRect: Rect, width: Int, height: Int): ByteArray {
        val bytes = ByteArray(width * height) { KEEP_BYTE }

        val maskBitmap = mask.maskBitmap
        val maskRowBytes = maskBitmap.rowBytes
        val maskAlpha = ByteArray(maskRowBytes * maskBitmap.height)
        maskBitmap.copyPixelsToBuffer(ByteBuffer.wrap(maskAlpha))

        val offsetX = mask.boundingBox.left - cropRect.left
        val offsetY = mask.boundingBox.top - cropRect.top
        for (y in 0 until maskBitmap.height) {
            val destY = offsetY + y
            if (destY < 0 || destY >= height) continue
            val srcRowStart = y * maskRowBytes
            val destRowStart = destY * width
            for (x in 0 until maskBitmap.width) {
                val destX = offsetX + x
                if (destX < 0 || destX >= width) continue
                val domainAlpha = maskAlpha[srcRowStart + x].toInt() and 0xFF
                bytes[destRowStart + destX] = if (domainAlpha >= 128) 0 else KEEP_BYTE
            }
        }
        return bytes
    }

    private fun decodeOutputPixels(outputBytes: ByteBuffer, width: Int, height: Int): IntArray {
        val size = width * height
        val pixels = IntArray(size)
        for (i in 0 until size) {
            val r = outputBytes.get(i).toInt() and 0xFF
            val g = outputBytes.get(size + i).toInt() and 0xFF
            val b = outputBytes.get(2 * size + i).toInt() and 0xFF
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        return pixels
    }
}
