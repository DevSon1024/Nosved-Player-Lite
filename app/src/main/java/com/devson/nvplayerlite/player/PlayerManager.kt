package com.devson.nvplayerlite.player

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.devson.nvplayerlite.model.Video
import androidx.media3.common.PlaybackException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.devson.nvplayerlite.model.TrackInfo
import com.devson.nvplayerlite.model.LanguageList
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.MediaItem.SubtitleConfiguration
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn
import com.nosved.nosvedmedia.NosvedMedia
import com.nosved.nosvedmedia.media3.FFmpegRenderersFactory
import com.nosved.nosvedmedia.media3.DecoderPreferences
import com.nosved.nosvedmedia.media3.DecoderPreference
import com.nosved.nosvedmedia.media3.DecoderSelectionListener
import com.nosved.nosvedmedia.media3.PlaybackDiagnosticsListener
import com.nosved.nosvedmedia.media3.DecoderState
import com.nosved.nosvedmedia.media3.TrackType
import com.nosved.nosvedmedia.media3.DecodingCategory
import com.nosved.nosvedmedia.media3.StructuredPlaybackError
import com.nosved.nosvedmedia.media3.TrackDecoderStatus
import com.nosved.nosvedmedia.media3.DecodingPerformanceMetrics
import androidx.media3.exoplayer.upstream.Loader
import android.media.audiofx.LoudnessEnhancer
import com.devson.nvplayerlite.util.AppLogger
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.session.MediaSession
import com.devson.nvplayerlite.repository.DecoderMode
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.video.VideoRendererEventListener
import androidx.media3.exoplayer.Renderer
import android.os.Handler
import androidx.media3.common.MimeTypes

@OptIn(UnstableApi::class)
class HDRFallbackRenderersFactory(
    context: Context,
    preferences: DecoderPreferences = DecoderPreferences.DEFAULT
) : FFmpegRenderersFactory(context, preferences) {
    override fun buildVideoRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        eventHandler: Handler,
        eventListener: VideoRendererEventListener,
        allowedVideoJoiningTimeMs: Long,
        out: java.util.ArrayList<Renderer>
    ) {
        val dvFallbackCodecSelector = object : MediaCodecSelector {
            override fun getDecoderInfos(
                mimeType: String,
                requiresSecureDecoder: Boolean,
                requiresTunnelingDecoder: Boolean
            ): MutableList<MediaCodecInfo> {
                val targetMimeType = if (mimeType == MimeTypes.VIDEO_DOLBY_VISION) {
                    MimeTypes.VIDEO_H265
                } else {
                    mimeType
                }
                return MediaCodecSelector.DEFAULT.getDecoderInfos(
                    targetMimeType,
                    requiresSecureDecoder,
                    requiresTunnelingDecoder
                )
            }
        }

        super.buildVideoRenderers(
            context,
            extensionRendererMode,
            dvFallbackCodecSelector,
            enableDecoderFallback,
            eventHandler,
            eventListener,
            allowedVideoJoiningTimeMs,
            out
        )
    }
}

@OptIn(UnstableApi::class)
class PlayerManager(private val context: Context) {

    var exoPlayer: ExoPlayer? = null
        private set

    var mediaSession: MediaSession? = null
        private set

    var onVideoEnded: (() -> Unit)? = null

    var onPlaybackError: ((String) -> Unit)? = null

    var onPlayNext: (() -> Unit)? = null

    var onPlayPrevious: (() -> Unit)? = null

    var onDecoderFallback: ((DecoderMode) -> Unit)? = null

    var currentDecoderMode: DecoderMode = DecoderMode.HW_PLUS
        private set

    var currentDecoderPreferences: DecoderPreferences = DecoderPreferences.DEFAULT
        private set

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _bufferedPosition = MutableStateFlow(0L)
    val bufferedPosition: StateFlow<Long> = _bufferedPosition.asStateFlow()

    private val _isPortraitVideo = MutableStateFlow<Boolean?>(null)
    val isPortraitVideo: StateFlow<Boolean?> = _isPortraitVideo.asStateFlow()

    private val _videoFps = MutableStateFlow(0f)
    val videoFps: StateFlow<Float> = _videoFps.asStateFlow()

    @Volatile private var isSeekingExoPlayer = false

    private val _videoDecoderName = MutableStateFlow<String?>(null)
    val videoDecoderName: StateFlow<String?> = _videoDecoderName.asStateFlow()

    private val _audioDecoderName = MutableStateFlow<String?>(null)
    val audioDecoderName: StateFlow<String?> = _audioDecoderName.asStateFlow()

    private val _videoDecoderState = MutableStateFlow(DecoderState.IDLE)
    val videoDecoderState: StateFlow<DecoderState> = _videoDecoderState.asStateFlow()

    private val _audioDecoderState = MutableStateFlow(DecoderState.IDLE)
    val audioDecoderState: StateFlow<DecoderState> = _audioDecoderState.asStateFlow()

    private val _fallbackReason = MutableStateFlow<String?>(null)
    val fallbackReason: StateFlow<String?> = _fallbackReason.asStateFlow()

    private val _averageDecodeLatencyMs = MutableStateFlow(0.0)
    val averageDecodeLatencyMs: StateFlow<Double> = _averageDecodeLatencyMs.asStateFlow()

    private val _framesDropped = MutableStateFlow(0L)
    val framesDropped: StateFlow<Long> = _framesDropped.asStateFlow()

