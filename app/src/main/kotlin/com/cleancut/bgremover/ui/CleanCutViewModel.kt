package com.cleancut.bgremover.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleancut.bgremover.export.ExportManager
import com.cleancut.bgremover.segmentation.BackgroundRemovalError
import com.cleancut.bgremover.segmentation.BackgroundRemovalStage
import com.cleancut.bgremover.segmentation.BackgroundRemover
import com.cleancut.bgremover.segmentation.MlKitBackgroundRemover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CleanCutViewModel(
    application: Application,
    private val backgroundRemover: BackgroundRemover = MlKitBackgroundRemover(application),
    private val exportManager: ExportManager = ExportManager(application),
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
                    _uiState.value = CleanCutUiState.Result(composite.outputBitmap)
                },
                onFailure = { error ->
                    _uiState.value = CleanCutUiState.Error(
                        message = errorMessage(error),
                        recoverable = error !is BackgroundRemovalError.PlayServicesUnavailable,
                    )
                },
            )
        }
    }

    fun onSaveClicked() {
        val state = _uiState.value as? CleanCutUiState.Result ?: return
        viewModelScope.launch {
            exportManager.saveToGallery(state.bitmap).onSuccess { uri ->
                lastSavedUri = uri
                _uiState.value = state.copy(savedMessage = "Saved to Pictures/CleanCut")
            }
        }
    }

    fun onShareClicked() {
        val state = _uiState.value as? CleanCutUiState.Result ?: return
        viewModelScope.launch {
            val uri = lastSavedUri ?: exportManager.saveToGallery(state.bitmap).getOrNull()?.also { newUri ->
                lastSavedUri = newUri
                _uiState.value = state.copy(savedMessage = "Saved to Pictures/CleanCut")
            }
            uri?.let { _shareIntent.value = exportManager.shareIntent(it) }
        }
    }

    fun onShareIntentLaunched() {
        _shareIntent.value = null
    }

    fun reset() {
        lastSavedUri = null
        _uiState.value = CleanCutUiState.Idle
    }

    private fun errorMessage(error: Throwable): String = when (error) {
        is BackgroundRemovalError.PlayServicesUnavailable ->
            "CleanCut requires Google Play Services for background removal, which isn't available on this device."
        is BackgroundRemovalError.ModuleDownloadFailed ->
            "Couldn't download the on-device AI model. Check your internet connection and try again."
        is BackgroundRemovalError.DecodeFailed ->
            "Couldn't read that image. Try a different photo."
        is BackgroundRemovalError.OutOfMemory ->
            "This photo is too large for this device to process."
        is BackgroundRemovalError.SegmentationFailed ->
            "Background removal failed. Try again."
        else -> "Something went wrong. Try again."
    }
}
