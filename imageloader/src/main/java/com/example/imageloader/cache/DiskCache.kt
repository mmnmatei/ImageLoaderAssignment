package com.example.imageloader.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.imageloader.utils.BitmapUtils
import com.example.imageloader.utils.DefaultTimeProvider
import com.example.imageloader.utils.HashUtils
import com.example.imageloader.utils.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Disk-based cache for Bitmaps stored in the application's cache directory.
 */
class DiskCache(
    context: Context,
    private val timeProvider: TimeProvider = DefaultTimeProvider
) {
    private val cacheDir = File(context.cacheDir, "image_loader_cache")
    private val cacheDurationMillis = TimeUnit.HOURS.toMillis(4)

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    /**
     * Saves a bitmap to the disk cache.
     */
    suspend fun put(url: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val fileName = HashUtils.md5(url)
        val file = File(cacheDir, fileName)
        try {
            FileOutputStream(file).use { out ->
                // Use PNG for lossless or JPEG for smaller files. Requirements didn't specify.
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves a bitmap from the disk cache if it exists and is not expired.
     * Downsamples the image to fit the target dimensions.
     */
    suspend fun get(url: String, reqWidth: Int, reqHeight: Int): Bitmap? = withContext(Dispatchers.IO) {
        val fileName = HashUtils.md5(url)
        val file = File(cacheDir, fileName)

        if (file.exists()) {
            val lastModified = file.lastModified()
            val now = timeProvider.currentTimeMillis()

            if (now - lastModified <= cacheDurationMillis) {
                return@withContext decodeSampledBitmapFromFile(file.absolutePath, reqWidth, reqHeight)
            } else {
                // Cache expired, delete file
                file.delete()
            }
        }
        return@withContext null
    }

    private fun decodeSampledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        // 1. Decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        // 2. Calculate inSampleSize
        options.inSampleSize = BitmapUtils.calculateInSampleSize(options, reqWidth, reqHeight)

        // 3. Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    /**
     * Clears all cached files in the disk cache directory.
     */
    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}