    @Volatile private var pendingHwVideoDecoderName: String? = null
    @Volatile private var pendingHwAudioDecoderName: String? = null
    @Volatile private var lastStructuredError: StructuredPlaybackError? = null

    private val _playerError = MutableStateFlow<String?>(null)
    val playerError: StateFlow<String?> = _playerError.asStateFlow()

    // --- Audio Tracks ---
    private val _audioTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val audioTracks: StateFlow<List<TrackInfo>> = _audioTracks.asStateFlow()

    private val _selectedAudioIndex = MutableStateFlow(-1)
    val selectedAudioIndex: StateFlow<Int> = _selectedAudioIndex.asStateFlow()

    // --- Subtitle Tracks ---
    private val _subtitleTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val subtitleTracks: StateFlow<List<TrackInfo>> = _subtitleTracks.asStateFlow()

    private val _selectedSubtitleIndex = MutableStateFlow(-1)
    val selectedSubtitleIndex: StateFlow<Int> = _selectedSubtitleIndex.asStateFlow()

    // --- Chapters ---
    private val _chapters = MutableStateFlow<List<com.devson.nvplayerlite.model.ChapterInfo>>(emptyList())
    val chapters: StateFlow<List<com.devson.nvplayerlite.model.ChapterInfo>> = _chapters.asStateFlow()

    private val _isAudioBoostEnabled = MutableStateFlow(false)
    val isAudioBoostEnabled: StateFlow<Boolean> = _isAudioBoostEnabled.asStateFlow()

    private var loudnessEnhancer: LoudnessEnhancer? = null

    var preferredAudioLang: String = ""
        private set
    var preferredSubtitleLang: String = ""
        private set
    private var hasAutoSelectedTracksForCurrentMedia = false
    private var currentExternalSubtitleUri: Uri? = null

    fun initializePlayer(
        decoderMode: DecoderMode = DecoderMode.HW_PLUS,
        defaultAudioLang: String = "",
        defaultSubtitleLang: String = "",
        customPreferences: DecoderPreferences? = null
    ) {
        currentDecoderMode = decoderMode
        preferredAudioLang = defaultAudioLang
        preferredSubtitleLang = defaultSubtitleLang

        val preferences = customPreferences ?: when (decoderMode) {
            DecoderMode.HW -> DecoderPreferences(
                video = DecoderPreference.PLATFORM_ONLY,
                audio = DecoderPreference.PLATFORM_ONLY,
                allowFallback = false
            )
            DecoderMode.HW_PLUS -> DecoderPreferences(
                video = DecoderPreference.PLATFORM_PREFERRED,
                audio = DecoderPreference.PLATFORM_PREFERRED,
                allowFallback = true
            )
            DecoderMode.SW -> DecoderPreferences(
                video = DecoderPreference.SOFTWARE_ONLY,
                audio = DecoderPreference.SOFTWARE_ONLY,
                allowFallback = false
            )
        }
        currentDecoderPreferences = preferences

        if (exoPlayer == null) {
            NosvedMedia.initialize(context)
            val extensionMode = when (preferences.video) {
                DecoderPreference.PLATFORM_ONLY -> androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
                DecoderPreference.PLATFORM_PREFERRED -> androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
                DecoderPreference.SOFTWARE_PREFERRED, DecoderPreference.SOFTWARE_ONLY -> androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                DecoderPreference.AUTO -> androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
            }
            val renderersFactory = HDRFallbackRenderersFactory(context, preferences).apply {
                setExtensionRendererMode(extensionMode)
                setEnableDecoderFallback(preferences.allowFallback)
                forceDisableMediaCodecAsynchronousQueueing()
                setDecoderPreferences(preferences)
                setSelectionListener(object : DecoderSelectionListener {
                    override fun onDecoderSelected(rendererName: String, isSoftware: Boolean, format: androidx.media3.common.Format) {
                        AppLogger.log("DecoderSelection: Candidate selected renderer=$rendererName isSoftware=$isSoftware")
                        val isVideo = format.sampleMimeType?.startsWith("video/") == true
                        if (isVideo) {
                            _videoDecoderState.value = DecoderState.CANDIDATE_SELECTED
                        } else {
                            _audioDecoderState.value = DecoderState.CANDIDATE_SELECTED
                        }
                    }

                    override fun onDecoderActivated(trackType: TrackType, rendererName: String, decoderEngine: String, format: androidx.media3.common.Format) {
                        AppLogger.log("DecoderSelection: Decoder activated track=$trackType -> $decoderEngine via $rendererName")
                        if (trackType == TrackType.VIDEO) {
                            _videoDecoderName.value = decoderEngine
                            _videoDecoderState.value = DecoderState.PLAYING
                        } else if (trackType == TrackType.AUDIO) {
                            _audioDecoderName.value = decoderEngine
                            _audioDecoderState.value = DecoderState.PLAYING
                        }
                    }

                    override fun onDecoderReleased(trackType: TrackType, rendererName: String) {
                        AppLogger.log("DecoderSelection: Decoder released track=$trackType -> $rendererName")
                        if (trackType == TrackType.VIDEO) {
                            _videoDecoderState.value = DecoderState.RELEASED
                        } else if (trackType == TrackType.AUDIO) {
                            _audioDecoderState.value = DecoderState.RELEASED
                        }
                    }
                })
                setDiagnosticsListener(object : PlaybackDiagnosticsListener {
                    override fun onDecoderStateChanged(status: TrackDecoderStatus) {
                        AppLogger.log("DiagnosticsListener: track=${status.trackType} state=${status.state} engine=${status.actualDecoderEngine}")
                        if (status.fallbackReason != null) {
                            _fallbackReason.value = status.fallbackReason
                        }
                        if (status.trackType == TrackType.VIDEO) {
                            _videoDecoderState.value = status.state
                            if (status.isActivelyDecoding && status.actualDecoderEngine != null) {
                                _videoDecoderName.value = status.actualDecoderEngine
                            }
                        } else if (status.trackType == TrackType.AUDIO) {
                            _audioDecoderState.value = status.state
                            if (status.isActivelyDecoding && status.actualDecoderEngine != null) {
                                _audioDecoderName.value = status.actualDecoderEngine
                            }
                        }
                    }

                    override fun onFirstFrameRendered(trackType: TrackType, decoderEngine: String, ptsUs: Long) {
                        AppLogger.log("DiagnosticsListener: First frame rendered track=$trackType engine=$decoderEngine at $ptsUs us")
                        if (trackType == TrackType.VIDEO) {
                            _videoDecoderName.value = decoderEngine
                            _videoDecoderState.value = DecoderState.PLAYING
                        } else if (trackType == TrackType.AUDIO) {
                            _audioDecoderName.value = decoderEngine
                            _audioDecoderState.value = DecoderState.PLAYING
                        }
                    }

                    override fun onPerformanceMetricsUpdated(metrics: DecodingPerformanceMetrics) {
                        if (metrics.currentFps > 0.0) {
                            _videoFps.value = metrics.currentFps.toFloat()
                        }
                        _averageDecodeLatencyMs.value = metrics.averageDecodeTimeMs
                        _framesDropped.value = metrics.totalFramesDropped
                    }

                    override fun onStructuredError(error: StructuredPlaybackError) {
                        AppLogger.log("DiagnosticsListener: Structured error: ${error.category} on ${error.trackType}: ${error.errorMessage}")
                        lastStructuredError = error
                    }
                })
            }

            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                    1500,
                    2500
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()

            exoPlayer = ExoPlayer.Builder(context)
                .setRenderersFactory(renderersFactory)
                .setLoadControl(loadControl)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .build().apply {
                    setSeekParameters(androidx.media3.exoplayer.SeekParameters.CLOSEST_SYNC)
                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            if (playbackState == Player.STATE_READY) {
                                _duration.value = duration.coerceAtLeast(0L)
                            } else if (playbackState == Player.STATE_ENDED) {
                                _isPlaying.value = false
                                onVideoEnded?.invoke()
                            }
                        }

                        override fun onPositionDiscontinuity(
                            oldPosition: Player.PositionInfo,
                            newPosition: Player.PositionInfo,
                            reason: Int
                        ) {
                            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                            isSeekingExoPlayer = false
                            _currentPosition.value = newPosition.positionMs.coerceAtLeast(0L)
                        }

                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            if (videoSize.width > 0 && videoSize.height > 0) {
                                _isPortraitVideo.value = videoSize.height > videoSize.width
                            }
                        }

                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            super.onMediaItemTransition(mediaItem, reason)
                            hasAutoSelectedTracksForCurrentMedia = false
                            currentExternalSubtitleUri = null
                            pendingHwVideoDecoderName = null
                            pendingHwAudioDecoderName = null
                            lastStructuredError = null
                            _videoDecoderName.value = null
                            _audioDecoderName.value = null
                            _videoDecoderState.value = DecoderState.IDLE
                            _audioDecoderState.value = DecoderState.IDLE
                            _videoFps.value = 0f
                        }

