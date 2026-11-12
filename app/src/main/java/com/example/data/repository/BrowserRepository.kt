package com.example.data.repository

import com.example.data.local.VidGetDatabase
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

class BrowserRepository(
    private val database: VidGetDatabase
) {
    val bookmarks: Flow<List<BookmarkEntity>> = database.browserDao().getAllBookmarks()
    val history: Flow<List<HistoryEntity>> = database.browserDao().getRecentHistory()

    suspend fun addBookmark(title: String, url: String, faviconUrl: String? = null) {
        database.browserDao().insertBookmark(BookmarkEntity(title = title, url = url, faviconUrl = faviconUrl))
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) {
        database.browserDao().deleteBookmark(bookmark)
    }

    suspend fun recordHistory(title: String, url: String) {
        database.browserDao().insertHistory(HistoryEntity(title = title, url = url))
    }

    suspend fun clearHistory() {
        database.browserDao().clearHistory()
    }
}
