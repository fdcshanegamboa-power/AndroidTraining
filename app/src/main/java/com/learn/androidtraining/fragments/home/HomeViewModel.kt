package com.learn.androidtraining.fragments.home

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.learn.androidtraining.cloudinary.CloudinaryRepository
import com.learn.androidtraining.firebase.PhotoRepository
import com.learn.androidtraining.photos.PhotoItem
import com.learn.androidtraining.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val photos: List<PhotoItem> = emptyList(),
    val lastPhotoUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val photoRepository = PhotoRepository()
    private val cloudinaryRepository = CloudinaryRepository()
    private val dataStoreManager = DataStoreManager.getInstance(application)
    private val _uiState = MutableStateFlow(HomeUiState())
    private val tag = "HomeViewModel"
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
        loadLastPhotoUrl()
    }

    private fun loadPhotos() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w(tag, "loadPhotos: user is not logged in")
            return
        }
        Log.d(tag, "loadPhotos: fetching photos for userId=$userId")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            photoRepository.getAllPhotos(userId).fold(
                onSuccess = { photos ->
                    Log.d(tag, "loadPhotos: success, count=${photos.size}")
                    _uiState.update { it.copy(photos = photos, isLoading = false) }
                },
                onFailure = { e ->
                    Log.e(tag, "loadPhotos: failed", e)
                    _uiState.update { it.copy(errorMessage = "Failed to load photos", isLoading = false) }
                }
            )
        }
    }

    private fun loadLastPhotoUrl() {
        viewModelScope.launch {
            val url = dataStoreManager.getLastPhotoUrl()
            _uiState.update { it.copy(lastPhotoUrl = url) }
        }
    }

    fun uploadPhoto(bitmap: Bitmap, cacheDir: File) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w(tag, "uploadPhoto: user is not logged in")
            return
        }
        val photoId = System.currentTimeMillis().toString()
        val timestamp = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date())
        val fileName = "photo_$photoId.jpg"
        Log.d(tag, "uploadPhoto: starting upload photoId=$photoId userId=$userId")

        val tempPhoto = PhotoItem(
            id = photoId,
            userId = userId,
            name = fileName,
            date = timestamp,
            imageUrl = "",
            timestamp = System.currentTimeMillis(),
        )

        _uiState.update { it.copy(photos = listOf(tempPhoto) + it.photos) }

        viewModelScope.launch {
            val file = bitmapToFile(bitmap, fileName, cacheDir)
            Log.d(tag, "uploadPhoto: bitmap written to file=${file.absolutePath}")

            cloudinaryRepository.uploadImage(file, photoId).fold(
                onSuccess = { imageUrl ->
                    Log.d(tag, "uploadPhoto: cloudinary success imageUrl=$imageUrl")
                    val photo = tempPhoto.copy(imageUrl = imageUrl)
                    photoRepository.savePhoto(photo).fold(
                        onSuccess = {
                            Log.d(tag, "uploadPhoto: firestore save success photoId=$photoId")
                            _uiState.update { state ->
                                state.copy(
                                    photos = state.photos.map { if (it.id == photoId) photo else it },
                                    lastPhotoUrl = imageUrl,
                                )
                            }
                            dataStoreManager.saveLastPhotoUrl(imageUrl)
                        },
                        onFailure = { e ->
                            Log.e(tag, "uploadPhoto: firestore save failed", e)
                            _uiState.update { state ->
                                state.copy(
                                    errorMessage = "Failed to save photo",
                                    photos = state.photos.filterNot { it.id == photoId },
                                )
                            }
                        }
                    )
                },
                onFailure = { e ->
                    Log.e(tag, "uploadPhoto: cloudinary upload failed", e)
                    _uiState.update { state ->
                        state.copy(
                            errorMessage = "Upload failed",
                            photos = state.photos.filterNot { it.id == photoId },
                        )
                    }
                }
            )
        }
    }

    fun deletePhoto(photo: PhotoItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w(tag, "deletePhoto: user is not logged in")
            return
        }
        Log.d(tag, "deletePhoto: deleting photoId=${photo.id} userId=$userId")
        viewModelScope.launch {
            photoRepository.deletePhoto(photo.id, userId).fold(
                onSuccess = {
                    Log.d(tag, "deletePhoto: success photoId=${photo.id}")
                    _uiState.update { state ->
                        state.copy(photos = state.photos.filterNot { it.id == photo.id })
                    }
                },
                onFailure = { e ->
                    Log.e(tag, "deletePhoto: failed photoId=${photo.id}", e)
                    _uiState.update { it.copy(errorMessage = "Failed to delete photo") }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun bitmapToFile(bitmap: Bitmap, fileName: String, cacheDir: File): File {
        val file = File(cacheDir, fileName)
        file.outputStream().use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }
}