                        override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                            super.onTracksChanged(tracks)

                            val newAudioTracks = mutableListOf<TrackInfo>()
                            val newSubtitleTracks = mutableListOf<TrackInfo>()
                            var currentAudioIdx = -1
                            var currentSubIdx = -1

                            val audioGroupCount = tracks.groups.count { it.type == androidx.media3.common.C.TRACK_TYPE_AUDIO }
                            val subtitleGroupCount = tracks.groups.count { it.type == androidx.media3.common.C.TRACK_TYPE_TEXT }

                            fun getAudioCodecDescription(mime: String?): String = when {
                                mime == null -> ""
                                mime.contains("eac3", ignoreCase = true) || mime.contains("e-ac3", ignoreCase = true) -> "Dolby Digital Plus"
                                mime.contains("ac3", ignoreCase = true) || mime.contains("ac-3", ignoreCase = true) -> "Dolby Digital"
                                mime.contains("dts-hd", ignoreCase = true) -> "DTS-HD"
                                mime.contains("dts", ignoreCase = true) -> "DTS"
                                mime.contains("truehd", ignoreCase = true) -> "Dolby TrueHD"
                                mime.contains("mp4a", ignoreCase = true) || mime.contains("aac", ignoreCase = true) -> "AAC"
                                mime.contains("opus", ignoreCase = true) -> "Opus"
                                mime.contains("vorbis", ignoreCase = true) -> "Vorbis"
                                mime.contains("flac", ignoreCase = true) -> "FLAC"
                                mime.contains("pcm", ignoreCase = true) || mime.contains("raw", ignoreCase = true) -> "PCM"
                                mime.contains("mpeg", ignoreCase = true) || mime.contains("mp3", ignoreCase = true) -> "MP3"
                                else -> mime.substringAfterLast("/").uppercase()
                            }

