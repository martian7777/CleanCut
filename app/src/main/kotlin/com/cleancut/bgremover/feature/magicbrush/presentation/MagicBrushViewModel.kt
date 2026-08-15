package com.cleancut.bgremover.feature.magicbrush.presentation

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleancut.bgremover.core.common.ImageDecodeUtils
import com.cleancut.bgremover.core.settings.SettingsManager
import com.cleancut.bgremover.feature.bgremoval.data.MediaStoreExportRepository
import com.cleancut.bgremover.feature.bgremoval.domain.ExportRepository
import com.cleancut.bgremover.feature.magicbrush.data.MiGanInpaintingEngine
import com.cleancut.bgremover.feature.magicbrush.domain.ErasePatch
import com.cleancut.bgremover.feature.magicbrush.domain.InpaintingEngine
import com.cleancut.bgremover.feature.magicbrush.domain.InpaintingError
import com.cleancut.bgremover.feature.magicbrush.domain.InpaintingStage
import com.cleancut.bgremover.feature.magicbrush.domain.MaskRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MagicBrushViewModel(
    application: Application,
    private val inpaintingEngine: InpaintingEngine = MiGanInpaintingEngine(application),
    private val exportRepository: ExportRepository = MediaStoreExportRepository(application),
    private val settingsManager: SettingsManager = SettingsManager.getInstance(application),
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<MagicBrushUiState>(MagicBrushUiState.Idle)
    val uiState: StateFlow<MagicBrushUiState> = _uiState.asStateFlow()

    private val _shareIntent = MutableStateFlow<Intent?>(null)
    val shareIntent: StateFlow<Intent?> = _shareIntent.asStateFlow()

    private var lastSavedUri: Uri? = null

    // Patch-based undo/redo: each entry is the small before/after crop touched by one erase
    // step (see ErasePatch), not a full-bitmap copy - a 48MP full-bitmap history would OOM
    // within a couple of steps. historyIndex points at the last *applied* patch; -1 means
    // nothing has been erased yet.
    private val history = mutableListOf<ErasePatch>()
    private var historyIndex = -1

    fun onImagePicked(uri: Uri) {
        history.clear()
        historyIndex = -1
        lastSavedUri = null

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val context = getApplication<Application>()
                val cacheFile = ImageDecodeUtils.copyToLocalCache(context.cacheDir, context.contentResolver, uri)
                val bytes = try {
                    ImageDecodeUtils.readSourceBytes(cacheFile)
                } finally {
                    cacheFile.delete()
                }

                val working = ImageDecodeUtils.decodeFullResolutionMutable(bytes)
                val original = ImageDecodeUtils.decodeFullResolutionMutable(bytes)
                _uiState.value = MagicBrushUiState.Editing(workingBitmap = working, originalBitmap = original)
            } catch (oom: OutOfMemoryError) {
                _uiState.value = MagicBrushUiState.Error(
                    message = "This photo is too large for this device to process.",
                    recoverable = true,
                )
            } catch (t: Throwable) {
                _uiState.value = MagicBrushUiState.Error(
                    message = "Couldn't read that image. Try a different photo.",
                    recoverable = true,
                )
            }
        }
    }

    fun onBrushSizeChanged(px: Float) {
        val state = _uiState.value as? MagicBrushUiState.Editing ?: return
        _uiState.value = state.copy(brushSizePx = px)
    }

    fun onEraseClicked(mask: MaskRegion) {
        val state = _uiState.value as? MagicBrushUiState.Editing ?: return

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = state.copy(erasingStage = InpaintingStage.LOADING_MODEL, errorMessage = null)

            val result = inpaintingEngine.inpaint(state.workingBitmap, mask) { stage ->
                val editingNow = _uiState.value as? MagicBrushUiState.Editing
                if (editingNow != null) {
                    _uiState.value = editingNow.copy(erasingStage = stage)
                }
            }

            val latest = _uiState.value as? MagicBrushUiState.Editing ?: return@launch
            result.fold(
                onSuccess = { inpaintResult ->
                    if (history.size > historyIndex + 1) {
                        history.subList(historyIndex + 1, history.size).clear()
                    }
                    history.add(inpaintResult.patch)
                    historyIndex++
                    _uiState.value = latest.copy(
                        revision = latest.revision + 1,
                        erasingStage = null,
                        canUndo = true,
                        canRedo = false,
                        savedMessage = null,
                        errorMessage = null,
                    )
                },
                onFailure = { error ->
                    _uiState.value = latest.copy(erasingStage = null, errorMessage = errorMessage(error))
                },
            )
        }
    }

    fun onUndoClicked() {
        val state = _uiState.value as? MagicBrushUiState.Editing ?: return
        if (historyIndex < 0) return

        val patch = history[historyIndex]
        state.workingBitmap.setPixels(
            patch.beforePixels,
            0,
            patch.bounds.width(),
            patch.bounds.left,
            patch.bounds.top,
            patch.bounds.width(),
            patch.bounds.height(),
        )
        historyIndex--
        _uiState.value = state.copy(
            revision = state.revision + 1,
            canUndo = historyIndex >= 0,
            canRedo = true,
            savedMessage = null,
        )
    }

    fun onRedoClicked() {
        val state = _uiState.value as? MagicBrushUiState.Editing ?: return
        if (historyIndex >= history.size - 1) return

        historyIndex++
        val patch = history[historyIndex]
        state.workingBitmap.setPixels(
            patch.afterPixels,
            0,
            patch.bounds.width(),
            patch.bounds.left,
            patch.bounds.top,
            patch.bounds.width(),
            patch.bounds.height(),
        )
        _uiState.value = state.copy(
            revision = state.revision + 1,
            canUndo = true,
            canRedo = historyIndex < history.size - 1,
            savedMessage = null,
        )
    }

    fun onSaveClicked() {
        val state = _uiState.value as? MagicBrushUiState.Editing ?: return
        val currentSettings = settingsManager.settings.value

        viewModelScope.launch(Dispatchers.Default) {
            exportRepository.saveToGallery(
                bitmap = state.workingBitmap,
                format = currentSettings.exportFormat,
                quality = currentSettings.exportQuality,
            ).onSuccess { uri ->
                lastSavedUri = uri
                val latest = _uiState.value as? MagicBrushUiState.Editing ?: return@onSuccess
                _uiState.value = latest.copy(savedMessage = "Saved to Pictures/CleanCut")
            }
        }
    }

    fun onShareClicked() {
        val state = _uiState.value as? MagicBrushUiState.Editing ?: return
        val currentSettings = settingsManager.settings.value

        viewModelScope.launch(Dispatchers.Default) {
            val uri = lastSavedUri ?: exportRepository.saveToGallery(
                bitmap = state.workingBitmap,
                format = currentSettings.exportFormat,
                quality = currentSettings.exportQuality,
            ).getOrNull()?.also { newUri ->
                lastSavedUri = newUri
                val latest = _uiState.value as? MagicBrushUiState.Editing ?: return@also
                _uiState.value = latest.copy(savedMessage = "Saved to Pictures/CleanCut")
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
        history.clear()
        historyIndex = -1
        lastSavedUri = null
        _uiState.value = MagicBrushUiState.Idle
    }

    private fun errorMessage(error: Throwable): String = when (error) {
        is InpaintingError.EmptyMask -> "Paint over something to erase first."
        is InpaintingError.OutOfMemory -> "This photo is too large for this device to process."
        is InpaintingError.ModelLoadFailed -> "Couldn't load the AI model. Try reinstalling the app."
        is InpaintingError.InferenceFailed -> {
            val detail = error.cause.message ?: error.cause::class.java.simpleName
            "Erase failed: $detail"
        }
        else -> "Something went wrong: ${error.message ?: error::class.java.simpleName}"
    }
}
