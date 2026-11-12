package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val mimeType: String,
    val localFilePath: String,
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: String = "DOWNLOADING", // DOWNLOADING, PAUSED, COMPLETED, FAILED
    val speedBytesPerSec: Long = 0,
    val etaSeconds: Long = 0,
    val quality: String = "720p",
    val isAudioOnly: Boolean = false,
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
