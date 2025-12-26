package com.InvalidHamdy.moviezshow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String, // "movie" or "tv"
    val isFavorite: Boolean = false,
    val saveList: String? = null // "completed", "dropped", "watch_later" or null
)
