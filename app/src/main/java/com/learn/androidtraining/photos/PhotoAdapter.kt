package com.learn.androidtraining.photos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.learn.androidtraining.databinding.ItemPhotoBinding
import java.io.File

class PhotoAdapter(
    private val onDeleteClick: (PhotoItem) -> Unit,
) : ListAdapter<PhotoItem, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    inner class PhotoViewHolder(
        private val binding: ItemPhotoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PhotoItem) {
            binding.textPhotoName.text = item.name
            binding.textPhotoDate.text = item.date
            binding.buttonDelete.setOnClickListener { onDeleteClick(item) }

            // Set sync status indicator color (circular badge)
            when (item.syncStatus) {
                SyncStatus.PENDING -> {
                    binding.syncStatusIndicator.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#FFA500")) // Orange
                    binding.syncStatusIndicator.visibility = View.VISIBLE
                }
                SyncStatus.SYNCED -> {
                    binding.syncStatusIndicator.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Green
                    binding.syncStatusIndicator.visibility = View.VISIBLE
                }
                SyncStatus.FAILED -> {
                    binding.syncStatusIndicator.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336")) // Red
                    binding.syncStatusIndicator.visibility = View.VISIBLE
                }
            }

            // Load image - prioritize Cloudinary URL, fallback to local file
            val imageSource = when {
                item.imageUrl.isNotEmpty() && item.syncStatus == SyncStatus.SYNCED -> {
                    // Use Cloudinary URL
                    item.imageUrl
                }
                item.localFilePath.isNotEmpty() -> {
                    // Use local file
                    File(item.localFilePath)
                }
                else -> null
            }

            if (imageSource != null) {
                Glide.with(binding.root)
                    .load(imageSource)
                    .into(binding.imageThumbnail)
            } else {
                binding.imageThumbnail.setImageDrawable(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

