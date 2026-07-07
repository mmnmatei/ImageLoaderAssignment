package com.example.imageloaderassignment.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.imageloader.loader.ImageLoader
import com.example.imageloaderassignment.R
import com.example.imageloaderassignment.databinding.ItemImageBinding
import com.example.imageloaderassignment.model.ImageItem

/**
 * RecyclerView adapter for displaying a list of images.
 */
class ImageAdapter : ListAdapter<ImageItem, ImageAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Binds an image item to the view.
         */
        fun bind(item: ImageItem) {
            binding.imageIdText.text = binding.root.context.getString(R.string.image_id_label, item.id)
            
            // Use the custom ImageLoader library to load the image
            ImageLoader.load(
                url = item.url,
                placeholder = R.drawable.placeholder,
                target = binding.imageView
            )
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ImageItem>() {
        override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem == newItem
        }
    }
}
