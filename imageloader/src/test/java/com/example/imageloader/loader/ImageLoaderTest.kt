package com.example.imageloader.loader

import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class ImageLoaderTest {

    @Before
    fun setup() {
        // ImageLoader is a singleton, we might need to reset it if possible, 
        // but it doesn't expose a reset method.
    }

    @Test
    fun `load sets placeholder initially`() {
        val imageView = mockk<ImageView>(relaxed = true)
        val url = "https://example.com/test.png"
        val placeholder = android.R.drawable.ic_menu_gallery
        
        // We need a context for ImageLoader to initialize its disk cache
        io.mockk.every { imageView.context } returns ApplicationProvider.getApplicationContext()
        // Provide resources for display metrics in case width/height is 0
        io.mockk.every { imageView.resources } returns ApplicationProvider.getApplicationContext<android.app.Application>().resources

        ImageLoader.load(url, placeholder, imageView)

        verify { imageView.setImageResource(placeholder) }
    }

    @Test
    fun `clearCache does not crash`() {
        // ImageLoader uses singletons for caches, so we just verify it runs without exception
        ImageLoader.clearCache()
    }
}
