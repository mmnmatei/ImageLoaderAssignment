package com.example.imageloader.cache

import android.graphics.Bitmap
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class MemoryCacheTest {

    private lateinit var memoryCache: MemoryCache

    @Before
    fun setup() {
        memoryCache = MemoryCache()
    }

    @Test
    fun `put and get returns same bitmap`() {
        val url = "test_url"
        val bitmap = mockk<Bitmap>(relaxed = true)
        
        memoryCache.put(url, bitmap)
        
        assertEquals(bitmap, memoryCache.get(url))
    }

    @Test
    fun `get returns null for non-existent url`() {
        assertNull(memoryCache.get("non_existent"))
    }

    @Test
    fun `clear removes all items`() {
        val url = "test_url"
        val bitmap = mockk<Bitmap>(relaxed = true)
        
        memoryCache.put(url, bitmap)
        memoryCache.clear()
        
        assertNull(memoryCache.get(url))
    }
}
