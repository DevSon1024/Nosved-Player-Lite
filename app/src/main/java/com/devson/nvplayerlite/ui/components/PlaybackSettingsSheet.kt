package com.devson.nvplayerlite.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.devson.nvplayerlite.model.PlayerSettingsActions
import com.devson.nvplayerlite.repository.FullScreenMode
import com.devson.nvplayerlite.repository.OrientationMode
import com.devson.nvplayerlite.repository.PlaybackSettings
import com.devson.nvplayerlite.repository.SoftButtonMode
import com.devson.nvplayerlite.viewmodel.ControlIconSize
import com.devson.nvplayerlite.viewmodel.SeekBarStyle
import kotlin.math.roundToInt

private fun String.toDisplayLabel(): String = this
    .replace('_', ' ')
    .lowercase()
    .replaceFirstChar { it.uppercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsSheet(
    showSettingsSheet: Boolean,
    sheetState: SheetState,
    playbackSettings: PlaybackSettings? = null,
    actions: PlayerSettingsActions? = null,
    seekDurationSeconds: Int = 10,
    seekBarStyle: SeekBarStyle = SeekBarStyle.DEFAULT,
    controlIconSize: ControlIconSize = ControlIconSize.MEDIUM,
    autoPlayEnabled: Boolean = false,
    useModernStyle: Boolean = false,
    showSeekButtons: Boolean = true,
    fastplaySpeed: Float = 2.0f,
    isLandscape: Boolean = false,
    onDismissRequest: () -> Unit,
    onSeekDurationChange: (Int) -> Unit = {},
    onSeekBarStyleChange: (SeekBarStyle) -> Unit = {},
    onControlIconSizeChange: (ControlIconSize) -> Unit = {},
    onAutoPlayChange: (Boolean) -> Unit = {},
    onModernStyleChange: (Boolean) -> Unit = {},
    onShowSeekButtonsChange: (Boolean) -> Unit = {},
    onFastplaySpeedChange: (Float) -> Unit = {},
    showStats: Boolean = false,
    onShowStatsChange: (Boolean) -> Unit = {}
) {
    if (!showSettingsSheet) return

    val currentOrientation = playbackSettings?.orientationMode ?: OrientationMode.SYSTEM_DEFAULT
    val currentFullScreen = playbackSettings?.fullScreenMode ?: FullScreenMode.OFF
    val currentSoftButton = playbackSettings?.softButtonMode ?: SoftButtonMode.AUTO_HIDE
    val currentIsBottomLayout = playbackSettings?.isBottomLayoutEnabled ?: false
    val currentControlIconSize = playbackSettings?.controlIconSize ?: when (controlIconSize) {
        ControlIconSize.SMALL -> "small"
        ControlIconSize.LARGE -> "large"
        else -> "medium"
    }
    val currentSeekBarStyle = playbackSettings?.seekBarStyle ?: when (seekBarStyle) {
        SeekBarStyle.FLAT -> "slim"
        else -> "standard"
    }
    val currentAutoPlay = playbackSettings?.autoPlayEnabled ?: autoPlayEnabled
    val currentShowSeekButtons = playbackSettings?.showSeekButtons ?: showSeekButtons
    val currentShowNextPrevButtons = playbackSettings?.showNextPrevButtons ?: true
    val currentShowRemainingTime = playbackSettings?.showRemainingTime ?: false
    val currentShowControlGradients = playbackSettings?.showControlGradients ?: true
    val currentBackgroundPlay = playbackSettings?.backgroundPlayEnabled ?: false

    val currentShowStats = playbackSettings?.showStatsOverlay ?: showStats
    val currentShowElapsedTime = playbackSettings?.showElapsedTimeOverlay ?: false
    val currentShowBatteryClock = playbackSettings?.showBatteryClockOverlay ?: false

    val currentSeekDurationSec = playbackSettings?.doubleTapSeekDuration?.let { (it / 1000).toInt() } ?: seekDurationSeconds
    val currentFastplaySpeed = playbackSettings?.longPressSpeed ?: fastplaySpeed

    val currentShowRotationBtn = playbackSettings?.showScreenRotationButton ?: true
    val currentPauseObstructed = playbackSettings?.pauseWhenObstructed ?: false
    val currentKeepAwake = playbackSettings?.keepAwakeAlways ?: false
    val currentSaveBrightness = playbackSettings?.saveBrightnessLevel ?: true

    // Callbacks
    val onUpdateOrientation: (OrientationMode) -> Unit = { mode ->
        actions?.onUpdateOrientationMode?.invoke(mode)
    }
    val onUpdateFullScreen: (FullScreenMode) -> Unit = { mode ->
        actions?.onUpdateFullScreenMode?.invoke(mode)
    }
    val onUpdateSoftButton: (SoftButtonMode) -> Unit = { mode ->
        actions?.onUpdateSoftButtonMode?.invoke(mode)
    }
    val onUpdateControlPosition: (Boolean) -> Unit = { isBottom ->
        actions?.onUpdateIsBottomLayoutEnabled?.invoke(isBottom)
    }
    val onUpdateIconSize: (String) -> Unit = { sizeStr ->
        actions?.onUpdateControlIconSize?.invoke(sizeStr)
        val sizeEnum = when (sizeStr.lowercase()) {
            "small" -> ControlIconSize.SMALL
            "large" -> ControlIconSize.LARGE
            else -> ControlIconSize.MEDIUM
        }
        onControlIconSizeChange(sizeEnum)
    }
    val onUpdateSeekStyle: (String) -> Unit = { styleStr ->
        actions?.onUpdateSeekBarStyle?.invoke(styleStr)
        val styleEnum = when (styleStr.lowercase()) {
            "flat", "slim" -> SeekBarStyle.FLAT
            else -> SeekBarStyle.DEFAULT
        }
        onSeekBarStyleChange(styleEnum)
    }
    val onUpdateAutoPlay: (Boolean) -> Unit = {
        onAutoPlayChange(it)
        actions?.onUpdateAutoPlayEnabled?.invoke(it)
    }
    val onUpdateSeekButtons: (Boolean) -> Unit = {
        onShowSeekButtonsChange(it)
        actions?.onUpdateShowSeekButtons?.invoke(it)
    }
    val onUpdateNextPrevButtons: (Boolean) -> Unit = {
        actions?.onUpdateShowNextPrevButtons?.invoke(it)
    }
    val onUpdateRemainingTime: (Boolean) -> Unit = {
        actions?.onUpdateShowRemainingTime?.invoke(it)
    }
    val onUpdateControlGradients: (Boolean) -> Unit = {
        actions?.onUpdateShowControlGradients?.invoke(it)
    }
    val onUpdateBackgroundPlay: (Boolean) -> Unit = {
        actions?.onUpdateBackgroundPlayEnabled?.invoke(it)
    }
    val onUpdateStatsOverlay: (Boolean) -> Unit = {
        onShowStatsChange(it)
        actions?.onUpdateShowStatsOverlay?.invoke(it)
    }
    val onUpdateElapsedTimeOverlay: (Boolean) -> Unit = {
        actions?.onUpdateShowElapsedTimeOverlay?.invoke(it)
    }
    val onUpdateBatteryClockOverlay: (Boolean) -> Unit = {
        actions?.onUpdateShowBatteryClockOverlay?.invoke(it)
    }
    val onUpdateModernStyle: (Boolean) -> Unit = {
        onModernStyleChange(it)
    }
    val onUpdateSeekDuration: (Int) -> Unit = {
        onSeekDurationChange(it)
        actions?.onUpdateDoubleTapSeekDuration?.invoke(it * 1000L)
    }
    val onUpdateFastplaySpeed: (Float) -> Unit = {
        onFastplaySpeedChange(it)
        actions?.onUpdateLongPressSpeed?.invoke(it)
        actions?.onUpdateTapAndHoldSpeed?.invoke(it)
    }
    val onUpdateRotationButton: (Boolean) -> Unit = {
        actions?.onUpdateShowScreenRotationButton?.invoke(it)
    }
    val onUpdatePauseObstructed: (Boolean) -> Unit = {
        actions?.onUpdatePauseWhenObstructed?.invoke(it)
    }
    val onUpdateKeepAwake: (Boolean) -> Unit = {
        actions?.onUpdateKeepAwakeAlways?.invoke(it)
    }
    val onUpdateSaveBrightness: (Boolean) -> Unit = {
        actions?.onUpdateSaveBrightnessLevel?.invoke(it)
    }

    if (isLandscape) {
        SideSheet(visible = showSettingsSheet, onDismissRequest = onDismissRequest) {
            PlaybackSettingsContent(
                orientationMode = currentOrientation,
                fullScreenMode = currentFullScreen,
                softButtonMode = currentSoftButton,
                isBottomLayoutEnabled = currentIsBottomLayout,
                controlIconSize = currentControlIconSize,
                seekBarStyle = currentSeekBarStyle,
                autoPlayEnabled = currentAutoPlay,
                showSeekButtons = currentShowSeekButtons,
                showNextPrevButtons = currentShowNextPrevButtons,
                showRemainingTime = currentShowRemainingTime,
                showControlGradients = currentShowControlGradients,
                backgroundPlayEnabled = currentBackgroundPlay,
                showStatsOverlay = currentShowStats,
                showElapsedTimeOverlay = currentShowElapsedTime,
                showBatteryClockOverlay = currentShowBatteryClock,
                useModernStyle = useModernStyle,
                seekDurationSeconds = currentSeekDurationSec,
                fastplaySpeed = currentFastplaySpeed,
                showScreenRotationButton = currentShowRotationBtn,
                pauseWhenObstructed = currentPauseObstructed,
                keepAwakeAlways = currentKeepAwake,
                saveBrightnessLevel = currentSaveBrightness,
                onUpdateOrientation = onUpdateOrientation,
                onUpdateFullScreen = onUpdateFullScreen,
                onUpdateSoftButton = onUpdateSoftButton,
                onUpdateControlPosition = onUpdateControlPosition,
                onUpdateIconSize = onUpdateIconSize,
                onUpdateSeekStyle = onUpdateSeekStyle,
                onUpdateAutoPlay = onUpdateAutoPlay,
                onUpdateSeekButtons = onUpdateSeekButtons,
                onUpdateNextPrevButtons = onUpdateNextPrevButtons,
                onUpdateRemainingTime = onUpdateRemainingTime,
                onUpdateControlGradients = onUpdateControlGradients,
                onUpdateBackgroundPlay = onUpdateBackgroundPlay,
                onUpdateStatsOverlay = onUpdateStatsOverlay,
                onUpdateElapsedTimeOverlay = onUpdateElapsedTimeOverlay,
                onUpdateBatteryClockOverlay = onUpdateBatteryClockOverlay,
                onUpdateModernStyle = onUpdateModernStyle,
                onUpdateSeekDuration = onUpdateSeekDuration,
                onUpdateFastplaySpeed = onUpdateFastplaySpeed,
                onUpdateRotationButton = onUpdateRotationButton,
                onUpdatePauseObstructed = onUpdatePauseObstructed,
                onUpdateKeepAwake = onUpdateKeepAwake,
                onUpdateSaveBrightness = onUpdateSaveBrightness
            )
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            com.devson.nvplayerlite.ui.theme.DialogNavigationBarThemeFix()
            PlaybackSettingsContent(
                orientationMode = currentOrientation,
                fullScreenMode = currentFullScreen,
                softButtonMode = currentSoftButton,
                isBottomLayoutEnabled = currentIsBottomLayout,
                controlIconSize = currentControlIconSize,
                seekBarStyle = currentSeekBarStyle,
                autoPlayEnabled = currentAutoPlay,
                showSeekButtons = currentShowSeekButtons,
                showNextPrevButtons = currentShowNextPrevButtons,
                showRemainingTime = currentShowRemainingTime,
                showControlGradients = currentShowControlGradients,
                backgroundPlayEnabled = currentBackgroundPlay,
                showStatsOverlay = currentShowStats,
                showElapsedTimeOverlay = currentShowElapsedTime,
                showBatteryClockOverlay = currentShowBatteryClock,
                useModernStyle = useModernStyle,
                seekDurationSeconds = currentSeekDurationSec,
                fastplaySpeed = currentFastplaySpeed,
                showScreenRotationButton = currentShowRotationBtn,
                pauseWhenObstructed = currentPauseObstructed,
                keepAwakeAlways = currentKeepAwake,
                saveBrightnessLevel = currentSaveBrightness,
                onUpdateOrientation = onUpdateOrientation,
                onUpdateFullScreen = onUpdateFullScreen,
                onUpdateSoftButton = onUpdateSoftButton,
                onUpdateControlPosition = onUpdateControlPosition,
                onUpdateIconSize = onUpdateIconSize,
                onUpdateSeekStyle = onUpdateSeekStyle,
                onUpdateAutoPlay = onUpdateAutoPlay,
                onUpdateSeekButtons = onUpdateSeekButtons,
                onUpdateNextPrevButtons = onUpdateNextPrevButtons,
                onUpdateRemainingTime = onUpdateRemainingTime,
                onUpdateControlGradients = onUpdateControlGradients,
                onUpdateBackgroundPlay = onUpdateBackgroundPlay,
                onUpdateStatsOverlay = onUpdateStatsOverlay,
                onUpdateElapsedTimeOverlay = onUpdateElapsedTimeOverlay,
                onUpdateBatteryClockOverlay = onUpdateBatteryClockOverlay,
                onUpdateModernStyle = onUpdateModernStyle,
                onUpdateSeekDuration = onUpdateSeekDuration,
                onUpdateFastplaySpeed = onUpdateFastplaySpeed,
                onUpdateRotationButton = onUpdateRotationButton,
                onUpdatePauseObstructed = onUpdatePauseObstructed,
                onUpdateKeepAwake = onUpdateKeepAwake,
                onUpdateSaveBrightness = onUpdateSaveBrightness
            )
        }
    }
}

@Composable
private fun PlaybackSettingsContent(
    orientationMode: OrientationMode,
    fullScreenMode: FullScreenMode,
    softButtonMode: SoftButtonMode,
    isBottomLayoutEnabled: Boolean,
    controlIconSize: String,
    seekBarStyle: String,
    autoPlayEnabled: Boolean,
    showSeekButtons: Boolean,
    showNextPrevButtons: Boolean,
    showRemainingTime: Boolean,
    showControlGradients: Boolean,
    backgroundPlayEnabled: Boolean,
    showStatsOverlay: Boolean,
    showElapsedTimeOverlay: Boolean,
    showBatteryClockOverlay: Boolean,
    useModernStyle: Boolean,
    seekDurationSeconds: Int,
    fastplaySpeed: Float,
    showScreenRotationButton: Boolean,
    pauseWhenObstructed: Boolean,
    keepAwakeAlways: Boolean,
    saveBrightnessLevel: Boolean,
    onUpdateOrientation: (OrientationMode) -> Unit,
    onUpdateFullScreen: (FullScreenMode) -> Unit,
    onUpdateSoftButton: (SoftButtonMode) -> Unit,
    onUpdateControlPosition: (Boolean) -> Unit,
    onUpdateIconSize: (String) -> Unit,
    onUpdateSeekStyle: (String) -> Unit,
    onUpdateAutoPlay: (Boolean) -> Unit,
    onUpdateSeekButtons: (Boolean) -> Unit,
    onUpdateNextPrevButtons: (Boolean) -> Unit,
    onUpdateRemainingTime: (Boolean) -> Unit,
    onUpdateControlGradients: (Boolean) -> Unit,
    onUpdateBackgroundPlay: (Boolean) -> Unit,
    onUpdateStatsOverlay: (Boolean) -> Unit,
    onUpdateElapsedTimeOverlay: (Boolean) -> Unit,
    onUpdateBatteryClockOverlay: (Boolean) -> Unit,
    onUpdateModernStyle: (Boolean) -> Unit,
    onUpdateSeekDuration: (Int) -> Unit,
    onUpdateFastplaySpeed: (Float) -> Unit,
    onUpdateRotationButton: (Boolean) -> Unit,
    onUpdatePauseObstructed: (Boolean) -> Unit,
    onUpdateKeepAwake: (Boolean) -> Unit,
    onUpdateSaveBrightness: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // Module 1: Player Control Settings
        PlayerControlSettingsSection(
            orientationMode = orientationMode,
            fullScreenMode = fullScreenMode,
            softButtonMode = softButtonMode,
            isBottomLayoutEnabled = isBottomLayoutEnabled,
            controlIconSize = controlIconSize,
            seekBarStyle = seekBarStyle,
            autoPlayEnabled = autoPlayEnabled,
            showSeekButtons = showSeekButtons,
            showNextPrevButtons = showNextPrevButtons,
            showRemainingTime = showRemainingTime,
            showControlGradients = showControlGradients,
            backgroundPlayEnabled = backgroundPlayEnabled,
            onUpdateOrientation = onUpdateOrientation,
            onUpdateFullScreen = onUpdateFullScreen,
            onUpdateSoftButton = onUpdateSoftButton,
            onUpdateControlPosition = onUpdateControlPosition,
            onUpdateIconSize = onUpdateIconSize,
            onUpdateSeekStyle = onUpdateSeekStyle,
            onUpdateAutoPlay = onUpdateAutoPlay,
            onUpdateSeekButtons = onUpdateSeekButtons,
            onUpdateNextPrevButtons = onUpdateNextPrevButtons,
            onUpdateRemainingTime = onUpdateRemainingTime,
            onUpdateControlGradients = onUpdateControlGradients,
            onUpdateBackgroundPlay = onUpdateBackgroundPlay
        )

        // Module 2: Player Overlays & Appearance
        PlayerOverlaysSection(
            showStatsOverlay = showStatsOverlay,
            showElapsedTimeOverlay = showElapsedTimeOverlay,
            showBatteryClockOverlay = showBatteryClockOverlay,
            showRemainingTime = showRemainingTime,
            showControlGradients = showControlGradients,
            useModernStyle = useModernStyle,
            onUpdateStatsOverlay = onUpdateStatsOverlay,
            onUpdateElapsedTimeOverlay = onUpdateElapsedTimeOverlay,
            onUpdateBatteryClockOverlay = onUpdateBatteryClockOverlay,
            onUpdateRemainingTime = onUpdateRemainingTime,
            onUpdateControlGradients = onUpdateControlGradients,
            onUpdateModernStyle = onUpdateModernStyle
        )

        // Module 3: Seek & Speed Settings
        SeekSpeedSettingsSection(
            seekDurationSeconds = seekDurationSeconds,
            fastplaySpeed = fastplaySpeed,
            seekBarStyle = seekBarStyle,
            showSeekButtons = showSeekButtons,
            onUpdateSeekDuration = onUpdateSeekDuration,
            onUpdateFastplaySpeed = onUpdateFastplaySpeed,
            onUpdateSeekStyle = onUpdateSeekStyle,
            onUpdateSeekButtons = onUpdateSeekButtons
        )

        // Module 4: Playback Behavior & Automation
        PlaybackBehaviorSection(
            showScreenRotationButton = showScreenRotationButton,
            pauseWhenObstructed = pauseWhenObstructed,
            keepAwakeAlways = keepAwakeAlways,
            saveBrightnessLevel = saveBrightnessLevel,
            onUpdateRotationButton = onUpdateRotationButton,
            onUpdatePauseObstructed = onUpdatePauseObstructed,
            onUpdateKeepAwake = onUpdateKeepAwake,
            onUpdateSaveBrightness = onUpdateSaveBrightness
        )
    }
}

