package com.learn.androidtraining.photos

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus {
    PENDING,    // Yellow - Uploading to cloud
    SYNCED,     // Green - Successfully uploaded
    FAILED      // Red - Upload failed
}

@Entity(tableName = "photos")
data class PhotoItem(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val date: String,
    val imageUrl: String,           // Cloudinary URL (empty until uploaded)
    val localFilePath: String,      // Local file path for offline access
    val timestamp: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncedAt: Long? = null
)