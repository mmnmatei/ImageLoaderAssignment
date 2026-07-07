package com.example.imageloaderassignment.repository

import com.example.imageloaderassignment.model.ImageItem
import com.example.imageloaderassignment.network.ApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImageRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: ImageRepository

    @Before
    fun setup() {
        apiService = mockk()
        repository = ImageRepository(apiService)
    }

    @Test
    fun `getImages returns success when api call is successful`() = runBlocking {
        val images = listOf(ImageItem(1, "url1"), ImageItem(2, "url2"))
        coEvery { apiService.getImages() } returns images

        val result = repository.getImages()

        assertTrue(result.isSuccess)
        assertEquals(images, result.getOrNull())
    }

    @Test
    fun `getImages returns failure when api call fails`() = runBlocking {
        coEvery { apiService.getImages() } throws Exception("Network error")

        val result = repository.getImages()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getImages returns empty list when api returns empty`() = runBlocking {
        coEvery { apiService.getImages() } returns emptyList()

        val result = repository.getImages()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }
}
