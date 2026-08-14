package com.cleancut.bgremover.feature.bgremoval.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cleancut.bgremover.feature.bgremoval.presentation.CleanCutUiState
import com.cleancut.bgremover.feature.bgremoval.presentation.CleanCutViewModel
import com.cleancut.bgremover.feature.bgremoval.ui.components.ExportBar
import com.cleancut.bgremover.feature.bgremoval.ui.components.PickerButton
import com.cleancut.bgremover.feature.bgremoval.ui.components.ProcessingIndicator
import com.cleancut.bgremover.feature.bgremoval.ui.components.ResultPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanCutScreen(viewModel: CleanCutViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareIntent by viewModel.shareIntent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let(viewModel::onImagePicked)
    }

    LaunchedEffect(shareIntent) {
        shareIntent?.let { intent ->
            context.startActivity(intent)
            viewModel.onShareIntentLaunched()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("CleanCut") }) },
    ) { padding ->
        when (val state = uiState) {
            is CleanCutUiState.Idle -> PickerButton(
                onPickClicked = {
                    pickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                modifier = Modifier.padding(padding).fillMaxSize(),
            )

            is CleanCutUiState.Processing -> ProcessingIndicator(
                stage = state.stage,
                modifier = Modifier.padding(padding).fillMaxSize(),
            )

            is CleanCutUiState.Result -> Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                ResultPreview(
                    bitmap = state.bitmap,
                    modifier = Modifier.weight(1f).fillMaxSize(),
                )
                ExportBar(
                    savedMessage = state.savedMessage,
                    onSaveClicked = viewModel::onSaveClicked,
                    onShareClicked = viewModel::onShareClicked,
                    onPickAnotherClicked = viewModel::reset,
                )
            }

            is CleanCutUiState.Error -> Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
                if (state.recoverable) {
                    TextButton(onClick = viewModel::reset) { Text("Try Again") }
                }
            }
        }
    }
}
