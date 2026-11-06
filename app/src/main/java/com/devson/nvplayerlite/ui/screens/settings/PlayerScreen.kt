package com.devson.nvplayerlite.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devson.nvplayerlite.repository.FullScreenMode
import com.devson.nvplayerlite.repository.OrientationMode
import com.devson.nvplayerlite.repository.SoftButtonMode
import com.devson.nvplayerlite.model.LanguageList
import com.devson.nvplayerlite.ui.components.sheets.LanguagePickerSheet
import com.devson.nvplayerlite.viewmodel.SettingsViewModel

// Pre-cached static option lists to eliminate per-recomposition allocations
private val orientationOptions = OrientationMode.entries.map { it.name }
private val fullScreenOptions = FullScreenMode.entries.map { it.name }
private val softButtonOptions = SoftButtonMode.entries.map { it.name }
private val playbackControlPositionOptions = listOf("Center", "Below Seekbar")
private val controlIconSizeOptions = listOf("Small", "Medium", "Large")
private val seekBarStyleOptions = listOf("Standard", "Thick", "Slim", "Wavy")

@Immutable
private data class RadioDialogData(
    val title: String,
    val options: List<String>,
    val selectedOption: String,
    val onOptionSelected: (String) -> Unit
)

// Maps raw enum names to user-readable labels
private fun String.toDisplayLabel(): String = this
    .replace('_', ' ')
    .lowercase()
    .replaceFirstChar { it.uppercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    onNavigateToControlEditor: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val playbackSettings  by settingsViewModel.playbackSettings.collectAsState()
    val defaultAudioLang  by settingsViewModel.defaultAudioLang.collectAsState()
    val defaultSubtitleLang by settingsViewModel.defaultSubtitleLang.collectAsState()

    var showAudioPicker by remember { mutableStateOf(false) }
    var showSubtitlePicker by remember { mutableStateOf(false) }
    var activeRadioDialog by remember { mutableStateOf<RadioDialogData?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Player Interface",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 32.dp
            )
        ) {
            item(key = "spacer_top") { Spacer(Modifier.height(8.dp)) }

            item(key = "label_layout") { SettingsSectionLabel("Control Regions Layout Customization") }
            item(key = "card_layout") {
                SettingsCard {
                    SettingsClickRow(
                        icon = Icons.Default.Dashboard,
                        title = "Custom Controls Layout",
                        subtitle = "Customize and reorder control buttons using an interactive player preview",
                        onClick = onNavigateToControlEditor
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            item(key = "label_display_settings") { SettingsSectionLabel("Display & Interface Controls") }
            item(key = "card_display_settings") {
                SettingsCard {
                    SettingsRadioItem(
                        icon = Icons.Default.ScreenRotation,
                        title = "Orientation Mode",
                        subtitle = "How the video orientation behaves",
                        selectedOption = playbackSettings.orientationMode.name,
                        onClick = {
                            activeRadioDialog = RadioDialogData(
                                title = "Orientation Mode",
                                options = orientationOptions,
                                selectedOption = playbackSettings.orientationMode.name,
                                onOptionSelected = {
                                    settingsViewModel.updateOrientationMode(OrientationMode.valueOf(it))
                                }
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsRadioItem(
                        icon = Icons.Default.Fullscreen,
                        title = "Full Screen Mode",
                        subtitle = "Full screen behaviour during playback",
                        selectedOption = playbackSettings.fullScreenMode.name,
                        onClick = {
                            activeRadioDialog = RadioDialogData(
                                title = "Full Screen Mode",
                                options = fullScreenOptions,
                                selectedOption = playbackSettings.fullScreenMode.name,
                                onOptionSelected = {
                                    settingsViewModel.updateFullScreenMode(FullScreenMode.valueOf(it))
                                }
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsRadioItem(
                        icon = Icons.Default.SmartButton,
                        title = "Soft Button",
                        subtitle = "Configure on-screen soft buttons",
                        selectedOption = playbackSettings.softButtonMode.name,
                        onClick = {
                            activeRadioDialog = RadioDialogData(
                                title = "Soft Button",
                                options = softButtonOptions,
                                selectedOption = playbackSettings.softButtonMode.name,
                                onOptionSelected = {
                                    settingsViewModel.updateSoftButtonMode(SoftButtonMode.valueOf(it))
                                }
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsRadioItem(
                        icon = Icons.Default.PlayCircle,
                        title = "Playback Control Position",
                        subtitle = if (playbackSettings.isBottomLayoutEnabled) "Below Seekbar" else "Center",
                        selectedOption = if (playbackSettings.isBottomLayoutEnabled) "Below Seekbar" else "Center",
                        onClick = {
                            activeRadioDialog = RadioDialogData(
                                title = "Playback Control Position",
                                options = playbackControlPositionOptions,
                                selectedOption = if (playbackSettings.isBottomLayoutEnabled) "Below Seekbar" else "Center",
                                onOptionSelected = {
                                    settingsViewModel.updateIsBottomLayoutEnabled(it == "Below Seekbar")
                                }
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsRadioItem(
                        icon = Icons.Default.PhotoSizeSelectLarge,
                        title = "Controls Icon Size",
                        subtitle = "Scale size of player control buttons",
                        selectedOption = playbackSettings.controlIconSize.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = {
                            activeRadioDialog = RadioDialogData(
                                title = "Controls Icon Size",
                                options = controlIconSizeOptions,
                                selectedOption = playbackSettings.controlIconSize.lowercase().replaceFirstChar { it.uppercase() },
                                onOptionSelected = {
                                    settingsViewModel.updateControlIconSize(it.lowercase())
                                }
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsRadioItem(
                        icon = Icons.Default.Waves,
                        title = "Seekbar Style",
                        subtitle = "Visual design of the playback progress bar",
                        selectedOption = playbackSettings.seekBarStyle.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = {
                            activeRadioDialog = RadioDialogData(
                                title = "Seekbar Style",
                                options = seekBarStyleOptions,
                                selectedOption = playbackSettings.seekBarStyle.lowercase().replaceFirstChar { it.uppercase() },
                                onOptionSelected = {
                                    settingsViewModel.updateSeekBarStyle(it.lowercase())
                                }
                            )
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            item(key = "label_playback_buttons") { SettingsSectionLabel("Playback Buttons & Actions") }
            item(key = "card_playback_buttons") {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.SkipNext,
                        title = "Auto-play Next Video",
                        subtitle = "Automatically load and play next video in folder",
                        checked = playbackSettings.autoPlayEnabled,
                        onCheckedChange = { settingsViewModel.updateAutoPlayEnabled(it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.FastForward,
                        title = "Show Seek Buttons",
                        subtitle = "Show fast forward and rewind seek buttons in player controls",
                        checked = playbackSettings.showSeekButtons,
                        onCheckedChange = { settingsViewModel.updateShowSeekButtons(it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.SkipNext,
                        title = "Show Skip Prev/Next Buttons",
                        subtitle = "Show previous/next chapter skip buttons in player controls",
                        checked = playbackSettings.showNextPrevButtons,
                        onCheckedChange = { settingsViewModel.updateShowNextPrevButtons(it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.HourglassBottom,
                        title = "Show Remaining Time",
                        subtitle = "Display remaining time instead of total duration",
                        checked = playbackSettings.showRemainingTime,
                        onCheckedChange = { settingsViewModel.updateShowRemainingTime(it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.Gradient,
                        title = "Controls Backdrop Dimming",
                        subtitle = "Show top and bottom black fade behind player controls",
                        checked = playbackSettings.showControlGradients,
                        onCheckedChange = { settingsViewModel.updateShowControlGradients(it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.Headphones,
                        title = "Background Playback",
                        subtitle = "Continue playing audio in background when exiting player",
                        checked = playbackSettings.backgroundPlayEnabled,
                        onCheckedChange = { settingsViewModel.updateBackgroundPlayEnabled(it) }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            item(key = "label_overlays") { SettingsSectionLabel("Video Player Overlays") }
            item(key = "card_overlays") {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.Timer,
                        title = "Show Elapsed Time Overlay",
                        subtitle = "Display elapsed time overlay on video",
                        checked = playbackSettings.showElapsedTimeOverlay,
                        onCheckedChange = { settingsViewModel.updateShowElapsedTimeOverlay(it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.BatteryFull,
                        title = "Show Battery & Clock Overlay",
                        subtitle = "Display battery and time over video",
                        checked = playbackSettings.showBatteryClockOverlay,
                        onCheckedChange = { settingsViewModel.updateShowBatteryClockOverlay(it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.Info,
                        title = "Show Statistics Overlay",
                        subtitle = "Display real-time FPS, codecs, NosvedMedia info, decoders, and audio/video/subtitle track details",
                        checked = playbackSettings.showStatsOverlay,
                        onCheckedChange = { settingsViewModel.updateShowStatsOverlay(it) }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            item(key = "label_behavior") { SettingsSectionLabel("Playback Behavior") }
            item(key = "card_behavior") {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.ScreenRotationAlt,
                        title = "Show Screen Rotation Button",
                        subtitle = "Show rotation button when device is rotated",
                        checked = playbackSettings.showScreenRotationButton,
                        onCheckedChange = { settingsViewModel.updateShowScreenRotationButton(it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.PauseCircle,
                        title = "Pause when Obstructed",
                        subtitle = "Pause video when screen is obstructed",
                        checked = playbackSettings.pauseWhenObstructed,
                        onCheckedChange = { settingsViewModel.updatePauseWhenObstructed(it) }
                    )
                }
            }

            item(key = "spacer_behavior_lang") { Spacer(Modifier.height(16.dp)) }
            item(key = "label_languages") { SettingsSectionLabel("Default Track Languages") }
            item(key = "card_languages") {
                SettingsCard {
                    SettingsClickRow(
                        icon     = Icons.Default.Headset,
                        title    = "Default Audio Language",
                        subtitle = if (defaultAudioLang.isBlank()) {
                            "None (System Default)"
                        } else {
                            val native = LanguageList.getNativeName(defaultAudioLang)
                            val display = LanguageList.getDisplayName(defaultAudioLang)
                            if (native != null && !native.equals(display, ignoreCase = true)) {
                                "$display ($native) • ${defaultAudioLang.uppercase()}"
                            } else {
                                "$display • ${defaultAudioLang.uppercase()}"
                            }
                        },
                        onClick  = { showAudioPicker = true }
                    )
                    SettingsDivider()
                    SettingsClickRow(
                        icon     = Icons.Default.Subtitles,
                        title    = "Default Subtitle Language",
                        subtitle = if (defaultSubtitleLang.isBlank()) {
                            "None (Off)"
                        } else {
                            val native = LanguageList.getNativeName(defaultSubtitleLang)
                            val display = LanguageList.getDisplayName(defaultSubtitleLang)
                            if (native != null && !native.equals(display, ignoreCase = true)) {
                                "$display ($native) • ${defaultSubtitleLang.uppercase()}"
                            } else {
                                "$display • ${defaultSubtitleLang.uppercase()}"
                            }
                        },
                        onClick  = { showSubtitlePicker = true }
                    )
                }
            }
        }
    }

    activeRadioDialog?.let { data ->
        RadioPickerDialog(
            title = data.title,
            options = data.options,
            selectedOption = data.selectedOption,
            onOptionSelected = {
                data.onOptionSelected(it)
                activeRadioDialog = null
            },
            onDismiss = { activeRadioDialog = null }
        )
    }

    if (showAudioPicker) {
        LanguagePickerSheet(
            title = "Default Audio Language",
            currentCode = defaultAudioLang,
            onSelect = { entry ->
                settingsViewModel.setDefaultAudioLang(entry?.code ?: "")
                showAudioPicker = false
            },
            onDismiss = { showAudioPicker = false }
        )
    }
    if (showSubtitlePicker) {
        LanguagePickerSheet(
            title = "Default Subtitle Language",
            currentCode = defaultSubtitleLang,
            onSelect = { entry ->
                settingsViewModel.setDefaultSubtitleLang(entry?.code ?: "")
                showSubtitlePicker = false
            },
            onDismiss = { showSubtitlePicker = false }
        )
    }
}

@Composable
private fun SettingsSectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsClickRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null,
             tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsRadioItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selectedOption: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = selectedOption.toDisplayLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RadioPickerDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(8.dp))
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = { onOptionSelected(option) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = option.toDisplayLabel(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
