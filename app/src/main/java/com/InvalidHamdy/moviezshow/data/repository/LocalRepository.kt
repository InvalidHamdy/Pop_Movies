package com.InvalidHamdy.moviezshow.data.repository

import com.InvalidHamdy.moviezshow.data.local.MediaDao
import com.InvalidHamdy.moviezshow.data.local.MediaEntity

class LocalRepository(private val mediaDao: MediaDao) {

    suspend fun toggleFavorite(mediaId: Int, title: String, posterPath: String?, mediaType: String): Boolean {
        val existing = mediaDao.getMediaById(mediaId)
        if (existing != null) {
            val newFavStatus = !existing.isFavorite
            // If checking 'saveList' is also null/empty, maybe we should delete the row?
            // For now, let's just update.
            mediaDao.updateFavorite(mediaId, newFavStatus)
            return newFavStatus
        } else {
            // New entry, so set Favorite = true
            val newItem = MediaEntity(
                id = mediaId,
                title = title,
                posterPath = posterPath,
                mediaType = mediaType,
                isFavorite = true,
                saveList = null
            )
            mediaDao.insertOrUpdate(newItem)
            return true
        }
    }

    suspend fun updateSaveList(mediaId: Int, title: String, posterPath: String?, mediaType: String, listName: String?) {
        val existing = mediaDao.getMediaById(mediaId)
        if (existing != null) {
            mediaDao.updateList(mediaId, listName)
        } else {
            if (listName != null) {
                val newItem = MediaEntity(
                    id = mediaId,
                    title = title,
                    posterPath = posterPath,
                    mediaType = mediaType,
                    isFavorite = false,
                    saveList = listName
                )
                mediaDao.insertOrUpdate(newItem)
            }
        }
    }

    suspend fun getMediaById(id: Int): MediaEntity? {
        return mediaDao.getMediaById(id)
    }

    suspend fun getAllMedia(): List<MediaEntity> {
        return mediaDao.getAll()
    }
}
