package com.learn.androidtraining.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.learn.androidtraining.photos.PhotoItem
import kotlinx.coroutines.tasks.await

class PhotoRepository {
    private val tag = "PhotoRepository"
    private val collectionRef = FirebaseFirestore.getInstance().collection("photos")

    suspend fun savePhoto(photo: PhotoItem): Result<Unit> = runCatching {
        collectionRef.document(photo.id).set(photo).await()
    }

    suspend fun getAllPhotos(userId: String): Result<List<PhotoItem>> = runCatching {
        collectionRef
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(PhotoItem::class.java)
    }.onFailure { Log.e(tag, "getAllPhotos failed", it) }

    suspend fun deletePhoto(photoId: String, userId: String): Result<Unit> = runCatching {
        val doc = collectionRef.document(photoId).get().await()
        val photo = doc.toObject(PhotoItem::class.java)

        require(photo?.userId == userId) { "Unauthorized delete attempt" }

        collectionRef.document(photoId).delete().await()
    }
}