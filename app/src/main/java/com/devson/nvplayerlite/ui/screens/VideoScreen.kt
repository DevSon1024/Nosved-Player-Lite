package com.devson.nvplayerlite.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import android.view.KeyEvent
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import com.devson.nvplayerlite.ui.components.DeviceStatsOverlay
import com.devson.nvplayerlite.ui.components.GestureOverlay
import com.devson.nvplayerlite.ui.components.PlayerControls
import com.devson.nvplayerlite.ui.components.ModernStylePlayerControls
import com.devson.nvplayerlite.ui.components.CapsulePlayerControls
import com.devson.nvplayerlite.model.AspectMode
import com.devson.nvplayerlite.model.PlayerButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.media3.ui.AspectRatioFrameLayout
import com.devson.nvplayerlite.ui.components.InformationBottomSheet
import com.devson.nvplayerlite.ui.components.ComposeSubtitleOverlay
import com.devson.nvplayerlite.ui.components.sheets.PlayerSettingsSideSheet
import com.devson.nvplayerlite.ui.components.sheets.AudioSettingsSideSheet
import com.devson.nvplayerlite.ui.components.sheets.SubtitleSettingsSideSheet
import com.devson.nvplayerlite.ui.components.sheets.DecoderSideSheet
import com.devson.nvplayerlite.ui.components.sheets.ChaptersSideSheet
import com.devson.nvplayerlite.model.PlayerSettingsActions
import com.devson.nvplayerlite.util.formatDuration
import com.devson.nvplayerlite.viewmodel.VideoViewModel
import com.devson.nvplayerlite.viewmodel.SettingsViewModel

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    viewModel: VideoViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val player by viewModel.playerInstance.collectAsState()
    val playingIntent by viewModel.playingIntent.collectAsState()

    // --- derivedStateOf: prevents whole-screen recomposition on rapid playback updates ---
    // Only composables that READ these values recompose when they change, not VideoScreen
    // as a whole. This is the primary fix for timeline-scrub jitter.
    val rawCurrentPosition by viewModel.currentPosition?.collectAsState(initial = 0L)
        ?: remember { mutableStateOf(0L) }
    val currentPosition by remember { derivedStateOf { rawCurrentPosition } }

    val rawDuration by viewModel.duration?.collectAsState(initial = 0L)
        ?: remember { mutableStateOf(0L) }
    val duration by remember { derivedStateOf { rawDuration } }

    val rawBufferedPosition by viewModel.bufferedPosition?.collectAsState(initial = 0L)
        ?: remember { mutableStateOf(0L) }
    @Suppress("unused")
    val bufferedPosition by remember { derivedStateOf { rawBufferedPosition } }

    val rawVideoFps by viewModel.videoFps?.collectAsState(initial = 0f)
        ?: remember { mutableStateOf(0f) }
    val videoFps by remember { derivedStateOf { rawVideoFps } }

    val rawVideoDecoderName by viewModel.videoDecoderName?.collectAsState(initial = null)
        ?: remember { mutableStateOf<String?>(null) }
    val videoDecoderName by remember { derivedStateOf { rawVideoDecoderName } }
    // -------------------------------------------------------------------------------

    val controlsVisible by viewModel.controlsVisible.collectAsState()
    val currentVideo by viewModel.currentVideo.collectAsState()
    val isPortrait by viewModel.isPortraitVideo?.collectAsState(initial = null)
        ?: remember { mutableStateOf<Boolean?>(null) }

    val showStats by viewModel.showStats.collectAsState()

    val playerError by viewModel.playerError?.collectAsState(initial = null)
        ?: remember { mutableStateOf<String?>(null) }

    val resizeMode by viewModel.resizeMode.collectAsState()
    val seekDurationSeconds by viewModel.seekDurationSeconds.collectAsState()
    val seekBarStyle by viewModel.seekBarStyle.collectAsState()
    val controlIconSize by viewModel.controlIconSize.collectAsState()
    val autoPlayEnabled by viewModel.autoPlayEnabled.collectAsState()
    val showSeekButtons by viewModel.showSeekButtons.collectAsState()
    val fastplaySpeed by viewModel.fastplaySpeed.collectAsState()
    val currentPlaylist by viewModel.currentPlaylist.collectAsState()
    val currentPlaylistIndex by viewModel.currentPlaylistIndex.collectAsState()
    val isSeeking by viewModel.isSeeking.collectAsState()
    val seekPreviewPosition by viewModel.seekPreviewPosition.collectAsState()

    val displayedPosition = if (isSeeking) seekPreviewPosition else currentPosition

    val playbackSettings by settingsViewModel.playbackSettings.collectAsState()
    val topLeftButtons by settingsViewModel.topLeftControls.collectAsState()
    val topRightButtons by settingsViewModel.topRightControls.collectAsState()
    val bottomLeftButtons by settingsViewModel.bottomLeftControls.collectAsState()
    val bottomRightButtons by settingsViewModel.bottomRightControls.collectAsState()
    val portraitTopLeftButtons by settingsViewModel.portraitTopLeftControls.collectAsState()
    val portraitTopRightButtons by settingsViewModel.portraitTopRightControls.collectAsState()
    val portraitBottomButtons by settingsViewModel.portraitBottomControls.collectAsState()

    var showUnlockButton by remember { mutableStateOf(false) }

    val orientationMode = playbackSettings.orientationMode
    val fullScreenMode = playbackSettings.fullScreenMode
    val softButtonMode = playbackSettings.softButtonMode
    val showElapsedTimeOverlay = playbackSettings.showElapsedTimeOverlay
    val showBatteryClockOverlay = playbackSettings.showBatteryClockOverlay
    val showScreenRotationButton = playbackSettings.showScreenRotationButton
    val pauseWhenObstructed = playbackSettings.pauseWhenObstructed
    val showRemainingTime = playbackSettings.showRemainingTime

    //  Player style preference 
    // Reads the player UI style set in SettingsScreen (default = false = default style)
    val useModernStyle by settingsViewModel.useModernPlayerStyle.collectAsState()

    var isLocked by remember { mutableStateOf(false) }
    var zoomScale by remember { mutableFloatStateOf(1f) }

    // Tracks & Subtitles state
    val audioTracks by viewModel.audioTracks?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val selectedAudioIndex by viewModel.selectedAudioIndex?.collectAsState(initial = -1) ?: remember { mutableStateOf(-1) }
    val isAudioBoostEnabled by viewModel.isAudioBoostEnabled?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }

    val subtitleTracks by viewModel.subtitleTracks?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val selectedSubtitleIndex by viewModel.selectedSubtitleIndex?.collectAsState(initial = -1) ?: remember { mutableStateOf(-1) }

    val subtitleTextSizeScale by viewModel.subtitleTextSizeScale.collectAsState()
    val subtitleBgStyle by viewModel.subtitleBgStyle.collectAsState()
    val subtitleDelayMs by viewModel.subtitleDelayMs.collectAsState()
    val subtitleVerticalOffset by viewModel.subtitleVerticalOffset.collectAsState()
    val currentDecoder by viewModel.currentDecoderMode.collectAsState()
    val currentPlaybackSpeed by viewModel.currentPlaybackSpeed.collectAsState()
    val updatedCurrentPlaybackSpeed by rememberUpdatedState(currentPlaybackSpeed)

    var preFastForwardSpeed by remember { mutableFloatStateOf(1f) }

    var isSubtitleGestureEnabled by rememberSaveable { mutableStateOf(true) }

    // Chapters state
    val chapters by viewModel.chapters?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val hasChapters = chapters.isNotEmpty()

    // Side sheet states from NosvedPlayer
    var showPlayerSettingsSideSheet by rememberSaveable { mutableStateOf(false) }
    var showAudioSettingsSideSheet by rememberSaveable { mutableStateOf(false) }
    var showSubtitleSettingsSideSheet by rememberSaveable { mutableStateOf(false) }
    var showDecoderSideSheet by rememberSaveable { mutableStateOf(false) }
    var showChaptersSideSheet by rememberSaveable { mutableStateOf(false) }

    var showInfoSheet by remember { mutableStateOf(false) }

    val isAnySideSheetVisible = showPlayerSettingsSideSheet ||
            showAudioSettingsSideSheet ||
            showSubtitleSettingsSideSheet ||
            showDecoderSideSheet ||
            showChaptersSideSheet ||
            showInfoSheet

    val shouldInterceptBack = isLocked || isAnySideSheetVisible

    val handleBack: () -> Unit = {
        if (isLocked) {
            showUnlockButton = true
        } else if (showPlayerSettingsSideSheet) {
            showPlayerSettingsSideSheet = false
        } else if (showAudioSettingsSideSheet) {
            showAudioSettingsSideSheet = false
        } else if (showSubtitleSettingsSideSheet) {
            showSubtitleSettingsSideSheet = false
        } else if (showDecoderSideSheet) {
            showDecoderSideSheet = false
        } else if (showChaptersSideSheet) {
            showChaptersSideSheet = false
        } else if (showInfoSheet) {
            showInfoSheet = false
        } else {
            viewModel.stopVideo()
        }
    }

    BackHandler(enabled = shouldInterceptBack) {
        handleBack()
    }

    val playerSettingsActions = remember(viewModel, settingsViewModel) {
        PlayerSettingsActions(
            onSpeedSelected = { viewModel.setPlaybackSpeed(it) },
            onUpdateDoubleTapAction = { settingsViewModel.updateDoubleTapAction(it) },
            onUpdateDoubleTapSeekDuration = { settingsViewModel.updateDoubleTapSeekDuration(it) },
            onUpdateTwoFingerAction = { settingsViewModel.updateTwoFingerAction(it) },
            onUpdateThreeFingerAction = { settingsViewModel.updateThreeFingerAction(it) },
            onUpdateLongPressEnabled = { settingsViewModel.updateLongPressEnabled(it) },
            onUpdateLongPressSpeed = { settingsViewModel.updateLongPressSpeed(it) },
            onUpdateTapAndHoldSpeed = { settingsViewModel.updateTapAndHoldSpeed(it) },
            onUpdateOrientationMode = { settingsViewModel.updateOrientationMode(it) },
            onUpdateFullScreenMode = { settingsViewModel.updateFullScreenMode(it) },
            onUpdateAspectMode = { settingsViewModel.updateAspectMode(it) },
            onUpdateSoftButtonMode = { settingsViewModel.updateSoftButtonMode(it) },
            onUpdateControlIconSize = { settingsViewModel.updateControlIconSize(it) },
            onUpdateSeekBarStyle = { settingsViewModel.updateSeekBarStyle(it) },
            onUpdateAutoPlayEnabled = { settingsViewModel.updateAutoPlayEnabled(it) },
            onUpdateShowSeekButtons = { settingsViewModel.updateShowSeekButtons(it) },
            onUpdateShowNextPrevButtons = { settingsViewModel.updateShowNextPrevButtons(it) },
            onUpdateShowRemainingTime = { settingsViewModel.updateShowRemainingTime(it) },
            onUpdateShowBatteryClockOverlay = { settingsViewModel.updateShowBatteryClockOverlay(it) },
            onUpdatePauseWhenObstructed = { settingsViewModel.updatePauseWhenObstructed(it) },
            onUpdateKeepAwakeAlways = { settingsViewModel.updateKeepAwakeAlways(it) },
            onUpdateShowUpNextQueue = { settingsViewModel.updateShowUpNextQueue(it) },
            onUpdateIsAmbientModeEnabled = { settingsViewModel.updateIsAmbientModeEnabled(it) },
            onUpdateAmbientBlurStyle = { settingsViewModel.updateAmbientBlurStyle(it) },
            onUpdateSaveBrightnessLevel = { settingsViewModel.updateSaveBrightnessLevel(it) },
            onUpdateIsBottomLayoutEnabled = { settingsViewModel.updateIsBottomLayoutEnabled(it) },
            onUpdateShowControlGradients = { settingsViewModel.updateShowControlGradients(it) },
            onUpdateBackgroundPlayEnabled = { settingsViewModel.updateBackgroundPlayEnabled(it) }
        )
    }

    // Encoding state for the external subtitle picker
    var pendingExternalEncoding by remember { mutableStateOf("UTF-8") }

    val externalSubtitleLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri) ?: "application/x-subrip"
            viewModel.loadExternalSubtitle(uri, mimeType, pendingExternalEncoding)
        }
    }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    var volumeLevel by remember {
        mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume)
    }
    var accumulatedVolume by remember { mutableStateOf(volumeLevel) }
    var showVolumeFeedback by remember { mutableStateOf(false) }
    var volumeFeedbackTrigger by remember { mutableStateOf(0) }

    var brightnessLevel by remember {
        mutableStateOf(
            activity?.window?.attributes?.screenBrightness?.takeIf { it >= 0f } ?: 0.5f
        )
    }
    var showBrightnessFeedback by remember { mutableStateOf(false) }
    var brightnessFeedbackTrigger by remember { mutableStateOf(0) }

    // Modern-style fast-forward - driven by GestureOverlay
    var ytIsFastForwarding by remember { mutableStateOf(false) }

    LaunchedEffect(volumeFeedbackTrigger) {
        if (volumeFeedbackTrigger > 0) { delay(1000); showVolumeFeedback = false }
    }
    LaunchedEffect(brightnessFeedbackTrigger) {
        if (brightnessFeedbackTrigger > 0) { delay(1000); showBrightnessFeedback = false }
    }

    val originalWindowParams = remember {
        val window = activity?.window
        val insetsController = if (window != null) androidx.core.view.WindowCompat.getInsetsController(window, window.decorView) else null
        @Suppress("DEPRECATION")
        object {
            val statusBarColor = window?.statusBarColor
            val navigationBarColor = window?.navigationBarColor
            val isNavBarContrastEnforced = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window?.isNavigationBarContrastEnforced else null
            val isStatusBarContrastEnforced = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window?.isStatusBarContrastEnforced else null
            val systemBarsBehavior = insetsController?.systemBarsBehavior
            val screenBrightness = window?.attributes?.screenBrightness
            val isAppearanceLightStatusBars = insetsController?.isAppearanceLightStatusBars
            val isAppearanceLightNavigationBars = insetsController?.isAppearanceLightNavigationBars
        }
    }

    val originalOrientation = remember {
        activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    LaunchedEffect(Unit) {
        viewModel.initializePlayer(context)
    }

    LaunchedEffect(playerError) {
        if (playerError != null) {
            Toast.makeText(context, "Playback Error: $playerError", Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(orientationMode, isPortrait) {
        activity ?: return@LaunchedEffect
        when (orientationMode) {
            com.devson.nvplayerlite.repository.OrientationMode.SYSTEM_DEFAULT,
            com.devson.nvplayerlite.repository.OrientationMode.VIDEO_ORIENTATION -> {
                val portrait = isPortrait ?: return@LaunchedEffect
                activity.requestedOrientation = if (portrait)
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            com.devson.nvplayerlite.repository.OrientationMode.LANDSCAPE -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            com.devson.nvplayerlite.repository.OrientationMode.REVERSE_LANDSCAPE -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
            com.devson.nvplayerlite.repository.OrientationMode.AUTO_ROTATION -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, pauseWhenObstructed) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (pauseWhenObstructed && event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                if (player?.isPlaying == true) {
                    player?.pause()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(fullScreenMode, softButtonMode) {
        val window = activity?.window
        if (window != null) {
            androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (fullScreenMode == com.devson.nvplayerlite.repository.FullScreenMode.ON) {
                    hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                } else if (fullScreenMode == com.devson.nvplayerlite.repository.FullScreenMode.OFF) {
                    show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                } else {
                    hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                }
                
                if (softButtonMode == com.devson.nvplayerlite.repository.SoftButtonMode.HIDE) {
                    hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
                } else if (softButtonMode == com.devson.nvplayerlite.repository.SoftButtonMode.SHOW) {
                    show(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
                } else {
                    hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (window != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
                window.isStatusBarContrastEnforced = false
            }
            insetsController.systemBarsBehavior =
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
            // hideSystemUI(activity) // Handled by LaunchedEffect now
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                @Suppress("DEPRECATION")
                originalWindowParams.statusBarColor?.let { window.statusBarColor = it }
                @Suppress("DEPRECATION")
                originalWindowParams.navigationBarColor?.let { window.navigationBarColor = it }
                @Suppress("DEPRECATION")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    originalWindowParams.isNavBarContrastEnforced?.let { window.isNavigationBarContrastEnforced = it }
                    originalWindowParams.isStatusBarContrastEnforced?.let { window.isStatusBarContrastEnforced = it }
                }
                originalWindowParams.systemBarsBehavior?.let { insetsController.systemBarsBehavior = it }
                originalWindowParams.isAppearanceLightStatusBars?.let { insetsController.isAppearanceLightStatusBars = it }
                originalWindowParams.isAppearanceLightNavigationBars?.let { insetsController.isAppearanceLightNavigationBars = it }
                val lp = window.attributes
                lp.screenBrightness = originalWindowParams.screenBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = lp
                showSystemUI(activity)
            }
            activity?.requestedOrientation = originalOrientation
        }
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    //  Player surface 
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                val keyCode = event.key.nativeKeyCode
                if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                    if (event.type == KeyEventType.KeyUp) {
                        if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                            viewModel.playNextVideo()
                        } else {
                            viewModel.playPreviousVideo()
                        }
                    }
                    true
                } else {
                    false
                }
            }
    ) {
        key(player) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        this.player = player
                        subtitleView?.visibility = android.view.View.GONE
                    }
                },
                update = { playerView ->
                    playerView.player = player
                    val aspect = playbackSettings.aspectMode
                    playerView.resizeMode = when (aspect) {
                        AspectMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        AspectMode.CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                    
                    playerView.subtitleView?.apply {
                        visibility = android.view.View.GONE
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = zoomScale
                        scaleY = zoomScale
                    }
            )
        }

        //  Gesture overlay (volume / brightness swipe - always active) 
        GestureOverlay(
            modifier = Modifier.fillMaxSize(),
            seekDurationSeconds = seekDurationSeconds,
            isPlaying = playingIntent,
            isLocked = isLocked,
            fastplaySpeed = fastplaySpeed,
            currentPosition = currentPosition,
            duration = duration,

            // Pass all custom gesture settings down
            seekGestureEnabled = playbackSettings.seekGestureEnabled,
            seekSensitivity = playbackSettings.seekSensitivity,
            brightnessGestureEnabled = playbackSettings.brightnessGestureEnabled,
            brightnessSensitivity = playbackSettings.brightnessSensitivity,
            volumeGestureEnabled = playbackSettings.volumeGestureEnabled,
            volumeSensitivity = playbackSettings.volumeSensitivity,
            twoFingerAction = playbackSettings.twoFingerAction,
            threeFingerAction = playbackSettings.threeFingerAction,
            longPressEnabled = playbackSettings.longPressEnabled,
            longPressSpeed = playbackSettings.tapAndHoldSpeed.takeIf { it > 0f } ?: playbackSettings.longPressSpeed,
            doubleTapAction = playbackSettings.doubleTapAction,

            onSingleTap = { viewModel.toggleControlsVisibility() },
            onDoubleTapLeft = {
                viewModel.seekBackward()
            },
            onDoubleTapCenter = { viewModel.togglePlayPause() },
            onDoubleTapRight = {
                viewModel.seekForward()
            },
            onSeekStart = { viewModel.beginSeek() },
            onSeekPreview = { pos -> viewModel.updateSeekPreview(pos) },
            onSeekCommit = { absPos -> viewModel.updateSeekPreview(absPos); viewModel.commitSeek() },

            // Revert back to proper currentPlaybackSpeed instead of hardcoded 1f
            onFastForwardToggle = { active, activeSpeed ->
                if (active) {
                    if (!ytIsFastForwarding) {
                        preFastForwardSpeed = updatedCurrentPlaybackSpeed  // always fresh
                        ytIsFastForwarding = true
                        viewModel.setPlaybackSpeed(activeSpeed)
                    }
                } else {
                    if (ytIsFastForwarding) {
                        ytIsFastForwarding = false
                        viewModel.setPlaybackSpeed(preFastForwardSpeed)
                    }
                }
            },

            onZoom = { factor -> zoomScale = (zoomScale * factor).coerceIn(1f, 4f) },
            zoomScale = zoomScale,
            onVolumeSwipe = { delta ->
                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                accumulatedVolume += delta
                val newVol = (accumulatedVolume * maxVolume).toInt().coerceIn(0, maxVolume)
                if (newVol != currentVol) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                    val actualVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (actualVol < newVol) {
                        accumulatedVolume = actualVol.toFloat() / maxVolume
                    }
                    volumeLevel = actualVol.toFloat() / maxVolume
                }
                showVolumeFeedback = true
                volumeFeedbackTrigger++
            },
            onBrightnessSwipe = { delta ->
                val window = activity?.window
                if (window != null) {
                    val lp = window.attributes
                    val currentBrightness = if (lp.screenBrightness >= 0f) lp.screenBrightness else 0.5f
                    val newBrightness = (currentBrightness + delta).coerceIn(0.01f, 1f)
                    lp.screenBrightness = newBrightness
                    window.attributes = lp
                    brightnessLevel = newBrightness
                    showBrightnessFeedback = true
                    brightnessFeedbackTrigger++
                }
            },
            volumeLevel = volumeLevel,
            brightnessLevel = brightnessLevel,
            showVolumeFeedback = showVolumeFeedback,
            showBrightnessFeedback = showBrightnessFeedback,
            isAudioBoostEnabled = isAudioBoostEnabled
        )

        ComposeSubtitleOverlay(
            player = player, 
            textSizeScale = subtitleTextSizeScale, 
            bgStyle = subtitleBgStyle,
            useSystemCaptionStyle = playbackSettings.useSystemCaptionStyle,
            subtitleFont = playbackSettings.subtitleFont,
            isSubtitleBold = playbackSettings.isSubtitleBold,
            forceAssSubtitleOverride = playbackSettings.forceAssSubtitleOverride,
            isSubtitleGestureEnabled = isSubtitleGestureEnabled,
            subtitleDelayMs = subtitleDelayMs,
            verticalOffsetFraction = subtitleVerticalOffset
        )

        //  Device stats overlay 
        DeviceStatsOverlay(
            visible = showStats,
            player = player,
            videoFps = videoFps,
            videoDecoderName = videoDecoderName,
            onDismiss = { viewModel.setShowStats(false) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
        )

        if (showElapsedTimeOverlay) {
            Text(
                text = formatDuration(displayedPosition),
                color = Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )
        }

        if (showBatteryClockOverlay) {
            val batteryManager = remember { context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager }
            val sdf = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
            var currentTime by remember { mutableStateOf(sdf.format(java.util.Date())) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000)
                    currentTime = sdf.format(java.util.Date())
                }
            }
            
            // Re-fetch battery percentage on re-compose (can also use a broadcast receiver for real updates)
            var currentBattery by remember { mutableStateOf(batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(60000)
                    currentBattery = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
                }
            }

            Text(
                text = "$currentTime  $currentBattery%",
                color = Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 8.dp)
            )
        }

        //  Player controls - switch between Default and Modern style 
        val hasPrevious = currentPlaylist.isNotEmpty() && currentPlaylistIndex > 0
        val hasNext = currentPlaylist.isNotEmpty() && currentPlaylistIndex in 0 until currentPlaylist.lastIndex

        if (useModernStyle) {
            // Capsule-style player controls from NosvedPlayer with custom control region positioning
            AnimatedVisibility(
                visible = controlsVisible && !isLocked && !isAnySideSheetVisible,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                CapsulePlayerControls(
                    title = currentVideo?.title ?: "",
                    isPlaying = playingIntent,
                    currentPosition = displayedPosition,
                    duration = duration,
                    onPlayPauseToggle = {
                        viewModel.togglePlayPause()
                        viewModel.showControlsAndDelayHide()
                    },
                    onSeek = { targetPos, isFinished ->
                        viewModel.updateSeekPreview(targetPos)
                        if (isFinished) {
                            viewModel.commitSeek()
                        }
                        viewModel.showControlsAndDelayHide()
                    },
                    onBackClick = { handleBack() },
                    onSpeedClick = {
                        showPlayerSettingsSideSheet = true
                        viewModel.showControlsAndDelayHide()
                    },
                    onShowDecoder = {
                        showDecoderSideSheet = true
                        viewModel.showControlsAndDelayHide()
                    },
                    currentDecoder = currentDecoder.displayName,
                    onCycleSubtitle = {
                        showSubtitleSettingsSideSheet = true
                        viewModel.showControlsAndDelayHide()
                    },
                    onCycleAudio = {
                        showAudioSettingsSideSheet = true
                        viewModel.showControlsAndDelayHide()
                    },
                    onShowChapters = {
                        showChaptersSideSheet = true
                        viewModel.showControlsAndDelayHide()
                    },
                    hasChapters = hasChapters,
                    onLockClick = {
                        isLocked = true
                        showUnlockButton = true
                    },
                    onAspectClick = {
                        val nextMode = when (playbackSettings.aspectMode) {
                            AspectMode.FIT -> AspectMode.STRETCH
                            AspectMode.STRETCH -> AspectMode.CROP
                            AspectMode.CROP -> AspectMode.ORIGINAL
                            AspectMode.ORIGINAL -> AspectMode.FIT
                        }
                        settingsViewModel.updateAspectMode(nextMode)
                        Toast.makeText(context, "Aspect: ${nextMode.displayName}", Toast.LENGTH_SHORT).show()
                        viewModel.showControlsAndDelayHide()
                    },
                    currentAspectMode = playbackSettings.aspectMode,
                    onPipClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val pipParams = android.app.PictureInPictureParams.Builder().build()
                            activity?.enterPictureInPictureMode(pipParams)
                        }
                    },
                    onBackgroundPlayClick = {
                        val next = !playbackSettings.backgroundPlayEnabled
                        settingsViewModel.updateBackgroundPlayEnabled(next)
                        Toast.makeText(context, if (next) "Background Playback ON" else "Background Playback OFF", Toast.LENGTH_SHORT).show()
                        viewModel.showControlsAndDelayHide()
                    },
                    onScreenshotClick = {
                        Toast.makeText(context, "Screenshot captured", Toast.LENGTH_SHORT).show()
                        viewModel.showControlsAndDelayHide()
                    },
                    onToggleScreenRotation = {
                        activity?.let { act ->
                            val currentMode = act.requestedOrientation
                            if (currentMode == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE || currentMode == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                        }
                        viewModel.showControlsAndDelayHide()
                    },
                    onTitleClick = {
                        showInfoSheet = true
                        viewModel.showControlsAndDelayHide()
                    },
                    onNextClick = { viewModel.playNextVideo() },
                    onPrevClick = { viewModel.playPreviousVideo() },
                    hasNext = hasNext,
                    hasPrevious = hasPrevious,
                    playbackSpeed = currentPlaybackSpeed,
                    seekBarStyle = playbackSettings.seekBarStyle,
                    controlIconSize = playbackSettings.controlIconSize,
                    showSeekButtons = playbackSettings.showSeekButtons,
                    showNextPrevButtons = playbackSettings.showNextPrevButtons,
                    showRemainingTime = playbackSettings.showRemainingTime,
                    showBatteryClockOverlay = playbackSettings.showBatteryClockOverlay,
                    seekDurationSeconds = seekDurationSeconds,
                    isBottomLayoutEnabled = playbackSettings.isBottomLayoutEnabled,
                    showControlGradients = playbackSettings.showControlGradients,
                    isBackgroundPlayEnabled = playbackSettings.backgroundPlayEnabled,
                    topLeftButtons = topLeftButtons,
                    topRightButtons = topRightButtons,
                    bottomLeftButtons = bottomLeftButtons,
                    bottomRightButtons = bottomRightButtons,
                    portraitTopLeftButtons = portraitTopLeftButtons,
                    portraitTopRightButtons = portraitTopRightButtons,
                    portraitBottomButtons = portraitBottomButtons,
                    resolutionLabel = com.devson.nvplayerlite.util.formatResolutionCompact(currentVideo?.resolution),
                    fileSizeLabel = currentVideo?.size?.let { if (it > 0) com.devson.nvplayerlite.util.formatSize(it) else null },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            //  Default controls 
            PlayerControls(
                isVisible = controlsVisible && !isAnySideSheetVisible,
                isPlaying = playingIntent,
                title = currentVideo?.title ?: "",
                currentPosition = displayedPosition,
                duration = duration,
                showStats = showStats,
                hasPrevious = hasPrevious,
                hasNext = hasNext,
                onBack = { handleBack() },
                onToggleStats = { viewModel.toggleStats() },
                onPlayPauseToggle = {
                    viewModel.togglePlayPause()
                    viewModel.showControlsAndDelayHide()
                },
                onSeekTo = { pos ->
                    viewModel.updateSeekPreview(pos)
                    viewModel.commitSeek()
                    viewModel.showControlsAndDelayHide()
                },
                onSeekForward = {
                    viewModel.seekForward()
                    viewModel.showControlsAndDelayHide()
                },
                onSeekBackward = {
                    viewModel.seekBackward()
                    viewModel.showControlsAndDelayHide()
                },
                onControlsStateChange = { viewModel.toggleControlsVisibility() },
                onToggleResizeMode = {
                    val modeLabel = viewModel.toggleResizeMode()
                    Toast.makeText(context, modeLabel, Toast.LENGTH_SHORT).show()
                    viewModel.showControlsAndDelayHide()
                },
                seekDurationSeconds = seekDurationSeconds,
                seekBarStyle = seekBarStyle,
                controlIconSize = controlIconSize,
                autoPlayEnabled = autoPlayEnabled,
                showSeekButtons = showSeekButtons,
                fastplaySpeed = fastplaySpeed,
                onSeekDurationChange = { viewModel.setSeekDuration(it) },
                onSeekBarStyleChange = { viewModel.setSeekBarStyle(it) },
                onControlIconSizeChange = { viewModel.setControlIconSize(it) },
                onAutoPlayChange = { viewModel.setAutoPlayEnabled(it) },
                onShowSeekButtonsChange = { viewModel.setShowSeekButtons(it) },
                onFastplaySpeedChange = { viewModel.setFastplaySpeed(it) },
                useModernStyle = useModernStyle,
                onModernStyleChange = { settingsViewModel.setModernPlayerStyle(it) },
                onInfoClick = {
                    showInfoSheet = true
                    viewModel.showControlsAndDelayHide()
                },
                onOpenAudioTracks = { showAudioSettingsSideSheet = true },
                onOpenSubtitles = { showSubtitleSettingsSideSheet = true },
                onPlayPrevious = { viewModel.playPreviousVideo() },
                onPlayNext = { viewModel.playNextVideo() },
                isLandscape = isLandscape,
                isLocked = isLocked,
                onToggleLock = {
                    isLocked = !isLocked
                    viewModel.showControlsAndDelayHide()
                },
                onPipToggle = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val pipParams = android.app.PictureInPictureParams.Builder().build()
                        activity?.enterPictureInPictureMode(pipParams)
                    }
                },
                showScreenRotationButton = showScreenRotationButton,
                showRemainingTime = showRemainingTime,
                onShowRemainingTimeChange = { settingsViewModel.updateShowRemainingTime(it) },
                currentDecoder = currentDecoder,
                onSelectDecoder = { showDecoderSideSheet = true },
                onToggleScreenRotation = {
                    val currentMode = activity?.requestedOrientation
                    if (currentMode == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE || currentMode == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                    viewModel.showControlsAndDelayHide()
                },
                onScrubbingModeChange = { viewModel.setScrubbingMode(it) },
                onSpeedMenuClick = { showPlayerSettingsSideSheet = true }
            )
        }
    }

    //  Nosved Player Side Sheets 

    PlayerSettingsSideSheet(
        visible = showPlayerSettingsSideSheet,
        currentSpeed = currentPlaybackSpeed,
        playbackSettings = playbackSettings,
        currentVideo = currentVideo,
        actions = playerSettingsActions,
        onDismiss = { showPlayerSettingsSideSheet = false }
    )

    AudioSettingsSideSheet(
        visible = showAudioSettingsSideSheet,
        audioTracks = audioTracks,
        audioBoosterEnabled = isAudioBoostEnabled,
        onToggleAudioBooster = { viewModel.toggleAudioBoost(it) },
        onSelectAudioTrack = { viewModel.selectAudioTrack(it) },
        onDismiss = { showAudioSettingsSideSheet = false }
    )

    SubtitleSettingsSideSheet(
        visible = showSubtitleSettingsSideSheet,
        playbackSettings = playbackSettings,
        subtitleTracks = subtitleTracks,
        onSelectSubtitleTrack = { viewModel.selectSubtitleTrack(it) },
        onSetSubtitleDelay = { viewModel.setSubtitleDelayMs(it) },
        onUpdateSubtitleFont = { settingsViewModel.updateSubtitleFont(it) },
        onUpdateIsSubtitleBold = { settingsViewModel.updateIsSubtitleBold(it) },
        onUpdateForceAssSubtitleOverride = { settingsViewModel.updateForceAssSubtitleOverride(it) },
        onUpdateSubtitleTextSizeScale = { viewModel.updateSubtitleTextSizeScale(it) },
        onUpdateSubtitleBgStyle = { viewModel.updateSubtitleBgStyle(it) },
        onUpdateSubtitleDelay = { viewModel.setSubtitleDelayMs(it) },
        onUpdateSubtitleVerticalOffset = { viewModel.setSubtitleVerticalOffset(it) },
        onUpdateSubtitleGesturesEnabled = { isSubtitleGestureEnabled = it },
        onDismiss = { showSubtitleSettingsSideSheet = false },
        onImportSubtitleClick = { externalSubtitleLauncher.launch("*/*") }
    )

    DecoderSideSheet(
        visible = showDecoderSideSheet,
        currentMode = currentDecoder,
        onSelectMode = { mode ->
            viewModel.setDecoderMode(mode)
            showDecoderSideSheet = false
        },
        onDismiss = { showDecoderSideSheet = false }
    )

    ChaptersSideSheet(
        visible = showChaptersSideSheet,
        chapters = chapters,
        currentPositionMs = displayedPosition,
        currentPositionProvider = { displayedPosition },
        onSelectChapter = { index ->
            chapters.getOrNull(index)?.let { ch ->
                viewModel.seekTo(ch.timeMs)
            }
            showChaptersSideSheet = false
        },
        onDismiss = { showChaptersSideSheet = false }
    )

    if (showInfoSheet) {
        val videoSet = currentVideo?.let { setOf(it) } ?: emptySet()
        InformationBottomSheet(
            selectedVideos = videoSet,
            onDismiss = { showInfoSheet = false },
            useSideSheet = true
        )
    }
}

private fun hideSystemUI(activity: Activity) {
    val window = activity.window
    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
    }
}

private fun showSystemUI(activity: Activity) {
    val window = activity.window
    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).show(
        androidx.core.view.WindowInsetsCompat.Type.systemBars()
    )
}