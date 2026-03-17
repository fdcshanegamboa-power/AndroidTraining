package com.learn.androidtraining.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.learn.androidtraining.photos.PhotoItem
import com.learn.androidtraining.photos.SyncStatus
import kotlinx.coroutines.tasks.await

class FirebasePhotoRepository {
    private val tag = "FirebasePhotoRepository"
    private val collectionRef = FirebaseFirestore.getInstance().collection("photos")

    /**
     * Save photo metadata to Firestore
     */
    suspend fun savePhoto(photo: PhotoItem): Result<Int> = runCatching {
        val photoData = hashMapOf(
            "id" to photo.id,
            "userId" to photo.userId,
            "name" to photo.name,
            "date" to photo.date,
            "imageUrl" to photo.imageUrl,
            "timestamp" to photo.timestamp,
            "syncStatus" to photo.syncStatus.name,
            "lastSyncedAt" to photo.lastSyncedAt
        )
        collectionRef.document(photo.id).set(photoData).await()
        Log.d(tag, "savePhoto: saved to Firestore photoId=${photo.id}")
    }.onFailure {
        Log.e(tag, "savePhoto: failed photoId=${photo.id}", it)
    }

    /**
     * Get all photos for a user from Firestore (cloud as source of truth)
     */
    suspend fun getAllPhotos(userId: String): Result<List<PhotoItem>> = runCatching {
        val snapshot = collectionRef
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val photos = snapshot.documents.mapNotNull { doc ->
            try {
                PhotoItem(
                    id = doc.getString("id") ?: return@mapNotNull null,
                    userId = doc.getString("userId") ?: return@mapNotNull null,
                    name = doc.getString("name") ?: "",
                    date = doc.getString("date") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    localFilePath = "", // Will be populated by local repository
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    syncStatus = SyncStatus.valueOf(doc.getString("syncStatus") ?: "SYNCED"),
                    lastSyncedAt = doc.getLong("lastSyncedAt")
                )
            } catch (e: Exception) {
                Log.e(tag, "getAllPhotos: failed to parse document ${doc.id}", e)
                null
            }
        }
        Log.d(tag, "getAllPhotos: fetched ${photos.size} photos for userId=$userId")
        photos
    }.onFailure {
        Log.e(tag, "getAllPhotos: failed for userId=$userId", it)
    }

    /**
     * Delete a photo from Firestore
     * Handles cases where document may not exist (pending photos)
     */
    suspend fun deletePhoto(photoId: String, userId: String): Result<Unit> = runCatching {
        val doc = collectionRef.document(photoId).get().await()

        // Check if document exists
        if (!doc.exists()) {
            Log.w(tag, "deletePhoto: document doesn't exist photoId=$photoId (likely pending photo)")
            return@runCatching Unit
        }

        val photoUserId = doc.getString("userId")

        // Verify user owns this photo
        if (photoUserId != userId) {
            throw SecurityException("User $userId is not authorized to delete photo $photoId")
        }

        // Delete the document
        collectionRef.document(photoId).delete().await()
        Log.d(tag, "deletePhoto: deleted from Firestore photoId=$photoId")
        Unit // Explicitly return Unit
    }.onFailure {
        Log.e(tag, "deletePhoto: failed photoId=$photoId", it)
    }
}

