package com.example.imageloader.loader

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.example.imageloader.cache.DiskCache
import com.example.imageloader.cache.MemoryCache
import com.example.imageloader.downloader.ImageDownloader
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Main entry point for the image loading library.
 * Exposes a simple API for loading images into ImageViews with caching.
 */
object ImageLoader {
    private val memoryCache = MemoryCache()
    private var diskCache: DiskCache? = null
    private val downloader = ImageDownloader()

    // Scope for image loading jobs
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Keeps track of active UI update jobs for each ImageView to ensure RecyclerView safety
    private val uiJobs = Collections.synchronizedMap(WeakHashMap<ImageView, Job>())

    // Prevents duplicate downloads for the same URL
    private val downloadJobs = ConcurrentHashMap<String, Deferred<Bitmap>>()

    /**
     * Initializes the ImageLoader with a context.
     * Required for disk caching.
     */
    private fun initDiskCache(context: Context) {
        if (diskCache == null) {
            diskCache = DiskCache(context.applicationContext)
        }
    }

    /**
     * Loads an image from the given URL into the target ImageView.
     * @param url The image URL to load.
     * @param placeholder The drawable resource ID to show while loading.
     * @param target The ImageView where the image will be displayed.
     */
    @JvmStatic
    fun load(url: String, @DrawableRes placeholder: Int, target: ImageView) {
        // Initialize disk cache if not already done
        initDiskCache(target.context)

        // Cancel any existing UI job for this ImageView to prevent incorrect images while scrolling
        uiJobs[target]?.cancel()

        // Show placeholder
        target.setImageResource(placeholder)

        // Check memory cache first - if found, display immediately without animation
        val cachedBitmap = memoryCache.get(url)
        if (cachedBitmap != null) {
            target.alpha = 1f // Reset alpha in case of recycling
            target.setImageBitmap(cachedBitmap)
            return
        }

        // Determine target dimensions for downsampling
        val reqWidth = if (target.width > 0) target.width else target.resources.displayMetrics.widthPixels
        val reqHeight = if (target.height > 0) target.height else target.resources.displayMetrics.heightPixels

        // Start a new job for loading from Disk or Network
        val job = scope.launch {
            try {
                // 1. Check Disk Cache with requested dimensions
                var bitmap = diskCache?.get(url, reqWidth, reqHeight)

                if (bitmap != null) {
                    // Store in memory cache for faster access next time
                    memoryCache.put(url, bitmap)
                } else {
                    // 2. Download from Network with Deduplication and requested dimensions
                    val deferred = downloadJobs.getOrPut(url) {
                        async(Dispatchers.IO) {
                            try {
                                downloader.download(url, reqWidth, reqHeight).also { downloaded ->
                                    // Save to Disk and Memory cache upon successful download
                                    diskCache?.put(url, downloaded)
                                    memoryCache.put(url, downloaded)
                                }
                            } finally {
                                // Remove from active downloads once finished or failed
                                downloadJobs.remove(url)
                            }
                        }
                    }
                    bitmap = deferred.await()
                }

                // Display the image on the main thread with a fade-in animation
                if (isActive) {
                    target.alpha = 0f
                    target.setImageBitmap(bitmap)
                    target.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                }
            } catch (e: Exception) {
                // In case of error, the placeholder remains
                e.printStackTrace()
            } finally {
                // Clean up the UI job mapping
                if (uiJobs[target] == coroutineContext[Job]) {
                    uiJobs.remove(target)
                }
            }
        }

        uiJobs[target] = job
    }

    /**
     * Clears both memory and disk caches.
     */
    @JvmStatic
    fun clearCache() {
        memoryCache.clear()
        diskCache?.clear()
        // Cancel all pending downloads if any
        downloadJobs.forEach { (_, deferred) -> deferred.cancel() }
        downloadJobs.clear()
    }
}
