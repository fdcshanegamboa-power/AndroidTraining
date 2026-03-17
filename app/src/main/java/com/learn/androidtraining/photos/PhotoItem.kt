package com.learn.androidtraining.photos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoItem(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val date: String,
    val imageUrl: String, // Local file path
    val timestamp: Long
)