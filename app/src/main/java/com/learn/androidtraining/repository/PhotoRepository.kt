package com.learn.androidtraining.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.learn.androidtraining.cloudinary.CloudinaryRepository
import com.learn.androidtraining.database.AppDatabase
import com.learn.androidtraining.firebase.FirebasePhotoRepository
import com.learn.androidtraining.photos.PhotoItem
import com.learn.androidtraining.photos.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PhotoRepository(private val context: Context) {
    private val tag = "PhotoRepository"
    private val photoDao = AppDatabase.getDatabase(context).photoDao()
    private val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
    private val cloudinaryRepository = CloudinaryRepository(context)
    private val firebaseRepository = FirebasePhotoRepository()

    /**
     * Save photo locally (instant) and trigger background cloud upload
     * Strategy: Room as cache, Cloud as source of truth
     */
    suspend fun savePhoto(photo: PhotoItem, bitmap: Bitmap): Result<Unit> = runCatching {
        // 1. Save bitmap to local file immediately
        val file = File(photosDir, "${photo.id}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        Log.d(tag, "savePhoto: saved bitmap to ${file.absolutePath}")

        // 2. Insert into Room with PENDING status (instant UI update)
        val photoWithLocalPath = photo.copy(
            localFilePath = file.absolutePath,
            imageUrl = "", // Cloudinary URL will be set after upload
            syncStatus = SyncStatus.PENDING
        )
        photoDao.insertPhoto(photoWithLocalPath)
        Log.d(tag, "savePhoto: inserted into Room photoId=${photo.id} with PENDING status")

        // 3. Trigger background upload to cloud (non-blocking)
        CoroutineScope(Dispatchers.IO).launch {
            uploadToCloud(photo.id, file)
        }

        Unit
    }.onFailure {
        Log.e(tag, "savePhoto failed for photoId=${photo.id}", it)
    }

    /**
     * Background upload to Cloudinary + Firebase
     */
    private suspend fun uploadToCloud(photoId: String, file: File) {
        try {
            Log.d(tag, "uploadToCloud: starting upload for photoId=$photoId")

            // Upload to Cloudinary
            cloudinaryRepository.uploadPhoto(file).fold(
                onSuccess = { cloudinaryUrl ->
                    Log.d(tag, "uploadToCloud: Cloudinary success url=$cloudinaryUrl")

                    // Get photo from Room to save to Firebase
                    val photo = photoDao.getPhotoById(photoId)
                    if (photo != null) {
                        // Save metadata to Firebase
                        val updatedPhoto = photo.copy(
                            imageUrl = cloudinaryUrl,
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncedAt = System.currentTimeMillis()
                        )
                        firebaseRepository.savePhoto(updatedPhoto).fold(
                            onSuccess = {
                                // Update Room with synced status
                                photoDao.updateSyncStatus(
                                    photoId = photoId,
                                    status = SyncStatus.SYNCED,
                                    cloudinaryUrl = cloudinaryUrl,
                                    lastSyncedAt = System.currentTimeMillis()
                                )
                                Log.d(tag, "uploadToCloud: complete sync for photoId=$photoId")

                                // Delete local file after successful upload
                                if (file.exists()) {
                                    file.delete()
                                    Log.d(tag, "uploadToCloud: deleted local file ${file.absolutePath}")
                                }
                            },
                            onFailure = { e ->
                                Log.e(tag, "uploadToCloud: Firebase save failed", e)
                                photoDao.updateSyncStatusOnly(photoId, SyncStatus.FAILED)
                            }
                        )
                    }
                },
                onFailure = { e ->
                    Log.e(tag, "uploadToCloud: Cloudinary upload failed", e)
                    photoDao.updateSyncStatusOnly(photoId, SyncStatus.FAILED)
                }
            )
        } catch (e: Exception) {
            Log.e(tag, "uploadToCloud: unexpected error", e)
            photoDao.updateSyncStatusOnly(photoId, SyncStatus.FAILED)
        }
    }

    /**
     * Get all photos for a user - returns Flow for reactive UI updates
     * On app start, syncs from cloud (cloud as source of truth)
     */
    fun getAllPhotosForUser(userId: String): Flow<List<PhotoItem>> {
        Log.d(tag, "getAllPhotosForUser: userId=$userId")

        // Trigger background sync from cloud
        CoroutineScope(Dispatchers.IO).launch {
            syncFromCloud(userId)
        }

        return photoDao.getAllPhotosForUser(userId)
    }

    /**
     * Sync photos from Firebase (cloud as source of truth)
     */
    private suspend fun syncFromCloud(userId: String) {
        try {
            Log.d(tag, "syncFromCloud: fetching from Firebase for userId=$userId")
            firebaseRepository.getAllPhotos(userId).fold(
                onSuccess = { cloudPhotos ->
                    Log.d(tag, "syncFromCloud: received ${cloudPhotos.size} photos from cloud")
                    cloudPhotos.forEach { cloudPhoto ->
                        // Check if photo exists in Room
                        val localPhoto = photoDao.getPhotoById(cloudPhoto.id)
                        if (localPhoto == null) {
                            // New photo from cloud - insert into Room
                            photoDao.insertPhoto(cloudPhoto)
                            Log.d(tag, "syncFromCloud: inserted new photo ${cloudPhoto.id}")
                        } else if (cloudPhoto.lastSyncedAt ?: 0 > localPhoto.lastSyncedAt ?: 0) {
                            // Cloud has newer version - update local (cloud wins)
                            photoDao.insertPhoto(cloudPhoto.copy(localFilePath = localPhoto.localFilePath))
                            Log.d(tag, "syncFromCloud: updated photo ${cloudPhoto.id} (cloud wins)")
                        }
                    }
                },
                onFailure = { e ->
                    Log.e(tag, "syncFromCloud: failed", e)
                }
            )
        } catch (e: Exception) {
            Log.e(tag, "syncFromCloud: unexpected error", e)
        }
    }

    /**
     * Delete a photo - handles both synced and pending photos
     * Strategy: Check sync status first, then delete accordingly
     */
    suspend fun deletePhoto(photo: PhotoItem): Result<Unit> = runCatching {
        Log.d(tag, "deletePhoto: starting delete for photoId=${photo.id}, syncStatus=${photo.syncStatus}")

        // Check if photo is synced to cloud or still pending
        when (photo.syncStatus) {
            SyncStatus.SYNCED -> {
                // Photo is in cloud - delete from cloud first, then local
                Log.d(tag, "deletePhoto: photo is SYNCED, deleting from cloud first")

                // 1. Delete from Firebase (cloud first)
                firebaseRepository.deletePhoto(photo.id, photo.userId).fold(
                    onSuccess = {
                        Log.d(tag, "deletePhoto: deleted from Firebase")
                    },
                    onFailure = { e ->
                        Log.w(tag, "deletePhoto: Firebase delete failed (may not exist), continuing: ${e.message}")
                    }
                )

                // 2. Delete from Cloudinary (if uploaded)
                if (photo.imageUrl.isNotEmpty()) {
                    cloudinaryRepository.deletePhoto(photo.id)
                    Log.d(tag, "deletePhoto: deleted from Cloudinary")
                }
            }
            SyncStatus.PENDING, SyncStatus.FAILED -> {
                // Photo is only local - skip cloud deletion
                Log.d(tag, "deletePhoto: photo is ${photo.syncStatus}, skipping cloud deletion")
            }
        }

        // 3. Delete from Room (always)
        photoDao.deletePhoto(photo)
        Log.d(tag, "deletePhoto: deleted from Room")

        // 4. Delete local file (if exists)
        if (photo.localFilePath.isNotEmpty()) {
            val localFile = File(photo.localFilePath)
            if (localFile.exists()) {
                localFile.delete()
                Log.d(tag, "deletePhoto: deleted local file")
            }
        }

        Unit
    }.onFailure {
        Log.e(tag, "deletePhoto failed for photoId=${photo.id}", it)
    }

    /**
     * Delete all photos for a user (useful for logout/cleanup)
     */
    suspend fun deleteAllPhotosForUser(userId: String): Result<Unit> = runCatching {
        photoDao.deleteAllPhotosForUser(userId)
        Log.d(tag, "deleteAllPhotosForUser: deleted all photos for userId=$userId")
        Unit
    }.onFailure {
        Log.e(tag, "deleteAllPhotosForUser failed for userId=$userId", it)
    }

    /**
     * Retry failed uploads
     */
    suspend fun retryFailedUploads(userId: String) {
        val pendingPhotos = photoDao.getPendingPhotos(userId)
        val failedPhotos = photoDao.getAllPhotosForUser(userId)
        // TODO: Implement retry logic
        Log.d(tag, "retryFailedUploads: found ${pendingPhotos.size} pending photos")
    }

    /**
     * Clear all photos from database and delete all local files
     * Useful for testing or resetting the app
     */
    suspend fun clearAllData(): Result<Unit> = runCatching {
        Log.d(tag, "clearAllData: starting database and file cleanup")

        // Delete all local photo files
        val photoFiles = photosDir.listFiles()
        photoFiles?.forEach { file ->
            if (file.isFile && file.extension == "jpg") {
                file.delete()
                Log.d(tag, "clearAllData: deleted file ${file.name}")
            }
        }

        // Clear database
        AppDatabase.clearDatabase(context)
        Log.d(tag, "clearAllData: database cleared successfully")

        Unit
    }.onFailure {
        Log.e(tag, "clearAllData: failed", it)
    }

    /**
     * Complete database reset (destroys and recreates)
     * Use only for major issues or testing
     */
    fun resetDatabase() {
        Log.d(tag, "resetDatabase: resetting database")
        AppDatabase.resetDatabase(context)

        // Also delete all local files
        val photoFiles = photosDir.listFiles()
        photoFiles?.forEach { file ->
            if (file.isFile && file.extension == "jpg") {
                file.delete()
            }
        }
        Log.d(tag, "resetDatabase: database reset complete")
    }
}
