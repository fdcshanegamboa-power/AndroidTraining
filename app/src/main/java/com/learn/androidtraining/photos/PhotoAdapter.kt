package com.learn.androidtraining.photos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.learn.androidtraining.databinding.ItemPhotoBinding

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

            Glide.with(binding.root)
                .load(item.imageUrl.ifBlank { null }) // don't load empty URLs
                .into(binding.imageThumbnail)
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