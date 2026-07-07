package com.example.imageloader.cache

import android.graphics.Bitmap
import android.util.LruCache

/**
 * In-memory cache for Bitmaps using LruCache.
 */
class MemoryCache {
    // Use 1/8th of the available memory for this cache
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val cache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    /**
     * Puts a bitmap into the cache.
     */
    fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }

    /**
     * Gets a bitmap from the cache.
     */
    fun get(url: String): Bitmap? {
        return cache.get(url)
    }

    /**
     * Clears all entries from the memory cache.
     */
    fun clear() {
        cache.evictAll()
    }
}
