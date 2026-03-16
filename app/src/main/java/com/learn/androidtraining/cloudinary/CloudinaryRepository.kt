package com.learn.androidtraining.cloudinary

import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import com.cloudinary.android.callback.UploadCallback
import com.learn.androidtraining.BuildConfig
import java.io.File
import kotlin.coroutines.resume

class CloudinaryRepository {

    private val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
    private val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
    private val uploadUrl = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

    suspend fun uploadImage(file: File, photoId: String): Result<String> =
        suspendCancellableCoroutine { cont ->
            Log.d("CloudinaryRepository", "Uploading image: ${file.path} with photoId: $photoId")
            MediaManager.get().upload(file.path)
                .unsigned(uploadPreset)
                .option("public_id", photoId)
                .option("folder", "android_training/photos")
                .callback(object : UploadCallback {
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        cont.resume(Result.success(resultData["secure_url"] as String))
                    }
                    override fun onError(requestId: String?, error: ErrorInfo) {
                        cont.resume(Result.failure(Exception(error.description)))
                    }
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        }
}