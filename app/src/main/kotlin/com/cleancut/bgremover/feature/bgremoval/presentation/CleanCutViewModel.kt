package com.cleancut.bgremover.feature.bgremoval.presentation

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleancut.bgremover.core.settings.ExportFormat
import com.cleancut.bgremover.core.settings.SettingsManager
import com.cleancut.bgremover.feature.bgremoval.data.MediaStoreExportRepository
import com.cleancut.bgremover.feature.bgremoval.data.RmbgBackgroundRemover
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundOption
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemovalError
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemovalStage
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemover
import com.cleancut.bgremover.feature.bgremoval.domain.ExportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CleanCutViewModel(
    application: Application,
    private val backgroundRemover: BackgroundRemover = RmbgBackgroundRemover(application),
    private val exportRepository: ExportRepository = MediaStoreExportRepository(application),
    private val settingsManager: SettingsManager = SettingsManager.getInstance(application),
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<CleanCutUiState>(CleanCutUiState.Idle)
    val uiState: StateFlow<CleanCutUiState> = _uiState.asStateFlow()

    private val _shareIntent = MutableStateFlow<Intent?>(null)
    val shareIntent: StateFlow<Intent?> = _shareIntent.asStateFlow()

    private var lastSavedUri: Uri? = null

    fun onImagePicked(uri: Uri) {
        lastSavedUri = null
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = CleanCutUiState.Processing(BackgroundRemovalStage.DECODING)

            val result = backgroundRemover.removeBackground(uri) { stage ->
                _uiState.value = CleanCutUiState.Processing(stage)
            }

            result.fold(
                onSuccess = { composite ->
                    _uiState.value = CleanCutUiState.Result(
                        bitmap = composite.outputBitmap,
                        originalBitmap = composite.originalPreview,
                        selectedBackground = BackgroundOption.Transparent,
                    )
                },
                onFailure = { error ->
                    _uiState.value = CleanCutUiState.Error(
                        message = errorMessage(error),
                        recoverable = true,
                    )
                },
            )
        }
    }

    fun onBackgroundSelected(option: BackgroundOption) {
        val state = _uiState.value as? CleanCutUiState.Result ?: return
        lastSavedUri = null
        _uiState.value = state.copy(selectedBackground = option, savedMessage = null)
    }

    fun onSaveClicked() {
        val state = _uiState.value as? CleanCutUiState.Result ?: return
        val currentSettings = settingsManager.settings.value
        val format = if (state.selectedBackground is BackgroundOption.SolidColor && currentSettings.exportFormat == ExportFormat.PNG) {
            // Can save as PNG with solid color or format as configured
            currentSettings.exportFormat
        } else {
            currentSettings.exportFormat
        }

        viewModelScope.launch(Dispatchers.Default) {
            val exportBitmap = prepareBitmapForExport(state.bitmap, state.selectedBackground)
            exportRepository.saveToGallery(
                bitmap = exportBitmap,
                format = format,
                quality = currentSettings.exportQuality,
            ).onSuccess { uri ->
                lastSavedUri = uri
                _uiState.value = state.copy(savedMessage = "Saved to Pictures/CleanCut")
            }
        }
    }

    fun onShareClicked() {
        val state = _uiState.value as? CleanCutUiState.Result ?: return
        val currentSettings = settingsManager.settings.value

        viewModelScope.launch(Dispatchers.Default) {
            val exportBitmap = prepareBitmapForExport(state.bitmap, state.selectedBackground)
            val uri = lastSavedUri ?: exportRepository.saveToGallery(
                bitmap = exportBitmap,
                format = currentSettings.exportFormat,
                quality = currentSettings.exportQuality,
            ).getOrNull()?.also { newUri ->
                lastSavedUri = newUri
                _uiState.value = state.copy(savedMessage = "Saved to Pictures/CleanCut")
            }

            uri?.let {
                _shareIntent.value = exportRepository.shareIntent(it, currentSettings.exportFormat.mimeType)
            }
        }
    }

    fun onShareIntentLaunched() {
        _shareIntent.value = null
    }

    fun reset() {
        lastSavedUri = null
        _uiState.value = CleanCutUiState.Idle
    }

    private fun prepareBitmapForExport(cutout: Bitmap, backgroundOption: BackgroundOption): Bitmap {
        return when (backgroundOption) {
            is BackgroundOption.Transparent -> cutout
            is BackgroundOption.SolidColor -> {
                val composite = Bitmap.createBitmap(cutout.width, cutout.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(composite)
                canvas.drawColor(backgroundOption.argbInt)
                canvas.drawBitmap(cutout, 0f, 0f, null)
                composite
            }
        }
    }

    private fun errorMessage(error: Throwable): String = when (error) {
        is BackgroundRemovalError.DecodeFailed ->
            "Couldn't read that image. Try a different photo."
        is BackgroundRemovalError.OutOfMemory ->
            "This photo is too large for this device to process."
        is BackgroundRemovalError.ModelLoadFailed ->
            "Couldn't load the AI model. Try reinstalling the app."
        is BackgroundRemovalError.SegmentationFailed -> {
            val detail = error.cause.message ?: error.cause::class.java.simpleName
            "Background removal failed: $detail"
        }
        else -> "Something went wrong: ${error.message ?: error::class.java.simpleName}"
    }
}
