package com.example.imageloader.cache

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import android.graphics.BitmapFactory
import com.example.imageloader.utils.TimeProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class DiskCacheTest {

    private lateinit var context: Context
    private lateinit var diskCache: DiskCache
    private lateinit var mockTimeProvider: TimeProvider

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockTimeProvider = mockk()
        diskCache = DiskCache(context, mockTimeProvider)
        mockkStatic(BitmapFactory::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(BitmapFactory::class)
    }

    @Test
    fun `get returns bitmap when file is newly cached`() = runBlocking {
        val url = "https://example.com/new.png"
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val startTime = System.currentTimeMillis()
        
        every { mockTimeProvider.currentTimeMillis() } returns startTime
        // BitmapFactory.decodeFile is called within decodeSampledBitmapFromFile
        every { BitmapFactory.decodeFile(any(), any()) } returns bitmap
        every { BitmapFactory.decodeFile(any()) } returns bitmap 
        
        diskCache.put(url, bitmap)
        
        val retrieved = diskCache.get(url, 100, 100)
        assertNotNull(retrieved)
    }

    @Test
    fun `get returns null when file is older than 4 hours`() = runBlocking {
        val url = "https://example.com/old.png"
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val startTime = 1000000L
        
        every { mockTimeProvider.currentTimeMillis() } returns startTime
        diskCache.put(url, bitmap)
        
        val fileName = com.example.imageloader.utils.HashUtils.md5(url)
        val file = File(context.cacheDir, "image_loader_cache/$fileName")
        file.setLastModified(startTime)

        val expiredTime = startTime + TimeUnit.HOURS.toMillis(4) + 1000
        every { mockTimeProvider.currentTimeMillis() } returns expiredTime
        
        val retrieved = diskCache.get(url, 100, 100)
        assertNull(retrieved)
    }

    @Test
    fun `get returns bitmap when file is exactly 4 hours old`() = runBlocking {
        val url = "https://example.com/edge.png"
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val startTime = 1000000L

        every { mockTimeProvider.currentTimeMillis() } returns startTime
        every { BitmapFactory.decodeFile(any(), any()) } returns bitmap
        every { BitmapFactory.decodeFile(any()) } returns bitmap
        diskCache.put(url, bitmap)
        
        val fileName = com.example.imageloader.utils.HashUtils.md5(url)
        val file = File(context.cacheDir, "image_loader_cache/$fileName")
        file.setLastModified(startTime)

        val boundaryTime = startTime + TimeUnit.HOURS.toMillis(4)
        every { mockTimeProvider.currentTimeMillis() } returns boundaryTime

        val retrieved = diskCache.get(url, 100, 100)
        assertNotNull(retrieved)
    }

    @Test
    fun `clear removes disk cache entries`() = runBlocking {
        val url = "https://example.com/test.png"
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        
        every { mockTimeProvider.currentTimeMillis() } returns System.currentTimeMillis()
        
        diskCache.put(url, bitmap)
        diskCache.clear()
        
        assertNull(diskCache.get(url, 100, 100))
    }
}
