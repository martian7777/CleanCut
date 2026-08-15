package com.cleancut.bgremover.feature.magicbrush.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.core.designsystem.ElectricViolet
import com.cleancut.bgremover.core.designsystem.PrimaryGradient
import com.cleancut.bgremover.feature.bgremoval.ui.components.ExportBar
import com.cleancut.bgremover.feature.bgremoval.ui.components.PickerButton
import com.cleancut.bgremover.feature.magicbrush.presentation.MagicBrushUiState
import com.cleancut.bgremover.feature.magicbrush.presentation.MagicBrushViewModel
import com.cleancut.bgremover.feature.magicbrush.ui.components.BrushCanvas
import com.cleancut.bgremover.feature.magicbrush.ui.components.BrushCanvasState
import com.cleancut.bgremover.feature.magicbrush.ui.components.BrushControlsBar
import com.cleancut.bgremover.feature.magicbrush.ui.components.InpaintingProgressOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagicBrushScreen(
    initialUri: Uri? = null,
    onNavigateToSettings: () -> Unit = {},
    onImagePicked: (Uri) -> Unit = {},
    modeToggle: @Composable () -> Unit = {},
    viewModel: MagicBrushViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                MagicBrushViewModel(application)
            }
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareIntent by viewModel.shareIntent.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val canvasState = remember { BrushCanvasState() }

    if (uiState !is MagicBrushUiState.Idle) {
        BackHandler { viewModel.reset() }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            onImagePicked(it)
            viewModel.onImagePicked(it)
        }
    }

    LaunchedEffect(initialUri) {
        initialUri?.let(viewModel::onImagePicked)
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
                    if (uiState !is MagicBrushUiState.Idle) {
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
                        if (uiState is MagicBrushUiState.Idle) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryGradient),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoFixHigh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Magic Eraser",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "Paint to erase objects",
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
        modeToggle()
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(250))
            },
            label = "magicBrushState",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { state ->
            when (state) {
                is MagicBrushUiState.Idle -> PickerButton(
                    onPickClicked = {
                        pickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                is MagicBrushUiState.Editing -> Column(modifier = Modifier.fillMaxSize()) {
                    var showOriginal by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                1.dp,
                                ElectricViolet.copy(alpha = 0.3f),
                                RoundedCornerShape(24.dp),
                            ),
                    ) {
                        Crossfade(targetState = showOriginal, animationSpec = tween(250), label = "beforeAfter") { isOriginal ->
                            if (isOriginal) {
                                Image(
                                    bitmap = remember(state.originalBitmap) { state.originalBitmap.asImageBitmap() },
                                    contentDescription = "Original photo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                BrushCanvas(
                                    bitmap = state.workingBitmap,
                                    revision = state.revision,
                                    brushSizePx = state.brushSizePx,
                                    state = canvasState,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            showOriginal = true
                                            tryAwaitRelease()
                                            showOriginal = false
                                        },
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = if (showOriginal) Icons.Rounded.Visibility else Icons.Rounded.Compare,
                                    contentDescription = "Hold to compare",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = if (showOriginal) "Original" else "Hold to Compare",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                )
                            }
                        }

                        state.erasingStage?.let { stage ->
                            InpaintingProgressOverlay(stage = stage, modifier = Modifier.fillMaxSize())
                        }
                    }

                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 },
                    ) {
                        state.errorMessage?.let { msg ->
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }

                    BrushControlsBar(
                        brushSizePx = state.brushSizePx,
                        onBrushSizeChanged = viewModel::onBrushSizeChanged,
                        canUndo = state.canUndo,
                        canRedo = state.canRedo,
                        onUndoClicked = viewModel::onUndoClicked,
                        onRedoClicked = viewModel::onRedoClicked,
                        canErase = canvasState.hasPaintedContent && state.erasingStage == null,
                        isErasing = state.erasingStage != null,
                        onEraseClicked = {
                            canvasState.toMaskRegion(state.brushSizePx)?.let(viewModel::onEraseClicked)
                        },
                    )
                    ExportBar(
                        savedMessage = state.savedMessage,
                        onSaveClicked = viewModel::onSaveClicked,
                        onShareClicked = viewModel::onShareClicked,
                        onPickAnotherClicked = viewModel::reset,
                    )
                }

                is MagicBrushUiState.Error -> Column(
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
                            Spacer(modifier = Modifier.size(16.dp))
                            Text(
                                text = "Unable to open photo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            if (state.recoverable) {
                                Spacer(modifier = Modifier.size(20.dp))
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
}
