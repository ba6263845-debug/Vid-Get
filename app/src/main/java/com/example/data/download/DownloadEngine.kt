package com.example.data.download

import android.content.Context
import android.os.Environment
import com.example.data.local.VidGetDatabase
import com.example.data.local.entity.DownloadTaskEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadEngine(
    private val context: Context,
    private val database: VidGetDatabase
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val activeJobs = ConcurrentHashMap<Long, Job>()

    // Global speed meter
    private val _totalSpeedBytesPerSec = MutableStateFlow(0L)
    val totalSpeedBytesPerSec = _totalSpeedBytesPerSec.asStateFlow()

    init {
        // Monitor total speeds
        scope.launch {
            while (isActive) {
                delay(1000)
                // update global stats if needed
            }
        }
    }

    fun startDownload(
        title: String,
        url: String,
        mimeType: String,
        quality: String,
        isAudioOnly: Boolean,
        isPrivate: Boolean = false,
        onQueued: (Long) -> Unit = {}
    ) {
        scope.launch {
            val downloadDir = getDownloadDirectory(isPrivate)
            val safeTitle = title.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val extension = if (isAudioOnly) ".mp3" else ".mp4"
            val targetFile = File(downloadDir, "${safeTitle}_${System.currentTimeMillis()}$extension")

            val entity = DownloadTaskEntity(
                title = title,
                url = url,
                mimeType = mimeType,
                localFilePath = targetFile.absolutePath,
                totalBytes = 0,
                downloadedBytes = 0,
                status = "DOWNLOADING",
                quality = quality,
                isAudioOnly = isAudioOnly,
                isPrivate = isPrivate
            )
            val newId = database.downloadDao().insertTask(entity)
            onQueued(newId)
            executeDownload(newId)
        }
    }

    fun resumeDownload(taskId: Long) {
        scope.launch {
            val task = database.downloadDao().getTaskById(taskId) ?: return@launch
            database.downloadDao().updateProgress(
                id = taskId,
                status = "DOWNLOADING",
                downloaded = task.downloadedBytes,
                total = task.totalBytes,
                speed = 0,
                eta = 0
            )
            executeDownload(taskId)
        }
    }

    fun pauseDownload(taskId: Long) {
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)
        scope.launch {
            val task = database.downloadDao().getTaskById(taskId)
            if (task != null) {
                database.downloadDao().updateProgress(
                    id = taskId,
                    status = "PAUSED",
                    downloaded = task.downloadedBytes,
                    total = task.totalBytes,
                    speed = 0,
                    eta = 0
                )
            }
        }
    }

    fun cancelDownload(taskId: Long) {
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)
        scope.launch {
            val task = database.downloadDao().getTaskById(taskId)
            if (task != null) {
                val file = File(task.localFilePath)
                if (file.exists()) {
                    file.delete()
                }
                database.downloadDao().deleteTaskById(taskId)
            }
        }
    }

    fun deleteFileAndTask(taskId: Long) {
        cancelDownload(taskId)
    }

    private fun executeDownload(taskId: Long) {
        activeJobs[taskId]?.cancel()

        val job = scope.launch {
            var task = database.downloadDao().getTaskById(taskId) ?: return@launch
            val file = File(task.localFilePath)
            val existingLength = if (file.exists()) file.length() else 0L

            try {
                val requestBuilder = Request.Builder()
                    .url(task.url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; VidGet Mobile)")

                if (existingLength > 0) {
                    requestBuilder.header("Range", "bytes=$existingLength-")
                }

                val response = client.newCall(requestBuilder.build()).execute()
                if (!response.isSuccessful && response.code != 206) {
                    database.downloadDao().updateProgress(
                        taskId, "FAILED", existingLength, task.totalBytes, 0, 0
                    )
                    return@launch
                }

                val body = response.body ?: throw IllegalStateException("Empty response body")
                val responseContentLength = body.contentLength()
                val totalLength = if (response.code == 206) {
                    existingLength + responseContentLength
                } else {
                    responseContentLength
                }

                val append = response.code == 206 && existingLength > 0
                val randomAccessFile = RandomAccessFile(file, "rw")
                if (append) {
                    randomAccessFile.seek(existingLength)
                } else {
                    randomAccessFile.setLength(0)
                }

                val inputStream: InputStream = body.byteStream()
                val buffer = ByteArray(32 * 1024)
                var bytesRead: Int
                var currentDownloaded = if (append) existingLength else 0L
                var lastTime = System.currentTimeMillis()
                var bytesSinceLastSample = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (!isActive) break

                    randomAccessFile.write(buffer, 0, bytesRead)
                    currentDownloaded += bytesRead
                    bytesSinceLastSample += bytesRead

                    val now = System.currentTimeMillis()
                    val deltaMs = now - lastTime
                    if (deltaMs >= 400) {
                        val speedBytesPerSec = if (deltaMs > 0) (bytesSinceLastSample * 1000) / deltaMs else 0
                        val remainingBytes = if (totalLength > currentDownloaded) totalLength - currentDownloaded else 0
                        val etaSec = if (speedBytesPerSec > 0) remainingBytes / speedBytesPerSec else 0

                        database.downloadDao().updateProgress(
                            id = taskId,
                            status = "DOWNLOADING",
                            downloaded = currentDownloaded,
                            total = totalLength,
                            speed = speedBytesPerSec,
                            eta = etaSec
                        )
                        lastTime = now
                        bytesSinceLastSample = 0
                    }
                }

                randomAccessFile.close()
                inputStream.close()

                if (isActive) {
                    database.downloadDao().updateProgress(
                        id = taskId,
                        status = "COMPLETED",
                        downloaded = file.length(),
                        total = file.length(),
                        speed = 0,
                        eta = 0
                    )
                }
            } catch (e: CancellationException) {
                // Job was cancelled by pause/cancel
            } catch (e: Exception) {
                e.printStackTrace()
                database.downloadDao().updateProgress(
                    id = taskId,
                    status = "FAILED",
                    downloaded = if (file.exists()) file.length() else 0,
                    total = task.totalBytes,
                    speed = 0,
                    eta = 0
                )
            } finally {
                activeJobs.remove(taskId)
            }
        }

        activeJobs[taskId] = job
    }

    private fun getDownloadDirectory(isPrivate: Boolean): File {
        val baseDir = if (isPrivate) {
            File(context.filesDir, "vault_private")
        } else {
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        }
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        return baseDir
    }
}
