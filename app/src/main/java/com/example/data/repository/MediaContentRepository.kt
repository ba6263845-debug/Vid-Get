package com.example.data.repository

import com.example.domain.model.MediaFormatOption
import com.example.domain.model.MediaItem
import com.example.domain.model.SiteShortcut

class MediaContentRepository {

    // Default site shortcuts: YouTube, Facebook, Instagram, Dailymotion, Vimeo, Archive.org, TikTok, Twitter/X
    fun getSiteShortcuts(): List<SiteShortcut> {
        return listOf(
            SiteShortcut("yt", "YouTube", "https://m.youtube.com", "Video", "youtube", 0xFFFF0000),
            SiteShortcut("archive", "Archive.org", "https://archive.org/details/movies", "Archive", "archive", 0xFF2D3748),
            SiteShortcut("vimeo", "Vimeo", "https://vimeo.com/watch", "Video", "vimeo", 0xFF1AB7EA),
            SiteShortcut("daily", "Dailymotion", "https://www.dailymotion.com", "Video", "dailymotion", 0xFF0066DC),
            SiteShortcut("fb", "Facebook", "https://m.facebook.com", "Social", "facebook", 0xFF1877F2),
            SiteShortcut("insta", "Instagram", "https://www.instagram.com", "Social", "instagram", 0xFFE1306C),
            SiteShortcut("tiktok", "TikTok", "https://www.tiktok.com", "Shorts", "tiktok", 0xFF000000),
            SiteShortcut("x", "Twitter / X", "https://x.com", "Social", "twitter", 0xFF1DA1F2)
        )
    }

