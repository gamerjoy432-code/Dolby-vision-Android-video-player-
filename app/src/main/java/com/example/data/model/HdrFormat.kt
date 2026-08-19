package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class HdrFormat(
    val displayName: String,
    val shortBadge: String,
    val badgeColor: Color,
    val description: String
) {
    DOLBY_VISION(
        displayName = "Dolby Vision",
        shortBadge = "VISION",
        badgeColor = Color(0xFFE5A93B), // Dolby Vision Gold
        description = "Dynamic metadata, 12-bit tone mapping & wide color gamut"
    ),
    HDR10_PLUS(
        displayName = "HDR10+",
        shortBadge = "HDR10+",
        badgeColor = Color(0xFF8B5CF6), // Violet
        description = "Dynamic scene-by-scene tone mapping (SMPTE ST 2094)"
    ),
    HDR10(
        displayName = "HDR10",
        shortBadge = "HDR10",
        badgeColor = Color(0xFF06B6D4), // Cyan
        description = "10-bit SMPTE ST 2084 (PQ) color curve with BT.2020 gamut"
    ),
    HLG(
        displayName = "HLG (Hybrid Log-Gamma)",
        shortBadge = "HLG",
        badgeColor = Color(0xFF10B981), // Emerald
        description = "Broadcast standard HDR (ARIB STD-B67 / BT.2100)"
    ),
    SDR(
        displayName = "SDR (Standard Dynamic Range)",
        shortBadge = "SDR",
        badgeColor = Color(0xFF94A3B8), // Slate
        description = "Standard Rec.709 8-bit dynamic range"
    ),
    AUTO(
        displayName = "Auto Detect",
        shortBadge = "AUTO",
        badgeColor = Color(0xFF6366F1),
        description = "ExoPlayer hardware codec auto-detection"
    );

    companion object {
        fun fromColorTransfer(colorTransfer: Int, mimeType: String? = null): HdrFormat {
            if (mimeType?.contains("dolby-vision", ignoreCase = true) == true ||
                mimeType?.startsWith("dvhe", ignoreCase = true) == true ||
                mimeType?.startsWith("dvh1", ignoreCase = true) == true ||
                mimeType?.startsWith("dav1", ignoreCase = true) == true
            ) {
                return DOLBY_VISION
            }

            return when (colorTransfer) {
                6 -> HDR10 // C.COLOR_TRANSFER_ST2084
                7 -> HLG   // C.COLOR_TRANSFER_HLG
                1, 2, 3 -> SDR // C.COLOR_TRANSFER_SDR
                else -> SDR
            }
        }
    }
}

enum class DolbyVisionProfile(val profileName: String, val description: String) {
    PROFILE_5("Profile 5 (dvhe.05)", "Single Layer HEVC with Dolby proprietary IPTPQc2 color space"),
    PROFILE_8_1("Profile 8.1 (dvhe.08)", "Cross-compatible HDR10/HEVC base with dynamic RPU metadata"),
    PROFILE_8_4("Profile 8.4 (dvhe.08)", "HLG base layer with Dolby Vision dynamic metadata (Apple/iPhone standard)"),
    PROFILE_7("Profile 7 (dvhe.07)", "Dual-layer Ultra HD Blu-ray standard with FEL/MEL"),
    PROFILE_4("Profile 4 (dvhe.04)", "Single Layer AVC with enhanced dynamic range"),
    NONE("None / Non-DV", "Standard HDR or SDR stream")
}
