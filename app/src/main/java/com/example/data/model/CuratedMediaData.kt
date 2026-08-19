package com.example.data.model

object CuratedMediaData {
    val sampleStreams = listOf(
        MediaItemEntity(
            id = 1,
            title = "Dolby Vision Profile 5 (LG Cinema Test)",
            description = "12-bit dynamic tone-mapping showcase with IPTPQc2 wide color spectrum and scene-by-scene brightness steering.",
            uri = "https://dash.akamaized.net/akamai/test/caption_test/ElephantsDream/elephants_dream_480p_heaac5_1.mp4",
            category = "Dolby Vision",
            hdrFormat = HdrFormat.DOLBY_VISION,
            dvProfile = "Profile 5 (dvhe.05)",
            durationMs = 653000,
            resolutionLabel = "4K UHD 60fps",
            audioLabel = "Dolby Digital Plus 5.1",
            thumbnailResName = "thumb_cyber_dv_1787151711348",
            isSample = true
        ),
        MediaItemEntity(
            id = 2,
            title = "HDR10 Nebula & Deep Cosmos Master",
            description = "Native 10-bit SMPTE ST 2084 (PQ) color transfer with BT.2020 wide color gamut and 1000-nit specular highlights.",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            category = "Curated HDR",
            hdrFormat = HdrFormat.HDR10,
            dvProfile = null,
            durationMs = 596000,
            resolutionLabel = "4K HDR10 10-Bit",
            audioLabel = "Dolby Audio 5.1",
            thumbnailResName = "thumb_cosmos_hdr_1787151695586",
            isSample = true
        ),
        MediaItemEntity(
            id = 3,
            title = "HLG Tropical Emerald Rainforest 4K",
            description = "Hybrid Log-Gamma (ARIB STD-B67 / BT.2100) broadcast HDR standard with ultra-rich green and water luminescence.",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            category = "HLG",
            hdrFormat = HdrFormat.HLG,
            dvProfile = null,
            durationMs = 15000,
            resolutionLabel = "4K HLG 60fps",
            audioLabel = "E-AC-3 Surround",
            thumbnailResName = "thumb_nature_hlg_1787151727687",
            isSample = true
        ),
        MediaItemEntity(
            id = 4,
            title = "Dolby Vision Profile 8.4 (iPhone ProRes DV)",
            description = "Cross-compatible HLG base layer with Dolby Vision RPU metadata for mobile HDR displays.",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            category = "Dolby Vision",
            hdrFormat = HdrFormat.DOLBY_VISION,
            dvProfile = "Profile 8.4 (dvhe.08)",
            durationMs = 734000,
            resolutionLabel = "4K UHD CinemaScope",
            audioLabel = "Dolby Atmos Spatial",
            thumbnailResName = "thumb_cyber_dv_1787151711348",
            isSample = true
        ),
        MediaItemEntity(
            id = 5,
            title = "HDR10+ Dynamic Metadata Demo (Tears of Steel)",
            description = "SMPTE ST 2094-40 dynamic tone mapping curve adapting peak white and shadow details per scene.",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            category = "Curated HDR",
            hdrFormat = HdrFormat.HDR10_PLUS,
            dvProfile = null,
            durationMs = 60000,
            resolutionLabel = "4K HDR10+ 10-Bit",
            audioLabel = "Stereo 24-bit 48kHz",
            thumbnailResName = "thumb_cosmos_hdr_1787151695586",
            isSample = true
        ),
        MediaItemEntity(
            id = 6,
            title = "4K UHD 60fps Spatial Showcase",
            description = "Ultra high framerate action sequence with full hardware decoder offloading and zero dropped frames.",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            category = "4K Showcase",
            hdrFormat = HdrFormat.AUTO,
            dvProfile = null,
            durationMs = 888000,
            resolutionLabel = "4K UHD 60 FPS",
            audioLabel = "Surround 5.1",
            thumbnailResName = "thumb_nature_hlg_1787151727687",
            isSample = true
        )
    )
}
