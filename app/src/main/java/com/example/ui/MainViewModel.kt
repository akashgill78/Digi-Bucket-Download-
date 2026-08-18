package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DownloadOption
import com.example.data.MediaItem
import com.example.data.MediaMetadata
import com.example.data.MediaRepository
import com.example.downloader.MediaDownloaderEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AnalysisState {
    data object Idle : AnalysisState
    data object Loading : AnalysisState
    data class Success(val metadata: MediaMetadata) : AnalysisState
    data class Error(val message: String) : AnalysisState
}

enum class MediaFilter {
    ALL, VIDEOS, AUDIO, IMAGES
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MediaRepository

    init {
        val database = AppDatabase.getDatabase(application)
        val downloaderEngine = MediaDownloaderEngine(application)
        repository = MediaRepository(database.mediaDao(), downloaderEngine)

        // Seed initial items if database is empty on first boot
        viewModelScope.launch {
            repository.seedInitialMediaIfEmpty()
        }
    }

    // UI Navigation Tab
    private val _currentTab = MutableStateFlow(0) // 0: Home / Downloader, 1: Browser / Sniffer, 2: Library
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    // Filter tab in Library
    private val _mediaFilter = MutableStateFlow(MediaFilter.ALL)
    val mediaFilter: StateFlow<MediaFilter> = _mediaFilter.asStateFlow()

    fun setMediaFilter(filter: MediaFilter) {
        _mediaFilter.value = filter
    }

    // URL input in Home screen
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    fun onUrlChange(newUrl: String) {
        _urlInput.value = newUrl
    }

    // Link analysis state
    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    // Active sheet / dialogs
    private val _formatSheetMetadata = MutableStateFlow<MediaMetadata?>(null)
    val formatSheetMetadata: StateFlow<MediaMetadata?> = _formatSheetMetadata.asStateFlow()

    private val _playingMediaItem = MutableStateFlow<MediaItem?>(null)
    val playingMediaItem: StateFlow<MediaItem?> = _playingMediaItem.asStateFlow()

    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    fun openProfileDialog() {
        _showProfileDialog.value = true
    }

    fun closeProfileDialog() {
        _showProfileDialog.value = false
    }

    fun openMediaViewer(item: MediaItem) {
        _playingMediaItem.value = item
    }

    fun closeMediaViewer() {
        _playingMediaItem.value = null
    }

    fun openFormatSheet(metadata: MediaMetadata) {
        _formatSheetMetadata.value = metadata
    }

    fun closeFormatSheet() {
        _formatSheetMetadata.value = null
    }

    // Browser state
    private val _browserUrl = MutableStateFlow("https://m.facebook.com")
    val browserUrl: StateFlow<String> = _browserUrl.asStateFlow()

    private val _sniffedMediaCount = MutableStateFlow(0)
    val sniffedMediaCount: StateFlow<Int> = _sniffedMediaCount.asStateFlow()

    private val _lastSniffedUrl = MutableStateFlow("")
    val lastSniffedUrl: StateFlow<String> = _lastSniffedUrl.asStateFlow()

    fun navigateBrowser(targetUrl: String) {
        _browserUrl.value = targetUrl
        _currentTab.value = 1 // Switch to browser
    }

    fun onBrowserMediaSniffed(mediaUrl: String, pageTitle: String, thumbnail: String) {
        _sniffedMediaCount.value = _sniffedMediaCount.value + 1
        _lastSniffedUrl.value = mediaUrl
    }

    fun triggerSnifferDownload() {
        val target = if (_lastSniffedUrl.value.isNotBlank()) _lastSniffedUrl.value else _browserUrl.value
        analyzeAndShowOptions(target)
    }

    // Data streams
    val allMedia: StateFlow<List<MediaItem>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMedia: StateFlow<List<MediaItem>> = combine(
        repository.allMedia,
        _mediaFilter
    ) { items, filter ->
        when (filter) {
            MediaFilter.ALL -> items
            MediaFilter.VIDEOS -> items.filter { it.mediaType == "VIDEO" }
            MediaFilter.AUDIO -> items.filter { it.mediaType == "AUDIO" }
            MediaFilter.IMAGES -> items.filter { it.mediaType == "IMAGE" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDownloads: StateFlow<List<MediaItem>> = repository.activeDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun analyzeAndShowOptions(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _analysisState.value = AnalysisState.Loading
            try {
                val metadata = repository.analyzeUrl(url)
                _analysisState.value = AnalysisState.Success(metadata)
                _formatSheetMetadata.value = metadata
            } catch (e: Exception) {
                _analysisState.value = AnalysisState.Error(e.message ?: "Failed to extract media")
            }
        }
    }

    fun startDownload(option: DownloadOption, metadata: MediaMetadata) {
        viewModelScope.launch {
            repository.enqueueAndStartDownload(
                title = metadata.title,
                sourceUrl = metadata.originalUrl,
                option = option,
                platform = metadata.platform,
                thumbnailUrl = metadata.thumbnailUrl
            )
        }
    }

    fun deleteMedia(item: MediaItem) {
        viewModelScope.launch {
            repository.deleteMedia(item)
        }
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}
