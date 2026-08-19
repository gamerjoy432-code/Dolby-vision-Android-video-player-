package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.AspectRatioMode
import com.example.player.PlayerViewModel
import com.example.ui.components.BookmarkDialog
import com.example.ui.components.HdrBadge
import com.example.ui.components.HdrMediaControlOverlay
import com.example.ui.components.StatsForNerdsOverlay
import com.example.ui.components.TrackSelectorSheet
import com.example.ui.components.VideoGestureOverlay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()

    var showTrackSheet by remember { mutableStateOf(false) }
    var showBookmarkDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }

    BackHandler {
        if (uiState.isLocked) {
            viewModel.toggleLock()
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            onBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("player_screen")
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Media3 ExoPlayer View (Configured for SurfaceView / 10-bit HDR / Dolby Vision)
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    // surface_type = surface_view is default in modern Media3 PlayerView, enabling hardware overlay HDR
                    player = viewModel.getOrCreatePlayer()
                }
            },
            update = { playerView ->
                playerView.player = viewModel.getOrCreatePlayer()
                playerView.resizeMode = when (uiState.aspectRatioMode) {
                    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatioMode.FILL_CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    AspectRatioMode.FIXED_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    AspectRatioMode.FIXED_21_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                    AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture Detection Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.isLocked) {
                    if (uiState.isLocked) {
                        detectTapGestures(
                            onTap = { viewModel.toggleControlsVisibility() }
                        )
                    } else {
                        detectTapGestures(
                            onTap = { viewModel.toggleControlsVisibility() },
                            onDoubleTap = { offset ->
                                val isLeftHalf = offset.x < size.width / 2
                                if (isLeftHalf) {
                                    viewModel.seekRelative(-10)
                                } else {
                                    viewModel.seekRelative(10)
                                }
                            }
                        )
                    }
                }
                .pointerInput(uiState.isLocked) {
                    if (!uiState.isLocked) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                val isRightSide = change.position.x >= size.width / 2
                                val delta = -dragAmount / 600f
                                if (isRightSide) {
                                    activity?.let { viewModel.adjustBrightness(it, delta) }
                                } else {
                                    viewModel.adjustVolume(delta)
                                }
                            }
                        )
                    }
                }
        )

        // Gesture feedback HUD
        VideoGestureOverlay(
            showVolume = uiState.showVolumeIndicator,
            volumePercent = uiState.volumePercent,
            showBrightness = uiState.showBrightnessIndicator,
            brightnessPercent = uiState.brightnessPercent,
            showSeek = uiState.showSeekIndicator,
            seekDeltaSeconds = uiState.seekDeltaSeconds,
            modifier = Modifier.align(Alignment.Center)
        )

        // Error message banner if any
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xE67F1D1D))
                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = uiState.errorMessage!!,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Locked Screen Unlock Action
        if (uiState.isLocked) {
            AnimatedVisibility(
                visible = uiState.isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopStart).padding(24.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleLock() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC0F172A))
                        .border(1.5.dp, Color(0xFF38BDF8), CircleShape)
                        .testTag("unlock_screen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Unlock Controls",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Main HDR Media Controls Overlay
        HdrMediaControlOverlay(
            isVisible = uiState.isControlsVisible && !uiState.isLocked,
            isPlaying = uiState.isPlaying,
            isBuffering = uiState.isBuffering,
            currentPositionMs = uiState.currentPositionMs,
            durationMs = uiState.durationMs,
            bufferedPositionMs = ((uiState.stats.bufferHealthSeconds * 1000).toLong() + uiState.currentPositionMs).coerceAtMost(uiState.durationMs),
            mediaItem = uiState.currentMedia,
            hdrFormat = uiState.currentHdrFormat,
            dvProfile = uiState.currentDvProfile,
            resolutionLabel = uiState.stats.resolution,
            aspectRatioMode = uiState.aspectRatioMode,
            playbackSpeed = uiState.playbackSpeed,
            sleepTimerRemaining = uiState.sleepTimerMinutesRemaining,
            isStatsVisible = uiState.isStatsVisible,
            onPlayPause = { viewModel.togglePlayPause() },
            onSeekTo = { viewModel.seekTo(it) },
            onSeekRelative = { viewModel.seekRelative(it) },
            onToggleStats = { viewModel.toggleStatsOverlay() },
            onCycleAspectRatio = {
                val nextMode = when (uiState.aspectRatioMode) {
                    AspectRatioMode.FIT -> AspectRatioMode.FILL_CROP
                    AspectRatioMode.FILL_CROP -> AspectRatioMode.FIXED_16_9
                    AspectRatioMode.FIXED_16_9 -> AspectRatioMode.FIXED_21_9
                    AspectRatioMode.FIXED_21_9 -> AspectRatioMode.STRETCH
                    AspectRatioMode.STRETCH -> AspectRatioMode.FIT
                    AspectRatioMode.ORIGINAL -> AspectRatioMode.FIT
                }
                viewModel.setAspectRatioMode(nextMode)
            },
            onToggleRotate = {
                isLandscape = !isLandscape
                activity?.requestedOrientation = if (isLandscape) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            },
            onOpenTracks = { showTrackSheet = true },
            onAddBookmark = { showBookmarkDialog = true },
            onLockScreen = { viewModel.toggleLock() },
            onSetSpeed = { viewModel.setPlaybackSpeed(it) },
            onSetSleepTimer = { viewModel.setSleepTimer(it) },
            onBack = {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                onBack()
            }
        )

        // Stats for Nerds overlay
        if (uiState.isStatsVisible) {
            StatsForNerdsOverlay(
                stats = uiState.stats,
                onClose = { viewModel.toggleStatsOverlay() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp, start = 16.dp)
                    .widthIn(max = 360.dp)
            )
        }

        // Track Selector Bottom Sheet
        if (showTrackSheet) {
            TrackSelectorSheet(
                audioTracks = uiState.availableAudioTracks,
                subtitleTracks = uiState.availableSubtitleTracks,
                onSelectAudio = { viewModel.selectAudioTrack(it) },
                onSelectSubtitle = { viewModel.selectSubtitleTrack(it) },
                onDismiss = { showTrackSheet = false }
            )
        }

        // Bookmark Dialog
        if (showBookmarkDialog) {
            BookmarkDialog(
                currentPositionMs = uiState.currentPositionMs,
                onDismiss = { showBookmarkDialog = false },
                onSaveBookmark = { title, note ->
                    viewModel.addBookmark(title, note)
                }
            )
        }
    }
}
