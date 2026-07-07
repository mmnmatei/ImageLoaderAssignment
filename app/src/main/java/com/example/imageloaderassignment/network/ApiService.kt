package com.example.imageloaderassignment.network

import com.example.imageloaderassignment.model.ImageItem
import retrofit2.http.GET

/**
 * Retrofit API interface for fetching images.
 */
interface ApiService {
    /**
     * Fetches a list of images.
     */
    @GET("v2/list")
    suspend fun getImages(): List<ImageItem>
}
