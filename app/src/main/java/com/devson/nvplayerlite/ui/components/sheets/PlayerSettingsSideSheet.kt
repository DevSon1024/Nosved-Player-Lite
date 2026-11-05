package com.devson.nvplayerlite.ui.components.sheets

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devson.nvplayerlite.model.AspectMode
import com.devson.nvplayerlite.model.PlayerSettingsActions
import com.devson.nvplayerlite.model.Video
import com.devson.nvplayerlite.repository.DoubleTapAction
import com.devson.nvplayerlite.repository.FullScreenMode
import com.devson.nvplayerlite.repository.MultiFingerAction
import com.devson.nvplayerlite.repository.OrientationMode
import com.devson.nvplayerlite.repository.PlaybackSettings
import com.devson.nvplayerlite.repository.SoftButtonMode
import com.devson.nvplayerlite.util.formatDate
import com.devson.nvplayerlite.util.formatDuration
import com.devson.nvplayerlite.util.formatSize
import com.devson.nvplayerlite.util.repeatingClickable
import com.devson.nvplayerlite.util.roundToTwoDecimals
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SPEED_STEP = 0.05f

@Composable
fun PlayerSettingsSideSheet(
    visible: Boolean,
    currentSpeed: Float,
    playbackSettings: PlaybackSettings,
    currentVideo: Video? = null,
    actions: PlayerSettingsActions = PlayerSettingsActions(),
    onDismiss: () -> Unit
) {
    val onSpeedSelected = actions.onSpeedSelected
    val onUpdateDoubleTapAction = actions.onUpdateDoubleTapAction
    val onUpdateDoubleTapSeekDuration = actions.onUpdateDoubleTapSeekDuration
    val onUpdateLongPressEnabled = actions.onUpdateLongPressEnabled
    val onUpdateTapAndHoldSpeed = actions.onUpdateTapAndHoldSpeed
    val onUpdateLongPressSpeed = actions.onUpdateLongPressSpeed
    val onUpdateOrientationMode = actions.onUpdateOrientationMode
    val onUpdateFullScreenMode = actions.onUpdateFullScreenMode
    val onUpdateAspectMode = actions.onUpdateAspectMode
    val onUpdateTwoFingerAction = actions.onUpdateTwoFingerAction
    val onUpdateThreeFingerAction = actions.onUpdateThreeFingerAction
    val onUpdateSoftButtonMode = actions.onUpdateSoftButtonMode
    val onUpdateControlIconSize = actions.onUpdateControlIconSize
    val onUpdateSeekBarStyle = actions.onUpdateSeekBarStyle
    val onUpdateAutoPlayEnabled = actions.onUpdateAutoPlayEnabled
    val onUpdateShowSeekButtons = actions.onUpdateShowSeekButtons
    val onUpdateShowNextPrevButtons = actions.onUpdateShowNextPrevButtons
    val onUpdateShowRemainingTime = actions.onUpdateShowRemainingTime
    val onUpdateShowBatteryClockOverlay = actions.onUpdateShowBatteryClockOverlay
    val onUpdatePauseWhenObstructed = actions.onUpdatePauseWhenObstructed
    val onUpdateKeepAwakeAlways = actions.onUpdateKeepAwakeAlways
    val onUpdateIsBottomLayoutEnabled = actions.onUpdateIsBottomLayoutEnabled
    val onUpdateShowControlGradients = actions.onUpdateShowControlGradients
    val onUpdateShowUpNextQueue = actions.onUpdateShowUpNextQueue
    val onUpdateIsAmbientModeEnabled = actions.onUpdateIsAmbientModeEnabled
    val onUpdateAmbientBlurStyle = actions.onUpdateAmbientBlurStyle
    val onUpdateSaveBrightnessLevel = actions.onUpdateSaveBrightnessLevel
    val onUpdateBackgroundPlayEnabled = actions.onUpdateBackgroundPlayEnabled
    val onUpdateShowStatsOverlay = actions.onUpdateShowStatsOverlay
    val onUpdateShowElapsedTimeOverlay = actions.onUpdateShowElapsedTimeOverlay
    val onUpdateShowScreenRotationButton = actions.onUpdateShowScreenRotationButton

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val sheetWidthPercent = if (isLandscape) 0.5f else 1.0f

    var activeTab by remember { mutableIntStateOf(0) }

    // Dialog trigger states
    var showDoubleTapDialog by remember { mutableStateOf(false) }
    var showSeekDurationDialog by remember { mutableStateOf(false) }
    var showTwoFingerDialog by remember { mutableStateOf(false) }
    var showThreeFingerDialog by remember { mutableStateOf(false) }
    var showOrientationDialog by remember { mutableStateOf(false) }
    var showScalingDialog by remember { mutableStateOf(false) }
    var showSoftButtonDialog by remember { mutableStateOf(false) }
    var showPlaybackControlPositionDialog by remember { mutableStateOf(false) }
    var showIconSizeDialog by remember { mutableStateOf(false) }
    var showSeekBarStyleDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = visible) {
        onDismiss()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter
    ) {
        // Scrim
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(250)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        // Sheet Content
        val enterAnim = if (isLandscape) {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
            )
        } else {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
            )
        }

        val exitAnim = if (isLandscape) {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300, easing = FastOutLinearInEasing)
            )
        } else {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutLinearInEasing)
            )
        }

        val sheetShape = if (isLandscape) {
            RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
        } else {
            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        }

        AnimatedVisibility(
            visible = visible,
            enter = enterAnim,
            exit = exitAnim,
            modifier = if (isLandscape) {
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(sheetWidthPercent)
            } else {
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Bottom)
            }
        ) {
            Surface(
                modifier = if (isLandscape) {
                    Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            shape = sheetShape
                        )
                } else {
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(max = (configuration.screenHeightDp * 0.9f).dp)
                        .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            shape = sheetShape
                        )
                },
                shape = sheetShape,
                color = MaterialTheme.colorScheme.background.copy(alpha = if (isLandscape) 0.92f else 0.95f),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = if (isLandscape) {
                        Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .statusBarsPadding()
                    }
                ) {
                    if (!isLandscape) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            )
                        }
                    }

                    if (isLandscape) {
                        Spacer(Modifier.height(12.dp))
                    } else {
                        Spacer(Modifier.height(4.dp))
                    }

                    // Tab selector row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            Icons.Rounded.Speed,
                            Icons.Rounded.TouchApp,
                            Icons.Rounded.Tune,
                            Icons.Rounded.Layers,
                            Icons.Rounded.AutoMode,
                            Icons.Rounded.Info
                        )
                        tabs.forEachIndexed { index, icon ->
                            val isSelected = activeTab == index
                            val bgSelectedColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            }
                            val contentSelectedColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgSelectedColor)
                                    .clickable { activeTab = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Tab $index",
                                    tint = contentSelectedColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Active Tab body
                    Column(
                        modifier = Modifier
                            .then(
                                if (isLandscape) Modifier.weight(1f)
                                else Modifier.weight(1f, fill = false)
                            )
                            .padding(horizontal = 24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        when (activeTab) {
                            0 -> SpeedTab(
                                currentSpeed = currentSpeed,
                                onSpeedSelected = onSpeedSelected
                            )
                            1 -> GesturesTab(
                                playbackSettings = playbackSettings,
                                onUpdateDoubleTapAction = onUpdateDoubleTapAction,
                                onUpdateDoubleTapSeekDuration = onUpdateDoubleTapSeekDuration,
                                onUpdateLongPressEnabled = onUpdateLongPressEnabled,
                                onUpdateTapAndHoldSpeed = onUpdateTapAndHoldSpeed,
                                onUpdateLongPressSpeed = onUpdateLongPressSpeed,
                                onShowDoubleTapDialog = { showDoubleTapDialog = true },
                                onShowSeekDurationDialog = { showSeekDurationDialog = true },
                                onShowTwoFingerDialog = { showTwoFingerDialog = true },
                                onShowThreeFingerDialog = { showThreeFingerDialog = true }
                            )
                            2 -> InterfaceTab(
                                playbackSettings = playbackSettings,
                                onUpdateAutoPlayEnabled = onUpdateAutoPlayEnabled,
                                onUpdateShowSeekButtons = onUpdateShowSeekButtons,
                                onUpdateShowNextPrevButtons = onUpdateShowNextPrevButtons,
                                onUpdateIsBottomLayoutEnabled = onUpdateIsBottomLayoutEnabled,
                                onUpdateShowControlGradients = onUpdateShowControlGradients,
                                onUpdateShowUpNextQueue = onUpdateShowUpNextQueue,
                                onUpdateBackgroundPlayEnabled = onUpdateBackgroundPlayEnabled,
                                onShowOrientationDialog = { showOrientationDialog = true },
                                onShowScalingDialog = { showScalingDialog = true },
                                onShowSoftButtonDialog = { showSoftButtonDialog = true },
                                onShowPlaybackControlPositionDialog = { showPlaybackControlPositionDialog = true },
                                onShowIconSizeDialog = { showIconSizeDialog = true },
                                onShowSeekBarStyleDialog = { showSeekBarStyleDialog = true }
                            )
                            3 -> OverlaysTab(
                                playbackSettings = playbackSettings,
                                onUpdateShowStatsOverlay = onUpdateShowStatsOverlay,
                                onUpdateShowElapsedTimeOverlay = onUpdateShowElapsedTimeOverlay,
                                onUpdateShowRemainingTime = onUpdateShowRemainingTime,
                                onUpdateShowBatteryClockOverlay = onUpdateShowBatteryClockOverlay,
                                onUpdateShowControlGradients = onUpdateShowControlGradients,
                                onUpdateIsAmbientModeEnabled = onUpdateIsAmbientModeEnabled,
                                onUpdateAmbientBlurStyle = onUpdateAmbientBlurStyle
                            )
                            4 -> AutomationTab(
                                playbackSettings = playbackSettings,
                                onUpdatePauseWhenObstructed = onUpdatePauseWhenObstructed,
                                onUpdateKeepAwakeAlways = onUpdateKeepAwakeAlways,
                                onUpdateSaveBrightnessLevel = onUpdateSaveBrightnessLevel,
                                onUpdateShowScreenRotationButton = onUpdateShowScreenRotationButton
                            )
                            5 -> InformationTab(
                                video = currentVideo
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Dialog: Double Tap Action
    if (showDoubleTapDialog) {
        val actionsList = remember { DoubleTapAction.entries.toList() }
        PremiumSelectionDialog(
            title = "Double Tap Action",
            onDismiss = { showDoubleTapDialog = false },
            options = actionsList,
            selectedOption = playbackSettings.doubleTapAction,
            onOptionSelected = {
                onUpdateDoubleTapAction(it)
                showDoubleTapDialog = false
            },
            optionLabel = { it.displayName },
            optionDescription = {
                when (it) {
                    DoubleTapAction.BOTH -> "Double tap edges to seek, center to play/pause"
                    DoubleTapAction.PLAY_PAUSE -> "Double tap anywhere on player to play/pause"
                    DoubleTapAction.FAST_FORWARD_REWIND -> "Double tap edges to fast forward / rewind"
                    DoubleTapAction.NONE -> "Disable double tap gesture shortcuts"
                }
            }
        )
    }

    // Dialog: Double Tap Seek Duration
    if (showSeekDurationDialog) {
        val durations = remember { listOf(5000L, 10000L, 15000L, 20000L, 30000L, 60000L) }
        PremiumSelectionDialog(
            title = "Double Tap Seek Duration",
            onDismiss = { showSeekDurationDialog = false },
            onReset = { onUpdateDoubleTapSeekDuration(10000L) },
            options = durations,
            selectedOption = playbackSettings.doubleTapSeekDuration,
            onOptionSelected = {
                onUpdateDoubleTapSeekDuration(it)
                showSeekDurationDialog = false
            },
            optionLabel = { "${it / 1000} seconds" },
            optionDescription = { "Jump forward or backward by ${it / 1000} seconds" }
        )
    }

    // Dialog: Two Finger Action
    if (showTwoFingerDialog) {
        val fingerActions = remember { MultiFingerAction.entries.toList() }
        PremiumSelectionDialog(
            title = "Two Finger Tap Action",
            onDismiss = { showTwoFingerDialog = false },
            options = fingerActions,
            selectedOption = playbackSettings.twoFingerAction,
            onOptionSelected = {
                onUpdateTwoFingerAction(it)
                showTwoFingerDialog = false
            },
            optionLabel = { it.displayName },
            optionDescription = {
                when (it) {
                    MultiFingerAction.PLAY_PAUSE -> "Play or pause video playback"
                    MultiFingerAction.FAST_PLAY -> "Lock video at temporary fast play rate"
                    MultiFingerAction.NONE -> "No operation"
                }
            }
        )
    }

    // Dialog: Three Finger Action
    if (showThreeFingerDialog) {
        val fingerActions = remember { MultiFingerAction.entries.toList() }
        PremiumSelectionDialog(
            title = "Three Finger Tap Action",
            onDismiss = { showThreeFingerDialog = false },
            options = fingerActions,
            selectedOption = playbackSettings.threeFingerAction,
            onOptionSelected = {
                onUpdateThreeFingerAction(it)
                showThreeFingerDialog = false
            },
            optionLabel = { it.displayName },
            optionDescription = {
                when (it) {
                    MultiFingerAction.PLAY_PAUSE -> "Play or pause video playback"
                    MultiFingerAction.FAST_PLAY -> "Lock video at temporary fast play rate"
                    MultiFingerAction.NONE -> "No operation"
                }
            }
        )
    }

    // Dialog: Screen Orientation
    if (showOrientationDialog) {
        val orientationOptions = remember { OrientationMode.entries.toList() }
        PremiumSelectionDialog(
            title = "Screen Orientation",
            onDismiss = { showOrientationDialog = false },
            options = orientationOptions,
            selectedOption = playbackSettings.orientationMode,
            onOptionSelected = {
                onUpdateOrientationMode(it)
                showOrientationDialog = false
            },
            optionLabel = {
                when (it) {
                    OrientationMode.SYSTEM_DEFAULT -> "System Default"
                    OrientationMode.VIDEO_ORIENTATION -> "Video Orientation"
                    OrientationMode.LANDSCAPE -> "Landscape"
                    OrientationMode.REVERSE_LANDSCAPE -> "Reverse Landscape"
                    OrientationMode.AUTO_ROTATION -> "Auto-Rotation"
                }
            },
            optionDescription = {
                when (it) {
                    OrientationMode.SYSTEM_DEFAULT -> "Follow device auto-rotate settings"
                    OrientationMode.VIDEO_ORIENTATION -> "Orient screen according to video aspect ratio"
                    OrientationMode.LANDSCAPE -> "Force landscape mode regardless of system"
                    OrientationMode.REVERSE_LANDSCAPE -> "Lock in reverse landscape orientation"
                    OrientationMode.AUTO_ROTATION -> "Rotate based on hardware sensor readings"
                }
            }
        )
    }

    // Dialog: Scaling / Aspect Mode
    if (showScalingDialog) {
        val aspectOptions = remember { AspectMode.entries.toList() }
        PremiumSelectionDialog(
            title = "Video Sizing & Aspect Ratio",
            onDismiss = { showScalingDialog = false },
            options = aspectOptions,
            selectedOption = playbackSettings.aspectMode,
            onOptionSelected = {
                onUpdateAspectMode(it)
                showScalingDialog = false
            },
            optionLabel = { it.displayName },
            optionDescription = {
                when (it) {
                    AspectMode.FIT -> "Letterbox with borders, preserving full picture"
                    AspectMode.STRETCH -> "Stretch video to completely fill the screen"
                    AspectMode.CROP -> "Zoom and crop edges to fill entire screen"
                    AspectMode.ORIGINAL -> "Display at 100% 1:1 original pixel scale"
                }
            }
        )
    }

    // Dialog: System Navigation Buttons (SoftButtonMode)
    if (showSoftButtonDialog) {
        val softButtonOptions = remember { SoftButtonMode.entries.toList() }
        PremiumSelectionDialog(
            title = "System Navigation Bars",
            onDismiss = { showSoftButtonDialog = false },
            options = softButtonOptions,
            selectedOption = playbackSettings.softButtonMode,
            onOptionSelected = {
                onUpdateSoftButtonMode(it)
                showSoftButtonDialog = false
            },
            optionLabel = {
                when (it) {
                    SoftButtonMode.AUTO_HIDE -> "Auto-Hide"
                    SoftButtonMode.SHOW -> "Always Show"
                    SoftButtonMode.HIDE -> "Always Hide (Immersive)"
                }
            },
            optionDescription = {
                when (it) {
                    SoftButtonMode.AUTO_HIDE -> "System bars show and hide along with playback controls"
                    SoftButtonMode.SHOW -> "Keep Android system navigation bars always visible"
                    SoftButtonMode.HIDE -> "Completely hide system bars for maximum immersion"
                }
            }
        )
    }

    // Dialog: Control Position (Center vs Below Seekbar)
    if (showPlaybackControlPositionDialog) {
        val positions = remember { listOf(false, true) }
        PremiumSelectionDialog(
            title = "Playback Controls Position",
            onDismiss = { showPlaybackControlPositionDialog = false },
            options = positions,
            selectedOption = playbackSettings.isBottomLayoutEnabled,
            onOptionSelected = {
                onUpdateIsBottomLayoutEnabled(it)
                showPlaybackControlPositionDialog = false
            },
            optionLabel = { if (it) "Below Seekbar" else "Center" },
            optionDescription = {
                if (it) "Floating bottom dock positioned neatly above the seekbar"
                else "Large floating playback controls placed in the center of the video"
            }
        )
    }

    // Dialog: Control Icon Size
    if (showIconSizeDialog) {
        val sizeOptions = remember { listOf("small", "medium", "large") }
        PremiumSelectionDialog(
            title = "Controls Icon Size",
            onDismiss = { showIconSizeDialog = false },
            options = sizeOptions,
            selectedOption = playbackSettings.controlIconSize.lowercase(),
            onOptionSelected = {
                onUpdateControlIconSize(it)
                showIconSizeDialog = false
            },
            optionLabel = {
                when (it) {
                    "small" -> "Small (Compact)"
                    "large" -> "Large (Spacious)"
                    else -> "Medium (Standard)"
                }
            },
            optionDescription = {
                when (it) {
                    "small" -> "Reduced icon dimensions for minimal unobtrusive look"
                    "large" -> "Enlarged icons with wide comfortable touch targets"
                    else -> "Balanced size for standard phones and tablets"
                }
            }
        )
    }

    // Dialog: Seekbar Style
    if (showSeekBarStyleDialog) {
        val styleOptions = remember { listOf("standard", "thick", "slim", "wavy") }
        PremiumSelectionDialog(
            title = "Seekbar Visual Style",
            onDismiss = { showSeekBarStyleDialog = false },
            options = styleOptions,
            selectedOption = playbackSettings.seekBarStyle.lowercase(),
            onOptionSelected = {
                onUpdateSeekBarStyle(it)
                showSeekBarStyleDialog = false
            },
            optionLabel = {
                when (it) {
                    "thick" -> "Thick Bar"
                    "slim" -> "Slim Line"
                    "wavy" -> "Wavy (Expressive)"
                    else -> "Standard"
                }
            },
            optionDescription = {
                when (it) {
                    "thick" -> "Enhanced bold scrubber track for easy visibility"
                    "slim" -> "Subtle thin hairline scrubber with small thumb"
                    "wavy" -> "Animated harmonic squiggly wave scrubber"
                    else -> "Clean classic progress bar with glowing pill thumb"
                }
            }
        )
    }
}


// TAB 0: SPEED

@Composable
private fun SpeedTab(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(currentSpeed) {
        mutableStateOf(String.format(Locale.US, "%.2f", currentSpeed))
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SheetSectionLabel("Playback Speed")

        // Click-to-Edit large display
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isEditing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isEditing = true }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2fx", currentSpeed),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit speed",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Tap speed value to type custom rate",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall
                )
            } else {
                val parsedSpeed = textValue.toFloatOrNull()
                val isValid = parsedSpeed != null && parsedSpeed in 0.25f..4.0f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { nv ->
                                val s = nv.filter { it.isDigit() || it == '.' }
                                if (s.count { it == '.' } <= 1) textValue = s
                            },
                            placeholder = { Text("1.00") },
                            singleLine = true,
                            isError = textValue.isNotEmpty() && !isValid,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (parsedSpeed != null && isValid) {
                                        onSpeedSelected(parsedSpeed)
                                        keyboardController?.hide()
                                        isEditing = false
                                    }
                                }
                            ),
                            modifier = Modifier.width(120.dp),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (parsedSpeed != null && isValid) {
                                        onSpeedSelected(parsedSpeed)
                                        keyboardController?.hide()
                                        isEditing = false
                                    }
                                },
                                enabled = isValid,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isValid) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Apply custom speed",
                                    tint = if (isValid) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    textValue = String.format(Locale.US, "%.2f", currentSpeed)
                                    keyboardController?.hide()
                                    isEditing = false
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Cancel edit",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Enter a speed between 0.25x and 4.0x",
                        color = if (isValid) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Speed Slider (0.25x - 4.00x)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SheetSectionLabel("Speed Slider")
            StepSlider(
                value = currentSpeed,
                onValueChange = { v ->
                    onSpeedSelected(v)
                    textValue = String.format(Locale.US, "%.2f", v)
                },
                valueRange = 0.25f..4f,
                step = SPEED_STEP,
                defaultValue = 1.0f
            )
            // Tick labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0.25x", "1x", "2x", "3x", "4x").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            }
        }

        // Speed Presets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SheetSectionLabel("Presets")
            val presets = remember { listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.chunked(5).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { speed ->
                            val isSelected = abs(currentSpeed - speed) < 0.01f
                            Surface(
                                onClick = { onSpeedSelected(speed) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${speed}x",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
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


// TAB 1: GESTURES & TOUCH

@Composable
private fun GesturesTab(
    playbackSettings: PlaybackSettings,
    onUpdateDoubleTapAction: (DoubleTapAction) -> Unit,
    onUpdateDoubleTapSeekDuration: (Long) -> Unit,
    onUpdateLongPressEnabled: (Boolean) -> Unit,
    onUpdateTapAndHoldSpeed: (Float) -> Unit,
    onUpdateLongPressSpeed: (Float) -> Unit,
    onShowDoubleTapDialog: () -> Unit,
    onShowSeekDurationDialog: () -> Unit,
    onShowTwoFingerDialog: () -> Unit,
    onShowThreeFingerDialog: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SheetSectionLabel("Press & Tap Controls")

        SheetCard {
            SheetRow(
                icon = Icons.Default.TouchApp,
                title = "Double Tap Action",
                subtitle = playbackSettings.doubleTapAction.displayName,
                onClick = onShowDoubleTapDialog
            )

            SheetDivider()

            SheetRow(
                icon = Icons.Default.Timer,
                title = "Double Tap Seek Duration",
                subtitle = "${playbackSettings.doubleTapSeekDuration / 1000} seconds",
                onClick = onShowSeekDurationDialog
            )

            SheetDivider()

            SheetRow(
                icon = Icons.Default.TouchApp,
                title = "Two Finger Tap Action",
                subtitle = playbackSettings.twoFingerAction.displayName,
                onClick = onShowTwoFingerDialog
            )

            SheetDivider()

            SheetRow(
                icon = Icons.Default.TouchApp,
                title = "Three Finger Tap Action",
                subtitle = playbackSettings.threeFingerAction.displayName,
                onClick = onShowThreeFingerDialog
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Rounded.Speed,
                title = "Press & Hold Acceleration",
                subtitle = "Hold screen to temporarily accelerate playback",
                checked = playbackSettings.longPressEnabled,
                onCheckedChange = onUpdateLongPressEnabled
            )

            if (playbackSettings.longPressEnabled) {
                SheetDivider()
                SpeedInlineSlider(
                    label = "Press & Hold Acceleration Speed",
                    value = playbackSettings.longPressSpeed,
                    valueRange = 1.5f..4.0f,
                    step = SPEED_STEP,
                    defaultValue = 2.0f,
                    onValueChange = onUpdateLongPressSpeed
                )
            }
        }
    }
}


// TAB 2: INTERFACE & CONTROLS

@Composable
private fun InterfaceTab(
    playbackSettings: PlaybackSettings,
    onUpdateAutoPlayEnabled: (Boolean) -> Unit,
    onUpdateShowSeekButtons: (Boolean) -> Unit,
    onUpdateShowNextPrevButtons: (Boolean) -> Unit,
    onUpdateIsBottomLayoutEnabled: (Boolean) -> Unit,
    onUpdateShowControlGradients: (Boolean) -> Unit,
    onUpdateShowUpNextQueue: (Boolean) -> Unit,
    onUpdateBackgroundPlayEnabled: (Boolean) -> Unit = {},
    onShowOrientationDialog: () -> Unit,
    onShowScalingDialog: () -> Unit,
    onShowSoftButtonDialog: () -> Unit,
    onShowPlaybackControlPositionDialog: () -> Unit,
    onShowIconSizeDialog: () -> Unit,
    onShowSeekBarStyleDialog: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SheetSectionLabel("Layout & Orientation")
        SheetCard {
            SheetRow(
                icon = Icons.Default.ScreenRotation,
                title = "Screen Orientation",
                subtitle = when (playbackSettings.orientationMode) {
                    OrientationMode.SYSTEM_DEFAULT -> "System Default"
                    OrientationMode.VIDEO_ORIENTATION -> "Video Orientation"
                    OrientationMode.LANDSCAPE -> "Landscape"
                    OrientationMode.REVERSE_LANDSCAPE -> "Reverse Landscape"
                    OrientationMode.AUTO_ROTATION -> "Auto-Rotation"
                },
                onClick = onShowOrientationDialog
            )

            SheetDivider()

            SheetRow(
                icon = Icons.Default.AspectRatio,
                title = "Fullscreen Scale Mode",
                subtitle = playbackSettings.aspectMode.displayName,
                onClick = onShowScalingDialog
            )

            SheetDivider()

            SheetRow(
                icon = Icons.Default.Fullscreen,
                title = "System Navigation Bars",
                subtitle = when (playbackSettings.softButtonMode) {
                    SoftButtonMode.AUTO_HIDE -> "Auto-Hide"
                    SoftButtonMode.SHOW -> "Always Show"
                    SoftButtonMode.HIDE -> "Always Hide (Immersive)"
                },
                onClick = onShowSoftButtonDialog
            )

            SheetDivider()

            SheetRow(
                icon = Icons.Default.PlayCircle,
                title = "Playback Controls Position",
                subtitle = if (playbackSettings.isBottomLayoutEnabled) "Below Seekbar" else "Center",
                onClick = onShowPlaybackControlPositionDialog
            )
        }

        Spacer(Modifier.height(4.dp))

        SheetSectionLabel("Controls & Seekbar Customization")
        SheetCard {
            SheetRow(
                icon = Icons.Default.PhotoSizeSelectLarge,
                title = "Controls Icon Size",
                subtitle = when (playbackSettings.controlIconSize.lowercase()) {
                    "small" -> "Small (Compact)"
                    "large" -> "Large (Spacious)"
                    else -> "Medium (Standard)"
                },
                onClick = onShowIconSizeDialog
            )

            SheetDivider()

            SheetRow(
                icon = Icons.Default.Waves,
                title = "Seekbar Style",
                subtitle = when (playbackSettings.seekBarStyle.lowercase()) {
                    "wavy" -> "Wavy"
                    "thick" -> "Thick"
                    "slim" -> "Slim"
                    else -> "Standard"
                },
                onClick = onShowSeekBarStyleDialog
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.FastForward,
                title = "Show Seek Buttons",
                subtitle = "Display jump buttons on the control dock",
                checked = playbackSettings.showSeekButtons,
                onCheckedChange = onUpdateShowSeekButtons
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.SkipNext,
                title = "Show Previous / Next Buttons",
                subtitle = "Display track navigation buttons in controls",
                checked = playbackSettings.showNextPrevButtons,
                onCheckedChange = onUpdateShowNextPrevButtons
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.SkipNext,
                title = "Auto-play Next Video",
                subtitle = "Automatically advance to next playlist item",
                checked = playbackSettings.autoPlayEnabled,
                onCheckedChange = onUpdateAutoPlayEnabled
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.Headphones,
                title = "Background Playback",
                subtitle = "Continue playing audio in background when exiting player",
                checked = playbackSettings.backgroundPlayEnabled,
                onCheckedChange = onUpdateBackgroundPlayEnabled
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.QueueMusic,
                title = "Show Up Next Queue",
                subtitle = "Display next videos in playlist overlay",
                checked = playbackSettings.showUpNextQueue,
                onCheckedChange = onUpdateShowUpNextQueue
            )
        }
    }
}


// TAB 3: OVERLAYS

@Composable
private fun OverlaysTab(
    playbackSettings: PlaybackSettings,
    onUpdateShowStatsOverlay: (Boolean) -> Unit = {},
    onUpdateShowElapsedTimeOverlay: (Boolean) -> Unit = {},
    onUpdateShowRemainingTime: (Boolean) -> Unit,
    onUpdateShowBatteryClockOverlay: (Boolean) -> Unit,
    onUpdateShowControlGradients: (Boolean) -> Unit = {},
    onUpdateIsAmbientModeEnabled: (Boolean) -> Unit = {},
    onUpdateAmbientBlurStyle: (String) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SheetSectionLabel("Player Overlays & Appearance")
        SheetCard {
            SheetToggleRow(
                icon = Icons.Default.Info,
                title = "Show Statistics Overlay",
                subtitle = "Display real-time FPS, codecs, NosvedMedia info, decoders, and audio/video/subtitle track details",
                checked = playbackSettings.showStatsOverlay,
                onCheckedChange = onUpdateShowStatsOverlay
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.Timer,
                title = "Show Elapsed Time Overlay",
                subtitle = "Display elapsed time overlay on video",
                checked = playbackSettings.showElapsedTimeOverlay,
                onCheckedChange = onUpdateShowElapsedTimeOverlay
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.HourglassBottom,
                title = "Show Remaining Time",
                subtitle = "Display remaining time countdown instead of total duration",
                checked = playbackSettings.showRemainingTime,
                onCheckedChange = onUpdateShowRemainingTime
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.BatteryChargingFull,
                title = "Battery & Clock Overlay",
                subtitle = "Display real-time battery level and clock in header",
                checked = playbackSettings.showBatteryClockOverlay,
                onCheckedChange = onUpdateShowBatteryClockOverlay
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.Gradient,
                title = "Controls Backdrop Dimming",
                subtitle = "Show top and bottom gradient vignettes behind controls",
                checked = playbackSettings.showControlGradients,
                onCheckedChange = onUpdateShowControlGradients
            )
        }
    }
}


// TAB 4: AUTOMATION & BEHAVIORS

@Composable
private fun AutomationTab(
    playbackSettings: PlaybackSettings,
    onUpdatePauseWhenObstructed: (Boolean) -> Unit,
    onUpdateKeepAwakeAlways: (Boolean) -> Unit,
    onUpdateSaveBrightnessLevel: (Boolean) -> Unit,
    onUpdateShowScreenRotationButton: (Boolean) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SheetSectionLabel("Automation & Power Behaviors")
        SheetCard {
            SheetToggleRow(
                icon = Icons.Default.ScreenRotationAlt,
                title = "Show Screen Rotation Button",
                subtitle = "Show rotation button when device is rotated",
                checked = playbackSettings.showScreenRotationButton,
                onCheckedChange = onUpdateShowScreenRotationButton
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.PauseCircle,
                title = "Pause on Obstruction",
                subtitle = "Pause playback when proximity sensor detects obstruction",
                checked = playbackSettings.pauseWhenObstructed,
                onCheckedChange = onUpdatePauseWhenObstructed
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.WbSunny,
                title = "Keep Screen Awake Always",
                subtitle = "Prevent screen timeout even when paused",
                checked = playbackSettings.keepAwakeAlways,
                onCheckedChange = onUpdateKeepAwakeAlways
            )

            SheetDivider()

            SheetToggleRow(
                icon = Icons.Default.LightMode,
                title = "Save Custom Brightness",
                subtitle = "Restore custom brightness level on subsequent launches",
                checked = playbackSettings.saveBrightnessLevel,
                onCheckedChange = onUpdateSaveBrightnessLevel
            )
        }
    }
}


