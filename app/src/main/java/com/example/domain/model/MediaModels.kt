package com.example.domain.model

data class MediaItem(
    val id: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String,
    val duration: String,
    val views: String,
    val streamUrl: String,
    val license: String, // "Creative Commons (CC-BY)", "Public Domain", "Standard License"
    val isDownloadAllowed: Boolean,
    val category: String, // "Trending", "Music", "Animation", "Creative Commons", "Education"
    val availableQualities: List<MediaFormatOption> = emptyList(),
    val description: String = ""
)

data class MediaFormatOption(
    val quality: String, // "1080p FHD", "720p HD", "480p SD", "360p", "MP3 320kbps", "MP3 192kbps"
    val format: String, // "mp4", "mp3", "webm"
    val estimatedSizeBytes: Long,
    val isAudioOnly: Boolean = false,
    val url: String
)

data class SiteShortcut(
    val id: String,
    val name: String,
    val url: String,
    val category: String,
    val iconKey: String,
    val colorHex: Long
)
