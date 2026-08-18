package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sourceUrl: String,
    val downloadUrl: String,
    val platform: String, // "Facebook", "Instagram", "Twitter/X", "YouTube", "TikTok", "Pinterest", "Reddit", "Threads", "Web"
    val mediaType: String, // "VIDEO", "AUDIO", "IMAGE"
    val formatLabel: String, // "MP4 1080p", "MP4 720p", "MP4 480p", "MP3 320kbps", "JPG HD", "PNG"
    val fileExtension: String, // "mp4", "mp3", "jpg", "png", "webm"
    val fileSize: Long = 0L, // in bytes
    val durationSeconds: Int = 0,
    val localFilePath: String? = null,
    val thumbnailUrl: String = "",
    val status: String = "COMPLETED", // "QUEUED", "DOWNLOADING", "COMPLETED", "FAILED", "PAUSED"
    val progress: Float = 1.0f, // 0.0f to 1.0f
    val downloadSpeed: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class DownloadOption(
    val formatLabel: String,
    val mediaType: String, // "VIDEO", "AUDIO", "IMAGE"
    val fileExtension: String,
    val qualityBadge: String, // "1080p FHD", "720p HD", "480p SD", "320 kbps", "HD Original"
    val estimatedSize: String, // e.g., "48.5 MB"
    val downloadUrl: String,
    val isRecommended: Boolean = false
)

data class MediaMetadata(
    val title: String,
    val platform: String,
    val originalUrl: String,
    val thumbnailUrl: String,
    val author: String = "",
    val durationText: String = "",
    val options: List<DownloadOption> = emptyList()
)
