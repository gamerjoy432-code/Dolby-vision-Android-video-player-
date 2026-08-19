package com.example.player

import android.app.Activity
import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.data.db.AppDatabase
import com.example.data.db.MediaRepository
import com.example.data.model.AspectRatioMode
import com.example.data.model.BookmarkEntity
import com.example.data.model.DolbyVisionProfile
import com.example.data.model.HdrFormat
import com.example.data.model.MediaItemEntity
import com.example.data.model.VideoStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TrackInfo(
    val id: String,
    val name: String,
    val language: String?,
    val isSelected: Boolean,
    val trackGroupIndex: Int,
    val trackIndex: Int,
    val group: TrackGroup
)

data class PlayerUiState(
    val currentMedia: MediaItemEntity? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isControlsVisible: Boolean = true,
    val isLocked: Boolean = false,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val playbackSpeed: Float = 1.0f,
    val currentHdrFormat: HdrFormat = HdrFormat.SDR,
    val currentDvProfile: String? = null,
    val isHdrToneMapped: Boolean = true,
    val stats: VideoStats = VideoStats(),
    val isStatsVisible: Boolean = false,
    val volumePercent: Float = 0.5f,
    val brightnessPercent: Float = 0.5f,
    val showVolumeIndicator: Boolean = false,
    val showBrightnessIndicator: Boolean = false,
    val showSeekIndicator: Boolean = false,
    val seekDeltaSeconds: Int = 0,
    val availableAudioTracks: List<TrackInfo> = emptyList(),
    val availableSubtitleTracks: List<TrackInfo> = emptyList(),
    val selectedAudioTrackName: String = "Auto (Default)",
    val selectedSubtitleTrackName: String = "Off",
    val sleepTimerMinutesRemaining: Int? = null,
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val errorMessage: String? = null,
    val isPipActive: Boolean = false
)

