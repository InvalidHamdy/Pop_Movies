package com.InvalidHamdy.moviezshow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(media: MediaEntity)

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: Int): MediaEntity?

    @Query("SELECT * FROM media_items WHERE isFavorite = 1")
    suspend fun getAllFavorites(): List<MediaEntity>

    @Query("SELECT * FROM media_items WHERE saveList = :listName")
    suspend fun getAllByList(listName: String): List<MediaEntity>
    
    @Query("SELECT * FROM media_items")
    suspend fun getAll(): List<MediaEntity>

    @Query("UPDATE media_items SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFav: Boolean)

    @Query("UPDATE media_items SET saveList = :listName WHERE id = :id")
    suspend fun updateList(id: Int, listName: String?)
    
    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun delete(id: Int)
}
