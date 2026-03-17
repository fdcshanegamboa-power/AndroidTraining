package com.learn.androidtraining.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learn.androidtraining.photos.PhotoItem
import com.learn.androidtraining.photos.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoItem)

    @Query("SELECT * FROM photos WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllPhotosForUser(userId: String): Flow<List<PhotoItem>>

    @Query("SELECT * FROM photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: String): PhotoItem?

    @Delete
    suspend fun deletePhoto(photo: PhotoItem)

    @Query("DELETE FROM photos WHERE userId = :userId")
    suspend fun deleteAllPhotosForUser(userId: String)

    // Sync-related queries
    @Query("SELECT * FROM photos WHERE syncStatus = 'PENDING' AND userId = :userId")
    suspend fun getPendingPhotos(userId: String): List<PhotoItem>

    @Query("UPDATE photos SET syncStatus = :status, imageUrl = :cloudinaryUrl, lastSyncedAt = :lastSyncedAt WHERE id = :photoId")
    suspend fun updateSyncStatus(photoId: String, status: SyncStatus, cloudinaryUrl: String, lastSyncedAt: Long)

    @Query("UPDATE photos SET syncStatus = :status WHERE id = :photoId")
    suspend fun updateSyncStatusOnly(photoId: String, status: SyncStatus)
}
