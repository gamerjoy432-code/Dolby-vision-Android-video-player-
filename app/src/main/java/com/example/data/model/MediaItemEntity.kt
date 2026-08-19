package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val uri: String,
    val category: String = "Curated HDR", // "Curated HDR", "Dolby Vision", "HLG", "Custom Streams", "Local Storage"
    val hdrFormat: HdrFormat = HdrFormat.AUTO,
    val dvProfile: String? = null,
    val durationMs: Long = 0,
    val lastPositionMs: Long = 0,
    val isFavorite: Boolean = false,
    val resolutionLabel: String = "4K UHD",
    val audioLabel: String = "Dolby Atmos / 5.1",
    val thumbnailResName: String? = null,
    val localUriString: String? = null,
    val lastPlayedTimestamp: Long = 0,
    val isSample: Boolean = false
)

@Entity(tableName = "video_bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaItemId: Long,
    val timestampMs: Long,
    val title: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