// Module 1: Player Control Settings Section
@Composable
private fun PlayerControlSettingsSection(
    orientationMode: OrientationMode,
    fullScreenMode: FullScreenMode,
    softButtonMode: SoftButtonMode,
    isBottomLayoutEnabled: Boolean,
    controlIconSize: String,
    seekBarStyle: String,
    autoPlayEnabled: Boolean,
    showSeekButtons: Boolean,
    showNextPrevButtons: Boolean,
    showRemainingTime: Boolean,
    showControlGradients: Boolean,
    backgroundPlayEnabled: Boolean,
    onUpdateOrientation: (OrientationMode) -> Unit,
    onUpdateFullScreen: (FullScreenMode) -> Unit,
    onUpdateSoftButton: (SoftButtonMode) -> Unit,
    onUpdateControlPosition: (Boolean) -> Unit,
    onUpdateIconSize: (String) -> Unit,
    onUpdateSeekStyle: (String) -> Unit,
    onUpdateAutoPlay: (Boolean) -> Unit,
    onUpdateSeekButtons: (Boolean) -> Unit,
    onUpdateNextPrevButtons: (Boolean) -> Unit,
    onUpdateRemainingTime: (Boolean) -> Unit,
    onUpdateControlGradients: (Boolean) -> Unit,
    onUpdateBackgroundPlay: (Boolean) -> Unit
) {
    var showOrientationDialog by remember { mutableStateOf(false) }
    var showFullScreenDialog by remember { mutableStateOf(false) }
    var showSoftButtonDialog by remember { mutableStateOf(false) }
    var showPositionDialog by remember { mutableStateOf(false) }
    var showIconSizeDialog by remember { mutableStateOf(false) }
    var showSeekStyleDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SheetSectionHeader("Player Control Settings")
        SheetCardContainer {
            SheetSelectItem(
                icon = Icons.Default.ScreenRotation,
                title = "Orientation Mode",
                subtitle = orientationMode.name.toDisplayLabel(),
                onClick = { showOrientationDialog = true }
            )
            SheetItemDivider()
            SheetSelectItem(
                icon = Icons.Default.Fullscreen,
                title = "Full Screen Mode",
                subtitle = fullScreenMode.name.toDisplayLabel(),
                onClick = { showFullScreenDialog = true }
            )
            SheetItemDivider()
            SheetSelectItem(
                icon = Icons.Default.SmartButton,
                title = "Soft Navigation Buttons",
                subtitle = softButtonMode.name.toDisplayLabel(),
                onClick = { showSoftButtonDialog = true }
            )
            SheetItemDivider()
            SheetSelectItem(
                icon = Icons.Default.PlayCircle,
                title = "Playback Control Position",
                subtitle = if (isBottomLayoutEnabled) "Below Seekbar" else "Center",
                onClick = { showPositionDialog = true }
            )
            SheetItemDivider()
            SheetSelectItem(
                icon = Icons.Default.PhotoSizeSelectLarge,
                title = "Controls Icon Size",
                subtitle = controlIconSize.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { showIconSizeDialog = true }
            )
            SheetItemDivider()
            SheetSelectItem(
                icon = Icons.Default.Waves,
                title = "Seekbar Style",
                subtitle = seekBarStyle.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { showSeekStyleDialog = true }
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.SkipNext,
                title = "Auto-play Next Video",
                subtitle = "Automatically load and play next video in folder",
                checked = autoPlayEnabled,
                onCheckedChange = onUpdateAutoPlay
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.FastForward,
                title = "Show Seek Buttons",
                subtitle = "Show fast forward and rewind seek buttons",
                checked = showSeekButtons,
                onCheckedChange = onUpdateSeekButtons
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.SkipNext,
                title = "Show Skip Prev/Next Buttons",
                subtitle = "Show previous/next video navigation buttons",
                checked = showNextPrevButtons,
                onCheckedChange = onUpdateNextPrevButtons
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.HourglassBottom,
                title = "Show Remaining Time",
                subtitle = "Display remaining time instead of total duration",
                checked = showRemainingTime,
                onCheckedChange = onUpdateRemainingTime
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.Gradient,
                title = "Controls Backdrop Dimming",
                subtitle = "Show top and bottom black fade behind player controls",
                checked = showControlGradients,
                onCheckedChange = onUpdateControlGradients
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.Headphones,
                title = "Background Playback",
                subtitle = "Continue playing audio in background when exiting player",
                checked = backgroundPlayEnabled,
                onCheckedChange = onUpdateBackgroundPlay
            )
        }
    }

    if (showOrientationDialog) {
        val options = OrientationMode.entries.toList()
        SheetOptionDialog(
            title = "Orientation Mode",
            options = options.map { it.name },
            selectedOption = orientationMode.name,
            onOptionSelected = {
                val selected = options.firstOrNull { opt -> opt.name == it } ?: OrientationMode.SYSTEM_DEFAULT
                onUpdateOrientation(selected)
                showOrientationDialog = false
            },
            onDismiss = { showOrientationDialog = false }
        )
    }

    if (showFullScreenDialog) {
        val options = FullScreenMode.entries.toList()
        SheetOptionDialog(
            title = "Full Screen Mode",
            options = options.map { it.name },
            selectedOption = fullScreenMode.name,
            onOptionSelected = {
                val selected = options.firstOrNull { opt -> opt.name == it } ?: FullScreenMode.OFF
                onUpdateFullScreen(selected)
                showFullScreenDialog = false
            },
            onDismiss = { showFullScreenDialog = false }
        )
    }

    if (showSoftButtonDialog) {
        val options = SoftButtonMode.entries.toList()
        SheetOptionDialog(
            title = "Soft Navigation Buttons",
            options = options.map { it.name },
            selectedOption = softButtonMode.name,
            onOptionSelected = {
                val selected = options.firstOrNull { opt -> opt.name == it } ?: SoftButtonMode.AUTO_HIDE
                onUpdateSoftButton(selected)
                showSoftButtonDialog = false
            },
            onDismiss = { showSoftButtonDialog = false }
        )
    }

    if (showPositionDialog) {
        val options = listOf("Center", "Below Seekbar")
        SheetOptionDialog(
            title = "Playback Control Position",
            options = options,
            selectedOption = if (isBottomLayoutEnabled) "Below Seekbar" else "Center",
            onOptionSelected = {
                onUpdateControlPosition(it == "Below Seekbar")
                showPositionDialog = false
            },
            onDismiss = { showPositionDialog = false }
        )
    }

    if (showIconSizeDialog) {
        val options = listOf("Small", "Medium", "Large")
        SheetOptionDialog(
            title = "Controls Icon Size",
            options = options,
            selectedOption = controlIconSize.lowercase().replaceFirstChar { it.uppercase() },
            onOptionSelected = {
                onUpdateIconSize(it.lowercase())
                showIconSizeDialog = false
            },
            onDismiss = { showIconSizeDialog = false }
        )
    }

    if (showSeekStyleDialog) {
        val options = listOf("Standard", "Thick", "Slim", "Wavy")
        SheetOptionDialog(
            title = "Seekbar Style",
            options = options,
            selectedOption = seekBarStyle.lowercase().replaceFirstChar { it.uppercase() },
            onOptionSelected = {
                onUpdateSeekStyle(it.lowercase())
                showSeekStyleDialog = false
            },
            onDismiss = { showSeekStyleDialog = false }
        )
    }
}

