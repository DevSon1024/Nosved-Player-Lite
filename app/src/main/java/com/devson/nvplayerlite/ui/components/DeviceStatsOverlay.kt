package com.devson.nvplayerlite.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import com.devson.nvplayerlite.repository.DecoderMode
import com.nosved.nosvedmedia.NosvedMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader

data class DeviceStats(
    val cpuPercent: Int = 0,
    val tempCelsius: Float = 0f,
    val usedMemMb: Long = 0,
    val totalMemMb: Long = 0,
    val batteryPercent: Int = 0,
    val batteryCharging: Boolean = false,
    val videoFps: Float = 0f
)

data class DecoderInfo(
    val badge: String,
    val isHardware: Boolean,
    val decoderName: String,
    val technology: String
)

data class PlaybackStats(
    // Mode
    val requestedDecoderMode: String = "Auto (HW+)",
    val requestedVideoPreference: String = "Auto",
    val requestedAudioPreference: String = "Auto",

    // Video Track
    val videoResolution: String = "N/A",
    val videoCodecMime: String = "N/A",
    val videoCodecFriendly: String = "N/A",
    val videoProfile: String = "",
    val videoBitDepth: String = "8-bit",
    val videoPixelFormat: String = "YUV420P",
    val videoBitrate: String = "N/A",
    val videoFramerate: String = "N/A",
    val videoTracksCount: Int = 0,
    val videoDecoderInfo: DecoderInfo = DecoderInfo("HW", true, "MediaCodec", "Hardware Accelerated Pipeline"),
    val videoDecoderStateDisplay: String = "Actively Decoding",
    val fallbackReason: String? = null,
    val decodeLatencyMs: Double = 0.0,
    val framesDropped: Long = 0L,

    // Audio Track
    val audioCodecMime: String = "N/A",
    val audioCodecFriendly: String = "N/A",
    val audioSampleRate: String = "N/A",
    val audioChannels: String = "N/A",
    val audioLanguage: String = "None",
    val audioTrackLabel: String = "None",
    val audioTracksCount: Int = 0,
    val audioOtherLanguages: String = "",
    val audioDecoderInfo: DecoderInfo = DecoderInfo("HW", true, "MediaCodec / AudioTrack", "Hardware Accelerated Audio"),
    val audioDecoderStateDisplay: String = "Actively Decoding",

    // Recovery and Errors
    val fallbackHistory: List<String> = emptyList(),
    val activeError: String? = null,

    // Subtitle Track
    val hasActiveSubtitle: Boolean = false,
    val subtitleCodecMime: String = "None",
    val subtitleCodecFriendly: String = "None",
    val subtitleLanguage: String = "None",
    val subtitleTracksCount: Int = 0,
    val subtitleOtherLanguages: String = "",

    // Playback & Display
    val bufferHealth: String = "0.0 / 0.0 s",
    val playbackSpeed: String = "1.00x",
    val displayRefreshRate: String = "N/A",
    val screenResolution: String = "N/A",

    // Engine Core
    val nosvedVersion: String = "1.0.0",
    val ffmpegVersion: String = "7.1",
    val dav1dVersion: String = "1.5.0"
)

