package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BrowserDao
import com.example.data.local.dao.DownloadDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.DownloadTaskEntity
import com.example.data.local.entity.HistoryEntity

@Database(
    entities = [
        DownloadTaskEntity::class,
        BookmarkEntity::class,
        HistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VidGetDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun browserDao(): BrowserDao

    companion object {
        @Volatile
        private var INSTANCE: VidGetDatabase? = null

        fun getDatabase(context: Context): VidGetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VidGetDatabase::class.java,
                    "vidget_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
