package com.example

import com.example.data.local.entity.DownloadTaskEntity
import com.example.data.repository.MediaContentRepository
import com.example.util.AppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLocalization_bengaliAndEnglish() {
        val bnHome = AppStrings.get("home", AppStrings.Language.BENGALI)
        val enHome = AppStrings.get("home", AppStrings.Language.ENGLISH)
        val hiHome = AppStrings.get("home", AppStrings.Language.HINDI)

        assertEquals("হোম", bnHome)
        assertEquals("Home", enHome)
        assertEquals("होम", hiHome)
    }

    @Test
    fun testMediaContentRepository_ccFiltering() {
        val repo = MediaContentRepository()
        val allMedia = repo.getCuratedMedia("All")
        assertTrue(allMedia.isNotEmpty())

        val ccMedia = allMedia.filter { it.isDownloadAllowed }
        assertTrue(ccMedia.isNotEmpty())

        // Ensure non-downloadable YouTube item is properly flagged
        val stdMedia = allMedia.find { !it.isDownloadAllowed }
        assertNotNull(stdMedia)
        assertFalse(stdMedia!!.isDownloadAllowed)
    }

    @Test
    fun testDownloadTaskEntity_properties() {
        val task = DownloadTaskEntity(
            title = "Test Video",
            url = "https://example.com/video.mp4",
            mimeType = "video/mp4",
            localFilePath = "/data/downloads/video.mp4",
            quality = "1080p",
            isAudioOnly = false
        )
        assertEquals("DOWNLOADING", task.status)
        assertEquals(0L, task.downloadedBytes)
        assertFalse(task.isPrivate)
    }
}