@Composable
fun DeviceStatsOverlay(
    visible: Boolean,
    player: Player?,
    modifier: Modifier = Modifier,
    videoFps: Float = 0f,
    videoFpsProvider: (() -> Float)? = null,
    videoDecoderName: String? = null,
    audioDecoderName: String? = null,
    decoderMode: DecoderMode? = null,
    videoDecoderPreference: com.nosved.nosvedmedia.media3.DecoderPreference? = null,
    audioDecoderPreference: com.nosved.nosvedmedia.media3.DecoderPreference? = null,
    videoDecoderState: com.nosved.nosvedmedia.media3.DecoderState? = null,
    audioDecoderState: com.nosved.nosvedmedia.media3.DecoderState? = null,
    fallbackReason: String? = null,
    decodeLatencyMs: Double = 0.0,
    decodeLatencyMsProvider: (() -> Double)? = null,
    framesDropped: Long = 0L,
    framesDroppedProvider: (() -> Long)? = null,
    fallbackHistory: List<String> = emptyList(),
    playerError: String? = null,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    var deviceStats by remember { mutableStateOf(DeviceStats()) }
    var playbackStats by remember { mutableStateOf(PlaybackStats()) }

    val updatedPlayer by rememberUpdatedState(player)
    val updatedFps by rememberUpdatedState(videoFps)
    val updatedVideoDecoderName by rememberUpdatedState(videoDecoderName)
    val updatedAudioDecoderName by rememberUpdatedState(audioDecoderName)
    val updatedDecoderMode by rememberUpdatedState(decoderMode)
    val updatedVideoPreference by rememberUpdatedState(videoDecoderPreference)
    val updatedAudioPreference by rememberUpdatedState(audioDecoderPreference)
    val updatedVideoState by rememberUpdatedState(videoDecoderState)
    val updatedAudioState by rememberUpdatedState(audioDecoderState)
    val updatedFallbackReason by rememberUpdatedState(fallbackReason)
    val updatedDecodeLatencyMs by rememberUpdatedState(decodeLatencyMs)
    val updatedFramesDropped by rememberUpdatedState(framesDropped)
    val updatedFallbackHistory by rememberUpdatedState(fallbackHistory)
    val updatedPlayerError by rememberUpdatedState(playerError)

    LaunchedEffect(visible) {
        if (visible) {
            var prevCpuStat: LongArray? = null
            while (isActive) {
                val currentFps = videoFpsProvider?.invoke() ?: updatedFps
                val currentLatency = decodeLatencyMsProvider?.invoke() ?: updatedDecodeLatencyMs
                val currentDropped = framesDroppedProvider?.invoke() ?: updatedFramesDropped

                val (dStats, newCpuStat) = withContext(Dispatchers.IO) {
                    collectStats(context, currentFps, prevCpuStat)
                }
                prevCpuStat = newCpuStat

                val pStats = collectPlaybackStats(
                    player = updatedPlayer,
                    context = context,
                    reportedFps = currentFps,
                    videoDecoderName = updatedVideoDecoderName,
                    audioDecoderName = updatedAudioDecoderName,
                    decoderMode = updatedDecoderMode,
                    videoDecoderPreference = updatedVideoPreference,
                    audioDecoderPreference = updatedAudioPreference,
                    videoDecoderState = updatedVideoState,
                    audioDecoderState = updatedAudioState,
                    fallbackReason = updatedFallbackReason,
                    decodeLatencyMs = currentLatency,
                    framesDropped = currentDropped,
                    fallbackHistory = updatedFallbackHistory,
                    playerError = updatedPlayerError
                )

                deviceStats = dStats
                playbackStats = pStats

                delay(1000)
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 310.dp, max = 370.dp)
                .heightIn(max = 520.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "STATS & DIAGNOSTICS",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Stats",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))

                // 1. VIDEO TRACK & DECODER
                TrackSectionHeader(
                    title = "VIDEO TRACK & DECODER",
                    badge = playbackStats.videoDecoderInfo.badge,
                    isHardware = playbackStats.videoDecoderInfo.isHardware
                )
                StatItem("Decoder Mode", playbackStats.requestedDecoderMode)
                StatItem("Active Decoder", playbackStats.videoDecoderInfo.decoderName)
                StatItem("Decoder State", playbackStats.videoDecoderStateDisplay)
                StatItem("Technology", playbackStats.videoDecoderInfo.technology)
                val profileText = if (playbackStats.videoProfile.isNotBlank()) " ${playbackStats.videoProfile}" else ""
                StatItem("Codec & Profile", "${playbackStats.videoCodecFriendly}$profileText (${playbackStats.videoCodecMime})")
                StatItem("Resolution", playbackStats.videoResolution)
                StatItem("Framerate", playbackStats.videoFramerate)
                StatItem("Bit Depth", playbackStats.videoBitDepth)
                StatItem("Pixel Format", playbackStats.videoPixelFormat)
                StatItem("Bitrate", playbackStats.videoBitrate)
                val reason = playbackStats.fallbackReason
                if (!reason.isNullOrBlank()) {
                    StatItem("Fallback Reason", reason, Color(0xFFFFB74D))
                }
                if (playbackStats.decodeLatencyMs > 0.0) {
                    StatItem("Decode Latency", "%.2f ms".format(playbackStats.decodeLatencyMs))
                }
                if (playbackStats.framesDropped > 0L) {
                    StatItem("Dropped Frames", "${playbackStats.framesDropped} frames", Color(0xFFEF5350))
                }
                if (playbackStats.videoTracksCount > 1) {
                    StatItem("Video Streams", "${playbackStats.videoTracksCount} track(s)")
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))

                // 2. AUDIO TRACK & DECODER
                TrackSectionHeader(
                    title = "AUDIO TRACK & DECODER",
                    badge = playbackStats.audioDecoderInfo.badge,
                    isHardware = playbackStats.audioDecoderInfo.isHardware
                )
                StatItem("Decoder Mode", playbackStats.requestedDecoderMode)
                StatItem("Active Track", "${playbackStats.audioTrackLabel} (${playbackStats.audioLanguage})")
                StatItem("Active Decoder", playbackStats.audioDecoderInfo.decoderName)
                StatItem("Decoder State", playbackStats.audioDecoderStateDisplay)
                StatItem("Technology", playbackStats.audioDecoderInfo.technology)
                StatItem("Codec", "${playbackStats.audioCodecFriendly} (${playbackStats.audioCodecMime})")
                StatItem("Channels", playbackStats.audioChannels)
                StatItem("Sample Rate", playbackStats.audioSampleRate)
                if (playbackStats.audioTracksCount > 1) {
                    val otherLangs = playbackStats.audioOtherLanguages.ifBlank { "None" }
                    StatItem("Tracks (${playbackStats.audioTracksCount})", otherLangs)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))

                // 3. SUBTITLE TRACK
                TrackSectionHeader(
                    title = "SUBTITLE TRACK",
                    badge = if (playbackStats.hasActiveSubtitle) "ACTIVE" else "OFF",
                    isHardware = !playbackStats.hasActiveSubtitle
                )
                StatItem("Active Subtitle", if (playbackStats.hasActiveSubtitle) playbackStats.subtitleLanguage else "None")
                if (playbackStats.hasActiveSubtitle) {
                    StatItem("Format / Codec", "${playbackStats.subtitleCodecFriendly} (${playbackStats.subtitleCodecMime})")
                    StatItem("Technology", "Media3 SubtitleParser + Compose")
                }
                if (playbackStats.subtitleTracksCount > 0) {
                    val subText = if (playbackStats.subtitleOtherLanguages.isNotBlank()) playbackStats.subtitleOtherLanguages else "${playbackStats.subtitleTracksCount} track(s)"
                    StatItem("Available Subs", subText)
                }

                // 4. RECOVERY & FALLBACK HISTORY
                if (playbackStats.fallbackHistory.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))
                    StatsSectionHeader("RECOVERY & FALLBACK HISTORY")
                    playbackStats.fallbackHistory.forEachIndexed { idx, entry ->
                        StatItem("Fallback #${idx + 1}", entry, Color(0xFFFFB74D))
                    }
                }

                // ERROR DETAILS
                val activeErr = playbackStats.activeError
                if (!activeErr.isNullOrBlank()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))
                    StatsSectionHeader("ERROR INFORMATION")
                    StatItem("Error Status", "Playback Issue Detected", Color(0xFFEF5350))
                    StatItem("Details", activeErr, Color(0xFFEF5350))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))

                // 4. PLAYBACK & DISPLAY
                StatsSectionHeader("PLAYBACK & DISPLAY")
                StatItem("Buffer Health", playbackStats.bufferHealth)
                StatItem("Playback Speed", playbackStats.playbackSpeed)
                StatItem("Display Refresh", playbackStats.displayRefreshRate)
                StatItem("Screen Resolution", playbackStats.screenResolution)

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))

                // 5. SYSTEM & ENGINE
                StatsSectionHeader("SYSTEM & ENGINE")
                StatItem(
                    "NosvedMedia Core",
                    "v${playbackStats.nosvedVersion} (FFmpeg ${playbackStats.ffmpegVersion}, dav1d ${playbackStats.dav1dVersion})"
                )
                val defaultValColor = MaterialTheme.colorScheme.onSurface
                StatItem(
                    "CPU Usage",
                    "${deviceStats.cpuPercent}%",
                    cpuWarningColor(deviceStats.cpuPercent, defaultValColor)
                )

                val ramText = if (deviceStats.totalMemMb > 1024) {
                    "%.1f / %.1f GB".format(deviceStats.usedMemMb / 1024f, deviceStats.totalMemMb / 1024f)
                } else {
                    "${deviceStats.usedMemMb} / ${deviceStats.totalMemMb} MB"
                }
                StatItem(
                    "Memory (RAM)",
                    ramText,
                    memWarningColor(deviceStats.usedMemMb, deviceStats.totalMemMb, defaultValColor)
                )
                StatItem(
                    "Battery",
                    "${deviceStats.batteryPercent}% ${if (deviceStats.batteryCharging) "⚡" else ""}",
                    batteryWarningColor(deviceStats.batteryPercent, defaultValColor)
                )
                StatItem(
                    "Temperature",
                    if (deviceStats.tempCelsius > 0f) "%.1f°C".format(deviceStats.tempCelsius) else "N/A",
                    tempWarningColor(deviceStats.tempCelsius, defaultValColor)
                )
            }
        }
    }
}

