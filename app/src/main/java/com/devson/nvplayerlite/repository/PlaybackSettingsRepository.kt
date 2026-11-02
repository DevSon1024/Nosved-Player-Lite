package com.devson.nvplayerlite.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.devson.nvplayerlite.model.AspectMode

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "playback_settings")

enum class OrientationMode { VIDEO_ORIENTATION, LANDSCAPE, REVERSE_LANDSCAPE, AUTO_ROTATION, SYSTEM_DEFAULT }
enum class FullScreenMode { ON, OFF, AUTO_SWITCH }
enum class SoftButtonMode { SHOW, HIDE, AUTO_HIDE }
enum class DecoderMode(val displayName: String) { HW("HW"), HW_PLUS("HW+"), SW("SW") }
enum class SubtitleFont { DEFAULT, MONOSPACE, SANS_SERIF, SERIF }
enum class DoubleTapAction(val displayName: String) {
    PLAY_PAUSE("Play / Pause"),
    FAST_FORWARD_REWIND("Fast Forward / Rewind"),
    BOTH("Both"),
    NONE("None")
}
enum class MultiFingerAction(val displayName: String) {
    PLAY_PAUSE("Play / Pause"),
    FAST_PLAY("Lock Video at Fast Play"),
    NONE("None")
}
data class PlaybackSettings(
    val seekDurationSeconds: Int,
    val seekBarStyle: String,
    val controlIconSize: String,
    val autoPlayEnabled: Boolean,
    val showSeekButtons: Boolean = true,
    val fastplaySpeed: Float = 2.0f,
    val orientationMode: OrientationMode = OrientationMode.SYSTEM_DEFAULT,
    val fullScreenMode: FullScreenMode = FullScreenMode.AUTO_SWITCH,
    val softButtonMode: SoftButtonMode = SoftButtonMode.AUTO_HIDE,
    val showElapsedTimeOverlay: Boolean = false,
    val showBatteryClockOverlay: Boolean = false,
    val showScreenRotationButton: Boolean = true,
    val pauseWhenObstructed: Boolean = true,
    val showRemainingTime: Boolean = false,
    val decoderMode: DecoderMode = DecoderMode.HW_PLUS,
    val isAmoledTheme: Boolean = false,
    val defaultAudioLanguage: String = "",
    val defaultSubtitleLanguage: String = "",
    val useSystemCaptionStyle: Boolean = false,
    val subtitleFont: SubtitleFont = SubtitleFont.DEFAULT,
    val isSubtitleBold: Boolean = false,
    val forceAssSubtitleOverride: Boolean = false,

    // Gesture Settings
    val seekGestureEnabled: Boolean = true,
    val seekSensitivity: Float = 0.5f,
    val brightnessGestureEnabled: Boolean = true,
    val brightnessSensitivity: Float = 0.5f,
    val volumeGestureEnabled: Boolean = true,
    val volumeSensitivity: Float = 0.5f,
    val twoFingerAction: MultiFingerAction = MultiFingerAction.PLAY_PAUSE,
    val threeFingerAction: MultiFingerAction = MultiFingerAction.FAST_PLAY,
    val longPressEnabled: Boolean = true,
    val longPressSpeed: Float = 2.0f,
    val doubleTapAction: DoubleTapAction = DoubleTapAction.BOTH,
    val topLeftControls: String = "BACK_ARROW,VIDEO_TITLE",
    val topRightControls: String = "DECODER,SUBTITLES,AUDIO_TRACK,MORE_OPTIONS",
    val bottomLeftControls: String = "LOCK_CONTROLS,PICTURE_IN_PICTURE",
    val bottomRightControls: String = "ASPECT_RATIO,SCREEN_ROTATION",
    val portraitTopLeftControls: String = "BACK_ARROW,VIDEO_TITLE",
    val portraitTopRightControls: String = "SUBTITLES,AUDIO_TRACK,MORE_OPTIONS",
    val portraitBottomControls: String = "DECODER,ASPECT_RATIO,SCREEN_ROTATION,LOCK_CONTROLS,PICTURE_IN_PICTURE",
    val isBottomLayoutEnabled: Boolean = false,
    val showControlGradients: Boolean = true,
    val showNextPrevButtons: Boolean = true,
    val aspectMode: AspectMode = AspectMode.FIT,
    val backgroundPlayEnabled: Boolean = false,
    val subtitleTextSizeScale: Float = 1.0f,
    val subtitleBgStyle: Int = 1,
    val subtitleDelayMs: Long = 0L,
    val subtitleVerticalOffset: Float = 0f,
    val subtitleGesturesEnabled: Boolean = true,
    val tapAndHoldSpeed: Float = 2.0f,
    val doubleTapSeekDuration: Long = 10000L,
    val keepAwakeAlways: Boolean = false,
    val audioBoosterEnabled: Boolean = false,
    val showUpNextQueue: Boolean = true,
    val isAmbientModeEnabled: Boolean = false,
    val ambientBlurStyle: String = "standard",
    val saveBrightnessLevel: Boolean = true
)

class PlaybackSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val SEEK_DURATION = intPreferencesKey("seek_duration_seconds")
        val SEEK_BAR_STYLE = stringPreferencesKey("seek_bar_style")
        val CONTROL_ICON_SIZE = stringPreferencesKey("control_icon_size")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")
        val SHOW_SEEK_BUTTONS = booleanPreferencesKey("show_seek_buttons")
        val FASTPLAY_SPEED = floatPreferencesKey("fastplay_speed")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val DARK_THEME_SET = booleanPreferencesKey("dark_theme_set")
        val DEVELOPER_MODE = booleanPreferencesKey("developer_mode")
        val Modern_PLAYER_STYLE = booleanPreferencesKey("Modern_player_style")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val SELECTED_PALETTE = stringPreferencesKey("selected_palette")
        val ORIENTATION_MODE = stringPreferencesKey("orientation_mode")
        val FULL_SCREEN_MODE = stringPreferencesKey("full_screen_mode")
        val SOFT_BUTTON_MODE = stringPreferencesKey("soft_button_mode")
        val IS_CUSTOM_BRIGHTNESS_ENABLED = booleanPreferencesKey("is_custom_brightness_enabled")
        val CUSTOM_BRIGHTNESS_LEVEL = floatPreferencesKey("custom_brightness_level")
        val SHOW_ELAPSED_TIME_OVERLAY = booleanPreferencesKey("show_elapsed_time_overlay")
        val SHOW_BATTERY_CLOCK_OVERLAY = booleanPreferencesKey("show_battery_clock_overlay")
        val SHOW_SCREEN_ROTATION_BUTTON = booleanPreferencesKey("show_screen_rotation_button")
        val PAUSE_WHEN_OBSTRUCTED = booleanPreferencesKey("pause_when_obstructed")
        val SHOW_REMAINING_TIME = booleanPreferencesKey("show_remaining_time")
        val NAV_BAR_TRANSPARENT = booleanPreferencesKey("nav_bar_transparent")
        val DECODER_MODE = stringPreferencesKey("decoder_mode")
        val AMOLED_THEME = booleanPreferencesKey("amoled_theme")
        val DEFAULT_AUDIO_LANG = stringPreferencesKey("default_audio_lang")
        val DEFAULT_SUBTITLE_LANG = stringPreferencesKey("default_subtitle_lang")
        val USE_SYSTEM_CAPTION_STYLE = booleanPreferencesKey("use_system_caption_style")
        val SUBTITLE_FONT = stringPreferencesKey("subtitle_font")
        val IS_SUBTITLE_BOLD = booleanPreferencesKey("is_subtitle_bold")
        val FORCE_ASS_SUBTITLE_OVERRIDE = booleanPreferencesKey("force_ass_subtitle_override")

        // Gesture Keys
        val SEEK_GESTURE_ENABLED = booleanPreferencesKey("seek_gesture_enabled")
        val SEEK_SENSITIVITY = floatPreferencesKey("seek_sensitivity")
        val BRIGHTNESS_GESTURE_ENABLED = booleanPreferencesKey("brightness_gesture_enabled")
        val BRIGHTNESS_SENSITIVITY = floatPreferencesKey("brightness_sensitivity")
        val VOLUME_GESTURE_ENABLED = booleanPreferencesKey("volume_gesture_enabled")
        val VOLUME_SENSITIVITY = floatPreferencesKey("volume_sensitivity")
        val TWO_FINGER_ACTION = stringPreferencesKey("two_finger_action")
        val THREE_FINGER_ACTION = stringPreferencesKey("three_finger_action")
        val LONG_PRESS_ENABLED = booleanPreferencesKey("long_press_enabled")
        val LONG_PRESS_SPEED = floatPreferencesKey("long_press_speed")
        val DOUBLE_TAP_ACTION = stringPreferencesKey("double_tap_action")
        val TOP_LEFT_CONTROLS = stringPreferencesKey("top_left_controls")
        val TOP_RIGHT_CONTROLS = stringPreferencesKey("top_right_controls")
        val BOTTOM_LEFT_CONTROLS = stringPreferencesKey("bottom_left_controls")
        val BOTTOM_RIGHT_CONTROLS = stringPreferencesKey("bottom_right_controls")
        val PORTRAIT_TOP_LEFT_CONTROLS = stringPreferencesKey("portrait_top_left_controls")
        val PORTRAIT_TOP_RIGHT_CONTROLS = stringPreferencesKey("portrait_top_right_controls")
        val PORTRAIT_BOTTOM_CONTROLS = stringPreferencesKey("portrait_bottom_controls")
        val IS_BOTTOM_LAYOUT_ENABLED = booleanPreferencesKey("is_bottom_layout_enabled")
        val SHOW_CONTROL_GRADIENTS = booleanPreferencesKey("show_control_gradients")
        val SHOW_NEXT_PREV_BUTTONS = booleanPreferencesKey("show_next_prev_buttons")
        val ASPECT_MODE = stringPreferencesKey("aspect_mode")
        val BACKGROUND_PLAY_ENABLED = booleanPreferencesKey("background_play_enabled")
        val SUBTITLE_TEXT_SIZE_SCALE = floatPreferencesKey("subtitle_text_size_scale")
        val SUBTITLE_BG_STYLE = intPreferencesKey("subtitle_bg_style")
        val SUBTITLE_DELAY_MS = androidx.datastore.preferences.core.longPreferencesKey("subtitle_delay_ms")
        val SUBTITLE_VERTICAL_OFFSET = floatPreferencesKey("subtitle_vertical_offset")
        val SUBTITLE_GESTURES_ENABLED = booleanPreferencesKey("subtitle_gestures_enabled")
        val TAP_AND_HOLD_SPEED = floatPreferencesKey("tap_and_hold_speed")
        val DOUBLE_TAP_SEEK_DURATION = androidx.datastore.preferences.core.longPreferencesKey("double_tap_seek_duration")
        val KEEP_AWAKE_ALWAYS = booleanPreferencesKey("keep_awake_always")
        val AUDIO_BOOSTER_ENABLED = booleanPreferencesKey("audio_booster_enabled")
        val SHOW_UP_NEXT_QUEUE = booleanPreferencesKey("show_up_next_queue")
        val IS_AMBIENT_MODE_ENABLED = booleanPreferencesKey("is_ambient_mode_enabled")
        val AMBIENT_BLUR_STYLE = stringPreferencesKey("ambient_blur_style")
        val SAVE_BRIGHTNESS_LEVEL = booleanPreferencesKey("save_brightness_level")
    }

    val playbackSettingsFlow: Flow<PlaybackSettings> = context.dataStore.data
        .map { preferences ->
            PlaybackSettings(
                seekDurationSeconds = preferences[PreferencesKeys.SEEK_DURATION] ?: 10,
                seekBarStyle = preferences[PreferencesKeys.SEEK_BAR_STYLE] ?: "DEFAULT",
                controlIconSize = preferences[PreferencesKeys.CONTROL_ICON_SIZE] ?: "MEDIUM",
                autoPlayEnabled = preferences[PreferencesKeys.AUTO_PLAY] ?: false,
                showSeekButtons = preferences[PreferencesKeys.SHOW_SEEK_BUTTONS] ?: true,
                fastplaySpeed = preferences[PreferencesKeys.FASTPLAY_SPEED] ?: 2.0f,
                orientationMode = try { OrientationMode.valueOf(preferences[PreferencesKeys.ORIENTATION_MODE] ?: OrientationMode.SYSTEM_DEFAULT.name) } catch (e: Exception) { OrientationMode.SYSTEM_DEFAULT },
                fullScreenMode = try { FullScreenMode.valueOf(preferences[PreferencesKeys.FULL_SCREEN_MODE] ?: FullScreenMode.AUTO_SWITCH.name) } catch (e: Exception) { FullScreenMode.AUTO_SWITCH },
                softButtonMode = try { SoftButtonMode.valueOf(preferences[PreferencesKeys.SOFT_BUTTON_MODE] ?: SoftButtonMode.AUTO_HIDE.name) } catch (e: Exception) { SoftButtonMode.AUTO_HIDE },
                showElapsedTimeOverlay = preferences[PreferencesKeys.SHOW_ELAPSED_TIME_OVERLAY] ?: false,
                showBatteryClockOverlay = preferences[PreferencesKeys.SHOW_BATTERY_CLOCK_OVERLAY] ?: false,
                showScreenRotationButton = preferences[PreferencesKeys.SHOW_SCREEN_ROTATION_BUTTON] ?: true,
                pauseWhenObstructed = preferences[PreferencesKeys.PAUSE_WHEN_OBSTRUCTED] ?: true,
                showRemainingTime = preferences[PreferencesKeys.SHOW_REMAINING_TIME] ?: false,
                decoderMode = try { DecoderMode.valueOf(preferences[PreferencesKeys.DECODER_MODE] ?: DecoderMode.HW_PLUS.name) } catch (e: Exception) { DecoderMode.HW_PLUS },
                isAmoledTheme = preferences[PreferencesKeys.AMOLED_THEME] ?: false,
                defaultAudioLanguage = preferences[PreferencesKeys.DEFAULT_AUDIO_LANG] ?: "",
                defaultSubtitleLanguage = preferences[PreferencesKeys.DEFAULT_SUBTITLE_LANG] ?: "",
                useSystemCaptionStyle = preferences[PreferencesKeys.USE_SYSTEM_CAPTION_STYLE] ?: false,
                subtitleFont = try { SubtitleFont.valueOf(preferences[PreferencesKeys.SUBTITLE_FONT] ?: SubtitleFont.DEFAULT.name) } catch (e: Exception) { SubtitleFont.DEFAULT },
                isSubtitleBold = preferences[PreferencesKeys.IS_SUBTITLE_BOLD] ?: false,
                forceAssSubtitleOverride = preferences[PreferencesKeys.FORCE_ASS_SUBTITLE_OVERRIDE] ?: false,

                seekGestureEnabled = preferences[PreferencesKeys.SEEK_GESTURE_ENABLED] ?: true,
                seekSensitivity = preferences[PreferencesKeys.SEEK_SENSITIVITY] ?: 0.5f,
                brightnessGestureEnabled = preferences[PreferencesKeys.BRIGHTNESS_GESTURE_ENABLED] ?: true,
                brightnessSensitivity = preferences[PreferencesKeys.BRIGHTNESS_SENSITIVITY] ?: 0.5f,
                volumeGestureEnabled = preferences[PreferencesKeys.VOLUME_GESTURE_ENABLED] ?: true,
                volumeSensitivity = preferences[PreferencesKeys.VOLUME_SENSITIVITY] ?: 0.5f,
                twoFingerAction = try { MultiFingerAction.valueOf(preferences[PreferencesKeys.TWO_FINGER_ACTION] ?: MultiFingerAction.PLAY_PAUSE.name) } catch (e: Exception) { MultiFingerAction.PLAY_PAUSE },
                threeFingerAction = try { MultiFingerAction.valueOf(preferences[PreferencesKeys.THREE_FINGER_ACTION] ?: MultiFingerAction.FAST_PLAY.name) } catch (e: Exception) { MultiFingerAction.FAST_PLAY },
                longPressEnabled = preferences[PreferencesKeys.LONG_PRESS_ENABLED] ?: true,
                longPressSpeed = preferences[PreferencesKeys.LONG_PRESS_SPEED] ?: 2.0f,
                doubleTapAction = try { DoubleTapAction.valueOf(preferences[PreferencesKeys.DOUBLE_TAP_ACTION] ?: DoubleTapAction.BOTH.name) } catch (e: Exception) { DoubleTapAction.BOTH },
                topLeftControls = preferences[PreferencesKeys.TOP_LEFT_CONTROLS] ?: "BACK_ARROW,VIDEO_TITLE",
                topRightControls = preferences[PreferencesKeys.TOP_RIGHT_CONTROLS] ?: "DECODER,SUBTITLES,AUDIO_TRACK,MORE_OPTIONS",
                bottomLeftControls = preferences[PreferencesKeys.BOTTOM_LEFT_CONTROLS] ?: "LOCK_CONTROLS,PICTURE_IN_PICTURE",
                bottomRightControls = preferences[PreferencesKeys.BOTTOM_RIGHT_CONTROLS] ?: "ASPECT_RATIO,SCREEN_ROTATION",
                portraitTopLeftControls = preferences[PreferencesKeys.PORTRAIT_TOP_LEFT_CONTROLS] ?: "BACK_ARROW,VIDEO_TITLE",
                portraitTopRightControls = preferences[PreferencesKeys.PORTRAIT_TOP_RIGHT_CONTROLS] ?: "SUBTITLES,AUDIO_TRACK,MORE_OPTIONS",
                portraitBottomControls = preferences[PreferencesKeys.PORTRAIT_BOTTOM_CONTROLS] ?: "DECODER,ASPECT_RATIO,SCREEN_ROTATION,LOCK_CONTROLS,PICTURE_IN_PICTURE",
                isBottomLayoutEnabled = preferences[PreferencesKeys.IS_BOTTOM_LAYOUT_ENABLED] ?: false,
                showControlGradients = preferences[PreferencesKeys.SHOW_CONTROL_GRADIENTS] ?: true,
                showNextPrevButtons = preferences[PreferencesKeys.SHOW_NEXT_PREV_BUTTONS] ?: true,
                aspectMode = try { AspectMode.valueOf(preferences[PreferencesKeys.ASPECT_MODE] ?: AspectMode.FIT.name) } catch (e: Exception) { AspectMode.FIT },
                backgroundPlayEnabled = preferences[PreferencesKeys.BACKGROUND_PLAY_ENABLED] ?: false,
                subtitleTextSizeScale = preferences[PreferencesKeys.SUBTITLE_TEXT_SIZE_SCALE] ?: 1.0f,
                subtitleBgStyle = preferences[PreferencesKeys.SUBTITLE_BG_STYLE] ?: 1,
                subtitleDelayMs = preferences[PreferencesKeys.SUBTITLE_DELAY_MS] ?: 0L,
                subtitleVerticalOffset = preferences[PreferencesKeys.SUBTITLE_VERTICAL_OFFSET] ?: 0f,
                subtitleGesturesEnabled = preferences[PreferencesKeys.SUBTITLE_GESTURES_ENABLED] ?: true,
                tapAndHoldSpeed = preferences[PreferencesKeys.TAP_AND_HOLD_SPEED] ?: 2.0f,
                doubleTapSeekDuration = preferences[PreferencesKeys.DOUBLE_TAP_SEEK_DURATION] ?: 10000L,
                keepAwakeAlways = preferences[PreferencesKeys.KEEP_AWAKE_ALWAYS] ?: false,
                audioBoosterEnabled = preferences[PreferencesKeys.AUDIO_BOOSTER_ENABLED] ?: false,
                showUpNextQueue = preferences[PreferencesKeys.SHOW_UP_NEXT_QUEUE] ?: true,
                isAmbientModeEnabled = preferences[PreferencesKeys.IS_AMBIENT_MODE_ENABLED] ?: false,
                ambientBlurStyle = preferences[PreferencesKeys.AMBIENT_BLUR_STYLE] ?: "standard",
                saveBrightnessLevel = preferences[PreferencesKeys.SAVE_BRIGHTNESS_LEVEL] ?: true
            )
        }

    val isDarkThemeFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        if (prefs[PreferencesKeys.DARK_THEME_SET] == true) {
            prefs[PreferencesKeys.DARK_THEME]
        } else null
    }

    val isAmoledThemeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AMOLED_THEME] ?: false
    }

    val defaultAudioLangFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DEFAULT_AUDIO_LANG] ?: ""
    }

    val defaultSubtitleLangFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DEFAULT_SUBTITLE_LANG] ?: ""
    }

    val isDeveloperModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DEVELOPER_MODE] ?: false
    }

    val useModernPlayerStyleFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.Modern_PLAYER_STYLE] ?: true
    }

    val hasSeenOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.HAS_SEEN_ONBOARDING] ?: false
    }

    val dynamicColorFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DYNAMIC_COLOR] ?: false
    }

    val selectedPaletteFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SELECTED_PALETTE] ?: "BLUE"
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DARK_THEME] = isDark
            prefs[PreferencesKeys.DARK_THEME_SET] = true
        }
    }

    suspend fun resetDarkTheme() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DARK_THEME_SET] = false
            prefs.remove(PreferencesKeys.DARK_THEME)
        }
    }

    suspend fun setAmoledTheme(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AMOLED_THEME] = enabled }
    }

    suspend fun setDefaultAudioLanguage(langCode: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_AUDIO_LANG] = langCode }
    }

    suspend fun setDefaultSubtitleLanguage(langCode: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_SUBTITLE_LANG] = langCode }
    }

    suspend fun setDeveloperMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DEVELOPER_MODE] = enabled
        }
    }

    suspend fun setModernPlayerStyle(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.Modern_PLAYER_STYLE] = enabled
        }
    }

    suspend fun setHasSeenOnboarding(seen: Boolean = true) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.HAS_SEEN_ONBOARDING] = seen
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setSelectedPalette(paletteName: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_PALETTE] = paletteName
        }
    }

    suspend fun updateSeekDuration(durationSeconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEEK_DURATION] = durationSeconds
        }
    }

    suspend fun updateSeekBarStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEEK_BAR_STYLE] = style
        }
    }

    suspend fun updateControlIconSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTROL_ICON_SIZE] = size
        }
    }

    suspend fun updateAutoPlayEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_PLAY] = enabled
        }
    }

    suspend fun updateShowSeekButtons(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SEEK_BUTTONS] = show
        }
    }

    suspend fun updateFastplaySpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FASTPLAY_SPEED] = speed
        }
    }

    suspend fun updateOrientationMode(mode: OrientationMode) {
        context.dataStore.edit { it[PreferencesKeys.ORIENTATION_MODE] = mode.name }
    }

    suspend fun updateFullScreenMode(mode: FullScreenMode) {
        context.dataStore.edit { it[PreferencesKeys.FULL_SCREEN_MODE] = mode.name }
    }

    suspend fun updateSoftButtonMode(mode: SoftButtonMode) {
        context.dataStore.edit { it[PreferencesKeys.SOFT_BUTTON_MODE] = mode.name }
    }

    suspend fun updateShowElapsedTimeOverlay(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_ELAPSED_TIME_OVERLAY] = show }
    }

    suspend fun updateShowBatteryClockOverlay(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_BATTERY_CLOCK_OVERLAY] = show }
    }

    suspend fun updateShowScreenRotationButton(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_SCREEN_ROTATION_BUTTON] = show }
    }

    suspend fun updatePauseWhenObstructed(pause: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PAUSE_WHEN_OBSTRUCTED] = pause }
    }

    suspend fun updateShowRemainingTime(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_REMAINING_TIME] = show }
    }

    suspend fun updateDecoderMode(mode: DecoderMode) {
        context.dataStore.edit { it[PreferencesKeys.DECODER_MODE] = mode.name }
    }

    val isNavBarTransparentFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.NAV_BAR_TRANSPARENT] ?: true
    }

    suspend fun setNavBarTransparent(transparent: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NAV_BAR_TRANSPARENT] = transparent }
    }

    suspend fun updateUseSystemCaptionStyle(useSystem: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.USE_SYSTEM_CAPTION_STYLE] = useSystem }
    }

    suspend fun updateSubtitleFont(font: SubtitleFont) {
        context.dataStore.edit { it[PreferencesKeys.SUBTITLE_FONT] = font.name }
    }

    suspend fun updateIsSubtitleBold(isBold: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_SUBTITLE_BOLD] = isBold }
    }

    suspend fun updateForceAssSubtitleOverride(force: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.FORCE_ASS_SUBTITLE_OVERRIDE] = force }
    }
    suspend fun updateSeekGestureEnabled(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.SEEK_GESTURE_ENABLED] = enabled } }
    suspend fun updateSeekSensitivity(sensitivity: Float) { context.dataStore.edit { it[PreferencesKeys.SEEK_SENSITIVITY] = sensitivity } }
    suspend fun updateBrightnessGestureEnabled(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.BRIGHTNESS_GESTURE_ENABLED] = enabled } }
    suspend fun updateBrightnessSensitivity(sensitivity: Float) { context.dataStore.edit { it[PreferencesKeys.BRIGHTNESS_SENSITIVITY] = sensitivity } }
    suspend fun updateVolumeGestureEnabled(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.VOLUME_GESTURE_ENABLED] = enabled } }
    suspend fun updateVolumeSensitivity(sensitivity: Float) { context.dataStore.edit { it[PreferencesKeys.VOLUME_SENSITIVITY] = sensitivity } }
    suspend fun updateTwoFingerAction(action: MultiFingerAction) { context.dataStore.edit { it[PreferencesKeys.TWO_FINGER_ACTION] = action.name } }
    suspend fun updateThreeFingerAction(action: MultiFingerAction) { context.dataStore.edit { it[PreferencesKeys.THREE_FINGER_ACTION] = action.name } }
    suspend fun updateLongPressEnabled(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.LONG_PRESS_ENABLED] = enabled } }
    suspend fun updateLongPressSpeed(speed: Float) { context.dataStore.edit { it[PreferencesKeys.LONG_PRESS_SPEED] = speed } }
    suspend fun updateDoubleTapAction(action: DoubleTapAction) { context.dataStore.edit { it[PreferencesKeys.DOUBLE_TAP_ACTION] = action.name } }

    suspend fun updateTopLeftControls(controls: String) { context.dataStore.edit { it[PreferencesKeys.TOP_LEFT_CONTROLS] = controls } }
    suspend fun updateTopRightControls(controls: String) { context.dataStore.edit { it[PreferencesKeys.TOP_RIGHT_CONTROLS] = controls } }
    suspend fun updateBottomLeftControls(controls: String) { context.dataStore.edit { it[PreferencesKeys.BOTTOM_LEFT_CONTROLS] = controls } }
    suspend fun updateBottomRightControls(controls: String) { context.dataStore.edit { it[PreferencesKeys.BOTTOM_RIGHT_CONTROLS] = controls } }
    suspend fun updatePortraitTopLeftControls(controls: String) { context.dataStore.edit { it[PreferencesKeys.PORTRAIT_TOP_LEFT_CONTROLS] = controls } }
    suspend fun updatePortraitTopRightControls(controls: String) { context.dataStore.edit { it[PreferencesKeys.PORTRAIT_TOP_RIGHT_CONTROLS] = controls } }
    suspend fun updatePortraitBottomControls(controls: String) { context.dataStore.edit { it[PreferencesKeys.PORTRAIT_BOTTOM_CONTROLS] = controls } }
    suspend fun updateIsBottomLayoutEnabled(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.IS_BOTTOM_LAYOUT_ENABLED] = enabled } }
    suspend fun updateShowControlGradients(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.SHOW_CONTROL_GRADIENTS] = enabled } }
    suspend fun updateShowNextPrevButtons(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.SHOW_NEXT_PREV_BUTTONS] = enabled } }
    suspend fun updateAspectMode(mode: AspectMode) { context.dataStore.edit { it[PreferencesKeys.ASPECT_MODE] = mode.name } }
    suspend fun updateBackgroundPlayEnabled(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.BACKGROUND_PLAY_ENABLED] = enabled } }
    suspend fun updateSubtitleTextSizeScale(scale: Float) { context.dataStore.edit { it[PreferencesKeys.SUBTITLE_TEXT_SIZE_SCALE] = scale } }
    suspend fun updateSubtitleBgStyle(style: Int) { context.dataStore.edit { it[PreferencesKeys.SUBTITLE_BG_STYLE] = style } }
    suspend fun updateSubtitleDelayMs(delayMs: Long) { context.dataStore.edit { it[PreferencesKeys.SUBTITLE_DELAY_MS] = delayMs } }
    suspend fun updateSubtitleVerticalOffset(offset: Float) { context.dataStore.edit { it[PreferencesKeys.SUBTITLE_VERTICAL_OFFSET] = offset } }
    suspend fun updateSubtitleGesturesEnabled(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.SUBTITLE_GESTURES_ENABLED] = enabled } }
    suspend fun updateTapAndHoldSpeed(speed: Float) { context.dataStore.edit { it[PreferencesKeys.TAP_AND_HOLD_SPEED] = speed } }
    suspend fun updateDoubleTapSeekDuration(durationMs: Long) { context.dataStore.edit { it[PreferencesKeys.DOUBLE_TAP_SEEK_DURATION] = durationMs } }
    suspend fun updateKeepAwakeAlways(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.KEEP_AWAKE_ALWAYS] = enabled } }
    suspend fun updateAudioBoosterEnabled(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.AUDIO_BOOSTER_ENABLED] = enabled } }
    suspend fun updateShowUpNextQueue(show: Boolean) { context.dataStore.edit { it[PreferencesKeys.SHOW_UP_NEXT_QUEUE] = show } }
    suspend fun updateIsAmbientModeEnabled(enabled: Boolean) { context.dataStore.edit { it[PreferencesKeys.IS_AMBIENT_MODE_ENABLED] = enabled } }
    suspend fun updateAmbientBlurStyle(style: String) { context.dataStore.edit { it[PreferencesKeys.AMBIENT_BLUR_STYLE] = style } }
    suspend fun updateSaveBrightnessLevel(save: Boolean) { context.dataStore.edit { it[PreferencesKeys.SAVE_BRIGHTNESS_LEVEL] = save } }
}