                            fun getChannelLayoutDescription(channels: Int): String = when (channels) {
                                1 -> "Mono (1.0)"
                                2 -> "Stereo (2.0)"
                                6 -> "5.1 Surround"
                                8 -> "7.1 Surround"
                                else -> if (channels > 0) "$channels Ch" else ""
                            }

                            fun getSubtitleFormatDescription(mime: String?): String = when {
                                mime == null -> ""
                                mime.contains("subrip", ignoreCase = true) || mime.contains("srt", ignoreCase = true) -> "SubRip (SRT)"
                                mime.contains("x-ssa", ignoreCase = true) || mime.contains("x-ass", ignoreCase = true) || mime.contains("ssa", ignoreCase = true) || mime.contains("ass", ignoreCase = true) -> "ASS/SSA"
                                mime.contains("vtt", ignoreCase = true) -> "WebVTT"
                                mime.contains("pgs", ignoreCase = true) -> "PGS"
                                mime.contains("ttml", ignoreCase = true) -> "TTML"
                                mime.contains("dvbsub", ignoreCase = true) -> "DVB Subtitle"
                                else -> mime.substringAfterLast("/").uppercase()
                            }

                            for (group in tracks.groups) {
                                if (group.type == androidx.media3.common.C.TRACK_TYPE_VIDEO && group.isSelected) {
                                    val format = group.getTrackFormat(0)
                                    if (format.frameRate > 0f) {
                                        _videoFps.value = format.frameRate
                                    }
                                } else if (group.type == androidx.media3.common.C.TRACK_TYPE_AUDIO) {
                                    val format = group.getTrackFormat(0)
                                    val trackIndex = newAudioTracks.size
                                    val langDisplayName = LanguageList.getDisplayName(format.language)
                                    val nativeLangName = LanguageList.getNativeName(format.language)

                                    val label = format.label?.takeIf { it.isNotBlank() }
                                        ?: if (audioGroupCount == 1) {
                                            if (langDisplayName != "Default") langDisplayName else "Default Audio"
                                        } else {
                                            if (langDisplayName != "Default") langDisplayName else "Track ${trackIndex + 1}"
                                        }

                                    val channelText = getChannelLayoutDescription(format.channelCount)
                                    val codecText = getAudioCodecDescription(format.sampleMimeType)
                                    val details = listOfNotNull(channelText.takeIf { it.isNotBlank() }, codecText.takeIf { it.isNotBlank() }).joinToString(" • ")

                                    newAudioTracks.add(
                                        TrackInfo(
                                            index = trackIndex,
                                            label = label,
                                            language = format.language,
                                            selected = group.isSelected,
                                            codec = format.sampleMimeType,
                                            isExternal = false,
                                            displayName = label,
                                            originalLanguageName = nativeLangName,
                                            trackDetails = details.ifBlank { null }
                                        )
                                    )
                                    if (group.isSelected) currentAudioIdx = trackIndex
                                } else if (group.type == androidx.media3.common.C.TRACK_TYPE_TEXT) {
                                    val format = group.getTrackFormat(0)
                                    val trackIndex = newSubtitleTracks.size
                                    val langDisplayName = LanguageList.getDisplayName(format.language)
                                    val nativeLangName = LanguageList.getNativeName(format.language)

                                    val label = format.label?.takeIf { it.isNotBlank() }
                                        ?: if (subtitleGroupCount == 1) {
                                            if (langDisplayName != "Default") langDisplayName else "Default Subtitle"
                                        } else {
                                            if (langDisplayName != "Default") langDisplayName else "Track ${trackIndex + 1}"
                                        }

                                    val formatText = getSubtitleFormatDescription(format.sampleMimeType)
                                    val isExt = (currentExternalSubtitleUri != null && trackIndex == subtitleGroupCount - 1) ||
                                        (format.id?.startsWith("content://") == true || format.id?.startsWith("file://") == true)

                                    newSubtitleTracks.add(
                                        TrackInfo(
                                            index = trackIndex,
                                            label = label,
                                            language = format.language,
                                            selected = group.isSelected,
                                            codec = format.sampleMimeType,
                                            isExternal = isExt,
                                            displayName = label,
                                            originalLanguageName = nativeLangName,
                                            trackDetails = formatText.ifBlank { null }
                                        )
                                    )
                                    if (group.isSelected) currentSubIdx = trackIndex
                                }
                            }

                            // Prepend "None (Off)" for subtitles
                            val isNoneSelected = (currentSubIdx == -1)
                            newSubtitleTracks.add(
                                0,
                                TrackInfo(
                                    index = -1,
                                    label = "None (Off)",
                                    language = null,
                                    selected = isNoneSelected,
                                    displayName = "None (Off)"
                                )
                            )

