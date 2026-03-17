package com.learn.androidtraining.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learn.androidtraining.photos.PhotoItem
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
}

