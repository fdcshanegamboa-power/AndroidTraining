package com.learn.androidtraining

import android.app.Application
import com.cloudinary.android.MediaManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MediaManager.init(this, mapOf(
            "cloud_name" to "dr6qp4dcs",
            "upload_preset" to "android_training"
        ))
    }
}