                            // Automatic preferred language selection for newly loaded media
                            if (!hasAutoSelectedTracksForCurrentMedia && (newAudioTracks.isNotEmpty() || newSubtitleTracks.size > 1)) {
                                hasAutoSelectedTracksForCurrentMedia = true

                                // Preferred Audio
                                if (preferredAudioLang.isNotBlank() && newAudioTracks.isNotEmpty()) {
                                    val matchedAudio = newAudioTracks.find { LanguageList.isMatch(it.language, it.label, preferredAudioLang) }
                                    if (matchedAudio != null && matchedAudio.id != currentAudioIdx) {
                                        selectAudioTrack(matchedAudio.id)
                                        currentAudioIdx = matchedAudio.id
                                    }
                                }

                                // Preferred Subtitle
                                if (preferredSubtitleLang.isNotBlank()) {
                                    val matchedSub = newSubtitleTracks.find { it.id != -1 && LanguageList.isMatch(it.language, it.label, preferredSubtitleLang) }
                                    if (matchedSub != null) {
                                        if (matchedSub.id != currentSubIdx) {
                                            selectSubtitleTrack(matchedSub.id)
                                            currentSubIdx = matchedSub.id
                                        }
                                    } else {
                                        // Preferred subtitle language not found in video -> disable subtitles
                                        if (currentSubIdx != -1) {
                                            selectSubtitleTrack(-1)
                                            currentSubIdx = -1
                                        }
                                    }
                                } else {
                                    // Subtitle preferred set to None -> disable subtitles
                                    if (currentSubIdx != -1) {
                                        selectSubtitleTrack(-1)
                                        currentSubIdx = -1
                                    }
                                }
                            }

                            _audioTracks.value = newAudioTracks.map { it.copy(selected = it.id == currentAudioIdx) }
                            _selectedAudioIndex.value = currentAudioIdx

