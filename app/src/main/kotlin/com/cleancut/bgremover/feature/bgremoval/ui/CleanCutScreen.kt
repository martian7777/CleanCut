package com.cleancut.bgremover.feature.bgremoval.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cleancut.bgremover.core.designsystem.CyberIndigo
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.core.designsystem.ElectricViolet
import com.cleancut.bgremover.core.designsystem.PrimaryGradient
import com.cleancut.bgremover.feature.bgremoval.presentation.CleanCutUiState
import com.cleancut.bgremover.feature.bgremoval.presentation.CleanCutViewModel
import com.cleancut.bgremover.feature.bgremoval.ui.components.BackgroundSelectorStrip
import com.cleancut.bgremover.feature.bgremoval.ui.components.ExportBar
import com.cleancut.bgremover.feature.bgremoval.ui.components.PickerButton
import com.cleancut.bgremover.feature.bgremoval.ui.components.ProcessingIndicator
import com.cleancut.bgremover.feature.bgremoval.ui.components.ResultPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanCutScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: CleanCutViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                CleanCutViewModel(application)
            }
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareIntent by viewModel.shareIntent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle physical / gesture back press on Result or Error screen to return to picker
    if (uiState !is CleanCutUiState.Idle) {
        BackHandler {
            viewModel.reset()
        }
    }

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
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (uiState !is CleanCutUiState.Idle) {
                        IconButton(
                            onClick = viewModel::reset,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (uiState is CleanCutUiState.Idle) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryGradient),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCut,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = if (uiState is CleanCutUiState.Result) "Studio Cutout" else "CleanCut",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElectricCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp),
                                ) {
                                    Text(
                                        text = "LEMINNO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan,
                                        fontSize = 9.sp,
                                    )
                                }
                            }
                            Text(
                                text = if (uiState is CleanCutUiState.Result) "Tap backdrop to customize" else "Neural Background Isolation",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(250))
            },
            label = "screenState",
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) { state ->
            when (state) {
                is CleanCutUiState.Idle -> PickerButton(
                    onPickClicked = {
                        pickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                is CleanCutUiState.Processing -> ProcessingIndicator(
                    stage = state.stage,
                    modifier = Modifier.fillMaxSize(),
                )

                is CleanCutUiState.Result -> Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ResultPreview(
                        cutoutBitmap = state.bitmap,
                        originalBitmap = state.originalBitmap,
                        backgroundOption = state.selectedBackground,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    )
                    BackgroundSelectorStrip(
                        selectedOption = state.selectedBackground,
                        onOptionSelected = viewModel::onBackgroundSelected,
                    )
                    ExportBar(
                        savedMessage = state.savedMessage,
                        onSaveClicked = viewModel::onSaveClicked,
                        onShareClicked = viewModel::onShareClicked,
                        onPickAnotherClicked = viewModel::reset,
                    )
                }

                is CleanCutUiState.Error -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                                RoundedCornerShape(24.dp),
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Unable to complete cut",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            if (state.recoverable) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(PrimaryGradient)
                                        .padding(horizontal = 24.dp, vertical = 10.dp),
                                ) {
                                    TextButton(onClick = viewModel::reset) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Refresh,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp),
                                            )
                                            Text(
                                                text = "Try Again",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
