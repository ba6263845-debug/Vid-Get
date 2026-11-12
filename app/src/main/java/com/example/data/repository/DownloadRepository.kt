package com.example.data.repository

import com.example.data.download.DownloadEngine
import com.example.data.local.VidGetDatabase
import com.example.data.local.entity.DownloadTaskEntity
import kotlinx.coroutines.flow.Flow

class DownloadRepository(
    private val database: VidGetDatabase,
    private val downloadEngine: DownloadEngine
) {
    val allTasks: Flow<List<DownloadTaskEntity>> = database.downloadDao().getAllTasks(isPrivate = false)
    val privateTasks: Flow<List<DownloadTaskEntity>> = database.downloadDao().getAllTasks(isPrivate = true)

    val downloadingTasks: Flow<List<DownloadTaskEntity>> = database.downloadDao().getTasksByStatus("DOWNLOADING", isPrivate = false)
    val completedVideos: Flow<List<DownloadTaskEntity>> = database.downloadDao().getCompletedVideos(isPrivate = false)
    val completedAudio: Flow<List<DownloadTaskEntity>> = database.downloadDao().getCompletedAudio(isPrivate = false)

    fun startDownload(
        title: String,
        url: String,
        mimeType: String = "video/mp4",
        quality: String = "720p",
        isAudioOnly: Boolean = false,
        isPrivate: Boolean = false,
        onQueued: (Long) -> Unit = {}
    ) {
        downloadEngine.startDownload(title, url, mimeType, quality, isAudioOnly, isPrivate, onQueued)
    }

    fun pauseDownload(taskId: Long) {
        downloadEngine.pauseDownload(taskId)
    }

    fun resumeDownload(taskId: Long) {
        downloadEngine.resumeDownload(taskId)
    }

    fun cancelOrDeleteDownload(taskId: Long) {
        downloadEngine.deleteFileAndTask(taskId)
    }

    suspend fun renameDownload(taskId: Long, newTitle: String) {
        database.downloadDao().renameTask(taskId, newTitle)
    }

    suspend fun setPrivate(taskId: Long, isPrivate: Boolean) {
        database.downloadDao().updateTaskPrivacy(taskId, isPrivate)
    }
}
