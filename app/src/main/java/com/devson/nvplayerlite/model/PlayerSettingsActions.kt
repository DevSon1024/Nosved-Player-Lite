package com.devson.nvplayerlite.model

import androidx.compose.runtime.Immutable
import com.devson.nvplayerlite.repository.DoubleTapAction
import com.devson.nvplayerlite.repository.FullScreenMode
import com.devson.nvplayerlite.repository.MultiFingerAction
import com.devson.nvplayerlite.repository.OrientationMode
import com.devson.nvplayerlite.repository.SoftButtonMode

@Immutable
data class PlayerSettingsActions(
    val onSpeedSelected: (Float) -> Unit = {},
    val onUpdateDoubleTapAction: (DoubleTapAction) -> Unit = {},
    val onUpdateDoubleTapSeekDuration: (Long) -> Unit = {},
    val onUpdateTwoFingerAction: (MultiFingerAction) -> Unit = {},
    val onUpdateThreeFingerAction: (MultiFingerAction) -> Unit = {},
    val onUpdateLongPressEnabled: (Boolean) -> Unit = {},
    val onUpdateLongPressSpeed: (Float) -> Unit = {},
    val onUpdateTapAndHoldSpeed: (Float) -> Unit = {},
    val onUpdateOrientationMode: (OrientationMode) -> Unit = {},
    val onUpdateFullScreenMode: (FullScreenMode) -> Unit = {},
    val onUpdateAspectMode: (AspectMode) -> Unit = {},
    val onUpdateSoftButtonMode: (SoftButtonMode) -> Unit = {},
    val onUpdateControlIconSize: (String) -> Unit = {},
    val onUpdateSeekBarStyle: (String) -> Unit = {},
    val onUpdateAutoPlayEnabled: (Boolean) -> Unit = {},
    val onUpdateShowSeekButtons: (Boolean) -> Unit = {},
    val onUpdateShowNextPrevButtons: (Boolean) -> Unit = {},
    val onUpdateShowRemainingTime: (Boolean) -> Unit = {},
    val onUpdateShowBatteryClockOverlay: (Boolean) -> Unit = {},
    val onUpdatePauseWhenObstructed: (Boolean) -> Unit = {},
    val onUpdateKeepAwakeAlways: (Boolean) -> Unit = {},
    val onUpdateShowUpNextQueue: (Boolean) -> Unit = {},
    val onUpdateIsAmbientModeEnabled: (Boolean) -> Unit = {},
    val onUpdateAmbientBlurStyle: (String) -> Unit = {},
    val onUpdateSaveBrightnessLevel: (Boolean) -> Unit = {},
    val onUpdateIsBottomLayoutEnabled: (Boolean) -> Unit = {},
    val onUpdateShowControlGradients: (Boolean) -> Unit = {},
    val onUpdateBackgroundPlayEnabled: (Boolean) -> Unit = {},
    val onUpdateShowStatsOverlay: (Boolean) -> Unit = {},
    val onUpdateShowElapsedTimeOverlay: (Boolean) -> Unit = {},
    val onUpdateShowScreenRotationButton: (Boolean) -> Unit = {}
)
