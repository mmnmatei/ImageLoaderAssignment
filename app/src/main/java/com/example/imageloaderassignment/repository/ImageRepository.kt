package com.example.imageloaderassignment.repository

import com.example.imageloaderassignment.model.ImageItem
import com.example.imageloaderassignment.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling image data operations.
 */
class ImageRepository(private val apiService: ApiService) {
    /**
     * Fetches images from the API on the IO dispatcher.
     */
    suspend fun getImages(): Result<List<ImageItem>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getImages()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