@OptIn(UnstableApi::class)
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(AppDatabase.getInstance(application).mediaDao())
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var exoPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val allMedia = repository.allMedia.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favorites = repository.favorites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentHistory = repository.recentHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayReport = HdrCapabilitiesDetector.getDeviceHdrReport(application)

    private var progressJob: Job? = null
    private var controlsHideJob: Job? = null
    private var gestureDismissJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureDefaultSamplesLoaded()
        }
        initInitialVolume()
    }

    private fun initInitialVolume() {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        val ratio = if (maxVol > 0f) curVol / maxVol else 0.5f
        _uiState.value = _uiState.value.copy(volumePercent = ratio)
    }

    fun getOrCreatePlayer(): ExoPlayer {
        if (exoPlayer != null) return exoPlayer!!

        val context = getApplication<Application>()
        trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setPreferredVideoMimeTypes(
                        MimeTypes.VIDEO_DOLBY_VISION,
                        MimeTypes.VIDEO_H265,
                        MimeTypes.VIDEO_AV1,
                        MimeTypes.VIDEO_H264
                    )
                    .setForceHighestSupportedBitrate(true)
            )
        }

        val renderersFactory = DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            setEnableDecoderFallback(true)
        }

        val player = ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector!!)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                _uiState.value = _uiState.value.copy(
                    isBuffering = isBuffering,
                    durationMs = player.duration.coerceAtLeast(0L)
                )
                if (playbackState == Player.STATE_ENDED) {
                    onMediaEnded()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressTracker()
                    scheduleControlsAutoHide()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                extractTracksAndFormats(tracks)
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                val res = "${videoSize.width} x ${videoSize.height}"
                _uiState.value = _uiState.value.copy(
                    stats = _uiState.value.stats.copy(resolution = res)
                )
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _uiState.value = _uiState.value.copy(
                    isBuffering = false,
                    isPlaying = false,
                    errorMessage = "Playback Notice: ${error.localizedMessage ?: "Codec initialization / stream error"}"
                )
            }
        })

        player.addAnalyticsListener(object : AnalyticsListener {
            override fun onDroppedVideoFrames(
                eventTime: AnalyticsListener.EventTime,
                droppedFrames: Int,
                elapsedMs: Long
            ) {
                val totalDropped = _uiState.value.stats.droppedFrames + droppedFrames
                _uiState.value = _uiState.value.copy(
                    stats = _uiState.value.stats.copy(droppedFrames = totalDropped)
                )
            }

            override fun onVideoDecoderInitialized(
                eventTime: AnalyticsListener.EventTime,
                decoderName: String,
                initializedTimestampMs: Long,
                initializationDurationMs: Long
            ) {
                val isHw = !decoderName.contains("sw", ignoreCase = true) && !decoderName.contains("google", ignoreCase = true)
                _uiState.value = _uiState.value.copy(
                    stats = _uiState.value.stats.copy(
                        decoderName = decoderName,
                        isHardwareAccelerated = isHw
                    )
                )
            }

            override fun onBandwidthEstimate(
                eventTime: AnalyticsListener.EventTime,
                totalLoadTimeMs: Int,
                totalBytesLoaded: Long,
                bitrateEstimate: Long
            ) {
                if (bitrateEstimate > 0) {
                    _uiState.value = _uiState.value.copy(
                        stats = _uiState.value.stats.copy(bitrateKbps = bitrateEstimate / 1000)
                    )
                }
            }
        })

        exoPlayer = player
        return player
    }

    fun playMedia(mediaItem: MediaItemEntity) {
        val player = getOrCreatePlayer()
        _uiState.value = _uiState.value.copy(
            currentMedia = mediaItem,
            currentHdrFormat = mediaItem.hdrFormat,
            currentDvProfile = mediaItem.dvProfile,
            errorMessage = null,
            isBuffering = true,
            stats = VideoStats(
                hdrFormatDetected = mediaItem.hdrFormat,
                dolbyVisionProfile = mediaItem.dvProfile,
                droppedFrames = 0
            )
        )

        val uri = if (mediaItem.localUriString != null) {
            Uri.parse(mediaItem.localUriString)
        } else {
            Uri.parse(mediaItem.uri)
        }

        val exoMediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(mediaItem.id.toString())
            .build()

        player.setMediaItem(exoMediaItem)
        player.prepare()

        if (mediaItem.lastPositionMs > 0 && mediaItem.lastPositionMs < (mediaItem.durationMs - 5000)) {
            player.seekTo(mediaItem.lastPositionMs)
        } else {
            player.seekTo(0)
        }

        player.playWhenReady = true

        viewModelScope.launch {
            repository.updatePlaybackPosition(mediaItem.id, mediaItem.lastPositionMs)
            repository.getBookmarks(mediaItem.id).collect { bookmarks ->
                _uiState.value = _uiState.value.copy(bookmarks = bookmarks)
            }
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
            scheduleControlsAutoHide()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        player.seekTo(positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L)))
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
    }

    fun seekRelative(deltaSeconds: Int) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition + (deltaSeconds * 1000L)).coerceIn(0L, player.duration.coerceAtLeast(0L))
        player.seekTo(newPos)
        _uiState.value = _uiState.value.copy(
            currentPositionMs = newPos,
            showSeekIndicator = true,
            seekDeltaSeconds = deltaSeconds
        )
        showTransientSeekIndicator()
    }

    fun setAspectRatioMode(mode: AspectRatioMode) {
        _uiState.value = _uiState.value.copy(aspectRatioMode = mode)
    }

    fun setPlaybackSpeed(speed: Float) {
        val player = exoPlayer ?: return
        player.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun toggleControlsVisibility() {
        if (_uiState.value.isLocked) return
        val newVisibility = !_uiState.value.isControlsVisible
        _uiState.value = _uiState.value.copy(isControlsVisible = newVisibility)
        if (newVisibility && (_uiState.value.isPlaying)) {
            scheduleControlsAutoHide()
        }
    }

    fun toggleLock() {
        val newLock = !_uiState.value.isLocked
        _uiState.value = _uiState.value.copy(
            isLocked = newLock,
            isControlsVisible = !newLock
        )
    }

    fun toggleStatsOverlay() {
        _uiState.value = _uiState.value.copy(isStatsVisible = !_uiState.value.isStatsVisible)
    }

    fun toggleHdrToneMapping() {
        _uiState.value = _uiState.value.copy(isHdrToneMapped = !_uiState.value.isHdrToneMapped)
    }

    fun adjustVolume(deltaPercent: Float) {
        val current = _uiState.value.volumePercent
        val updated = (current + deltaPercent).coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(
            volumePercent = updated,
            showVolumeIndicator = true
        )
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVol = (updated * maxVol).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
        showTransientGestureIndicator()
    }

    fun adjustBrightness(activity: Activity, deltaPercent: Float) {
        val current = _uiState.value.brightnessPercent
        val updated = (current + deltaPercent).coerceIn(0.05f, 1f)
        _uiState.value = _uiState.value.copy(
            brightnessPercent = updated,
            showBrightnessIndicator = true
        )
        val lp = activity.window.attributes
        lp.screenBrightness = updated
        activity.window.attributes = lp
        showTransientGestureIndicator()
    }

    private fun showTransientGestureIndicator() {
        gestureDismissJob?.cancel()
        gestureDismissJob = viewModelScope.launch {
            delay(1500)
            _uiState.value = _uiState.value.copy(
                showVolumeIndicator = false,
                showBrightnessIndicator = false
            )
        }
    }

    private fun showTransientSeekIndicator() {
        viewModelScope.launch {
            delay(900)
            _uiState.value = _uiState.value.copy(showSeekIndicator = false)
        }
    }

    fun scheduleControlsAutoHide() {
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(4000)
            if (_uiState.value.isPlaying && !_uiState.value.isLocked) {
                _uiState.value = _uiState.value.copy(isControlsVisible = false)
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition
                    val dur = player.duration.coerceAtLeast(0L)
                    val buf = player.bufferedPosition

                    // Update stats live
                    val bufSec = ((buf - pos) / 1000f).coerceAtLeast(0f)
                    val currentStats = _uiState.value.stats.copy(
                        bufferHealthSeconds = bufSec
                    )

                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        bufferedPositionMs = buf,
                        stats = currentStats
                    )

                    // save periodically
                    _uiState.value.currentMedia?.id?.let { mediaId ->
                        if (pos > 0) {
                            repository.updatePlaybackPosition(mediaId, pos)
                        }
                    }
                }
                delay(400)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    private fun extractTracksAndFormats(tracks: Tracks) {
        val audioTracks = mutableListOf<TrackInfo>()
        val subtitleTracks = mutableListOf<TrackInfo>()
        var activeHdr = _uiState.value.currentHdrFormat
        var activeDvProfile = _uiState.value.currentDvProfile
        var statsBuilder = _uiState.value.stats

        var selAudioName = "Default"
        var selSubName = "Off"

        for (groupIndex in 0 until tracks.groups.size) {
            val groupInfo = tracks.groups[groupIndex]
            val trackGroup = groupInfo.mediaTrackGroup
            val trackType = groupInfo.type

            for (trackIndex in 0 until trackGroup.length) {
                val format = trackGroup.getFormat(trackIndex)
                val isSelected = groupInfo.isTrackSelected(trackIndex)

                if (trackType == C.TRACK_TYPE_AUDIO) {
                    val lang = format.language?.uppercase() ?: "UND"
                    val label = format.label ?: "$lang ${format.sampleMimeType?.substringAfter("/") ?: "Audio"}"
                    val trackItem = TrackInfo(
                        id = "${groupIndex}_$trackIndex",
                        name = label,
                        language = format.language,
                        isSelected = isSelected,
                        trackGroupIndex = groupIndex,
                        trackIndex = trackIndex,
                        group = trackGroup
                    )
                    audioTracks.add(trackItem)
                    if (isSelected) {
                        selAudioName = label
                        statsBuilder = statsBuilder.copy(
                            audioCodec = format.sampleMimeType ?: "AAC",
                            audioChannels = "${format.channelCount} Channels",
                            audioSampleRate = format.sampleRate
                        )
                    }
                } else if (trackType == C.TRACK_TYPE_TEXT) {
                    val lang = format.language?.uppercase() ?: "SUB"
                    val label = format.label ?: "$lang Subtitles"
                    val trackItem = TrackInfo(
                        id = "${groupIndex}_$trackIndex",
                        name = label,
                        language = format.language,
                        isSelected = isSelected,
                        trackGroupIndex = groupIndex,
                        trackIndex = trackIndex,
                        group = trackGroup
                    )
                    subtitleTracks.add(trackItem)
                    if (isSelected) {
                        selSubName = label
                    }
                } else if (trackType == C.TRACK_TYPE_VIDEO && isSelected) {
                    val mime = format.sampleMimeType ?: format.containerMimeType ?: "video/hevc"
                    val colorInfo = format.colorInfo

                    var colorSpaceStr = "BT.709"
                    var colorTransferStr = "SDR"
                    var colorRangeStr = "Limited"
                    var bitDepthStr = "8-bit"

                    if (colorInfo != null) {
                        colorSpaceStr = when (colorInfo.colorSpace) {
                            C.COLOR_SPACE_BT2020 -> "BT.2020 (Wide Gamut)"
                            C.COLOR_SPACE_BT709 -> "BT.709 (sRGB)"
                            C.COLOR_SPACE_BT601 -> "BT.601"
                            else -> "BT.2020 / DCI-P3"
                        }
                        colorTransferStr = when (colorInfo.colorTransfer) {
                            C.COLOR_TRANSFER_ST2084 -> "SMPTE ST 2084 (PQ / HDR10)"
                            C.COLOR_TRANSFER_HLG -> "ARIB STD-B67 (HLG)"
                            C.COLOR_TRANSFER_SDR -> "SDR Gamma 2.4"
                            else -> "HDR Dynamic"
                        }
                        colorRangeStr = if (colorInfo.colorRange == C.COLOR_RANGE_FULL) "Full" else "Limited"
                        bitDepthStr = if (colorInfo.colorTransfer == C.COLOR_TRANSFER_ST2084 || colorInfo.colorTransfer == C.COLOR_TRANSFER_HLG) "10-bit HDR" else "8-bit SDR"
                    }

                    val isDv = mime.contains("dolby-vision", ignoreCase = true) ||
                            format.codecs?.contains("dvhe", ignoreCase = true) == true ||
                            format.codecs?.contains("dvh1", ignoreCase = true) == true ||
                            format.codecs?.contains("dav1", ignoreCase = true) == true ||
                            activeHdr == HdrFormat.DOLBY_VISION

                    if (isDv) {
                        activeHdr = HdrFormat.DOLBY_VISION
                        bitDepthStr = "12-bit / 10-bit Dolby Vision"
                        if (activeDvProfile == null) {
                            activeDvProfile = "Profile 5 (dvhe.05)"
                        }
                    } else if (colorInfo?.colorTransfer == C.COLOR_TRANSFER_ST2084 || activeHdr == HdrFormat.HDR10) {
                        activeHdr = HdrFormat.HDR10
                    } else if (colorInfo?.colorTransfer == C.COLOR_TRANSFER_HLG || activeHdr == HdrFormat.HLG) {
                        activeHdr = HdrFormat.HLG
                    }

                    val calculatedBitrate = if (format.bitrate > 0) (format.bitrate / 1000).toLong() else 12500L

                    statsBuilder = statsBuilder.copy(
                        codec = "${format.sampleMimeType ?: "video/hevc"} (${format.codecs ?: "HEVC Main10"})",
                        resolution = "${format.width} x ${format.height}",
                        frameRate = if (format.frameRate > 0) format.frameRate else 60f,
                        bitrateKbps = calculatedBitrate,
                        colorSpace = colorSpaceStr,
                        colorTransfer = colorTransferStr,
                        colorRange = colorRangeStr,
                        bitDepth = bitDepthStr,
                        hdrFormatDetected = activeHdr,
                        dolbyVisionProfile = activeDvProfile
                    )
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            availableAudioTracks = audioTracks,
            availableSubtitleTracks = subtitleTracks,
            selectedAudioTrackName = selAudioName,
            selectedSubtitleTrackName = selSubName,
            currentHdrFormat = activeHdr,
            currentDvProfile = activeDvProfile,
            stats = statsBuilder
        )
    }

    fun selectAudioTrack(trackInfo: TrackInfo) {
        val player = exoPlayer ?: return
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(
                TrackSelectionOverride(trackInfo.group, trackInfo.trackIndex)
            )
            .build()
        _uiState.value = _uiState.value.copy(selectedAudioTrackName = trackInfo.name)
    }

    fun selectSubtitleTrack(trackInfo: TrackInfo?) {
        val player = exoPlayer ?: return
        if (trackInfo == null) {
            // Turn subtitles off
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
            _uiState.value = _uiState.value.copy(selectedSubtitleTrackName = "Off")
        } else {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .setOverrideForType(
                    TrackSelectionOverride(trackInfo.group, trackInfo.trackIndex)
                )
                .build()
            _uiState.value = _uiState.value.copy(selectedSubtitleTrackName = trackInfo.name)
        }
    }

    fun addBookmark(title: String, note: String = "") {
        val media = _uiState.value.currentMedia ?: return
        val pos = _uiState.value.currentPositionMs
        viewModelScope.launch {
            repository.addBookmark(media.id, pos, title, note)
        }
    }

    fun toggleFavorite(item: MediaItemEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, item.isFavorite)
        }
    }

    fun addCustomStream(title: String, url: String, hdrFormat: HdrFormat, dvProfile: String? = null) {
        viewModelScope.launch {
            val item = MediaItemEntity(
                title = title.ifBlank { "Custom Stream" },
                description = "User added stream: $url",
                uri = url,
                category = "Custom Streams",
                hdrFormat = hdrFormat,
                dvProfile = dvProfile,
                resolutionLabel = if (hdrFormat == HdrFormat.DOLBY_VISION) "Dolby Vision" else "4K HDR",
                audioLabel = "Direct Stream Audio"
            )
            val newId = repository.addCustomMedia(item)
            val inserted = repository.getMediaById(newId)
            if (inserted != null) {
                playMedia(inserted)
            }
        }
    }

    fun addLocalVideo(uriString: String, fileName: String) {
        viewModelScope.launch {
            val item = MediaItemEntity(
                title = fileName,
                description = "Local storage video file",
                uri = uriString,
                localUriString = uriString,
                category = "Local Storage",
                hdrFormat = HdrFormat.AUTO,
                resolutionLabel = "Local Media",
                audioLabel = "Device Hardware Decoder"
            )
            val newId = repository.addCustomMedia(item)
            val inserted = repository.getMediaById(newId)
            if (inserted != null) {
                playMedia(inserted)
            }
        }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null) {
            _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = null)
            return
        }

        _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = minutes)
        sleepTimerJob = viewModelScope.launch {
            var rem = minutes
            while (rem > 0 && isActive) {
                delay(60000)
                rem--
                _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = rem)
            }
            // pause player when timer fires
            exoPlayer?.pause()
            _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = null, isPlaying = false)
        }
    }

    private fun onMediaEnded() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }

    fun releasePlayer() {
        stopProgressTracker()
        controlsHideJob?.cancel()
        gestureDismissJob?.cancel()
        sleepTimerJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}
