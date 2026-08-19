package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HdrFormat

@Composable
fun HdrBadge(
    hdrFormat: HdrFormat,
    modifier: Modifier = Modifier,
    dvProfile: String? = null,
    isGlow: Boolean = false
) {
    val (bgColor, borderColor, textGradient, titleText) = when (hdrFormat) {
        HdrFormat.DOLBY_VISION -> Quadruple(
            Color(0xFF1E1605),
            Color(0xFFE5A93B),
            listOf(Color(0xFFFFDF79), Color(0xFFE5A93B), Color(0xFFCA8A04)),
            "DOLBY VISION"
        )
        HdrFormat.HDR10_PLUS -> Quadruple(
            Color(0xFF190F2E),
            Color(0xFFA855F7),
            listOf(Color(0xFFD8B4FE), Color(0xFFA855F7)),
            "HDR10+"
        )
        HdrFormat.HDR10 -> Quadruple(
            Color(0xFF06202A),
            Color(0xFF06B6D4),
            listOf(Color(0xFF67E8F9), Color(0xFF06B6D4)),
            "HDR10"
        )
        HdrFormat.HLG -> Quadruple(
            Color(0xFF062319),
            Color(0xFF10B981),
            listOf(Color(0xFF6EE7B7), Color(0xFF10B981)),
            "HLG"
        )
        HdrFormat.SDR -> Quadruple(
            Color(0xFF1E293B),
            Color(0xFF64748B),
            listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)),
            "SDR"
        )
        HdrFormat.AUTO -> Quadruple(
            Color(0xFF1A1A3A),
            Color(0xFF6366F1),
            listOf(Color(0xFFA5B4FC), Color(0xFF6366F1)),
            "AUTO HDR"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor.copy(alpha = 0.85f))
            .border(1.dp, borderColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (hdrFormat == HdrFormat.DOLBY_VISION) {
                // Iconic D-D symbol preview
                Text(
                    text = "D",
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    color = Color(0xFFE5A93B),
                    fontFamily = FontFamily.SansSerif
                )
            }
            Text(
                text = titleText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp,
                color = borderColor
            )
            if (dvProfile != null && hdrFormat == HdrFormat.DOLBY_VISION) {
                Text(
                    text = dvProfile.substringBefore("(").trim(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 8.sp,
                    color = Color(0xFFFFDF79)
                )
            }
        }
    }
}

@Composable
fun ResolutionBadge(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(1.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp,
            color = Color(0xFFE2E8F0)
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
