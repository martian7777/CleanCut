package com.cleancut.bgremover.feature.bgremoval.data

import android.content.Context
import android.net.Uri
import com.cleancut.bgremover.core.common.ImageDecodeUtils
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemovalError
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemovalStage
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemover
import com.cleancut.bgremover.feature.bgremoval.domain.CompositeResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.tasks.await
import java.io.File

private const val SEGMENTATION_INPUT_LONG_EDGE_PX = 1024

/**
 * Wraps ML Kit's Subject Segmentation API (general people/pets/objects, not just
 * selfies) behind [BackgroundRemover]. Segmentation always runs on a small
 * downscaled copy of the source - the model doesn't need a 48MP input - while the
 * final composite is built against the full-resolution decode so kept pixels stay
 * byte-identical to the source.
 */
class MlKitBackgroundRemover(private val context: Context) : BackgroundRemover {

    private val availability = SegmentationAvailability(context)

    // Deferred until first use (inside removeBackground's try block) rather than
    // built eagerly here - this class is constructed as part of ViewModel setup at
    // app launch, and any construction failure here would otherwise crash the app
    // on every launch instead of surfacing as a recoverable error.
    private val segmenter by lazy {
        val segmenterOptions = SubjectSegmenterOptions.Builder()
            .enableForegroundConfidenceMask()
            .build()
        SubjectSegmentation.getClient(segmenterOptions)
    }

    override suspend fun removeBackground(
        sourceUri: Uri,
        onProgress: (BackgroundRemovalStage) -> Unit,
    ): Result<CompositeResult> {
        val resolver = context.contentResolver

        var fullResSource: android.graphics.Bitmap? = null
        var segInputBitmap: android.graphics.Bitmap? = null
        var cachedSourceFile: File? = null

        return try {
            onProgress(BackgroundRemovalStage.CHECKING_MODEL)
            val availabilityResult = availability.ensureModuleAvailable(segmenter) {
                onProgress(BackgroundRemovalStage.DOWNLOADING_MODEL)
            }
            availabilityResult.exceptionOrNull()?.let { error ->
                return Result.failure(error)
            }

            onProgress(BackgroundRemovalStage.DECODING)
            // Copy once into app-private cache - Photo Picker Uris aren't reliably
            // reopenable across the multiple decode passes below (see ImageDecodeUtils).
            val cacheFile = ImageDecodeUtils.copyToLocalCache(context.cacheDir, resolver, sourceUri)
            cachedSourceFile = cacheFile
            val localUri = Uri.fromFile(cacheFile)

            fullResSource = ImageDecodeUtils.decodeFullResolutionMutable(resolver, localUri)
            segInputBitmap = ImageDecodeUtils.decodeDownscaledForSegmentation(
                resolver,
                localUri,
                SEGMENTATION_INPUT_LONG_EDGE_PX,
            )

            onProgress(BackgroundRemovalStage.SEGMENTING)
            val inputImage = InputImage.fromBitmap(segInputBitmap, 0)
            val segResult = segmenter.process(inputImage).await()

            val maskWidth = segInputBitmap.width
            val maskHeight = segInputBitmap.height
            segInputBitmap.recycle()
            segInputBitmap = null

            val confidenceMask = segResult.foregroundConfidenceMask
                ?: return Result.failure(
                    BackgroundRemovalError.SegmentationFailed(
                        IllegalStateException("No confidence mask returned"),
                    ),
                )

            onProgress(BackgroundRemovalStage.COMPOSITING)
            val fullMask = BitmapCompositor.buildFullResolutionMask(
                confidenceMask,
                maskWidth,
                maskHeight,
                fullResSource.width,
                fullResSource.height,
            )
            val output = BitmapCompositor.compositeInPlace(fullResSource, fullMask)

            onProgress(BackgroundRemovalStage.DONE)
            Result.success(CompositeResult(output, output.width, output.height))
        } catch (oom: OutOfMemoryError) {
            fullResSource?.recycle()
            Result.failure(BackgroundRemovalError.OutOfMemory)
        } catch (t: Throwable) {
            fullResSource?.recycle()
            Result.failure(BackgroundRemovalError.SegmentationFailed(t))
        } finally {
            segInputBitmap?.recycle()
            cachedSourceFile?.delete()
        }
    }
}
