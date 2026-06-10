package com.devson.nvplayerlite.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.devson.nvplayerlite.model.Video
import com.devson.nvplayerlite.navigation.NavGraph
import com.devson.nvplayerlite.viewmodel.SettingsViewModel
import com.devson.nvplayerlite.viewmodel.VideoViewModel

@Composable
fun MainScreen(
    videoViewModel: VideoViewModel = viewModel()
) {
    val currentVideo by videoViewModel.currentVideo.collectAsState()
    var resumePositionMs by remember { mutableStateOf(0L) }
    val settingsViewModel: SettingsViewModel = viewModel()
    
    val navController = rememberNavController()

    val isPlayerVisible = currentVideo != null

    Box(modifier = Modifier.fillMaxSize()) {
        NavGraph(
            navController = navController,
            videoViewModel = videoViewModel,
            settingsViewModel = settingsViewModel,
            onVideoSelected = { video, playlist, position ->
                resumePositionMs = position
                videoViewModel.playVideo(video, playlist, position)
            }
        )
        
            if (isPlayerVisible) {
            BackHandler {
                videoViewModel.stopVideo()
                resumePositionMs = 0L
            }
            VideoScreen(viewModel = videoViewModel)
        }
    }
}
