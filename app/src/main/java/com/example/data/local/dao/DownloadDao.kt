package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DownloadTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_tasks WHERE isPrivate = :isPrivate ORDER BY createdAt DESC")
    fun getAllTasks(isPrivate: Boolean = false): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status = :status AND isPrivate = :isPrivate ORDER BY createdAt DESC")
    fun getTasksByStatus(status: String, isPrivate: Boolean = false): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status = 'COMPLETED' AND isAudioOnly = 1 AND isPrivate = :isPrivate ORDER BY createdAt DESC")
    fun getCompletedAudio(isPrivate: Boolean = false): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status = 'COMPLETED' AND isAudioOnly = 0 AND isPrivate = :isPrivate ORDER BY createdAt DESC")
    fun getCompletedVideos(isPrivate: Boolean = false): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): DownloadTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DownloadTaskEntity): Long

    @Update
    suspend fun updateTask(task: DownloadTaskEntity)

    @Query("UPDATE download_tasks SET status = :status, speedBytesPerSec = :speed, etaSeconds = :eta, downloadedBytes = :downloaded, totalBytes = :total WHERE id = :id")
    suspend fun updateProgress(id: Long, status: String, downloaded: Long, total: Long, speed: Long, eta: Long)

    @Delete
    suspend fun deleteTask(task: DownloadTaskEntity)

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE download_tasks SET isPrivate = :isPrivate WHERE id = :id")
    suspend fun updateTaskPrivacy(id: Long, isPrivate: Boolean)

    @Query("UPDATE download_tasks SET title = :newTitle WHERE id = :id")
    suspend fun renameTask(id: Long, newTitle: String)
}
