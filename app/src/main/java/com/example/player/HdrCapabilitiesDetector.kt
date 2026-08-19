package com.example.player

import android.content.Context
import android.hardware.display.DisplayManager
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import android.view.Display
import com.example.data.model.HdrFormat

data class DisplayHdrReport(
    val isDisplayHdrCapable: Boolean,
    val isWideColorGamutSupported: Boolean,
    val supportedHdrTypes: List<HdrFormat>,
    val maxLuminanceNits: Float,
    val minLuminanceNits: Float,
    val maxAverageLuminanceNits: Float,
    val dolbyVisionHardwareDecoders: List<String>,
    val hevcHdrDecoders: List<String>,
    val av1HdrDecoders: List<String>,
    val displayResolution: String,
    val refreshRateHz: Float
)

object HdrCapabilitiesDetector {

    fun getDeviceHdrReport(context: Context): DisplayHdrReport {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val defaultDisplay = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)

        var isHdr = false
        var isWideGamut = false
        val supportedTypes = mutableListOf<HdrFormat>()
        var maxLum = 0f
        var minLum = 0f
        var maxAvgLum = 0f
        var resolution = "1080 x 2400"
        var refreshRate = 60f

        if (defaultDisplay != null) {
            refreshRate = defaultDisplay.refreshRate
            val mode = defaultDisplay.mode
            resolution = "${mode.physicalWidth} x ${mode.physicalHeight}"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isHdr = defaultDisplay.isHdr
                isWideGamut = defaultDisplay.isWideColorGamut
            }

            val hdrCaps = defaultDisplay.hdrCapabilities
            if (hdrCaps != null) {
                val types = hdrCaps.supportedHdrTypes
                for (type in types) {
                    when (type) {
                        Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> supportedTypes.add(HdrFormat.DOLBY_VISION)
                        Display.HdrCapabilities.HDR_TYPE_HDR10 -> supportedTypes.add(HdrFormat.HDR10)
                        Display.HdrCapabilities.HDR_TYPE_HLG -> supportedTypes.add(HdrFormat.HLG)
                        Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> supportedTypes.add(HdrFormat.HDR10_PLUS)
                    }
                }
                maxLum = hdrCaps.desiredMaxLuminance
                minLum = hdrCaps.desiredMinLuminance
                maxAvgLum = hdrCaps.desiredMaxAverageLuminance
            }
        }

        // Check MediaCodecList for Hardware Decoders
        val dvDecoders = mutableListOf<String>()
        val hevcDecoders = mutableListOf<String>()
        val av1Decoders = mutableListOf<String>()

        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            for (info in codecList.codecInfos) {
                if (info.isEncoder) continue
                val types = info.supportedTypes
                for (type in types) {
                    if (type.equals("video/dolby-vision", ignoreCase = true)) {
                        dvDecoders.add(info.name)
                        if (!supportedTypes.contains(HdrFormat.DOLBY_VISION)) {
                            supportedTypes.add(HdrFormat.DOLBY_VISION)
                        }
                    }
                    if (type.equals("video/hevc", ignoreCase = true)) {
                        hevcDecoders.add(info.name)
                    }
                    if (type.equals("video/av01", ignoreCase = true)) {
                        av1Decoders.add(info.name)
                    }
                }
            }
        } catch (e: Exception) {
            // MediaCodec query fallback
        }

        return DisplayHdrReport(
            isDisplayHdrCapable = isHdr || supportedTypes.isNotEmpty(),
            isWideColorGamutSupported = isWideGamut,
            supportedHdrTypes = supportedTypes.distinct(),
            maxLuminanceNits = if (maxLum > 0f) maxLum else 1000f,
            minLuminanceNits = minLum,
            maxAverageLuminanceNits = if (maxAvgLum > 0f) maxAvgLum else 600f,
            dolbyVisionHardwareDecoders = dvDecoders,
            hevcHdrDecoders = hevcDecoders,
            av1HdrDecoders = av1Decoders,
            displayResolution = resolution,
            refreshRateHz = refreshRate
        )
    }
}
