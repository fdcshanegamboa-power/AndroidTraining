package com.learn.androidtraining.cloudinary

import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.learn.androidtraining.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class CloudinaryRepository(context: Context) {
    private val tag = "CloudinaryRepository"

    init {
        try {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to "",
                "api_secret" to ""
            )
            MediaManager.init(context, config)
            Log.d(tag, "Cloudinary MediaManager initialized")
        } catch (_: IllegalStateException) {
            // Already initialized, ignore
            Log.d(tag, "Cloudinary MediaManager already initialized")
        }
    }

    /**
     * Upload a photo file to Cloudinary and return the secure URL
     */
    suspend fun uploadPhoto(file: File, uploadPreset: String = BuildConfig.CLOUDINARY_UPLOAD_PRESET): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            Log.d(tag, "uploadPhoto: starting upload for ${file.name}")

            val requestId = MediaManager.get()
                .upload(file.absolutePath)
                .unsigned(uploadPreset)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(tag, "uploadPhoto: upload started requestId=$requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes.toDouble() / totalBytes.toDouble() * 100).toInt()
                        Log.d(tag, "uploadPhoto: progress=$progress% ($bytes/$totalBytes)")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        Log.d(tag, "uploadPhoto: success url=$secureUrl")
                        if (secureUrl != null) {
                            continuation.resume(Result.success(secureUrl))
                        } else {
                            continuation.resume(Result.failure(Exception("No secure_url in response")))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e(tag, "uploadPhoto: error code=${error.code} desc=${error.description}")
                        continuation.resume(Result.failure(Exception("Cloudinary upload failed: ${error.description}")))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w(tag, "uploadPhoto: rescheduled")
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                Log.d(tag, "uploadPhoto: cancelled, cancelling request $requestId")
                MediaManager.get().cancelRequest(requestId)
            }
        }
    }

    /**
     * Delete a photo from Cloudinary by public ID
     *
     * NOTE: The Cloudinary Android SDK does NOT support client-side deletion
     * for unsigned uploads (which we're using for security).
     *
     * Options for deletion:
     * 1. Implement server-side API endpoint that calls Cloudinary Admin API
     * 2. Use Cloudinary's Auto-Delete feature (delete after X days)
     * 3. Manual cleanup through Cloudinary dashboard
     * 4. Keep images indefinitely (free tier: 25GB storage)
     *
     * For now, we're relying on Firebase Firestore as source of truth.
     * Images remain in Cloudinary but won't be accessible through the app.
     */
    fun deletePhoto(publicId: String): Result<Unit> {
        return try {
            Log.d(tag, "deletePhoto: publicId=$publicId")
            Log.d(tag, "deletePhoto: Client-side deletion not supported for unsigned uploads")
            Log.d(tag, "deletePhoto: Image remains in Cloudinary but removed from app")
            Log.d(tag, "deletePhoto: Consider implementing server-side deletion or auto-expire")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "deletePhoto: failed for publicId=$publicId", e)
            Result.failure(e)
        }
    }
}

