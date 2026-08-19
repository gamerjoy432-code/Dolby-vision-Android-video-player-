package com.example.data.db

import com.example.data.model.BookmarkEntity
import com.example.data.model.CuratedMediaData
import com.example.data.model.MediaItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MediaRepository(private val mediaDao: MediaDao) {

    val allMedia: Flow<List<MediaItemEntity>> = mediaDao.getAllMedia()
    val favorites: Flow<List<MediaItemEntity>> = mediaDao.getFavorites()
    val recentHistory: Flow<List<MediaItemEntity>> = mediaDao.getRecentHistory()

    suspend fun ensureDefaultSamplesLoaded() {
        val existing = mediaDao.getAllMedia().first()
        if (existing.isEmpty()) {
            mediaDao.insertAll(CuratedMediaData.sampleStreams)
        }
    }

    suspend fun getMediaById(id: Long): MediaItemEntity? = mediaDao.getMediaById(id)

    suspend fun getMediaByUri(uri: String): MediaItemEntity? = mediaDao.getMediaByUri(uri)

    suspend fun addCustomMedia(item: MediaItemEntity): Long = mediaDao.insertMedia(item)

    suspend fun updatePlaybackPosition(id: Long, positionMs: Long) {
        mediaDao.updatePlaybackPosition(id, positionMs, System.currentTimeMillis())
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        mediaDao.setFavorite(id, !currentStatus)
    }

    suspend fun deleteCustomMedia(id: Long) {
        mediaDao.deleteCustomMedia(id)
    }

    fun getBookmarks(mediaItemId: Long): Flow<List<BookmarkEntity>> =
        mediaDao.getBookmarksForMedia(mediaItemId)

    suspend fun addBookmark(mediaItemId: Long, timestampMs: Long, title: String, note: String = ""): Long {
        return mediaDao.insertBookmark(
            BookmarkEntity(
                mediaItemId = mediaItemId,
                timestampMs = timestampMs,
                title = title,
                note = note
            )
        )
    }

    suspend fun deleteBookmark(bookmarkId: Long) {
        mediaDao.deleteBookmark(bookmarkId)
    }
}
