package com.example.imageloaderassignment.model

import com.google.gson.annotations.SerializedName

/**
 * Data model for an image item from the API.
 */
data class ImageItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("download_url")
    val url: String
)
