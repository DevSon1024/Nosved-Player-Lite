package com.devson.nvplayerlite.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun PlayingIndicator(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val transition = rememberInfiniteTransition(label = "PlayingIndicatorTransition")

    val bar1Fraction by if (isPlaying) {
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 450, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar1"
        )
    } else {
        remember { mutableStateOf(0.2f) }
    }

    val bar2Fraction by if (isPlaying) {
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar2"
        )
    } else {
        remember { mutableStateOf(0.2f) }
    }

    val bar3Fraction by if (isPlaying) {
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 550, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar3"
        )
    } else {
        remember { mutableStateOf(0.2f) }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width / 5f
        val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

        // Bar 1
        val h1 = height * bar1Fraction
        drawRoundRect(
            color = barColor,
            topLeft = Offset(0f, height - h1),
            size = Size(barWidth, h1),
            cornerRadius = cornerRadius
        )

        // Bar 2
        val h2 = height * bar2Fraction
        drawRoundRect(
            color = barColor,
            topLeft = Offset(barWidth * 2f, height - h2),
            size = Size(barWidth, h2),
            cornerRadius = cornerRadius
        )

        // Bar 3
        val h3 = height * bar3Fraction
        drawRoundRect(
            color = barColor,
            topLeft = Offset(barWidth * 4f, height - h3),
            size = Size(barWidth, h3),
            cornerRadius = cornerRadius
        )
    }
}

