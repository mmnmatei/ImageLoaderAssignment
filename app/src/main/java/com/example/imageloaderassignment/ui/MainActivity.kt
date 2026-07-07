package com.example.imageloaderassignment.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imageloader.loader.ImageLoader
import com.example.imageloaderassignment.R
import com.example.imageloaderassignment.databinding.ActivityMainBinding
import com.example.imageloaderassignment.network.ApiService
import com.example.imageloaderassignment.repository.ImageRepository
import com.example.imageloaderassignment.viewmodel.MainViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Main activity showing a list of images and a cache invalidation button.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val imageAdapter = ImageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewModel()
        setupUI()
        observeViewModel()
    }

    private fun setupViewModel() {
        // Manual Dependency Injection
        val retrofit = Retrofit.Builder()
            .baseUrl("https://picsum.photos/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val repository = ImageRepository(apiService)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
        }

        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

    private fun setupUI() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = imageAdapter
        }

        binding.invalidateCacheButton.setOnClickListener {
            // Clear the cache using our library
            ImageLoader.clearCache()
            Toast.makeText(this, getString(R.string.toast_cache_invalidated), Toast.LENGTH_SHORT).show()
            
            // Reload images
            viewModel.fetchImages()
        }
    }

    private fun observeViewModel() {
        viewModel.images.observe(this) { images ->
            imageAdapter.submitList(images)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, getString(R.string.error_message, it), Toast.LENGTH_LONG).show()
            }
        }
    }
}