// Module 2: Player Overlays & Appearance Section
@Composable
private fun PlayerOverlaysSection(
    showStatsOverlay: Boolean,
    showElapsedTimeOverlay: Boolean,
    showBatteryClockOverlay: Boolean,
    showRemainingTime: Boolean,
    showControlGradients: Boolean,
    useModernStyle: Boolean,
    onUpdateStatsOverlay: (Boolean) -> Unit,
    onUpdateElapsedTimeOverlay: (Boolean) -> Unit,
    onUpdateBatteryClockOverlay: (Boolean) -> Unit,
    onUpdateRemainingTime: (Boolean) -> Unit,
    onUpdateControlGradients: (Boolean) -> Unit,
    onUpdateModernStyle: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SheetSectionHeader("Player Overlays & Appearance")
        SheetCardContainer {
            SheetToggleItem(
                icon = Icons.Default.Info,
                title = "Show Statistics Overlay",
                subtitle = "Display real-time FPS, codecs, NosvedMedia info, decoders, and audio/video/subtitle track details",
                checked = showStatsOverlay,
                onCheckedChange = onUpdateStatsOverlay
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.Timer,
                title = "Show Elapsed Time Overlay",
                subtitle = "Display elapsed time overlay on video",
                checked = showElapsedTimeOverlay,
                onCheckedChange = onUpdateElapsedTimeOverlay
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.BatteryFull,
                title = "Show Battery & Clock Overlay",
                subtitle = "Display battery level and clock over video",
                checked = showBatteryClockOverlay,
                onCheckedChange = onUpdateBatteryClockOverlay
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.HourglassBottom,
                title = "Show Remaining Time",
                subtitle = "Display remaining time countdown instead of total duration",
                checked = showRemainingTime,
                onCheckedChange = onUpdateRemainingTime
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.Gradient,
                title = "Controls Backdrop Dimming",
                subtitle = "Show top and bottom black fade behind player controls",
                checked = showControlGradients,
                onCheckedChange = onUpdateControlGradients
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.Palette,
                title = "Use Modern Controls Style",
                subtitle = "Enable modern capsule floating controls aesthetic",
                checked = useModernStyle,
                onCheckedChange = onUpdateModernStyle
            )
        }
    }
}

