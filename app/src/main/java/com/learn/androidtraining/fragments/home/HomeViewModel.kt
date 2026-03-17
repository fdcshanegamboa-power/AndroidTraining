package com.learn.androidtraining.fragments.home

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.learn.androidtraining.repository.PhotoRepository
import com.learn.androidtraining.photos.PhotoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val photoRepository = PhotoRepository(application)
    private val _uiState = MutableStateFlow(HomeUiState())
    private val tag = "HomeViewModel"
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
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
            photoRepository.getAllPhotosForUser(userId).collect { photos ->
                Log.d(tag, "loadPhotos: received ${photos.size} photos")
                // Set lastPhotoUrl to the most recent photo's imageUrl (Cloudinary) or localFilePath
                val lastUrl = photos.firstOrNull()?.let { photo ->
                    if (photo.imageUrl.isNotEmpty()) photo.imageUrl else photo.localFilePath
                }
                _uiState.update { it.copy(photos = photos, isLoading = false, lastPhotoUrl = lastUrl) }
                Log.d(tag, "loadPhotos: updated lastPhotoUrl=$lastUrl")
            }
        }
    }

    fun uploadPhoto(bitmap: Bitmap) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w(tag, "uploadPhoto: user is not logged in")
            return
        }
        val photoId = System.currentTimeMillis().toString()
        val timestamp = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date())
        val fileName = "photo_$photoId.jpg"
        Log.d(tag, "uploadPhoto: starting save photoId=$photoId userId=$userId")

        viewModelScope.launch {
            val photo = PhotoItem(
                id = photoId,
                userId = userId,
                name = fileName,
                date = timestamp,
                imageUrl = "", // Will be set after Cloudinary upload
                localFilePath = "", // Will be set by repository
                timestamp = System.currentTimeMillis(),
            )

            photoRepository.savePhoto(photo, bitmap).fold(
                onSuccess = {
                    Log.d(tag, "uploadPhoto: save success photoId=$photoId")
                    // Photo list and lastPhotoUrl will auto-update via Flow from loadPhotos()
                },
                onFailure = { e ->
                    Log.e(tag, "uploadPhoto: save failed", e)
                    _uiState.update { state ->
                        state.copy(errorMessage = "Failed to save photo")
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
            photoRepository.deletePhoto(photo).fold(
                onSuccess = {
                    Log.d(tag, "deletePhoto: success photoId=${photo.id}")
                    // Photo list will auto-update via Flow
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


}