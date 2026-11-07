package com.devson.nvplayerlite

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.devson.nvplayerlite.model.Video
import com.devson.nvplayerlite.ui.screens.MainScreen
import com.devson.nvplayerlite.ui.theme.AppThemePalette
import com.devson.nvplayerlite.ui.theme.NosvedPlayerTheme
import com.devson.nvplayerlite.viewmodel.SettingsViewModel
import com.devson.nvplayerlite.viewmodel.VideoViewModel
import kotlinx.coroutines.launch

@UnstableApi
class MainActivity : AppCompatActivity() {

    private var videoViewModelRef: VideoViewModel? = null
    private var isPipRequested = false

    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PIP_PLAY -> {
                    videoViewModelRef?.resumeVideo()
                    updatePipActions(true)
                }
                ACTION_PIP_PAUSE -> {
                    videoViewModelRef?.pauseVideo()
                    updatePipActions(false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val filter = IntentFilter().apply {
            addAction(ACTION_PIP_PLAY)
            addAction(ACTION_PIP_PAUSE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pipReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(pipReceiver, filter)
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val forceDark         by settingsViewModel.isDarkTheme.collectAsState()
            val dynamicColor      by settingsViewModel.dynamicColor.collectAsState()
            val selectedPalette   by settingsViewModel.selectedPalette.collectAsState()
            val navBarTransparent by settingsViewModel.isNavBarTransparent.collectAsState()
            val isAmoledTheme     by settingsViewModel.isAmoledTheme.collectAsState()

            NosvedPlayerTheme(
                forceDark           = forceDark,
                dynamicColor        = dynamicColor,
                palette             = selectedPalette,
                isNavBarTransparent = navBarTransparent,
                isAmoledTheme       = isAmoledTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val videoViewModel: VideoViewModel = viewModel()
                    videoViewModelRef = videoViewModel
                    MainScreen(videoViewModel = videoViewModel)

                    LaunchedEffect(intent) {
                        handleIntent(intent)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                videoViewModelRef?.playingIntent?.collect { isPlaying ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode) {
                        updatePipActions(isPlaying)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                val video = Video(
                    uri = uri.toString(),
                    title = uri.lastPathSegment ?: "External Video"
                )
                videoViewModelRef?.playVideo(video)
            }
        }
    }

    fun enterPipMode(videoWidth: Int = 0, videoHeight: Int = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                isPipRequested = true
                val builder = PictureInPictureParams.Builder()
                if (videoWidth > 0 && videoHeight > 0) {
                    val aspect = videoWidth.toFloat() / videoHeight.toFloat()
                    val clamped = aspect.coerceIn(0.41841f, 2.39f)
                    builder.setAspectRatio(Rational((clamped * 1000).toInt(), 1000))
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    builder.setAutoEnterEnabled(true)
                    builder.setSeamlessResizeEnabled(true)
                }
                val isPlaying = videoViewModelRef?.playingIntent?.value == true
                builder.setActions(buildPipActions(isPlaying))
                val entered = enterPictureInPictureMode(builder.build())
                if (!entered) {
                    isPipRequested = false
                }
            } catch (e: Exception) {
                isPipRequested = false
            }
        }
    }

    fun updatePipActions(isPlaying: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setActions(buildPipActions(isPlaying))
                    .build()
                setPictureInPictureParams(params)
            } catch (e: Exception) {
                // Graceful degradation
            }
        }
    }

    private fun buildPipActions(isPlaying: Boolean): List<RemoteAction> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return emptyList()
        val actions = mutableListOf<RemoteAction>()
        val actionType = if (isPlaying) ACTION_PIP_PAUSE else ACTION_PIP_PLAY
        val intent = Intent(actionType).setPackage(packageName)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getBroadcast(this, if (isPlaying) 101 else 102, intent, flags)
        val iconRes = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val title = if (isPlaying) "Pause" else "Play"
        actions.add(RemoteAction(Icon.createWithResource(this, iconRes), title, title, pendingIntent))
        return actions
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipRequested = false
        videoViewModelRef?.setInPipMode(isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            videoViewModelRef?.resumeVideo()
            updatePipActions(true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (isInPictureInPictureMode || isPipRequested)) {
            return
        }
        videoViewModelRef?.pauseVideo()
    }

    override fun onResume() {
        super.onResume()
        videoViewModelRef?.resumeVideo()
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing || !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode)) {
            videoViewModelRef?.pauseVideo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(pipReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }

    companion object {
        const val ACTION_PIP_PLAY = "com.devson.nvplayerlite.PIP_PLAY"
        const val ACTION_PIP_PAUSE = "com.devson.nvplayerlite.PIP_PAUSE"
    }
}