// Module 3: Seek & Speed Settings Section
@Composable
private fun SeekSpeedSettingsSection(
    seekDurationSeconds: Int,
    fastplaySpeed: Float,
    seekBarStyle: String,
    showSeekButtons: Boolean,
    onUpdateSeekDuration: (Int) -> Unit,
    onUpdateFastplaySpeed: (Float) -> Unit,
    onUpdateSeekStyle: (String) -> Unit,
    onUpdateSeekButtons: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var seekText by remember(seekDurationSeconds) { mutableStateOf(seekDurationSeconds.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SheetSectionHeader("Seek & Speed Settings")
        SheetCardContainer {
            // Seek duration presets + custom input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Seek Duration",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 30).forEach { secs ->
                        ChipButton(
                            label = "${secs}s",
                            selected = seekDurationSeconds == secs,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                seekText = secs.toString()
                                onUpdateSeekDuration(secs)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Custom Duration",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = seekText,
                        onValueChange = { v ->
                            if (v.length <= 3 && v.all { it.isDigit() }) seekText = v
                        },
                        modifier = Modifier.width(110.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            val parsed = seekText.toIntOrNull()?.coerceIn(1, 999)
                            if (parsed != null) {
                                seekText = parsed.toString()
                                onUpdateSeekDuration(parsed)
                            }
                            focusManager.clearFocus()
                        }),
                        suffix = { Text("s", fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            SheetItemDivider()

            // Tap & Hold Speed Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tap & Hold Fast-Forward Speed",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Speed multiplier when long-pressing playback",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val rounded = (fastplaySpeed * 10).roundToInt() / 10f
                    Text(
                        text = "%.1f×".format(rounded),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = fastplaySpeed.coerceIn(1.0f, 4.0f),
                    onValueChange = { raw ->
                        val snapped = ((raw * 10).roundToInt() / 10f).coerceIn(1.0f, 4.0f)
                        onUpdateFastplaySpeed(snapped)
                    },
                    valueRange = 1.0f..4.0f,
                    steps = 29,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("1.0×", "2.0×", "3.0×", "4.0×").forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SheetItemDivider()

            SheetToggleItem(
                icon = Icons.Default.FastForward,
                title = "Show Jump Seek Buttons",
                subtitle = "Show forward and rewind buttons in controls dock",
                checked = showSeekButtons,
                onCheckedChange = onUpdateSeekButtons
            )
        }
    }
}

// Module 4: Playback Behavior & Automation Section
@Composable
private fun PlaybackBehaviorSection(
    showScreenRotationButton: Boolean,
    pauseWhenObstructed: Boolean,
    keepAwakeAlways: Boolean,
    saveBrightnessLevel: Boolean,
    onUpdateRotationButton: (Boolean) -> Unit,
    onUpdatePauseObstructed: (Boolean) -> Unit,
    onUpdateKeepAwake: (Boolean) -> Unit,
    onUpdateSaveBrightness: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SheetSectionHeader("Playback Behavior & Automation")
        SheetCardContainer {
            SheetToggleItem(
                icon = Icons.Default.ScreenRotationAlt,
                title = "Show Screen Rotation Button",
                subtitle = "Show rotation shortcut button when orientation changes",
                checked = showScreenRotationButton,
                onCheckedChange = onUpdateRotationButton
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.PauseCircle,
                title = "Pause when Obstructed",
                subtitle = "Pause video playback when proximity sensor detects obstruction",
                checked = pauseWhenObstructed,
                onCheckedChange = onUpdatePauseObstructed
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.WbSunny,
                title = "Keep Screen Awake Always",
                subtitle = "Prevent screen timeout even when paused",
                checked = keepAwakeAlways,
                onCheckedChange = onUpdateKeepAwake
            )
            SheetItemDivider()
            SheetToggleItem(
                icon = Icons.Default.LightMode,
                title = "Save Custom Brightness",
                subtitle = "Restore player brightness level on subsequent launches",
                checked = saveBrightnessLevel,
                onCheckedChange = onUpdateSaveBrightness
            )
        }
    }
}

// Reusable UI components
@Composable
private fun SheetSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SheetCardContainer(content: @Composable ColumnScope.() -> Unit) {
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
private fun SheetToggleItem(
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
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SheetSelectItem(
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
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SheetItemDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

@Composable
private fun SheetOptionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    items(options) { opt ->
                        val isSelected = opt.equals(selectedOption, ignoreCase = true)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(opt) }
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onOptionSelected(opt) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = opt.toDisplayLabel(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 10.dp)
    )
    content()
}

@Composable
fun ChipButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val textColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    val borderColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