                            _subtitleTracks.value = newSubtitleTracks.map { it.copy(selected = it.id == currentSubIdx) }
                            _selectedSubtitleIndex.value = currentSubIdx
                        }

                        override fun onMetadata(metadata: androidx.media3.common.Metadata) {
                            super.onMetadata(metadata)
                            val list = mutableListOf<com.devson.nvplayerlite.model.ChapterInfo>()
                            for (i in 0 until metadata.length()) {
                                val entry = metadata.get(i)
                                if (entry is androidx.media3.extractor.metadata.id3.ChapterFrame) {
                                    var title = entry.chapterId.ifEmpty { "Chapter ${list.size + 1}" }
                                    for (j in 0 until entry.subFrameCount) {
                                        val sub = entry.getSubFrame(j)
                                        if (sub is androidx.media3.extractor.metadata.id3.TextInformationFrame) {
                                            sub.values.firstOrNull()?.let { title = it }
                                            break
                                        }
                                    }
                                    list.add(
                                        com.devson.nvplayerlite.model.ChapterInfo(
                                            index = list.size,
                                            title = title,
                                            timeMs = entry.startTimeMs.toLong()
                                        )
                                    )
                                }
                            }
                            if (list.isNotEmpty()) {
                                _chapters.value = list.sortedBy { it.timeMs }.mapIndexed { idx, item ->
                                    item.copy(index = idx)
                                }
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            super.onPlayerError(error)

                            AppLogger.log("ExoPlayer error: ${error.message} - $error")

                            val structured = lastStructuredError
                            val category = structured?.category ?: when (error.errorCode) {
                                PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> DecodingCategory.UNSUPPORTED_FORMAT
                                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> DecodingCategory.DECODER_INITIALIZATION_FAILED
                                PlaybackException.ERROR_CODE_DECODING_FAILED -> DecodingCategory.DECODING_FAILED
                                PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED,
                                PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED -> DecodingCategory.AUDIO_OUTPUT_FAILED
                                PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
                                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
                                PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE,
                                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
                                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                                PlaybackException.ERROR_CODE_IO_NO_PERMISSION,
                                PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED,
                                PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE -> DecodingCategory.SOURCE_ERROR
                                else -> if (error.message?.contains("MediaCodecVideoRenderer") == true) DecodingCategory.DECODING_FAILED else null
                            }

                            val isFormatUnsupported = category == DecodingCategory.UNSUPPORTED_FORMAT || category == DecodingCategory.UNSUPPORTED_PROFILE
                            val isDecoderFailed = category == DecodingCategory.DECODER_INITIALIZATION_FAILED
                            val isDecodingFailed = category == DecodingCategory.DECODING_FAILED
                            val isSourceError = category == DecodingCategory.SOURCE_ERROR ||
                                    error.cause?.cause is IllegalStateException ||
                                    error.cause is Loader.UnexpectedLoaderException

                            val isAudioError = structured?.trackType == TrackType.AUDIO ||
                                    error.message?.contains("Audio", ignoreCase = true) == true ||
                                    error.cause?.message?.contains("Audio", ignoreCase = true) == true ||
                                    error.errorCodeName.contains("AUDIO", ignoreCase = true)

                            when {
                                isDecodingFailed && !isAudioError && currentDecoderMode == DecoderMode.HW -> {
                                    AppLogger.log("Recovering from hardware decoding failure...")
                                    val lastPos = exoPlayer?.currentPosition ?: _currentPosition.value
                                    if (onDecoderFallback != null) {
                                        onDecoderFallback?.invoke(DecoderMode.HW_PLUS)
                                    } else {
                                        exoPlayer?.prepare()
                                        exoPlayer?.seekTo(lastPos)
                                    }
                                }
                                isFormatUnsupported || isDecoderFailed || isDecodingFailed -> {
                                    val fallbackMode = when (currentDecoderMode) {
                                        DecoderMode.HW -> DecoderMode.HW_PLUS
                                        DecoderMode.SW -> if (currentDecoderPreferences.allowFallback) DecoderMode.HW_PLUS else null
                                        DecoderMode.HW_PLUS -> if (isAudioError) null else DecoderMode.SW
                                    }
                                    if (fallbackMode != null && fallbackMode != currentDecoderMode && onDecoderFallback != null) {
                                        AppLogger.log("Decoder fallback: Switching from $currentDecoderMode to $fallbackMode due to ${category.name}")
                                        onDecoderFallback?.invoke(fallbackMode)
                                    } else {
                                        val prefix = when (category) {
                                            DecodingCategory.UNSUPPORTED_FORMAT -> "Unsupported format"
                                            DecodingCategory.UNSUPPORTED_PROFILE -> "Unsupported profile"
                                            DecodingCategory.DECODER_INITIALIZATION_FAILED -> "Decoder initialization failed"
                                            DecodingCategory.DECODING_FAILED -> "Decoding failed"
                                            DecodingCategory.INVALID_CODEC_CONFIGURATION -> "Invalid codec configuration"
                                            DecodingCategory.SURFACE_RENDERING_FAILED -> "Surface rendering failed"
                                            DecodingCategory.AUDIO_OUTPUT_FAILED -> "Audio output failed"
                                            DecodingCategory.MEMORY_ALLOCATION_FAILED -> "Memory allocation failed"
                                            DecodingCategory.SOURCE_ERROR -> "Source error"
                                        }
                                        val trackMsg = if (isAudioError) "Audio" else "Video"
                                        val engineInfo = structured?.decoderEngine?.let { " [$it]" } ?: ""
                                        val errorMsg = "$prefix: $trackMsg$engineInfo (${error.localizedMessage ?: error.errorCodeName})"
                                        _playerError.value = errorMsg
                                    }
                                }
                                isSourceError -> {
                                    // Skip the broken file - notify the ViewModel
                                    val msg = error.message ?: "Source error"
                                    onPlaybackError?.invoke(msg)
                                }
                                else -> {
                                    _playerError.value = error.localizedMessage ?: error.message ?: "Unknown playback error"
                                }
                            }
                        }
                    })

                    addAnalyticsListener(object : androidx.media3.exoplayer.analytics.AnalyticsListener {
                        override fun onAudioSessionIdChanged(
                            eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
                            audioSessionId: Int
                        ) {
                            if (_isAudioBoostEnabled.value) {
                                applyLoudnessEnhancer(audioSessionId)
                            }
                        }

                        override fun onVideoDecoderInitialized(
                            eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
                            decoderName: String,
                            initializedTimestampMs: Long,
                            initializationDurationMs: Long
                        ) {
                            pendingHwVideoDecoderName = decoderName
                            if (_videoDecoderState.value != DecoderState.PLAYING) {
                                _videoDecoderState.value = DecoderState.INITIALIZED
                            }
                        }

                        override fun onRenderedFirstFrame(
                            eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
                            output: Any,
                            renderTimeMs: Long
                        ) {
                            val name = pendingHwVideoDecoderName
                            if (name != null) {
                                _videoDecoderName.value = name
                                _videoDecoderState.value = DecoderState.PLAYING
                            }
                        }

                        override fun onAudioDecoderInitialized(
                            eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
                            decoderName: String,
                            initializedTimestampMs: Long,
                            initializationDurationMs: Long
                        ) {
                            pendingHwAudioDecoderName = decoderName
                            if (_audioDecoderState.value != DecoderState.PLAYING) {
                                _audioDecoderState.value = DecoderState.INITIALIZED
                            }
                        }

                        override fun onAudioTrackInitialized(
                            eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
                            audioTrackConfig: androidx.media3.exoplayer.audio.AudioSink.AudioTrackConfig
                        ) {
                            val name = pendingHwAudioDecoderName ?: "System AudioTrack"
                            _audioDecoderName.value = name
                            _audioDecoderState.value = DecoderState.PLAYING
                        }

                        override fun onAudioPositionAdvancing(
                            eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
                            playoutStartSystemTimeMs: Long
                        ) {
                            if (_audioDecoderName.value == null && pendingHwAudioDecoderName != null) {
                                _audioDecoderName.value = pendingHwAudioDecoderName
                                _audioDecoderState.value = DecoderState.PLAYING
                            }
                        }
                    })

                    // Dynamically calculate FPS for decoders rendering frames
                    setVideoFrameMetadataListener(object : androidx.media3.exoplayer.video.VideoFrameMetadataListener {
                        private var frameCount = 0
                        private var lastTimeMs = System.currentTimeMillis()

                        override fun onVideoFrameAboutToBeRendered(
                            presentationTimeUs: Long,
                            releaseTimeNs: Long,
                            format: androidx.media3.common.Format,
                            mediaFormat: android.media.MediaFormat?
                        ) {
                            frameCount++
                            val now = System.currentTimeMillis()
                            val diff = now - lastTimeMs
                            if (diff >= 1000) {
                                val currentFps = (frameCount * 1000f) / diff
                                _videoFps.value = currentFps
                                frameCount = 0
                                lastTimeMs = now
                            }
                        }
                    })
                }

            val player = exoPlayer ?: return
            val forwardingPlayer = object : ForwardingPlayer(player) {
                override fun seekToNext() {
                    onPlayNext?.invoke()
                }
                override fun seekToNextMediaItem() {
                    onPlayNext?.invoke()
                }
                override fun seekToPrevious() {
                    onPlayPrevious?.invoke()
                }
                override fun seekToPreviousMediaItem() {
                    onPlayPrevious?.invoke()
                }
                override fun getAvailableCommands(): Player.Commands {
                    return super.getAvailableCommands().buildUpon()
                        .add(Player.COMMAND_SEEK_TO_NEXT)
                        .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                        .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .build()
                }
            }

            mediaSession = MediaSession.Builder(context, forwardingPlayer).build()

            val audioEquivs = LanguageList.getEquivalents(defaultAudioLang)
            val textEquivs = LanguageList.getEquivalents(defaultSubtitleLang)
            val paramBuilder = player.trackSelectionParameters.buildUpon()
            if (audioEquivs.isNotEmpty()) {
                paramBuilder.setPreferredAudioLanguages(*audioEquivs.toTypedArray())
            } else {
                paramBuilder.setPreferredAudioLanguages()
            }
            if (textEquivs.isNotEmpty()) {
                paramBuilder.setPreferredTextLanguages(*textEquivs.toTypedArray())
                paramBuilder.setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, false)
            } else {
                paramBuilder.setPreferredTextLanguages()
                paramBuilder.setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, true)
            }
            exoPlayer?.trackSelectionParameters = paramBuilder.build()
        }
    }

    fun updatePreferredLanguages(audioLang: String, subLang: String) {
        preferredAudioLang = audioLang
        preferredSubtitleLang = subLang
        val player = exoPlayer ?: return

        val audioEquivs = LanguageList.getEquivalents(audioLang)
        val textEquivs = LanguageList.getEquivalents(subLang)
        val paramBuilder = player.trackSelectionParameters.buildUpon()

        if (audioEquivs.isNotEmpty()) {
            paramBuilder.setPreferredAudioLanguages(*audioEquivs.toTypedArray())
        } else {
            paramBuilder.setPreferredAudioLanguages()
        }

        if (textEquivs.isNotEmpty()) {
            paramBuilder.setPreferredTextLanguages(*textEquivs.toTypedArray())
            paramBuilder.setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, false)
        } else {
            paramBuilder.setPreferredTextLanguages()
            paramBuilder.setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, true)
        }
        player.trackSelectionParameters = paramBuilder.build()

        if (audioLang.isNotBlank()) {
            val matchingAudio = _audioTracks.value.find { LanguageList.isMatch(it.language, it.label, audioLang) }
            if (matchingAudio != null) {
                selectAudioTrack(matchingAudio.id)
            }
        }

        if (subLang.isNotBlank()) {
            val matchingSub = _subtitleTracks.value.find { it.id != -1 && LanguageList.isMatch(it.language, it.label, subLang) }
            if (matchingSub != null) {
                selectSubtitleTrack(matchingSub.id)
            } else {
                selectSubtitleTrack(-1)
            }
        } else {
            selectSubtitleTrack(-1)
        }
    }

    private fun applyLoudnessEnhancer(audioSessionId: Int) {
        if (audioSessionId == androidx.media3.common.C.AUDIO_SESSION_ID_UNSET) return
        try {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                // 1500 mB = 15 dB boost (roughly 2x to 3x perceived loudness)
                setTargetGain(1500)
                enabled = true
            }
        } catch (e: Exception) {
            AppLogger.log("Failed to initialize LoudnessEnhancer: ${e.message}")
        }
    }

    @SuppressLint("Range")
    fun toggleAudioBoost(enabled: Boolean) {
        _isAudioBoostEnabled.value = enabled
        val player = exoPlayer ?: return

        if (enabled) {
            player.volume = 2.0f
            applyLoudnessEnhancer(player.audioSessionId)
        } else {
            player.volume = 1.0f
            try {
                loudnessEnhancer?.enabled = false
                loudnessEnhancer?.release()
            } catch (e: Exception) {}
            loudnessEnhancer = null
        }
    }

    fun playVideo(video: Video, startPositionMs: Long = 0L) {
        val player = exoPlayer ?: return
        hasAutoSelectedTracksForCurrentMedia = false
        currentExternalSubtitleUri = null
        pendingHwVideoDecoderName = null
        pendingHwAudioDecoderName = null
        lastStructuredError = null
        _playerError.value = null
        _videoDecoderName.value = null
        _audioDecoderName.value = null
        _videoDecoderState.value = DecoderState.IDLE
        _audioDecoderState.value = DecoderState.IDLE
        _fallbackReason.value = null
        _averageDecodeLatencyMs.value = 0.0
        _framesDropped.value = 0L
        _videoFps.value = 0f
        _chapters.value = emptyList()
        val mediaItem = MediaItem.fromUri(video.uri)
        if (startPositionMs > 0L) {
            player.setMediaItem(mediaItem, startPositionMs)
            _currentPosition.value = startPositionMs
        } else {
            player.setMediaItem(mediaItem)
        }
        player.prepare()
        player.playWhenReady = true
        _isPlaying.value = true
    }

    fun clearError() {
        _playerError.value = null
    }

    fun setPlaybackSpeed(speed: Float) {
        val player = exoPlayer ?: return
        player.playbackParameters = androidx.media3.common.PlaybackParameters(speed)
    }

    fun playPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying || (player.playWhenReady && player.playbackState != Player.STATE_ENDED)) {
            player.pause()
            _isPlaying.value = false
        } else {
            player.play()
            _isPlaying.value = true
        }
    }

    fun pause() {
        _isPlaying.value = false
        exoPlayer?.pause()
    }

    fun resume() {
        _isPlaying.value = true
        exoPlayer?.play()
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        val duration = player.duration.coerceAtLeast(0L)
        val target = positionMs.coerceIn(0L, duration)
        isSeekingExoPlayer = true
        _videoFps.value = 0f
        player.seekTo(target)
        _currentPosition.value = target
    }

    fun seekSmooth(positionMs: Long) {
        val player = exoPlayer ?: return
        val duration = player.duration.coerceAtLeast(0L)
        val target = positionMs.coerceIn(0L, duration)
        isSeekingExoPlayer = true
        _videoFps.value = 0f
        player.seekTo(target)
        _currentPosition.value = target
    }

    fun seekForward(ms: Long = 10000L) {
        val player = exoPlayer ?: return
        val currentPos = _currentPosition.value
        val newPos = (currentPos + ms).coerceAtMost(player.duration.coerceAtLeast(0L))
        seekTo(newPos)
    }

    fun seekBackward(ms: Long = 10000L) {
        val player = exoPlayer ?: return
        val currentPos = _currentPosition.value
        val newPos = (currentPos - ms).coerceAtLeast(0L)
        seekTo(newPos)
    }

    fun updateProgress() {
        val player = exoPlayer ?: return
        if (!isSeekingExoPlayer) {
            _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
        }
        _bufferedPosition.value = player.bufferedPosition.coerceAtLeast(0L)
        _duration.value = player.duration.coerceAtLeast(0L)
        
        player.videoFormat?.frameRate?.let { fps ->
            if (fps > 0f) {
                _videoFps.value = fps
            }
        }
    }

    fun releasePlayerAndKeepState(): Pair<Long, Boolean> {
        val pos = exoPlayer?.currentPosition?.takeIf { it > 0L } ?: _currentPosition.value
        val playing = exoPlayer?.isPlaying ?: _isPlaying.value
        releasePlayer()
        return Pair(pos, playing)
    }

    fun releasePlayer() {
        pendingHwVideoDecoderName = null
        pendingHwAudioDecoderName = null
        lastStructuredError = null
        _videoDecoderName.value = null
        _audioDecoderName.value = null
        _videoDecoderState.value = DecoderState.RELEASED
        _audioDecoderState.value = DecoderState.RELEASED
        _fallbackReason.value = null
        _averageDecodeLatencyMs.value = 0.0
        _framesDropped.value = 0L
        _videoFps.value = 0f
        mediaSession?.release()
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
        try {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
        } catch (e: Exception) {}
        loudnessEnhancer = null
    }

    @UnstableApi
    fun setScrubbingMode(enabled: Boolean) {
        exoPlayer?.setScrubbingModeEnabled(enabled)
    }

    // --- Audio & Subtitle Selection ---

    fun selectAudioTrack(index: Int) {
        val player = exoPlayer ?: return
        var audioGroupIndex = -1
        var matchCount = 0

        val tracks = player.currentTracks
        for (i in tracks.groups.indices) {
            val group = tracks.groups[i]
            if (group.type == androidx.media3.common.C.TRACK_TYPE_AUDIO) {
                if (matchCount == index) {
                    audioGroupIndex = i
                    break
                }
                matchCount++
            }
        }

        if (audioGroupIndex != -1) {
            val trackGroup = tracks.groups[audioGroupIndex].mediaTrackGroup
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setOverrideForType(TrackSelectionOverride(trackGroup, 0))
                .build()
        }
        _selectedAudioIndex.value = index
        _audioTracks.value = _audioTracks.value.map { it.copy(selected = it.id == index) }
    }

    fun selectSubtitleTrack(index: Int) {
        val player = exoPlayer ?: return

        if (index == -1) {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, true)
                .build()
            _selectedSubtitleIndex.value = -1
            _subtitleTracks.value = _subtitleTracks.value.map { it.copy(selected = it.id == -1) }
            return
        }

        var textGroupIndex = -1
        var matchCount = 0

        val tracks = player.currentTracks
        for (i in tracks.groups.indices) {
            val group = tracks.groups[i]
            if (group.type == androidx.media3.common.C.TRACK_TYPE_TEXT) {
                if (matchCount == index) {
                    textGroupIndex = i
                    break
                }
                matchCount++
            }
        }

        if (textGroupIndex != -1) {
            val trackGroup = tracks.groups[textGroupIndex].mediaTrackGroup
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, false)
                .setOverrideForType(TrackSelectionOverride(trackGroup, 0))
                .build()
        }
        _selectedSubtitleIndex.value = index
        _subtitleTracks.value = _subtitleTracks.value.map { it.copy(selected = it.id == index) }
    }

    fun loadExternalSubtitle(uri: Uri, mimeType: String, encodingLabel: String = "UTF-8") {
        val player = exoPlayer ?: return
        val currentMediaItem = player.currentMediaItem ?: return
        currentExternalSubtitleUri = uri
        hasAutoSelectedTracksForCurrentMedia = true

        val uriPath = uri.path ?: uri.toString()
        val ext = uriPath.substringAfterLast('.', "").lowercase()
        val resolvedMimeType = when {
            ext == "ass" || ext == "ssa" -> MimeTypes.TEXT_SSA
            mimeType.isNotEmpty() -> mimeType
            else -> MimeTypes.APPLICATION_SUBRIP
        }

        val subtitleConfig = SubtitleConfiguration.Builder(uri)
            .setMimeType(resolvedMimeType)
            .setLanguage("en")
            .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
            .setLabel(encodingLabel)
            .build()

        val newMediaItem = currentMediaItem.buildUpon()
            .setSubtitleConfigurations(listOf(subtitleConfig))
            .build()

        val currentPos = player.currentPosition
        val isPlaying = player.isPlaying

        player.setMediaItem(newMediaItem)
        player.prepare()
        player.seekTo(currentPos)
        player.playWhenReady = isPlaying

        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, false)
            .build()
    }
}