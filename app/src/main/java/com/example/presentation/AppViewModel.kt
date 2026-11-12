package com.example.presentation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.download.DownloadEngine
import com.example.data.local.VidGetDatabase
import com.example.data.local.entity.DownloadTaskEntity
import com.example.data.repository.BrowserRepository
import com.example.data.repository.DownloadRepository
import com.example.data.repository.MediaContentRepository
import com.example.domain.model.MediaFormatOption
import com.example.domain.model.MediaItem
import com.example.domain.model.SiteShortcut
import com.example.util.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActivePlayback(
    val title: String,
    val author: String,
    val mediaUrl: String,
    val isLocalFile: Boolean = false,
    val isAudioOnly: Boolean = false
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VidGetDatabase.getDatabase(application)
    val downloadEngine = DownloadEngine(application, database)
    val downloadRepository = DownloadRepository(database, downloadEngine)
    val browserRepository = BrowserRepository(database)
    val mediaRepository = MediaContentRepository()

    // Preferences
    private val prefs = application.getSharedPreferences("vidget_prefs", Context.MODE_PRIVATE)

    // Language state
    private val _currentLanguage = MutableStateFlow(
        when (prefs.getString("app_language", "bn")) {
            "en" -> AppStrings.Language.ENGLISH
            "hi" -> AppStrings.Language.HINDI
            else -> AppStrings.Language.BENGALI
        }
    )
    val currentLanguage = _currentLanguage.asStateFlow()

    // Theme state
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", true))
    val isDarkTheme = _isDarkTheme.asStateFlow()

    // First run disclaimer acceptance
    private val _isDisclaimerAccepted = MutableStateFlow(prefs.getBoolean("disclaimer_accepted", false))
    val isDisclaimerAccepted = _isDisclaimerAccepted.asStateFlow()

    // Download Tasks Flows
    val allTasks: StateFlow<List<DownloadTaskEntity>> = downloadRepository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val privateTasks: StateFlow<List<DownloadTaskEntity>> = downloadRepository.privateTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Home / Discover media
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _curatedMedia = MutableStateFlow(mediaRepository.getCuratedMedia("All"))
    val curatedMedia = _curatedMedia.asStateFlow()

    val siteShortcuts: List<SiteShortcut> = mediaRepository.getSiteShortcuts()

    // Format Selector Bottom Sheet state
    private val _selectedMediaForDownload = MutableStateFlow<MediaItem?>(null)
    val selectedMediaForDownload = _selectedMediaForDownload.asStateFlow()

    // Active playback in Player
    private val _currentPlayback = MutableStateFlow<ActivePlayback?>(null)
    val currentPlayback = _currentPlayback.asStateFlow()

    // Active Browser URL to navigate to
    private val _browserTargetUrl = MutableStateFlow<String?>(null)
    val browserTargetUrl = _browserTargetUrl.asStateFlow()

    // Detected clipboard link
    private val _clipboardDetectedUrl = MutableStateFlow<String?>(null)
    val clipboardDetectedUrl = _clipboardDetectedUrl.asStateFlow()

    // Vault unlocked
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked = _isVaultUnlocked.asStateFlow()

    // Wi-Fi Only & Concurrency Settings
    private val _isWifiOnly = MutableStateFlow(prefs.getBoolean("wifi_only", false))
    val isWifiOnly = _isWifiOnly.asStateFlow()

    private val _maxConcurrent = MutableStateFlow(prefs.getInt("max_concurrent", 3))
    val maxConcurrent = _maxConcurrent.asStateFlow()

    fun setLanguage(language: AppStrings.Language) {
        _currentLanguage.value = language
        prefs.edit().putString("app_language", language.code).apply()
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        prefs.edit().putBoolean("dark_theme", isDark).apply()
    }

    fun acceptDisclaimer() {
        _isDisclaimerAccepted.value = true
        prefs.edit().putBoolean("disclaimer_accepted", true).apply()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        filterMedia()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterMedia()
    }

    private fun filterMedia() {
        val query = _searchQuery.value
        val cat = _selectedCategory.value
        _curatedMedia.value = if (query.isBlank()) {
            mediaRepository.getCuratedMedia(cat)
        } else {
            mediaRepository.searchMedia(query)
        }
    }

    fun openDownloadSheet(mediaItem: MediaItem) {
        _selectedMediaForDownload.value = mediaItem
    }

    fun closeDownloadSheet() {
        _selectedMediaForDownload.value = null
    }

    fun triggerDownload(
        mediaItem: MediaItem,
        selectedOption: MediaFormatOption,
        customTitle: String? = null,
        isPrivate: Boolean = false
    ) {
        val title = customTitle?.ifBlank { mediaItem.title } ?: mediaItem.title
        downloadRepository.startDownload(
            title = title,
            url = selectedOption.url,
            mimeType = if (selectedOption.isAudioOnly) "audio/mpeg" else "video/mp4",
            quality = selectedOption.quality,
            isAudioOnly = selectedOption.isAudioOnly,
            isPrivate = isPrivate
        )
        closeDownloadSheet()
    }

    fun playMedia(mediaItem: MediaItem) {
        _currentPlayback.value = ActivePlayback(
            title = mediaItem.title,
            author = mediaItem.author,
            mediaUrl = mediaItem.streamUrl,
            isLocalFile = false,
            isAudioOnly = mediaItem.category == "Music"
        )
    }

    fun playLocalTask(task: DownloadTaskEntity) {
        _currentPlayback.value = ActivePlayback(
            title = task.title,
            author = "Offline Media",
            mediaUrl = task.localFilePath,
            isLocalFile = true,
            isAudioOnly = task.isAudioOnly
        )
    }

    fun stopPlayback() {
        _currentPlayback.value = null
    }

    fun navigateToBrowser(url: String) {
        _browserTargetUrl.value = url
    }

    fun clearBrowserTarget() {
        _browserTargetUrl.value = null
    }

    fun setDetectedClipboardUrl(url: String?) {
        _clipboardDetectedUrl.value = url
    }

    fun unlockVault(pin: String): Boolean {
        val savedPin = prefs.getString("vault_pin", "1234") ?: "1234"
        return if (pin == savedPin) {
            _isVaultUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun setWifiOnly(enabled: Boolean) {
        _isWifiOnly.value = enabled
        prefs.edit().putBoolean("wifi_only", enabled).apply()
    }

    fun setMaxConcurrent(limit: Int) {
        _maxConcurrent.value = limit
        prefs.edit().putInt("max_concurrent", limit).apply()
    }

    fun string(key: String): String {
        return AppStrings.get(key, _currentLanguage.value)
    }
}
