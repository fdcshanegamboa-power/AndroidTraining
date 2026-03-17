package com.learn.androidtraining.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.learn.androidtraining.photos.PhotoItem
import com.learn.androidtraining.photos.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}

@Database(entities = [PhotoItem::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photo_database"
                )
                    .fallbackToDestructiveMigration() // Simple migration strategy
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Clear all data from database (keeps structure, deletes all rows)
         * Useful for logout or testing
         */
        suspend fun clearDatabase(context: Context) {
            getDatabase(context).clearAllTables()
        }

        /**
         * Destroy and recreate the database (full reset)
         * Useful for testing or major data corruption
         */
        fun resetDatabase(context: Context) {
            synchronized(this) {
                INSTANCE?.close()
                context.deleteDatabase("photo_database")
                INSTANCE = null
            }
        }
    }
}

