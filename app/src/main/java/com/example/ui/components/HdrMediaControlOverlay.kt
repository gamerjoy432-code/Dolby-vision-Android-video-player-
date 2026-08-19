package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioMode
import com.example.data.model.HdrFormat
import com.example.data.model.MediaItemEntity
import kotlin.math.roundToInt

@Composable
fun HdrMediaControlOverlay(
    isVisible: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long,
    mediaItem: MediaItemEntity?,
    hdrFormat: HdrFormat,
    dvProfile: String?,
    resolutionLabel: String,
    aspectRatioMode: AspectRatioMode,
    playbackSpeed: Float,
    sleepTimerRemaining: Int?,
    isStatsVisible: Boolean,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekRelative: (Int) -> Unit,
    onToggleStats: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onToggleRotate: () -> Unit,
    onOpenTracks: () -> Unit,
    onAddBookmark: () -> Unit,
    onLockScreen: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetSleepTimer: (Int?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    val accentGradient = when (hdrFormat) {
        HdrFormat.DOLBY_VISION -> listOf(Color(0xFFFFDF79), Color(0xFFE5A93B), Color(0xFFB45309))
        HdrFormat.HDR10_PLUS -> listOf(Color(0xFFE9D5FF), Color(0xFFA855F7), Color(0xFF7E22CE))
        HdrFormat.HDR10 -> listOf(Color(0xFF67E8F9), Color(0xFF00E5FF), Color(0xFF0284C7))
        HdrFormat.HLG -> listOf(Color(0xFF6EE7B7), Color(0xFF10B981), Color(0xFF047857))
        HdrFormat.AUTO -> listOf(Color(0xFFA5B4FC), Color(0xFF6366F1), Color(0xFF4338CA))
        HdrFormat.SDR -> listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF475569))
    }

    val primaryAccent = accentGradient.getOrElse(1) { Color(0xFF00E5FF) }

    val infiniteTransition = rememberInfiniteTransition(label = "halo_transition")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(300)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("hdr_media_control_overlay")
        ) {
            // Cinema OLED Vignette Scrims (Preserves HDR highlights while making HUD text readable)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF0020617),
                                Color(0x99020617),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x99020617),
                                Color(0xF5020617)
                            )
                        )
                    )
            )

            // Top Bar: Back, Title, HDR Glow Badge, Track / Stats / Aspect / Rotation / Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x660F172A))
                            .testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = mediaItem?.title ?: "HDR Video Playback",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            HdrBadge(
                                hdrFormat = hdrFormat,
                                dvProfile = dvProfile,
                                isGlow = true
                            )
                            if (resolutionLabel.isNotBlank()) {
                                ResolutionBadge(label = resolutionLabel)
                            }
                        }
                    }
                }

                // Action Cluster
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Audio & Subtitles
                    IconButton(
                        onClick = onOpenTracks,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x440F172A))
                            .testTag("track_selector_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = "Audio and Subtitles",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Stats for Nerds HUD
                    IconButton(
                        onClick = onToggleStats,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isStatsVisible) Color(0x880284C7) else Color(0x440F172A))
                            .border(1.dp, if (isStatsVisible) Color(0xFF00E5FF) else Color.Transparent, CircleShape)
                            .testTag("toggle_stats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = "HDR Stats HUD",
                            tint = if (isStatsVisible) Color(0xFF00E5FF) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Aspect Ratio mode
                    IconButton(
                        onClick = onCycleAspectRatio,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x440F172A))
                            .testTag("aspect_ratio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = "Aspect: ${aspectRatioMode.label}",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Screen Rotation
                    IconButton(
                        onClick = onToggleRotate,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x440F172A))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Screen Rotation",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings dropdown (Playback speed, Sleep timer, Bookmarks)
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x440F172A))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Settings Menu",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Speed: ${playbackSpeed}x", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF38BDF8)) },
                                onClick = {
                                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                                    val nextIdx = (speeds.indexOf(playbackSpeed) + 1) % speeds.size
                                    onSetSpeed(speeds[nextIdx])
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (sleepTimerRemaining != null) "Sleep Timer: ${sleepTimerRemaining}m" else "Sleep Timer: Off",
                                        color = Color.White
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFE5A93B)) },
                                onClick = {
                                    val nextTimer = when (sleepTimerRemaining) {
                                        null -> 15
                                        15 -> 30
                                        30 -> 60
                                        else -> null
                                    }
                                    onSetSleepTimer(nextTimer)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Bookmark", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = Color(0xFF34D399)) },
                                onClick = {
                                    showMenu = false
                                    onAddBookmark()
                                }
                            )
                        }
                    }
                }
            }

            // Center Control Cluster: Rewind 10s, Cinematic Pulsing Play/Pause, Forward 10s
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seek -10s
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x800B1120))
                        .border(1.dp, Color(0x4038BDF8), CircleShape)
                        .clickable { onSeekRelative(-10) }
                        .testTag("seek_backward_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10 Seconds",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Play / Pause / Buffering Halo Ring
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    primaryAccent.copy(alpha = 0.35f),
                                    Color(0xCC0F172A)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(accentGradient),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(42.dp),
                            color = primaryAccent,
                            strokeWidth = 3.5.dp
                        )
                    } else {
                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                // Seek +10s
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x800B1120))
                        .border(1.dp, Color(0x4038BDF8), CircleShape)
                        .clickable { onSeekRelative(10) }
                        .testTag("seek_forward_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10 Seconds",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Bottom Bar: High Dynamic Range Seek Bar, Timestamps, Bookmarks, and Lock
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val effectivePosition = if (isScrubbing) (scrubProgress * durationMs).toLong() else currentPositionMs
                val curSec = (effectivePosition / 1000).coerceAtLeast(0)
                val durSec = (durationMs / 1000).coerceAtLeast(0)

                val timeStr = "%02d:%02d".format(curSec / 60, curSec % 60)
                val totalTimeStr = "%02d:%02d".format(durSec / 60, durSec % 60)

                // Timestamp Header with Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = timeStr,
                            color = primaryAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "/",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                        Text(
                            text = totalTimeStr,
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                        if (isScrubbing) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(primaryAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SCRUBBING",
                                    color = primaryAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Bookmark
                        IconButton(
                            onClick = onAddBookmark,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x440F172A))
                                .testTag("quick_bookmark_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Add Bookmark",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Lock Screen
                        IconButton(
                            onClick = onLockScreen,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x440F172A))
                                .testTag("lock_screen_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Controls",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Custom HDR Glowing Seek Bar
                val currentProgress = if (durationMs > 0) {
                    (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                val bufferedProgress = if (durationMs > 0) {
                    (bufferedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                HdrGlowSeekBar(
                    progress = if (isScrubbing) scrubProgress else currentProgress,
                    bufferProgress = bufferedProgress,
                    accentColor = primaryAccent,
                    accentGradient = accentGradient,
                    onScrubStart = {
                        isScrubbing = true
                        scrubProgress = it
                    },
                    onScrubChange = {
                        scrubProgress = it
                    },
                    onScrubEnd = {
                        isScrubbing = false
                        val targetMs = (it * durationMs).toLong()
                        onSeekTo(targetMs)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("player_seek_slider")
                )
            }
        }
    }
}

/**
 * High Dynamic Range Multi-Layer Glowing Seek Bar
 */
@Composable
private fun HdrGlowSeekBar(
    progress: Float,
    bufferProgress: Float,
    accentColor: Color,
    accentGradient: List<Color>,
    onScrubStart: (Float) -> Unit,
    onScrubChange: (Float) -> Unit,
    onScrubEnd: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val newProg = (offset.x / size.width).coerceIn(0f, 1f)
                        onScrubStart(newProg)
                        onScrubEnd(newProg)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val newProg = (offset.x / size.width).coerceIn(0f, 1f)
                        onScrubStart(newProg)
                    },
                    onDrag = { change, _ ->
                        val newProg = (change.position.x / size.width).coerceIn(0f, 1f)
                        onScrubChange(newProg)
                    },
                    onDragEnd = {
                        isDragging = false
                        onScrubEnd(progress)
                    },
                    onDragCancel = {
                        isDragging = false
                        onScrubEnd(progress)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        val trackHeight = if (isDragging) 8.dp else 5.dp
        val thumbRadius = if (isDragging) 10.dp else 7.dp

        // Background Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(Color(0x55475569))
        )

        // Buffered Progress Track
        Box(
            modifier = Modifier
                .fillMaxWidth(bufferProgress)
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(Color(0x8894A3B8))
        )

        // Active Played Progress Track with HDR Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(Brush.horizontalGradient(accentGradient))
        )

        // Floating Glowing Thumb
        Box(
            modifier = Modifier
                .offset {
                    val totalWidthPx = maxWidth.toPx()
                    val thumbDiameterPx = (thumbRadius * 2).toPx()
                    val pxPos = (progress * (totalWidthPx - thumbDiameterPx)).roundToInt().coerceAtLeast(0)
                    IntOffset(pxPos, 0)
                }
                .size(thumbRadius * 2)
                .shadow(
                    elevation = if (isDragging) 12.dp else 6.dp,
                    shape = CircleShape,
                    ambientColor = accentColor,
                    spotColor = accentColor
                )
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, accentColor, CircleShape)
        )
    }
}
