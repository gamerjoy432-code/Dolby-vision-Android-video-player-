package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Gesture overlay containing the right-side vertical Brightness slider,
 * left-side vertical Volume slider, and center seek indicators.
 */
@Composable
fun VideoGestureOverlay(
    showVolume: Boolean,
    volumePercent: Float,
    showBrightness: Boolean,
    brightnessPercent: Float,
    showSeek: Boolean,
    seekDeltaSeconds: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("video_gesture_overlay")
    ) {
        // Left-side Vertical Volume Slider Capsule
        AnimatedVisibility(
            visible = showVolume,
            enter = fadeIn(tween(150)) + slideInHorizontally(tween(200)) { -it },
            exit = fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .testTag("volume_gesture_slider_container")
        ) {
            VerticalVolumeSliderControl(
                volumePercent = volumePercent.coerceIn(0f, 1f)
            )
        }

        // Right-side Vertical Brightness Slider Capsule (Appears on vertical swipe on the right side)
        AnimatedVisibility(
            visible = showBrightness,
            enter = fadeIn(tween(150)) + slideInHorizontally(tween(200)) { it },
            exit = fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .testTag("brightness_gesture_slider_container")
        ) {
            VerticalBrightnessSliderControl(
                brightnessPercent = brightnessPercent.coerceIn(0.01f, 1f)
            )
        }

        // Center Seek Delta HUD
        AnimatedVisibility(
            visible = showSeek,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xEE0B132B))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(20.dp))
                    .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF00E5FF), spotColor = Color(0xFF00E5FF))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (seekDeltaSeconds >= 0) Icons.Default.FastForward else Icons.Default.FastRewind,
                        contentDescription = "Seek Indicator",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = if (seekDeltaSeconds >= 0) "+${seekDeltaSeconds}s" else "${seekDeltaSeconds}s",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Vertical Brightness Slider capsule with HDR glowing gradient track and dynamic sun icon.
 */
@Composable
fun VerticalBrightnessSliderControl(
    brightnessPercent: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = brightnessPercent,
        animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
        label = "brightness_progress"
    )

    val brightnessIcon = when {
        brightnessPercent < 0.35f -> Icons.Default.BrightnessLow
        brightnessPercent < 0.70f -> Icons.Default.BrightnessMedium
        else -> Icons.Default.BrightnessHigh
    }

    val isPeakHdr = brightnessPercent >= 0.95f
    val accentGold = if (isPeakHdr) Color(0xFFFFDF79) else Color(0xFFFBBF24)
    val accentGlow = if (isPeakHdr) Color(0xFFFFB703) else Color(0xFFD97706)

    Box(
        modifier = modifier
            .width(48.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xE60F172A))
            .border(
                width = if (isPeakHdr) 1.5.dp else 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        accentGold.copy(alpha = 0.8f),
                        Color(0xFF334155)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag("brightness_slider_capsule"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Sun icon at the top
            Icon(
                imageVector = brightnessIcon,
                contentDescription = "Brightness level",
                tint = accentGold,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("brightness_icon")
            )

            // Vertical Track Container
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .width(10.dp)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Filled portion of the vertical track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedProgress)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accentGold,
                                    accentGlow,
                                    Color(0xFFB45309)
                                )
                            )
                        )
                )
            }

            // Percentage readout or HDR PEAK text
            val percentInt = (brightnessPercent * 100).toInt().coerceIn(1, 100)
            Text(
                text = if (isPeakHdr) "MAX" else "$percentInt%",
                color = if (isPeakHdr) Color(0xFFFFDF79) else Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isPeakHdr) 9.sp else 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}

/**
 * Vertical Volume Slider capsule with Cyan/Blue glow gradient track.
 */
@Composable
fun VerticalVolumeSliderControl(
    volumePercent: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = volumePercent,
        animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
        label = "volume_progress"
    )

    val volIcon = when {
        volumePercent <= 0.01f -> Icons.Default.VolumeMute
        volumePercent < 0.5f -> Icons.Default.VolumeDown
        else -> Icons.Default.VolumeUp
    }

    val primaryCyan = Color(0xFF38BDF8)
    val deepCyan = Color(0xFF0284C7)

    Box(
        modifier = modifier
            .width(48.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xE60F172A))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        primaryCyan.copy(alpha = 0.8f),
                        Color(0xFF334155)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag("volume_slider_capsule"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Volume icon at top
            Icon(
                imageVector = volIcon,
                contentDescription = "Volume level",
                tint = primaryCyan,
                modifier = Modifier.size(24.dp)
            )

            // Vertical Track Container
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .width(10.dp)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Filled portion of track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedProgress)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    primaryCyan,
                                    deepCyan,
                                    Color(0xFF0369A1)
                                )
                            )
                        )
                )
            }

            // Volume percentage readout
            val percentInt = (volumePercent * 100).toInt().coerceIn(0, 100)
            Text(
                text = "$percentInt%",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}
