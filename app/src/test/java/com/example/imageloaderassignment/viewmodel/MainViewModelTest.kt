package com.example.imageloaderassignment.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.imageloaderassignment.model.ImageItem
import com.example.imageloaderassignment.repository.ImageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: ImageRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchImages success updates images LiveData`() {
        val images = listOf(ImageItem(1, "url1"))
        coEvery { repository.getImages() } returns Result.success(images)

        viewModel = MainViewModel(repository)

        assertEquals(images, viewModel.images.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun `fetchImages failure updates error LiveData`() {
        val errorMessage = "Error fetching images"
        coEvery { repository.getImages() } returns Result.failure(Exception(errorMessage))

        viewModel = MainViewModel(repository)

        assertEquals(null, viewModel.images.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(errorMessage, viewModel.error.value)
    }

    @Test
    fun `fetchImages clears previous error`() {
        // Initial failure
        coEvery { repository.getImages() } returns Result.failure(Exception("First error"))
        viewModel = MainViewModel(repository)
        assertEquals("First error", viewModel.error.value)

        // Subsequent success
        val images = listOf(ImageItem(1, "url1"))
        coEvery { repository.getImages() } returns Result.success(images)
        
        viewModel.fetchImages()

        assertEquals(null, viewModel.error.value)
        assertEquals(images, viewModel.images.value)
    }
}
