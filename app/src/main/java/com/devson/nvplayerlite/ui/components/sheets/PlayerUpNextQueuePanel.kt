package com.devson.nvplayerlite.ui.components.sheets

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devson.nvplayerlite.model.LayoutMode
import com.devson.nvplayerlite.model.Video
import com.devson.nvplayerlite.ui.components.PlayingIndicator
import com.devson.nvplayerlite.ui.screens.videolist.components.list.VideoThumbnail
import com.devson.nvplayerlite.util.formatDuration
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PlayerUpNextQueuePanel(
    isQueueVisible: Boolean,
    queueLayoutMode: LayoutMode,
    queueList: List<Video>,
    currentVideoId: String?,
    onQueueVisibleChange: (Boolean) -> Unit,
    onUpdateQueueLayoutMode: (LayoutMode) -> Unit,
    onQueueVideoClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val panelHeight = if (queueLayoutMode == LayoutMode.LIST) {
        if (isLandscape) configuration.screenHeightDp.dp * 0.9f else (configuration.screenHeightDp.dp * 0.65f)
    } else {
        240.dp
    }
    val panelHeightPx = with(density) { panelHeight.toPx() }
    val offsetY = remember { Animatable(panelHeightPx) }
    var isDragging by remember { mutableStateOf(false) }

    BackHandler(enabled = isQueueVisible) {
        onQueueVisibleChange(false)
    }

    LaunchedEffect(isQueueVisible, panelHeightPx) {
        if (!isDragging) {
            if (isQueueVisible) {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            } else {
                offsetY.animateTo(
                    targetValue = panelHeightPx,
                    animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                )
            }
        }
    }

    val dismissWithAnimation: () -> Unit = {
        coroutineScope.launch {
            offsetY.animateTo(
                targetValue = panelHeightPx,
                animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
            )
            onQueueVisibleChange(false)
        }
    }

    val velocityTracker = remember { VelocityTracker() }

    val dragModifier = Modifier.pointerInput(panelHeightPx) {
        detectVerticalDragGestures(
            onDragStart = {
                isDragging = true
                velocityTracker.resetTracking()
            },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                velocityTracker.addPosition(change.uptimeMillis, change.position)
                coroutineScope.launch {
                    offsetY.snapTo((offsetY.value + dragAmount).coerceAtLeast(0f))
                }
            },
            onDragEnd = {
                isDragging = false
                val velocityY = velocityTracker.calculateVelocity().y
                val draggedFraction = (offsetY.value / panelHeightPx).coerceIn(0f, 1f)
                coroutineScope.launch {
                    if (draggedFraction > 0.35f || velocityY > 1400f) {
                        offsetY.animateTo(
                            targetValue = panelHeightPx,
                            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                        )
                        onQueueVisibleChange(false)
                    } else {
                        offsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }
            },
            onDragCancel = {
                isDragging = false
                coroutineScope.launch {
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }
        )
    }

    val nestedScrollConnection = remember(panelHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0 && offsetY.value > 0f) {
                    val newOffset = (offsetY.value + delta).coerceAtLeast(0f)
                    val consumed = offsetY.value - newOffset
                    coroutineScope.launch { offsetY.snapTo(newOffset) }
                    return Offset(0f, -consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0 && available.y > 0) {
                    val newOffset = (offsetY.value + delta).coerceAtLeast(0f)
                    val consumedY = newOffset - offsetY.value
                    coroutineScope.launch { offsetY.snapTo(newOffset) }
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offsetY.value > 0f) {
                    val draggedFraction = (offsetY.value / panelHeightPx).coerceIn(0f, 1f)
                    if (draggedFraction > 0.35f || available.y > 1400f) {
                        offsetY.animateTo(
                            targetValue = panelHeightPx,
                            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                        )
                        onQueueVisibleChange(false)
                    } else {
                        offsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    if (isQueueVisible || offsetY.value < panelHeightPx) {
        val draggedFraction = (offsetY.value / panelHeightPx).coerceIn(0f, 1f)
        val scrimAlpha = (1f - draggedFraction) * 0.55f

        Box(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = dismissWithAnimation
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(if (isLandscape) 0.65f else 1f)
                    .height(panelHeight)
                    .offset { IntOffset(x = 0, y = offsetY.value.roundToInt()) }
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .then(if (queueLayoutMode == LayoutMode.LIST) Modifier.nestedScroll(nestedScrollConnection) else Modifier)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Drag pill handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(dragModifier)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        )
                    }

                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.VideoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Up Next Queue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${queueList.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onUpdateQueueLayoutMode(LayoutMode.LIST) },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (queueLayoutMode == LayoutMode.LIST) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ViewList,
                                    contentDescription = "List View",
                                    tint = if (queueLayoutMode == LayoutMode.LIST) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onUpdateQueueLayoutMode(LayoutMode.GRID) },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (queueLayoutMode == LayoutMode.GRID) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ViewCarousel,
                                    contentDescription = "Grid/Carousel View",
                                    tint = if (queueLayoutMode == LayoutMode.GRID) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = dismissWithAnimation
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Hide Queue",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        thickness = 1.dp
                    )

                    val activeIndex = remember(queueList, currentVideoId) {
                        queueList.indexOfFirst { it.uri == currentVideoId }
                    }

                    if (queueLayoutMode == LayoutMode.LIST) {
                        val listState = rememberLazyListState()
                        LaunchedEffect(isQueueVisible) {
                            if (isQueueVisible && activeIndex >= 0) {
                                listState.animateScrollToItem(activeIndex)
                            }
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            itemsIndexed(
                                items = queueList,
                                key = { _, video -> video.uri }
                            ) { _, video ->
                                val isPlaying = video.uri == currentVideoId
                                QueueVideoItem(
                                    video = video,
                                    isPlaying = isPlaying,
                                    onClick = { onQueueVideoClick(video) }
                                )
                            }
                        }
                    } else {
                        val rowState = rememberLazyListState()
                        LaunchedEffect(isQueueVisible) {
                            if (isQueueVisible && activeIndex >= 0) {
                                rowState.animateScrollToItem(activeIndex)
                            }
                        }
                        LazyRow(
                            state = rowState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            itemsIndexed(
                                items = queueList,
                                key = { _, video -> video.uri }
                            ) { _, video ->
                                val isPlaying = video.uri == currentVideoId
                                QueueVideoGridItem(
                                    video = video,
                                    isPlaying = isPlaying,
                                    onClick = { onQueueVideoClick(video) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueVideoItem(
    video: Video,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isPlaying) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        Color.Transparent
    }

    val titleColor = if (isPlaying) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            VideoThumbnail(
                uri = video.uri,
                showPlayIcon = false,
                modifier = Modifier.fillMaxSize()
            )

            // Duration badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = formatDuration(video.duration),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    PlayingIndicator(
                        isPlaying = true,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = video.title,
                color = titleColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = video.folderName,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QueueVideoGridItem(
    video: Video,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderStroke = if (isPlaying) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .width(160.dp)
            .height(150.dp),
        shape = RoundedCornerShape(12.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp))
            ) {
                VideoThumbnail(
                    uri = video.uri,
                    showPlayIcon = false,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formatDuration(video.duration),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayingIndicator(
                            isPlaying = true,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = video.title,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