// TAB 5: INFORMATION

@Composable
private fun InformationTab(
    video: Video?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SheetSectionLabel("Media Information")
        if (video != null) {
            SheetCard {
                InfoDisplayRow("Title", video.title)
                SheetDivider()
                InfoDisplayRow("Resolution", video.resolution ?: "Unknown")
                SheetDivider()
                InfoDisplayRow("Duration", formatDuration(video.duration))
                SheetDivider()
                InfoDisplayRow("File Size", formatSize(video.size))
                if (video.path.isNotBlank()) {
                    SheetDivider()
                    InfoDisplayRow("File Path", video.path)
                }
                if (video.dateAdded > 0) {
                    SheetDivider()
                    InfoDisplayRow("Added Date", formatDate(video.dateAdded))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No media information available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoDisplayRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


// REUSABLE UI HELPERS

@Composable
private fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SheetCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SheetDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

@Composable
private fun SheetRow(
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SheetToggleRow(
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun SpeedInlineSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float = SPEED_STEP,
    defaultValue: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val formattedValue = remember(value) { String.format(Locale.US, "%.2fx", value) }
                Text(
                    text = formattedValue,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    onClick = { onValueChange(defaultValue) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        StepSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            step = step,
            defaultValue = defaultValue
        )
    }
}

@Composable
private fun StepSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float = 0.05f,
    defaultValue: Float = valueRange.start
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LongPressStepButton(
            icon = Icons.Default.Remove,
            contentDescription = "Decrease",
            onStep = {
                val next = (value - step).coerceIn(valueRange.start, valueRange.endInclusive).roundToTwoDecimals()
                val snapped = ((next / step).roundToInt() * step).roundToTwoDecimals()
                onValueChange(snapped.coerceIn(valueRange.start, valueRange.endInclusive))
            }
        )

        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = { raw ->
                val snapped = ((raw / step).roundToInt() * step).roundToTwoDecimals()
                    .coerceIn(valueRange.start, valueRange.endInclusive)
                onValueChange(snapped)
            },
            valueRange = valueRange,
            modifier = Modifier.weight(1f)
        )

        LongPressStepButton(
            icon = Icons.Default.Add,
            contentDescription = "Increase",
            onStep = {
                val next = (value + step).coerceIn(valueRange.start, valueRange.endInclusive).roundToTwoDecimals()
                val snapped = ((next / step).roundToInt() * step).roundToTwoDecimals()
                onValueChange(snapped.coerceIn(valueRange.start, valueRange.endInclusive))
            }
        )
    }
}

@Composable
private fun LongPressStepButton(
    icon: ImageVector,
    contentDescription: String,
    onStep: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.secondaryContainer
            )
            .repeatingClickable(
                initialDelayMillis = 500,
                delayMillis = 100,
                interactionSource = interactionSource,
                onClick = onStep
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = if (isPressed) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun <T> PremiumSelectionDialog(
    title: String,
    onDismiss: () -> Unit,
    onReset: (() -> Unit)? = null,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    optionDescription: (@Composable (T) -> String?)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Title and Reset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (onReset != null) {
                        TextButton(
                            onClick = onReset,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Reset", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                // Options List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(options) { option ->
                        val isSelected = option == selectedOption
                        val cardBgColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        } else {
                            Color.Transparent
                        }
                        val cardBorderColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        } else {
                            Color.Transparent
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = cardBgColor,
                            border = BorderStroke(1.dp, cardBorderColor),
                            onClick = {
                                onOptionSelected(option)
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = optionLabel(option),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    val desc = optionDescription?.invoke(option)
                                    if (desc != null) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
