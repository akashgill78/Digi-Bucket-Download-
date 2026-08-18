package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY timestamp DESC")
    fun getMediaByType(type: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE status = 'DOWNLOADING' OR status = 'QUEUED' ORDER BY timestamp DESC")
    fun getActiveDownloads(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: Long): MediaItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItem): Long

    @Update
    suspend fun updateMedia(item: MediaItem)

    @Query("UPDATE media_items SET status = :status, progress = :progress, downloadSpeed = :speed, localFilePath = :localPath WHERE id = :id")
    suspend fun updateDownloadProgress(id: Long, status: String, progress: Float, speed: String, localPath: String?)

    @Delete
    suspend fun deleteMedia(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("DELETE FROM media_items")
    suspend fun clearAll()
}
