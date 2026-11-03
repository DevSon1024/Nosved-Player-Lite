package com.devson.nvplayerlite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devson.nvplayerlite.repository.PlaybackSettingsRepository
import com.devson.nvplayerlite.ui.theme.AppThemePalette
import com.devson.nvplayerlite.ui.theme.AppThemePaletteHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.devson.nvplayerlite.model.ControlRegion
import com.devson.nvplayerlite.model.PlayerButton
import com.devson.nvplayerlite.model.AspectMode

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = PlaybackSettingsRepository(application.applicationContext)
    private val viewSettingsRepo = com.devson.nvplayerlite.repository.ViewSettingsRepository(application.applicationContext)

    val viewSettings = viewSettingsRepo.viewSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.devson.nvplayerlite.model.ViewSettings())

    /**
     * Emits null = follow system, true = force dark, false = force light.
     */
    val isDarkTheme: StateFlow<Boolean?> = settingsRepo.isDarkThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isAmoledTheme: StateFlow<Boolean> = settingsRepo.isAmoledThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val defaultAudioLang: StateFlow<String> = settingsRepo.defaultAudioLangFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val defaultSubtitleLang: StateFlow<String> = settingsRepo.defaultSubtitleLangFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val isDeveloperMode: StateFlow<Boolean> = settingsRepo.isDeveloperModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * true = Modern-style player UI, false = default player UI.
     * Persisted via PlaybackSettingsRepository / DataStore.
     */
    val useModernPlayerStyle: StateFlow<Boolean> = settingsRepo.useModernPlayerStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /**
     * null  = DataStore not yet loaded (show blank splash)
     * false = first launch, show onboarding
     * true  = already seen onboarding, go straight to Home
     */
    val hasSeenOnboarding: StateFlow<Boolean?> = settingsRepo.hasSeenOnboardingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * true = use Material You (wallpaper-based) colours, false = Nosved custom palette.
     * Only takes visual effect on API 31+.
     */
    val dynamicColor: StateFlow<Boolean> = settingsRepo.dynamicColorFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** The currently selected built-in colour palette (defaults to BLUE / Nosved Blue). */
    val selectedPalette: StateFlow<AppThemePalette> = settingsRepo.selectedPaletteFlow
        .map { key -> AppThemePaletteHelper.fromKey(key) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppThemePalette.BLUE)

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch { settingsRepo.setDarkTheme(isDark) }
    }

    /** Clears any explicit dark/light override - theme follows the device system setting. */
    fun resetDarkTheme() {
        viewModelScope.launch { settingsRepo.resetDarkTheme() }
    }

    fun setAmoledTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setAmoledTheme(enabled) }
    }

    fun setDefaultAudioLang(langCode: String) {
        viewModelScope.launch { settingsRepo.setDefaultAudioLanguage(langCode) }
    }

    fun setDefaultSubtitleLang(langCode: String) {
        viewModelScope.launch { settingsRepo.setDefaultSubtitleLanguage(langCode) }
    }

    fun enableDeveloperMode() {
        viewModelScope.launch { settingsRepo.setDeveloperMode(true) }
    }

    fun setModernPlayerStyle(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setModernPlayerStyle(enabled) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setDynamicColor(enabled) }
    }

    fun setSelectedPalette(palette: AppThemePalette) {
        viewModelScope.launch { settingsRepo.setSelectedPalette(palette.name) }
    }

    /** Call when the user finishes or skips the onboarding screen. */
    fun markOnboardingComplete() {
        viewModelScope.launch { settingsRepo.setHasSeenOnboarding(true) }
    }

    fun updateRecognizeNoMedia(recognize: Boolean) {
        viewModelScope.launch { viewSettingsRepo.updateRecognizeNoMedia(recognize) }
    }

    fun updateShowHiddenFiles(show: Boolean) {
        viewModelScope.launch { viewSettingsRepo.updateShowHiddenFiles(show) }
    }

    fun updateShowFloatingButton(show: Boolean) {
        viewModelScope.launch { viewSettingsRepo.updateShowFloatingButton(show) }
    }

    fun updateSelectByThumbnail(select: Boolean) {
        viewModelScope.launch { viewSettingsRepo.updateSelectByThumbnail(select) }
    }

    fun updateEnableFabPreview(enable: Boolean) {
        viewModelScope.launch { viewSettingsRepo.updateEnableFabPreview(enable) }
    }

    fun updateScanFoldersList(folders: Set<String>) {
        viewModelScope.launch { viewSettingsRepo.updateScanFoldersList(folders) }
    }

    fun updateShowHistoryCard(show: Boolean) {
        viewModelScope.launch { viewSettingsRepo.updateShowHistoryCard(show) }
    }

    fun updateShowVideoCard(show: Boolean) {
        viewModelScope.launch { viewSettingsRepo.updateShowVideoCard(show) }
    }

    fun updateShowStorageTracker(show: Boolean) {
        viewModelScope.launch { viewSettingsRepo.updateShowStorageTracker(show) }
    }

    fun updateDefaultScreen(screen: com.devson.nvplayerlite.model.DefaultScreen) {
        viewModelScope.launch { viewSettingsRepo.updateDefaultScreen(screen) }
    }

    val playbackSettings = settingsRepo.playbackSettingsFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            com.devson.nvplayerlite.repository.PlaybackSettings(
                seekDurationSeconds = 10,
                seekBarStyle = "line",
                controlIconSize = "medium",
                autoPlayEnabled = false,
                showSeekButtons = true,
                fastplaySpeed = 2.0f,
                orientationMode = com.devson.nvplayerlite.repository.OrientationMode.SYSTEM_DEFAULT,
                fullScreenMode = com.devson.nvplayerlite.repository.FullScreenMode.AUTO_SWITCH,
                softButtonMode = com.devson.nvplayerlite.repository.SoftButtonMode.AUTO_HIDE,
                showElapsedTimeOverlay = false,
                showBatteryClockOverlay = false,
                showScreenRotationButton = true,
                pauseWhenObstructed = true,
                showRemainingTime = false,
                useSystemCaptionStyle = false,
                subtitleFont = com.devson.nvplayerlite.repository.SubtitleFont.DEFAULT,
                isSubtitleBold = false,
                forceAssSubtitleOverride = false,
                // New Gesture Defaults
                seekGestureEnabled = true,
                seekSensitivity = 0.5f,
                brightnessGestureEnabled = true,
                brightnessSensitivity = 0.5f,
                volumeGestureEnabled = true,
                volumeSensitivity = 0.5f,
                twoFingerAction = com.devson.nvplayerlite.repository.MultiFingerAction.PLAY_PAUSE,
                threeFingerAction = com.devson.nvplayerlite.repository.MultiFingerAction.FAST_PLAY,
                longPressEnabled = true,
                longPressSpeed = 2.0f,
                doubleTapAction = com.devson.nvplayerlite.repository.DoubleTapAction.BOTH
            )
        )

    fun updateOrientationMode(mode: com.devson.nvplayerlite.repository.OrientationMode) {
        viewModelScope.launch { settingsRepo.updateOrientationMode(mode) }
    }

    fun updateFullScreenMode(mode: com.devson.nvplayerlite.repository.FullScreenMode) {
        viewModelScope.launch { settingsRepo.updateFullScreenMode(mode) }
    }

    fun updateSoftButtonMode(mode: com.devson.nvplayerlite.repository.SoftButtonMode) {
        viewModelScope.launch { settingsRepo.updateSoftButtonMode(mode) }
    }

    fun updateShowElapsedTimeOverlay(show: Boolean) {
        viewModelScope.launch { settingsRepo.updateShowElapsedTimeOverlay(show) }
    }

    fun updateShowBatteryClockOverlay(show: Boolean) {
        viewModelScope.launch { settingsRepo.updateShowBatteryClockOverlay(show) }
    }

    fun updateShowScreenRotationButton(show: Boolean) {
        viewModelScope.launch { settingsRepo.updateShowScreenRotationButton(show) }
    }

    fun updatePauseWhenObstructed(pause: Boolean) {
        viewModelScope.launch { settingsRepo.updatePauseWhenObstructed(pause) }
    }

    fun updateShowRemainingTime(show: Boolean) {
        viewModelScope.launch { settingsRepo.updateShowRemainingTime(show) }
    }

    val isNavBarTransparent: StateFlow<Boolean> = settingsRepo.isNavBarTransparentFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setNavBarTransparent(transparent: Boolean) {
        viewModelScope.launch { settingsRepo.setNavBarTransparent(transparent) }
    }

    fun updateUseSystemCaptionStyle(useSystem: Boolean) {
        viewModelScope.launch { settingsRepo.updateUseSystemCaptionStyle(useSystem) }
    }

    fun updateSubtitleFont(font: com.devson.nvplayerlite.repository.SubtitleFont) {
        viewModelScope.launch { settingsRepo.updateSubtitleFont(font) }
    }

    fun updateIsSubtitleBold(isBold: Boolean) {
        viewModelScope.launch { settingsRepo.updateIsSubtitleBold(isBold) }
    }

    fun updateForceAssSubtitleOverride(force: Boolean) {
        viewModelScope.launch { settingsRepo.updateForceAssSubtitleOverride(force) }
    }

    // --- New Gesture Dispatchers ---

    fun updateSeekGesture(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSeekGestureEnabled(enabled) }
    }

    fun updateSeekSensitivity(sensitivity: Float) {
        viewModelScope.launch { settingsRepo.updateSeekSensitivity(sensitivity) }
    }

    fun updateBrightnessGesture(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateBrightnessGestureEnabled(enabled) }
    }

    fun updateBrightnessSensitivity(sensitivity: Float) {
        viewModelScope.launch { settingsRepo.updateBrightnessSensitivity(sensitivity) }
    }

    fun updateVolumeGesture(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateVolumeGestureEnabled(enabled) }
    }

    fun updateVolumeSensitivity(sensitivity: Float) {
        viewModelScope.launch { settingsRepo.updateVolumeSensitivity(sensitivity) }
    }

    fun updateTwoFingerAction(action: com.devson.nvplayerlite.repository.MultiFingerAction) {
        viewModelScope.launch { settingsRepo.updateTwoFingerAction(action) }
    }

    fun updateThreeFingerAction(action: com.devson.nvplayerlite.repository.MultiFingerAction) {
        viewModelScope.launch { settingsRepo.updateThreeFingerAction(action) }
    }

    fun updateLongPressEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateLongPressEnabled(enabled) }
    }

    fun updateLongPressSpeed(speed: Float) {
        viewModelScope.launch { settingsRepo.updateLongPressSpeed(speed) }
    }

    private fun parseControls(value: String): List<PlayerButton> {
        if (value.isBlank()) return emptyList()
        return value.split(',').mapNotNull { runCatching { PlayerButton.valueOf(it) }.getOrNull() }
    }

    val topLeftControls: StateFlow<List<PlayerButton>> = settingsRepo.playbackSettingsFlow
        .map { parseControls(it.topLeftControls) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), parseControls("BACK_ARROW,VIDEO_TITLE"))

    val topRightControls: StateFlow<List<PlayerButton>> = settingsRepo.playbackSettingsFlow
        .map { parseControls(it.topRightControls) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), parseControls("DECODER,SUBTITLES,AUDIO_TRACK,MORE_OPTIONS"))

    val bottomLeftControls: StateFlow<List<PlayerButton>> = settingsRepo.playbackSettingsFlow
        .map { parseControls(it.bottomLeftControls) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), parseControls("LOCK_CONTROLS,PICTURE_IN_PICTURE"))

    val bottomRightControls: StateFlow<List<PlayerButton>> = settingsRepo.playbackSettingsFlow
        .map { parseControls(it.bottomRightControls) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), parseControls("ASPECT_RATIO,SCREEN_ROTATION"))

    val portraitTopLeftControls: StateFlow<List<PlayerButton>> = settingsRepo.playbackSettingsFlow
        .map { parseControls(it.portraitTopLeftControls) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), parseControls("BACK_ARROW,VIDEO_TITLE"))

    val portraitTopRightControls: StateFlow<List<PlayerButton>> = settingsRepo.playbackSettingsFlow
        .map { parseControls(it.portraitTopRightControls) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), parseControls("SUBTITLES,AUDIO_TRACK,MORE_OPTIONS"))

    val portraitBottomControls: StateFlow<List<PlayerButton>> = settingsRepo.playbackSettingsFlow
        .map { parseControls(it.portraitBottomControls) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), parseControls("DECODER,ASPECT_RATIO,SCREEN_ROTATION,LOCK_CONTROLS,PICTURE_IN_PICTURE"))

    fun updateControls(region: ControlRegion, controls: List<PlayerButton>) {
        val controlsStr = controls.joinToString(",")
        viewModelScope.launch {
            when (region) {
                ControlRegion.TOP_LEFT -> settingsRepo.updateTopLeftControls(controlsStr)
                ControlRegion.TOP_RIGHT -> settingsRepo.updateTopRightControls(controlsStr)
                ControlRegion.BOTTOM_LEFT -> settingsRepo.updateBottomLeftControls(controlsStr)
                ControlRegion.BOTTOM_RIGHT -> settingsRepo.updateBottomRightControls(controlsStr)
                ControlRegion.PORTRAIT_TOP_LEFT -> settingsRepo.updatePortraitTopLeftControls(controlsStr)
                ControlRegion.PORTRAIT_TOP_RIGHT -> settingsRepo.updatePortraitTopRightControls(controlsStr)
                ControlRegion.PORTRAIT_BOTTOM -> settingsRepo.updatePortraitBottomControls(controlsStr)
            }
        }
    }

    fun updateIsBottomLayoutEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateIsBottomLayoutEnabled(enabled) }
    }

    fun updateShowControlGradients(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateShowControlGradients(enabled) }
    }

    fun updateShowNextPrevButtons(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateShowNextPrevButtons(enabled) }
    }

    fun updateAspectMode(mode: AspectMode) {
        viewModelScope.launch { settingsRepo.updateAspectMode(mode) }
    }

    fun updateControlIconSize(size: String) {
        viewModelScope.launch { settingsRepo.updateControlIconSize(size) }
    }

    fun updateSeekBarStyle(style: String) {
        viewModelScope.launch { settingsRepo.updateSeekBarStyle(style) }
    }

    fun updateAutoPlayEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateAutoPlayEnabled(enabled) }
    }

    fun updateShowSeekButtons(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateShowSeekButtons(enabled) }
    }

    fun updateBackgroundPlayEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateBackgroundPlayEnabled(enabled) }
    }

    fun updateTapAndHoldSpeed(speed: Float) {
        viewModelScope.launch { settingsRepo.updateTapAndHoldSpeed(speed) }
    }

    fun updateDoubleTapSeekDuration(durationMs: Long) {
        viewModelScope.launch { settingsRepo.updateDoubleTapSeekDuration(durationMs) }
    }

    fun updateKeepAwakeAlways(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateKeepAwakeAlways(enabled) }
    }

    fun updateShowUpNextQueue(show: Boolean) {
        viewModelScope.launch { settingsRepo.updateShowUpNextQueue(show) }
    }

    fun updateQueueLayoutMode(mode: com.devson.nvplayerlite.model.LayoutMode) {
        viewModelScope.launch { settingsRepo.updateQueueLayoutMode(mode) }
    }

    fun updateIsAmbientModeEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateIsAmbientModeEnabled(enabled) }
    }

    fun updateAmbientBlurStyle(style: String) {
        viewModelScope.launch { settingsRepo.updateAmbientBlurStyle(style) }
    }

    fun updateSaveBrightnessLevel(save: Boolean) {
        viewModelScope.launch { settingsRepo.updateSaveBrightnessLevel(save) }
    }

    fun updateDecoderMode(mode: com.devson.nvplayerlite.repository.DecoderMode) {
        viewModelScope.launch { settingsRepo.updateDecoderMode(mode) }
    }

    fun updateAudioBoosterEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateAudioBoosterEnabled(enabled) }
    }

    fun updateSubtitleTextSizeScale(scale: Float) {
        viewModelScope.launch { settingsRepo.updateSubtitleTextSizeScale(scale) }
    }

    fun updateSubtitleBgStyle(style: Int) {
        viewModelScope.launch { settingsRepo.updateSubtitleBgStyle(style) }
    }

    fun updateSubtitleDelayMs(delayMs: Long) {
        viewModelScope.launch { settingsRepo.updateSubtitleDelayMs(delayMs) }
    }

    fun updateSubtitleVerticalOffset(offset: Float) {
        viewModelScope.launch { settingsRepo.updateSubtitleVerticalOffset(offset) }
    }

    fun updateSubtitleGesturesEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.updateSubtitleGesturesEnabled(enabled) }
    }

    fun updateDoubleTapAction(action: com.devson.nvplayerlite.repository.DoubleTapAction) {
        viewModelScope.launch { settingsRepo.updateDoubleTapAction(action) }
    }
}