@Composable
private fun TrackSectionHeader(
    title: String,
    badge: String? = null,
    isHardware: Boolean? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        if (badge != null && isHardware != null) {
            DecoderBadge(badge = badge, isHardware = isHardware)
        }
    }
}

@Composable
private fun DecoderBadge(badge: String, isHardware: Boolean) {
    val (bgColor, textColor) = if (badge == "PCM") {
        Color(0x222196F3) to Color(0xFF90CAF9)
    } else if (isHardware) {
        Color(0x224CAF50) to Color(0xFF81C784)
    } else {
        Color(0x28FF9800) to Color(0xFFFFB74D)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.45f))
    ) {
        Text(
            text = badge,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun StatsSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun parseVideoDecoder(
    decoderName: String?,
    sampleMimeType: String?,
    decoderMode: DecoderMode?
): DecoderInfo {
    if (decoderName.isNullOrBlank()) {
        val modeBadge = when (decoderMode) {
            DecoderMode.SW -> "SW"
            else -> "HW"
        }
        return DecoderInfo(
            badge = modeBadge,
            isHardware = decoderMode != DecoderMode.SW,
            decoderName = "Initializing...",
            technology = "Waiting for first decoded frame"
        )
    }

    val lower = decoderName.lowercase()
    return when {
        lower.contains("dav1d") -> DecoderInfo(
            badge = "SW",
            isHardware = false,
            decoderName = "NosvedMedia dav1d Video",
            technology = "dav1d 1.5.0 SIMD AV1 Native"
        )
        lower.contains("ffmpeg") -> DecoderInfo(
            badge = "SW",
            isHardware = false,
            decoderName = "NosvedMedia FFmpeg Video",
            technology = "FFmpeg 7.1 Native Multi-threaded (libavcodec)"
        )
        lower.contains("c2.android") || lower.contains("omx.google") -> DecoderInfo(
            badge = if (decoderMode == DecoderMode.SW) "SW" else "HW",
            isHardware = decoderMode != DecoderMode.SW,
            decoderName = decoderName,
            technology = if (decoderMode == DecoderMode.SW) "Android Platform Software Codec" else "Platform MediaCodec Pipeline"
        )
        lower.contains("c2.qti") || lower.contains("omx.qcom") -> DecoderInfo(
            badge = "HW",
            isHardware = true,
            decoderName = decoderName,
            technology = "Qualcomm MediaCodec (Hardware NDK)"
        )
        lower.contains("c2.exynos") || lower.contains("omx.exynos") -> DecoderInfo(
            badge = "HW",
            isHardware = true,
            decoderName = decoderName,
            technology = "Samsung Exynos MediaCodec (Hardware NDK)"
        )
        lower.contains("c2.mtk") || lower.contains("omx.mtk") -> DecoderInfo(
            badge = "HW",
            isHardware = true,
            decoderName = decoderName,
            technology = "MediaTek MediaCodec (Hardware NDK)"
        )
        else -> DecoderInfo(
            badge = if (decoderMode == DecoderMode.SW) "SW" else "HW",
            isHardware = decoderMode != DecoderMode.SW,
            decoderName = decoderName,
            technology = "Hardware Accelerated MediaCodec"
        )
    }
}

private fun parseAudioDecoder(
    decoderName: String?,
    sampleMimeType: String?,
    decoderMode: DecoderMode?
): DecoderInfo {
    if (sampleMimeType == "audio/raw") {
        return DecoderInfo(
            badge = if (decoderMode == DecoderMode.SW) "SW" else "HW",
            isHardware = decoderMode != DecoderMode.SW,
            decoderName = "System AudioTrack",
            technology = "Direct Linear PCM Output"
        )
    }

    if (decoderName.isNullOrBlank()) {
        val modeBadge = when (decoderMode) {
            DecoderMode.SW -> "SW"
            else -> "HW"
        }
        return DecoderInfo(
            badge = modeBadge,
            isHardware = decoderMode != DecoderMode.SW,
            decoderName = "Initializing...",
            technology = "Waiting for first decoded sample"
        )
    }

    val lower = decoderName.lowercase()
    return when {
        lower.contains("ffmpeg") -> DecoderInfo(
            badge = "SW",
            isHardware = false,
            decoderName = "NosvedMedia FFmpeg Audio",
            technology = "FFmpeg 7.1 Audio Decoding (libswresample)"
        )
        lower.contains("c2.android") || lower.contains("omx.google") -> DecoderInfo(
            badge = if (decoderMode == DecoderMode.SW) "SW" else "HW",
            isHardware = decoderMode != DecoderMode.SW,
            decoderName = decoderName,
            technology = if (decoderMode == DecoderMode.SW) "Android Platform Software Codec" else "Platform MediaCodec Audio"
        )
        lower.contains("c2.qti") || lower.contains("omx.qcom") || lower.contains("c2.exynos") || lower.contains("c2.mtk") -> DecoderInfo(
            badge = "HW",
            isHardware = true,
            decoderName = decoderName,
            technology = "Hardware Accelerated MediaCodec Audio"
        )
        else -> DecoderInfo(
            badge = if (decoderMode == DecoderMode.SW) "SW" else "HW",
            isHardware = decoderMode != DecoderMode.SW,
            decoderName = decoderName,
            technology = "Hardware Accelerated Audio Pipeline"
        )
    }
}

private fun friendlyVideoCodec(mime: String?): String = when (mime) {
    "video/hevc" -> "HEVC / H.265"
    "video/avc" -> "AVC / H.264"
    "video/av01" -> "AV1"
    "video/x-vnd.on2.vp9" -> "VP9"
    "video/x-vnd.on2.vp8" -> "VP8"
    "video/mp4v-es" -> "MPEG-4 Part 2"
    "video/mpeg2" -> "MPEG-2"
    "video/wvc1", "video/x-vc1" -> "VC-1"
    "video/mpeg", "video/mpeg1" -> "MPEG-1"
    "video/x-theora", "video/theora" -> "Theora"
    "video/x-ffv1", "video/ffv1" -> "FFV1 Lossless"
    "video/x-dnxhd", "video/dnxhd" -> "Avid DNxHD"
    "video/3gpp", "video/h263" -> "H.263"
    "video/x-ms-wmv2" -> "WMV2"
    "video/x-ms-wmv", "video/x-ms-wmv3" -> "WMV3 / VC-1 SM"
    "video/x-prores", "video/prores" -> "Apple ProRes"
    "video/x-pn-realvideo", "video/rv40" -> "RealVideo 4 (RV40)"
    "video/mjpeg", "video/x-mjpeg" -> "Motion JPEG (MJPEG)"
    "video/dolby-vision" -> "Dolby Vision"
    null, "N/A" -> "N/A"
    else -> mime.substringAfter("video/").uppercase()
}

private fun friendlyAudioCodec(mime: String?): String = when (mime) {
    "audio/eac3", "audio/eac3-joc" -> "Dolby Digital Plus (E-AC-3)"
    "audio/ac3" -> "Dolby Digital (AC-3)"
    "audio/ac4" -> "Dolby AC-4"
    "audio/mp4a-latm", "audio/aac" -> "AAC"
    "audio/opus" -> "Opus"
    "audio/flac", "audio/x-flac" -> "FLAC Lossless"
    "audio/alac" -> "Apple Lossless (ALAC)"
    "audio/true-hd", "audio/mlp" -> "Dolby TrueHD"
    "audio/vnd.dts" -> "DTS Digital Surround"
    "audio/vnd.dts.hd" -> "DTS-HD Master Audio"
    "audio/raw", "audio/wav", "audio/x-wav" -> "Linear PCM"
    "audio/vorbis" -> "Vorbis"
    "audio/mpeg", "audio/mp3" -> "MP3"
    "audio/mpeg-L1" -> "MP1"
    "audio/mpeg-L2" -> "MP2"
    "audio/x-ape", "audio/ape" -> "Monkey's Audio (APE)"
    "audio/x-wavpack", "audio/wavpack" -> "WavPack Lossless"
    "audio/x-tta", "audio/tta" -> "True Audio (TTA)"
    "audio/x-nellymoser", "audio/nellymoser" -> "Nellymoser Asao"
    "audio/atrac3", "audio/x-vnd.sony.atrac3" -> "Sony ATRAC3"
    "audio/atrac3p", "audio/x-vnd.sony.atrac3p" -> "Sony ATRAC3+"
    "audio/x-adpcm-ms", "audio/adpcm-ms" -> "Microsoft ADPCM"
    "audio/x-adpcm-ima-wav", "audio/adpcm-ima-wav" -> "IMA WAV ADPCM"
    "audio/x-cook", "audio/cook" -> "RealAudio Cook"
    "audio/x-ms-wma", "audio/wma" -> "Windows Media Audio (WMA)"
    "audio/x-ms-wmapro" -> "WMA Pro"
    "audio/x-ms-wmalossless" -> "WMA Lossless"
    null, "N/A" -> "N/A"
    else -> mime.substringAfter("audio/").uppercase()
}

private fun friendlySubtitleCodec(mime: String?): String = when (mime) {
    "application/x-subrip" -> "SubRip (SRT)"
    "text/x-ssa" -> "SubStation Alpha (SSA)"
    "text/x-ass" -> "Advanced SSA (ASS)"
    "text/vtt" -> "WebVTT"
    "application/pgs" -> "PGS Bitmap"
    "application/dvbsubs" -> "DVB Subtitles"
    "application/ttml+xml" -> "TTML"
    null, "None", "N/A" -> "Text"
    else -> mime.substringAfterLast("/")
}

private fun formatLanguage(lang: String?): String {
    if (lang.isNullOrBlank() || lang == "und") return "Unknown"
    return try {
        val loc = java.util.Locale(lang)
        val disp = loc.displayLanguage
        if (disp.isNotBlank()) disp.replaceFirstChar { if (it.isLowerCase()) it.titlecase(loc) else it.toString() } else lang
    } catch (_: Exception) {
        lang
    }
}

private fun cpuWarningColor(percent: Int, defaultColor: Color): Color = when {
    percent >= 85 -> Color(0xFFEF5350)
    percent >= 70 -> Color(0xFFFFB74D)
    else -> defaultColor
}

private fun tempWarningColor(temp: Float, defaultColor: Color): Color = when {
    temp >= 48f -> Color(0xFFEF5350)
    temp >= 42f -> Color(0xFFFFB74D)
    else -> defaultColor
}

private fun batteryWarningColor(percent: Int, defaultColor: Color): Color = when {
    percent <= 15 -> Color(0xFFEF5350)
    percent <= 25 -> Color(0xFFFFB74D)
    else -> defaultColor
}

private fun memWarningColor(usedMb: Long, totalMb: Long, defaultColor: Color): Color {
    if (totalMb <= 0) return defaultColor
    val pct = usedMb.toFloat() / totalMb.toFloat()
    return when {
        pct >= 0.90f -> Color(0xFFEF5350)
        pct >= 0.80f -> Color(0xFFFFB74D)
        else -> defaultColor
    }
}

private fun formatDecoderState(state: com.nosved.nosvedmedia.media3.DecoderState?): String {
    return when (state) {
        com.nosved.nosvedmedia.media3.DecoderState.PLAYING,
        com.nosved.nosvedmedia.media3.DecoderState.FIRST_FRAME_DECODED -> "Actively Decoding"
        com.nosved.nosvedmedia.media3.DecoderState.INITIALIZING,
        com.nosved.nosvedmedia.media3.DecoderState.INITIALIZED -> "Initialized"
        com.nosved.nosvedmedia.media3.DecoderState.REQUESTED,
        com.nosved.nosvedmedia.media3.DecoderState.CANDIDATE_SELECTED -> "Advertised"
        com.nosved.nosvedmedia.media3.DecoderState.FALLBACK_PENDING -> "Fallback Pending"
        com.nosved.nosvedmedia.media3.DecoderState.FAILED -> "Failed"
        com.nosved.nosvedmedia.media3.DecoderState.RELEASED -> "Released"
        com.nosved.nosvedmedia.media3.DecoderState.IDLE, null -> "Idle"
    }
}

private fun extractProfile(format: Format): String {
    val codecs = format.codecs?.lowercase() ?: ""
    val mime = format.sampleMimeType?.lowercase() ?: ""

    return when {
        mime.contains("hevc") || mime.contains("h265") -> {
            if (codecs.contains("hev1.2") || codecs.contains("hvc1.2") || codecs.contains("main10")) {
                "Main 10"
            } else {
                "Main"
            }
        }
        mime.contains("avc") || mime.contains("h264") -> {
            if (codecs.contains("6400")) "High"
            else if (codecs.contains("4d40")) "Main"
            else if (codecs.contains("42e0") || codecs.contains("4280")) "Baseline"
            else if (codecs.contains("6e00") || codecs.contains("7a00")) "High 10"
            else "Main"
        }
        mime.contains("vp9") -> {
            if (codecs.contains("vp09.02") || codecs.contains("vp09.2")) "Profile 2 (10-bit)"
            else "Profile 0 (8-bit)"
        }
        mime.contains("av01") || mime.contains("av1") -> {
            if (codecs.contains("av01.0.10") || codecs.contains(".10m")) "Main 10"
            else "Main 8"
        }
        else -> ""
    }
}

private fun extractBitDepth(format: Format): String {
    val colorInfo = format.colorInfo
    if (colorInfo != null) {
        if (colorInfo.colorTransfer == C.COLOR_TRANSFER_ST2084 ||
            colorInfo.colorTransfer == C.COLOR_TRANSFER_HLG
        ) {
            return "10-bit"
        }
    }
    val codecs = format.codecs?.lowercase() ?: ""
    if (codecs.contains("hev1.2") || codecs.contains("hvc1.2") ||
        codecs.contains("main10") || codecs.contains("vp09.02")
    ) {
        return "10-bit"
    }
    return "8-bit"
}

private fun extractPixelFormat(format: Format): String {
    val is10Bit = extractBitDepth(format) == "10-bit"
    val colorInfo = format.colorInfo
    val transfer = when (colorInfo?.colorTransfer) {
        C.COLOR_TRANSFER_ST2084 -> "PQ"
        C.COLOR_TRANSFER_HLG -> "HLG"
        C.COLOR_TRANSFER_SDR -> "SDR"
        else -> if (is10Bit) "PQ" else "SDR"
    }
    val space = when (colorInfo?.colorSpace) {
        C.COLOR_SPACE_BT2020 -> "BT.2020"
        C.COLOR_SPACE_BT709 -> "BT.709"
        C.COLOR_SPACE_BT601 -> "BT.601"
        else -> if (is10Bit) "BT.2020" else "BT.709"
    }
    val pixFmt = if (is10Bit) "YUV420P10LE" else "YUV420P"
    return "$pixFmt ($space $transfer)"
}

private fun collectPlaybackStats(
    player: Player?,
    context: Context,
    reportedFps: Float,
    videoDecoderName: String?,
    audioDecoderName: String?,
    decoderMode: DecoderMode?,
    videoDecoderPreference: com.nosved.nosvedmedia.media3.DecoderPreference? = null,
    audioDecoderPreference: com.nosved.nosvedmedia.media3.DecoderPreference? = null,
    videoDecoderState: com.nosved.nosvedmedia.media3.DecoderState? = null,
    audioDecoderState: com.nosved.nosvedmedia.media3.DecoderState? = null,
    fallbackReason: String? = null,
    decodeLatencyMs: Double = 0.0,
    framesDropped: Long = 0L,
    fallbackHistory: List<String> = emptyList(),
    playerError: String? = null
): PlaybackStats {
    var vRes = "N/A"
    var vMime = "N/A"
    var vBitrate = "N/A"
    var formatFps = 0f
    var videoCount = 0
    var selectedVideoFormat: Format? = null

    var aMime = "N/A"
    var aSampleRate = "N/A"
    var aChannels = "N/A"
    var aLang = "None"
    var aLabel = "None"
    var audioCount = 0
    val audioLangs = mutableListOf<String>()

    var hasSub = false
    var sMime = "None"
    var sLang = "None"
    var subCount = 0
    val subLangs = mutableListOf<String>()

    val groups = player?.currentTracks?.groups ?: emptyList()
    for (group in groups) {
        when (group.type) {
            C.TRACK_TYPE_VIDEO -> {
                videoCount += group.length
                for (i in 0 until group.length) {
                    if (group.isTrackSelected(i)) {
                        val format = group.getTrackFormat(i)
                        selectedVideoFormat = format
                        vRes = if (format.width != Format.NO_VALUE && format.height != Format.NO_VALUE) {
                            "${format.width}x${format.height}"
                        } else "N/A"
                        vMime = format.sampleMimeType ?: "N/A"
                        vBitrate = if (format.bitrate != Format.NO_VALUE) "${format.bitrate / 1000} kbps" else "N/A"
                        formatFps = if (format.frameRate != Format.NO_VALUE.toFloat() && format.frameRate > 0f) format.frameRate else 0f
                        break
                    }
                }
            }
            C.TRACK_TYPE_AUDIO -> {
                audioCount += group.length
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    val formattedLang = formatLanguage(format.language)
                    if (!audioLangs.contains(formattedLang)) audioLangs.add(formattedLang)
                    if (group.isTrackSelected(i)) {
                        aMime = format.sampleMimeType ?: "N/A"
                        aSampleRate = if (format.sampleRate != Format.NO_VALUE) "${format.sampleRate} Hz" else "N/A"
                        aLang = formattedLang
                        aLabel = format.label?.takeIf { it.isNotBlank() } ?: formattedLang
                        aChannels = when (format.channelCount) {
                            1 -> "Mono (1 ch)"
                            2 -> "Stereo (2 ch)"
                            6 -> "5.1 Surround (6 ch)"
                            8 -> "7.1 Surround (8 ch)"
                            Format.NO_VALUE -> "N/A"
                            else -> "${format.channelCount} channels"
                        }
                    }
                }
            }
            C.TRACK_TYPE_TEXT -> {
                subCount += group.length
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    val formattedLang = formatLanguage(format.language)
                    if (!subLangs.contains(formattedLang)) subLangs.add(formattedLang)
                    if (group.isTrackSelected(i)) {
                        hasSub = true
                        sMime = format.sampleMimeType ?: "Text"
                        sLang = format.label?.takeIf { it.isNotBlank() } ?: formattedLang
                    }
                }
            }
        }
    }

    val fpsStr = if (reportedFps > 0f) {
        "%.2f fps".format(reportedFps)
    } else if (formatFps > 0f) {
        "%.2f fps".format(formatFps)
    } else {
        "N/A"
    }

    val bufferPos = player?.bufferedPosition ?: 0L
    val currentPos = player?.currentPosition ?: 0L
    val bufferHealth = "%.1fs (cur: %.1fs)".format(bufferPos / 1000f, currentPos / 1000f)
    val speed = player?.playbackParameters?.speed ?: 1f

    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    }
    val refreshRate = display?.refreshRate ?: 0f
    val screenRes = context.resources.displayMetrics.run { "${widthPixels}x${heightPixels}" }

    val ffmpegVer = try { NosvedMedia.getFFmpegVersion() } catch (_: Throwable) { "7.1" }
    val dav1dVer = try { NosvedMedia.getDav1dVersion() } catch (_: Throwable) { "1.5.0" }

    val vDecInfo = parseVideoDecoder(videoDecoderName, vMime, decoderMode)
    val aDecInfo = parseAudioDecoder(audioDecoderName, aMime, decoderMode)

    val reqModeStr = when (decoderMode) {
        DecoderMode.SW -> "SW (Software Only)"
        DecoderMode.HW -> "HW (Hardware Only)"
        else -> "HW+ (Hardware+ Auto Fallback)"
    }
    val reqVideoPref = videoDecoderPreference?.displayName ?: reqModeStr
    val reqAudioPref = audioDecoderPreference?.displayName ?: reqModeStr

    val vProfile = if (selectedVideoFormat != null) extractProfile(selectedVideoFormat) else ""
    val vBitDepth = if (selectedVideoFormat != null) extractBitDepth(selectedVideoFormat) else "8-bit"
    val vPixelFmt = if (selectedVideoFormat != null) extractPixelFormat(selectedVideoFormat) else "YUV420P"
    val vStateDisplay = formatDecoderState(videoDecoderState)
    val aStateDisplay = formatDecoderState(audioDecoderState)

    return PlaybackStats(
        requestedDecoderMode = reqModeStr,
        requestedVideoPreference = reqVideoPref,
        requestedAudioPreference = reqAudioPref,
        videoResolution = vRes,
        videoCodecMime = vMime,
        videoCodecFriendly = friendlyVideoCodec(vMime),
        videoProfile = vProfile,
        videoBitDepth = vBitDepth,
        videoPixelFormat = vPixelFmt,
        videoBitrate = vBitrate,
        videoFramerate = fpsStr,
        videoTracksCount = videoCount,
        videoDecoderInfo = vDecInfo,
        videoDecoderStateDisplay = vStateDisplay,
        fallbackReason = fallbackReason,
        decodeLatencyMs = decodeLatencyMs,
        framesDropped = framesDropped,

        audioCodecMime = aMime,
        audioCodecFriendly = friendlyAudioCodec(aMime),
        audioSampleRate = aSampleRate,
        audioChannels = aChannels,
        audioLanguage = aLang,
        audioTrackLabel = aLabel,
        audioTracksCount = audioCount,
        audioOtherLanguages = audioLangs.filter { it != aLang }.joinToString(", "),
        audioDecoderInfo = aDecInfo,
        audioDecoderStateDisplay = aStateDisplay,

        fallbackHistory = fallbackHistory,
        activeError = playerError,

        hasActiveSubtitle = hasSub,
        subtitleCodecMime = sMime,
        subtitleCodecFriendly = friendlySubtitleCodec(sMime),
        subtitleLanguage = sLang,
        subtitleTracksCount = subCount,
        subtitleOtherLanguages = subLangs.filter { it != sLang }.joinToString(", "),

        bufferHealth = bufferHealth,
        playbackSpeed = "%.2fx".format(speed),
        displayRefreshRate = if (refreshRate > 0f) "%.1f Hz".format(refreshRate) else "N/A",
        screenResolution = screenRes,

        nosvedVersion = "1.0.0",
        ffmpegVersion = ffmpegVer,
        dav1dVersion = dav1dVer
    )
}

