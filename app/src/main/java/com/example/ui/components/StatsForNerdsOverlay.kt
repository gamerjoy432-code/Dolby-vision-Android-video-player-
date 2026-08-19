package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HdrFormat
import com.example.data.model.VideoStats

/**
 * Real-time HDR / Dolby Vision Playback Diagnostics Overlay.
 * Displays live Media3 ExoPlayer stream metrics: codec, color space, transfer, bitrate,
 * hardware decoder name, and dropped frames.
 */
@Composable
fun StatsForNerdsOverlay(
    stats: VideoStats,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hdrThemeColor = when (stats.hdrFormatDetected) {
        HdrFormat.DOLBY_VISION -> Color(0xFFFFDF79)
        HdrFormat.HDR10_PLUS -> Color(0xFFC084FC)
        HdrFormat.HDR10 -> Color(0xFF00E5FF)
        HdrFormat.HLG -> Color(0xFF34D399)
        HdrFormat.AUTO -> Color(0xFF818CF8)
        HdrFormat.SDR -> Color(0xFF94A3B8)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xF2090D16))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        hdrThemeColor.copy(alpha = 0.6f),
                        Color(0xFF1E293B)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
            .testTag("stats_for_nerds_overlay")
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Bar with Real-time Status Badge and Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (stats.isHardwareAccelerated) Color(0xFF10B981) else Color(0xFFF59E0B))
                    )
                    Text(
                        text = "HDR STREAM INSPECTOR",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0x33334155))
                        .testTag("close_stats_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Stats Overlay",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // HDR Pipeline Status Hero Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x660F172A))
                    .border(1.dp, hdrThemeColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ACTIVE DYNAMIC RANGE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (stats.dolbyVisionProfile != null) "Dolby Vision (${stats.dolbyVisionProfile})" else stats.hdrFormatDetected.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = hdrThemeColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(hdrThemeColor.copy(alpha = 0.15f))
                            .border(1.dp, hdrThemeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stats.bitDepth,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = hdrThemeColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Group 1: Color Space & HDR Optics
            SectionHeader(icon = Icons.Default.Videocam, title = "COLOR PIPELINE & OPTICS")
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                StatRow("Color Space", stats.colorSpace, Color(0xFF00E5FF))
                StatRow("Color Transfer (EOTF)", stats.colorTransfer, Color(0xFF38BDF8))
                StatRow("Color Range", stats.colorRange, Color.White)
                StatRow("Bit Depth", stats.bitDepth, Color(0xFF34D399))
                if (stats.dolbyVisionProfile != null) {
                    StatRow("Dolby Vision RPU", stats.dolbyVisionProfile, Color(0xFFFFDF79))
                }
            }

            HorizontalDivider(color = Color(0x33334155), thickness = 1.dp)

            // Group 2: Video Codec & Hardware Decoder
            SectionHeader(icon = Icons.Default.Memory, title = "CODEC & DECODER ENGINE")
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                StatRow("Video Codec", stats.codec, Color.White)
                StatRow("Decoder Name", stats.decoderName, Color(0xFFA78BFA))
                StatRow(
                    "Acceleration",
                    if (stats.isHardwareAccelerated) "Hardware (Direct Surface)" else "Software Emulation",
                    if (stats.isHardwareAccelerated) Color(0xFF10B981) else Color(0xFFF59E0B)
                )
                StatRow("Resolution", stats.resolution, Color(0xFFF43F5E))
                StatRow("Framerate", "${"%.2f".format(stats.frameRate)} fps", Color.White)
            }

            HorizontalDivider(color = Color(0x33334155), thickness = 1.dp)

            // Group 3: Live Stream Telemetry & Dropped Frames
            SectionHeader(icon = Icons.Default.Speed, title = "STREAM TELEMETRY & INTEGRITY")
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                StatRow("Live Bitrate", "${stats.bitrateKbps} kbps", Color(0xFF38BDF8))
                StatRow("Buffer Health", "${"%.1f".format(stats.bufferHealthSeconds)} s", Color(0xFF4ADE80))

                val hasDrops = stats.droppedFrames > 0
                StatRow(
                    label = "Dropped Frames",
                    value = "${stats.droppedFrames} frames",
                    valueColor = if (hasDrops) Color(0xFFF87171) else Color(0xFF4ADE80)
                )
                StatRow(
                    label = "Playback Health",
                    value = if (!hasDrops) "Optimal 60fps (0 Drops)" else "Frames Dropped (${stats.droppedFrames})",
                    valueColor = if (!hasDrops) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                )
            }

            HorizontalDivider(color = Color(0x33334155), thickness = 1.dp)

            // Group 4: Audio Stream Details
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                StatRow("Audio Codec", stats.audioCodec, Color(0xFFE2E8F0))
                StatRow("Audio Layout", "${stats.audioChannels} @ ${stats.audioSampleRate} Hz", Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = valueColor
        )
    }
}
