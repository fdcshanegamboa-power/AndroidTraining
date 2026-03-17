package com.learn.androidtraining.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.learn.androidtraining.database.AppDatabase
import com.learn.androidtraining.photos.PhotoItem
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

class PhotoRepository(context: Context) {
    private val tag = "PhotoRepository"
    private val photoDao = AppDatabase.getDatabase(context).photoDao()
    private val photosDir = File(context.filesDir, "photos").apply { mkdirs() }

    /**
     * Save photo bitmap to local storage and insert metadata into Room database
     */
    suspend fun savePhoto(photo: PhotoItem, bitmap: Bitmap): Result<Unit> = runCatching {
        // Save bitmap to file
        val file = File(photosDir, "${photo.id}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        Log.d(tag, "savePhoto: saved bitmap to ${file.absolutePath}")

        // Update photo with local file path
        val photoWithPath = photo.copy(imageUrl = file.absolutePath)

        // Insert into database
        photoDao.insertPhoto(photoWithPath)
        Log.d(tag, "savePhoto: inserted into database photoId=${photo.id}")
        Unit // Explicitly return Unit
    }.onFailure {
        Log.e(tag, "savePhoto failed for photoId=${photo.id}", it)
    }

    /**
     * Get all photos for a specific user as a Flow
     */
    fun getAllPhotosForUser(userId: String): Flow<List<PhotoItem>> {
        Log.d(tag, "getAllPhotosForUser: userId=$userId")
        return photoDao.getAllPhotosForUser(userId)
    }

    /**
     * Delete a photo from database and local storage
     */
    suspend fun deletePhoto(photo: PhotoItem): Result<Unit> = runCatching {
        // Delete from database
        photoDao.deletePhoto(photo)
        Log.d(tag, "deletePhoto: removed from database photoId=${photo.id}")

        // Delete file from storage
        val file = File(photo.imageUrl)
        if (file.exists()) {
            file.delete()
            Log.d(tag, "deletePhoto: deleted file ${file.absolutePath}")
        }
    }.onFailure {
        Log.e(tag, "deletePhoto failed for photoId=${photo.id}", it)
    }

    /**
     * Delete all photos for a user (useful for logout/cleanup)
     */
    suspend fun deleteAllPhotosForUser(userId: String): Result<Unit> = runCatching {
        photoDao.deleteAllPhotosForUser(userId)
        Log.d(tag, "deleteAllPhotosForUser: deleted all photos for userId=$userId")
        Unit // Explicitly return Unit
    }.onFailure {
        Log.e(tag, "deleteAllPhotosForUser failed for userId=$userId", it)
    }
}

