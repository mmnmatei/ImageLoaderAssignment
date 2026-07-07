package com.example.imageloaderassignment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imageloaderassignment.model.ImageItem
import com.example.imageloaderassignment.repository.ImageRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen.
 */
class MainViewModel(private val repository: ImageRepository) : ViewModel() {
    
    private val _images = MutableLiveData<List<ImageItem>>()
    val images: LiveData<List<ImageItem>> = _images

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchImages()
    }

    /**
     * Fetches images from the repository.
     */
    fun fetchImages() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getImages()
                .onSuccess {
                    _images.value = it
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            
            _isLoading.value = false
        }
    }
}
