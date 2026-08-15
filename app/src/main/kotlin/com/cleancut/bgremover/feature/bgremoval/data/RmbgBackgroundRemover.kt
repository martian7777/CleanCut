package com.cleancut.bgremover.feature.bgremoval.data

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.cleancut.bgremover.core.common.ImageDecodeUtils
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemovalError
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemovalStage
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemover
import com.cleancut.bgremover.feature.bgremoval.domain.CompositeResult
import java.io.File
import java.nio.FloatBuffer

// Verified against briaai/RMBG-1.4's onnx/model_quantized.onnx (downloaded 2026-08-15,
// 44,403,226 bytes) via Netron/onnxruntime inspection: input "input" is
// [1,3,1024,1024] NCHW float32, output "output" is [1,1,1024,1024] float32 with
// Sigmoid already the graph's final op (no manual sigmoid needed). Preprocessing
// verified against the model repo's utilities.py: plain bilinear stretch-resize to
// 1024x1024 (not letterboxed), pixel/255, normalized per channel with mean=0.5,
// std=1.0 (not ImageNet stats).
private const val MODEL_ASSET_PATH = "rmbg-1.4.onnx"
private const val INPUT_TENSOR_NAME = "input"
private const val MODEL_INPUT_SIZE = 1024
private const val RMBG_MEAN = 0.5f
private const val RMBG_STD = 1.0f
private const val SEGMENTATION_INPUT_LONG_EDGE_PX = 1024

class RmbgBackgroundRemover(private val context: Context) : BackgroundRemover {

    private val ortEnvironment: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }

    private val session: OrtSession by lazy {
        val modelBytes = context.assets.open(MODEL_ASSET_PATH).use { it.readBytes() }
        val sessionOptions = OrtSession.SessionOptions().apply {
            // ORT's default arena allocator pre-reserves large contiguous native
            // memory blocks for speed. On memory-constrained real devices (as
            // opposed to a desktop-backed emulator) that reservation itself can
            // abort the process natively - no catchable Kotlin exception, the app
            // just disappears. Disabling the arena/memory-pattern optimizations and
            // capping thread count trades a little inference speed for allocating
            // only what's actually needed.
            setMemoryPatternOptimization(false)
            setCPUArenaAllocator(false)
            setIntraOpNumThreads(2)
        }
        ortEnvironment.createSession(modelBytes, sessionOptions)
    }

    override fun close() {
        if (this::session.isInitialized) session.close()
        if (this::ortEnvironment.isInitialized) ortEnvironment.close()
    }

    override suspend fun removeBackground(
        sourceUri: Uri,
        onProgress: (BackgroundRemovalStage) -> Unit,
    ): Result<CompositeResult> {
        val resolver = context.contentResolver

        var fullResSource: Bitmap? = null
        var originalPreview: Bitmap? = null
        var cachedSourceFile: File? = null

        return try {
            onProgress(BackgroundRemovalStage.LOADING_MODEL)
            val activeSession = try {
                session
            } catch (t: Throwable) {
                return Result.failure(BackgroundRemovalError.ModelLoadFailed(t))
            }

            onProgress(BackgroundRemovalStage.DECODING)
            val cacheFile = ImageDecodeUtils.copyToLocalCache(context.cacheDir, resolver, sourceUri)
            cachedSourceFile = cacheFile
            val sourceBytes = ImageDecodeUtils.readSourceBytes(cacheFile)

            fullResSource = ImageDecodeUtils.decodeFullResolutionMutable(sourceBytes)
            val workingBitmap = ImageDecodeUtils.decodeDownscaledForSegmentation(
                sourceBytes,
                SEGMENTATION_INPUT_LONG_EDGE_PX,
            )
            originalPreview = ImageDecodeUtils.decodeDownscaledForSegmentation(
                sourceBytes,
                SEGMENTATION_INPUT_LONG_EDGE_PX,
            )
            val squareInput = Bitmap.createScaledBitmap(
                workingBitmap,
                MODEL_INPUT_SIZE,
                MODEL_INPUT_SIZE,
                true,
            )
            workingBitmap.recycle()

            onProgress(BackgroundRemovalStage.SEGMENTING)
            val confidenceMask = runInference(activeSession, squareInput)
            squareInput.recycle()

            onProgress(BackgroundRemovalStage.COMPOSITING)
            val fullMask = BitmapCompositor.buildFullResolutionMask(
                confidenceMask,
                MODEL_INPUT_SIZE,
                MODEL_INPUT_SIZE,
                fullResSource.width,
                fullResSource.height,
            )
            val output = BitmapCompositor.compositeInPlace(fullResSource, fullMask)

            onProgress(BackgroundRemovalStage.DONE)
            Result.success(CompositeResult(output, output.width, output.height, originalPreview))
        } catch (oom: OutOfMemoryError) {
            fullResSource?.recycle()
            originalPreview?.recycle()
            Result.failure(BackgroundRemovalError.OutOfMemory)
        } catch (t: Throwable) {
            fullResSource?.recycle()
            originalPreview?.recycle()
            Result.failure(BackgroundRemovalError.SegmentationFailed(t))
        } finally {
            cachedSourceFile?.delete()
        }
    }

    /** Runs one square 1024x1024 bitmap through the model, returning a row-major, contrast-stretched 0..1 mask. */
    private fun runInference(session: OrtSession, squareBitmap: Bitmap): FloatBuffer {
        OnnxTensor.createTensor(
            ortEnvironment,
            buildInputBuffer(squareBitmap),
            longArrayOf(1, 3, MODEL_INPUT_SIZE.toLong(), MODEL_INPUT_SIZE.toLong()),
        ).use { inputTensor ->
            session.run(mapOf(INPUT_TENSOR_NAME to inputTensor)).use { result ->
                val outputTensor = result.get(0) as OnnxTensor
                return normalizeContrast(outputTensor.floatBuffer)
            }
        }
    }

    private fun buildInputBuffer(squareBitmap: Bitmap): FloatBuffer {
        val size = MODEL_INPUT_SIZE
        val pixels = IntArray(size * size)
        squareBitmap.getPixels(pixels, 0, size, 0, 0, size, size)

        val buffer = FloatBuffer.allocate(3 * size * size)
        // NCHW layout: fill the R plane fully, then G, then B.
        for (channel in 0 until 3) {
            for (pixel in pixels) {
                val channelValue = when (channel) {
                    0 -> (pixel shr 16) and 0xFF
                    1 -> (pixel shr 8) and 0xFF
                    else -> pixel and 0xFF
                }
                buffer.put((channelValue / 255f - RMBG_MEAN) / RMBG_STD)
            }
        }
        buffer.rewind()
        return buffer
    }

    /**
     * BRIA's reference postprocessing (the model repo's utilities.py, postprocess_image)
     * stretches the sigmoid mask to fill the full 0..1 range per image rather than using
     * raw sigmoid values directly - skipping this leaves the cutout looking washed out
     * (semi-transparent) instead of a crisp matte.
     */
    private fun normalizeContrast(mask: FloatBuffer): FloatBuffer {
        mask.rewind()
        val values = FloatArray(mask.remaining())
        mask.get(values)

        var min = Float.MAX_VALUE
        var max = -Float.MAX_VALUE
        for (v in values) {
            if (v < min) min = v
            if (v > max) max = v
        }
        val range = (max - min).coerceAtLeast(1e-6f)
        for (i in values.indices) {
            values[i] = ((values[i] - min) / range).coerceIn(0f, 1f)
        }
        return FloatBuffer.wrap(values)
    }
}
