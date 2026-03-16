package com.learn.androidtraining.photos

data class PhotoItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val date: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L
)