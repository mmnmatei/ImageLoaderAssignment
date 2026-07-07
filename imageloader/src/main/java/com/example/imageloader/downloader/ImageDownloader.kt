package com.example.imageloader.downloader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.imageloader.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Handles downloading images from a URL using OkHttp.
 */
class ImageDownloader {
    private val client = OkHttpClient()

    /**
     * Downloads an image from the given URL and returns it as a Bitmap.
     * Downsamples the image to fit the target dimensions.
     * @throws IOException if the download fails or response is invalid.
     */
    suspend fun download(url: String, reqWidth: Int, reqHeight: Int): Bitmap = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val bytes = response.body?.bytes() ?: throw IOException("Empty response body")
            
            // 1. Decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            // 2. Calculate inSampleSize
            options.inSampleSize = BitmapUtils.calculateInSampleSize(options, reqWidth, reqHeight)

            // 3. Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                ?: throw IOException("Failed to decode bitmap")
        }
    }
}
