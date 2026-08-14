package com.cleancut.bgremover.feature.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cleancut.bgremover.core.designsystem.BorderDark
import com.cleancut.bgremover.core.designsystem.CyberIndigo
import com.cleancut.bgremover.core.designsystem.ElectricCyan
import com.cleancut.bgremover.core.designsystem.ElectricViolet
import com.cleancut.bgremover.core.designsystem.PrimaryGradient
import com.cleancut.bgremover.core.settings.AppSettings
import com.cleancut.bgremover.core.settings.AppThemeMode
import com.cleancut.bgremover.core.settings.ExportFormat
import com.cleancut.bgremover.feature.settings.presentation.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                SettingsViewModel(application)
            }
        },
    ),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Theme Mode Section
            item {
                SectionHeader(title = "Appearance", icon = Icons.Rounded.Brightness4)
                Spacer(modifier = Modifier.height(10.dp))
                ThemeSelectorCard(
                    selectedTheme = settings.themeMode,
                    onThemeSelected = viewModel::onThemeSelected,
                )
            }

            // Export Preferences Section
            item {
                SectionHeader(title = "Export & Format", icon = Icons.Rounded.Image)
                Spacer(modifier = Modifier.height(10.dp))
                ExportFormatCard(
                    selectedFormat = settings.exportFormat,
                    onFormatSelected = viewModel::onExportFormatSelected,
                    quality = settings.exportQuality,
                    onQualityChanged = viewModel::onExportQualityChanged,
                )
            }

            // AI & Performance Section
            item {
                SectionHeader(title = "Engine & Performance", icon = Icons.Rounded.Speed)
                Spacer(modifier = Modifier.height(10.dp))
                PerformanceCard(
                    highQuality = settings.highQualityUpsampling,
                    onHighQualityToggled = viewModel::onHighQualityToggled,
                )
            }

            // Powered by LEMINNO Signature Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                LeminnoBrandingCard()
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricViolet,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ThemeSelectorCard(
    selectedTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp),
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeOptionChip(
                title = "Dark Studio",
                icon = Icons.Rounded.DarkMode,
                isSelected = selectedTheme == AppThemeMode.DARK,
                onClick = { onThemeSelected(AppThemeMode.DARK) },
                modifier = Modifier.weight(1f),
            )
            ThemeOptionChip(
                title = "Light Minimal",
                icon = Icons.Rounded.LightMode,
                isSelected = selectedTheme == AppThemeMode.LIGHT,
                onClick = { onThemeSelected(AppThemeMode.LIGHT) },
                modifier = Modifier.weight(1f),
            )
            ThemeOptionChip(
                title = "System",
                icon = Icons.Rounded.AutoAwesome,
                isSelected = selectedTheme == AppThemeMode.SYSTEM,
                onClick = { onThemeSelected(AppThemeMode.SYSTEM) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeOptionChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (isSelected) {
        CyberIndigo.copy(alpha = 0.18f)
    } else {
        Color.Transparent
    }
    val borderColor = if (isSelected) CyberIndigo else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ExportFormatCard(
    selectedFormat: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit,
    quality: Int,
    onQualityChanged: (Int) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp),
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Text(
                text = "Default Output Format",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "PNG preserves alpha transparency; JPEG flattens with custom background.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )

            ExportFormat.values().forEach { format ->
                val isSelected = format == selectedFormat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) CyberIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        )
                        .border(
                            1.dp,
                            if (isSelected) CyberIndigo.copy(alpha = 0.6f) else Color.Transparent,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable { onFormatSelected(format) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoSizeSelectActual,
                            contentDescription = null,
                            tint = if (isSelected) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = format.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Selected",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Directory info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp),
                )
                Column {
                    Text(
                        text = "Destination Gallery Folder",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Pictures/CleanCut",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceCard(
    highQuality: Boolean,
    onHighQualityToggled: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp),
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = null,
                        tint = ElectricViolet,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        text = "On-Device Neural Engine",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Zero cloud uploads. 100% private subject segmentation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LeminnoBrandingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        CyberIndigo.copy(alpha = 0.25f),
                        ElectricViolet.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(ElectricViolet.copy(alpha = 0.6f), ElectricCyan.copy(alpha = 0.6f)),
                ),
                RoundedCornerShape(24.dp),
            )
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryGradient)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "POWERED BY LEMINNO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CleanCut Studio",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Next-generation on-device AI background isolation. Crafted for hyper-fast edits without compromising original pixel resolution.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BadgeText(text = "v0.1.0-release")
                BadgeText(text = "ML Kit Vision")
                BadgeText(text = "Hardware Accelerated")
            }
        }
    }
}

@Composable
private fun BadgeText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = ElectricCyan,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ElectricCyan.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