    // Rich catalog of Creative Commons, Open Movies, and verified CDN media streams
    fun getCuratedMedia(category: String = "All"): List<MediaItem> {
        val bbbUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4"
        val echoUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4"
        val bbb480Url = "https://raw.githubusercontent.com/bower-media-samples/big-buck-bunny-480p-30s/master/video.mp4"
        val audioUrl = "https://raw.githubusercontent.com/rafaelreis-hotmart/Audio-Sample-files/master/sample.mp3"

        val allItems = listOf(
            MediaItem(
                id = "cc_bbb",
                title = "Big Buck Bunny - Open Movie (4K Remaster)",
                author = "Blender Foundation",
                thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80",
                duration = "09:56",
                views = "14.2M views",
                streamUrl = bbbUrl,
                license = "Creative Commons (CC-BY 3.0)",
                isDownloadAllowed = true,
                category = "Creative Commons",
                description = "Open source movie produced by the Blender Institute. Free to download, share, and remix under Creative Commons Attribution 3.0.",
                availableQualities = listOf(
                    MediaFormatOption("1080p FHD (MP4)", "mp4", 158 * 1024 * 1024L, false, bbbUrl),
                    MediaFormatOption("720p HD (MP4)", "mp4", 82 * 1024 * 1024L, false, bbbUrl),
                    MediaFormatOption("480p SD (MP4)", "mp4", 45 * 1024 * 1024L, false, bbb480Url),
                    MediaFormatOption("360p Fast (MP4)", "mp4", 28 * 1024 * 1024L, false, bbb480Url),
                    MediaFormatOption("Audio MP3 320kbps", "mp3", 12 * 1024 * 1024L, true, audioUrl),
                    MediaFormatOption("Audio M4A 128kbps", "m4a", 8 * 1024 * 1024L, true, audioUrl)
                )
            ),
            MediaItem(
                id = "cc_sintel",
                title = "Sintel - Epic CGI Fantasy Short Film",
                author = "Durian Open Movie Project",
                thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
                duration = "14:48",
                views = "8.9M views",
                streamUrl = echoUrl,
                license = "Creative Commons (CC-BY 3.0)",
                isDownloadAllowed = true,
                category = "Creative Commons",
                description = "Sintel is an independently produced open-movie fantasy, freely licensed for public remixing and educational distribution.",
                availableQualities = listOf(
                    MediaFormatOption("1080p FHD (MP4)", "mp4", 210 * 1024 * 1024L, false, echoUrl),
                    MediaFormatOption("720p HD (MP4)", "mp4", 120 * 1024 * 1024L, false, echoUrl),
                    MediaFormatOption("480p SD (MP4)", "mp4", 75 * 1024 * 1024L, false, bbb480Url),
                    MediaFormatOption("Audio MP3 192kbps", "mp3", 14 * 1024 * 1024L, true, audioUrl)
                )
            ),
            MediaItem(
                id = "cc_tears",
                title = "Tears of Steel - Sci-Fi VFX Showcase",
                author = "Mango Open Movie Project",
                thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80",
                duration = "12:14",
                views = "5.1M views",
                streamUrl = bbb480Url,
                license = "Creative Commons (CC-BY 3.0)",
                isDownloadAllowed = true,
                category = "Movies",
                description = "Open source VFX and live action sci-fi thriller set in dystopian Amsterdam. Created with open source tools.",
                availableQualities = listOf(
                    MediaFormatOption("1080p FHD (MP4)", "mp4", 195 * 1024 * 1024L, false, echoUrl),
                    MediaFormatOption("720p HD (MP4)", "mp4", 98 * 1024 * 1024L, false, bbb480Url),
                    MediaFormatOption("Audio MP3 320kbps", "mp3", 15 * 1024 * 1024L, true, audioUrl)
                )
            ),
            MediaItem(
                id = "cc_elephants",
                title = "Elephants Dream - Surreal Cyberpunk Animation",
                author = "Orange Open Studio",
                thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&q=80",
                duration = "10:53",
                views = "3.4M views",
                streamUrl = bbbUrl,
                license = "Creative Commons (CC-BY 2.5)",
                isDownloadAllowed = true,
                category = "Creative Commons",
                description = "The world's first open movie project, made entirely using open source graphics software.",
                availableQualities = listOf(
                    MediaFormatOption("720p HD (MP4)", "mp4", 88 * 1024 * 1024L, false, bbbUrl),
                    MediaFormatOption("480p SD (MP4)", "mp4", 48 * 1024 * 1024L, false, bbb480Url),
                    MediaFormatOption("Audio MP3 192kbps", "mp3", 11 * 1024 * 1024L, true, audioUrl)
                )
            ),
            MediaItem(
                id = "music_cc1",
                title = "Lo-Fi Beats & Ambient Soundscape (Royalty-Free)",
                author = "Open Audio Collective",
                thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&q=80",
                duration = "04:32",
                views = "920K views",
                streamUrl = audioUrl,
                license = "Creative Commons (CC-0 Public Domain)",
                isDownloadAllowed = true,
                category = "Music",
                description = "Relaxing royalty free lo-fi chillout soundtrack for studying, coding, or relaxation. Free download permitted.",
                availableQualities = listOf(
                    MediaFormatOption("HQ Audio MP3 320kbps", "mp3", 8 * 1024 * 1024L, true, audioUrl),
                    MediaFormatOption("Audio M4A 192kbps", "m4a", 5 * 1024 * 1024L, true, audioUrl),
                    MediaFormatOption("Music Video 720p (MP4)", "mp4", 32 * 1024 * 1024L, false, echoUrl)
                )
            ),
            MediaItem(
                id = "nasa_space",
                title = "NASA Mars Exploration Documentary & 4K Views",
                author = "NASA Public Domain Archives",
                thumbnailUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=800&q=80",
                duration = "08:15",
                views = "2.8M views",
                streamUrl = echoUrl,
                license = "Public Domain (NASA US Govt)",
                isDownloadAllowed = true,
                category = "Education",
                description = "Authentic NASA Mars Rover imagery and scientific audio logs. In the public domain and available for educational download.",
                availableQualities = listOf(
                    MediaFormatOption("1080p FHD (MP4)", "mp4", 140 * 1024 * 1024L, false, echoUrl),
                    MediaFormatOption("720p HD (MP4)", "mp4", 75 * 1024 * 1024L, false, bbb480Url),
                    MediaFormatOption("Audio Podcast MP3", "mp3", 9 * 1024 * 1024L, true, audioUrl)
                )
            ),
            MediaItem(
                id = "yt_std_1",
                title = "World Music Tour - Live Studio Performance",
                author = "Global Studio Official",
                thumbnailUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&q=80",
                duration = "05:40",
                views = "45.1M views",
                streamUrl = bbbUrl,
                license = "Standard YouTube License",
                isDownloadAllowed = false, // Policy compliant: not downloadable, open in official player!
                category = "Trending",
                description = "Official copyright-protected live studio release. YouTube Terms of Service prohibit direct downloading; enjoy in-app official playback or save to Watch Later.",
                availableQualities = emptyList()
            )
        )

        return if (category == "All") {
            allItems
        } else {
            allItems.filter { it.category.equals(category, ignoreCase = true) }
        }
    }

    fun searchMedia(query: String): List<MediaItem> {
        val trimmed = query.trim().lowercase()
        return getCuratedMedia("All").filter {
            it.title.lowercase().contains(trimmed) ||
            it.author.lowercase().contains(trimmed) ||
            it.category.lowercase().contains(trimmed) ||
            it.license.lowercase().contains(trimmed)
        }
    }
}
