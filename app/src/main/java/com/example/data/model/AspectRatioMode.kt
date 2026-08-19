package com.example.data.model

enum class AspectRatioMode(val label: String, val description: String) {
    FIT("Fit to Screen", "Maintains original aspect ratio inside container"),
    FILL_CROP("Crop & Fill", "Fills entire screen, cropping edges as needed"),
    FIXED_16_9("16:9 Widescreen", "Forces standard 16:9 aspect ratio"),
    FIXED_21_9("21:9 CinemaScope", "Forces cinematic 21:9 ultrawide aspect ratio"),
    STRETCH("Stretch", "Stretches to fill entire display bounds"),
    ORIGINAL("Original 1:1", "Pixel-to-pixel exact source dimensions")
}

data class VideoStats(
    val codec: String = "Detecting...",
    val decoderName: String = "Hardware Decoder",
    val resolution: String = "0x0",
    val frameRate: Float = 0f,
    val bitrateKbps: Long = 0,
    val colorSpace: String = "BT.709",
    val colorTransfer: String = "SDR",
    val colorRange: String = "Limited",
    val bitDepth: String = "8-bit",
    val hdrFormatDetected: HdrFormat = HdrFormat.SDR,
    val dolbyVisionProfile: String? = null,
    val audioCodec: String = "AAC",
    val audioChannels: String = "Stereo 2.0",
    val audioSampleRate: Int = 48000,
    val droppedFrames: Int = 0,
    val bufferHealthSeconds: Float = 0f,
    val isHardwareAccelerated: Boolean = true
)
