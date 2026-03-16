package com.learn.androidtraining.photos

import androidx.recyclerview.widget.DiffUtil

class PhotoDiffCallback : DiffUtil.ItemCallback<PhotoItem>() {
    override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean =
        oldItem == newItem
}