private fun collectStats(
    context: Context,
    videoFps: Float,
    prevCpuStat: LongArray?
): Pair<DeviceStats, LongArray?> {
    val (cpu, newCpuStat) = readCpuUsage(prevCpuStat)

    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    am.getMemoryInfo(memInfo)
    val totalMb = memInfo.totalMem / 1_048_576L
    val availMb = memInfo.availMem / 1_048_576L
    val usedMb = totalMb - availMb

    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
    val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: 0
    val charging = plugged != 0

    val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
    val tempC = rawTemp / 10f

    val stats = DeviceStats(
        cpuPercent = cpu,
        tempCelsius = tempC,
        usedMemMb = usedMb,
        totalMemMb = totalMb,
        batteryPercent = batteryPct,
        batteryCharging = charging,
        videoFps = videoFps
    )
    return Pair(stats, newCpuStat)
}

private fun readCpuUsage(prevStat: LongArray?): Pair<Int, LongArray?> {
    return try {
        val line = BufferedReader(FileReader("/proc/stat")).use { it.readLine() } ?: return Pair(0, null)
        val parts = line.trim().split("\\s+".toRegex()).drop(1)
        val currStat = parts.take(8).map { it.toLongOrNull() ?: 0L }.toLongArray()
        if (currStat.isEmpty()) return Pair(0, null)
        if (prevStat == null || prevStat.isEmpty()) {
            return Pair(0, currStat)
        }

        val idle1 = prevStat.getOrElse(3) { 0L }
        val idle2 = currStat.getOrElse(3) { 0L }
        val total1 = prevStat.sum()
        val total2 = currStat.sum()

        val totalDiff = total2 - total1
        val idleDiff = idle2 - idle1
        val usage = if (totalDiff <= 0) 0
        else ((totalDiff - idleDiff) * 100L / totalDiff).toInt().coerceIn(0, 100)
        Pair(usage, currStat)
    } catch (_: Exception) {
        Pair(0, null)
    }
}
