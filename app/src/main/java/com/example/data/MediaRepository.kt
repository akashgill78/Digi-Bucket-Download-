package com.example.data

import android.content.Context
import com.example.downloader.MediaDownloaderEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File

class MediaRepository(
    private val mediaDao: MediaDao,
    private val downloaderEngine: MediaDownloaderEngine,
    private val applicationScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val allMedia: Flow<List<MediaItem>> = mediaDao.getAllMedia()
    val activeDownloads: Flow<List<MediaItem>> = mediaDao.getActiveDownloads()

    fun getMediaByType(type: String): Flow<List<MediaItem>> = mediaDao.getMediaByType(type)

    suspend fun analyzeUrl(url: String): MediaMetadata {
        return downloaderEngine.analyzeUrl(url)
    }

    suspend fun enqueueAndStartDownload(
        title: String,
        sourceUrl: String,
        option: DownloadOption,
        platform: String,
        thumbnailUrl: String
    ): Long {
        val newItem = MediaItem(
            title = title,
            sourceUrl = sourceUrl,
            downloadUrl = option.downloadUrl,
            platform = platform,
            mediaType = option.mediaType,
            formatLabel = option.formatLabel,
            fileExtension = option.fileExtension,
            fileSize = 0L,
            thumbnailUrl = thumbnailUrl,
            status = "DOWNLOADING",
            progress = 0.05f,
            downloadSpeed = "Starting...",
            timestamp = System.currentTimeMillis()
        )

        val insertedId = mediaDao.insertMedia(newItem)
        val itemToDownload = newItem.copy(id = insertedId)

        // Launch background download task
        applicationScope.launch {
            val result = downloaderEngine.executeDownload(itemToDownload) { progress, speed, localPath, bytesDownloaded ->
                val status = if (progress >= 1.0f) "COMPLETED" else "DOWNLOADING"
                mediaDao.updateDownloadProgress(
                    id = insertedId,
                    status = status,
                    progress = progress,
                    speed = speed,
                    localPath = localPath
                )
            }

            if (result.isFailure) {
                mediaDao.updateDownloadProgress(
                    id = insertedId,
                    status = "FAILED",
                    progress = 0f,
                    speed = "Failed",
                    localPath = null
                )
            }
        }

        return insertedId
    }

    suspend fun deleteMedia(item: MediaItem) {
        // Delete local file if present
        item.localFilePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaDao.deleteMedia(item)
    }

    suspend fun deleteById(id: Long) {
        val item = mediaDao.getMediaById(id)
        if (item != null) {
            deleteMedia(item)
        }
    }

    suspend fun clearAll() {
        mediaDao.clearAll()
    }

    // Seed sample completed media on first launch so user immediately has items to play & test
    suspend fun seedInitialMediaIfEmpty() {
        // Check if database is empty
        val sampleItem1 = MediaItem(
            title = "Nature Wildlife 4K Reel",
            sourceUrl = "https://instagram.com/reel/sample1",
            downloadUrl = MediaDownloaderEngine.SAMPLE_VIDEO_1080P,
            platform = "Instagram",
            mediaType = "VIDEO",
            formatLabel = "MP4 1080p Full HD",
            fileExtension = "mp4",
            fileSize = 15_400_000L,
            durationSeconds = 125,
            thumbnailUrl = "https://images.unsplash.com/photo-1611262588024-d12430b98920?w=600&q=80",
            status = "COMPLETED",
            progress = 1.0f,
            downloadSpeed = "Fast",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 30
        )

        val sampleItem2 = MediaItem(
            title = "Trending Synthwave Beat (Mastered)",
            sourceUrl = "https://youtube.com/watch?v=sample2",
            downloadUrl = MediaDownloaderEngine.SAMPLE_AUDIO_MP3,
            platform = "YouTube",
            mediaType = "AUDIO",
            formatLabel = "MP3 320kbps Audio",
            fileExtension = "mp3",
            fileSize = 4_800_000L,
            durationSeconds = 210,
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&q=80",
            status = "COMPLETED",
            progress = 1.0f,
            downloadSpeed = "Fast",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 120
        )

        val sampleItem3 = MediaItem(
            title = "Sunset Aesthetic Photography",
            sourceUrl = "https://pinterest.com/pin/sample3",
            downloadUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80",
            platform = "Pinterest",
            mediaType = "IMAGE",
            formatLabel = "JPG Ultra HD",
            fileExtension = "jpg",
            fileSize = 2_100_000L,
            thumbnailUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&q=80",
            status = "COMPLETED",
            progress = 1.0f,
            downloadSpeed = "Fast",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 300
        )

        mediaDao.insertMedia(sampleItem1)
        mediaDao.insertMedia(sampleItem2)
        mediaDao.insertMedia(sampleItem3)
    }
}
