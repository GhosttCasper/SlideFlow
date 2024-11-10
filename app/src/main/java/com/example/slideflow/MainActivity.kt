package com.example.slideflow

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.slideflow.databinding.ActivityMainBinding
import com.example.slideflow.network.PlaylistItem
import com.example.slideflow.viewmodels.SlideFlowViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private val viewModel: SlideFlowViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private var slideshowJob: Job? = null
    private val videoCacheManager by lazy { VideoCacheManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        lifecycleScope.launch {
            viewModel.mediaFiles.collectLatest { files ->
                startSlideshow(files)
            }
        }

        viewModel.updatePlaylistsPeriodically()
    }

    private fun startSlideshow(files: List<PlaylistItem>) {
        Log.d("Slideshow", "Current Index: ${files.toString()}, Total Items: ${files.size}")
        if (files.isEmpty()) return

        slideshowJob = lifecycleScope.launch {
            var currentIndex = 0

            while (true) {
                val currentItem = files[currentIndex]
                Log.w("Slideshow", "Current Index: ${currentIndex}, Total Items: ${files.size}")

                binding.imageView.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction {
                        loadMedia(currentItem)
                        binding.imageView.animate().alpha(1f).setDuration(1000).start()
                    }
                    .start()

                delay(currentItem.duration * 1000L)
                currentIndex = (currentIndex + 1) % files.size
            }
        }
    }

    private fun stopSlideshow() {
        slideshowJob?.cancel()
    }


    private fun loadMedia(item: PlaylistItem) {
        val mediaUrl = "https://test.onsignage.com/PlayerBackend/creative/get/${item.creativeKey}"

        if (item.creativeKey.endsWith(".mp4")) {
            showVideo(mediaUrl)
        } else {
            showImage(mediaUrl)
        }
    }

    private fun showImage(url: String) {
        binding.apply {
            videoView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
        }

        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(binding.imageView)
    }

    private fun showVideo(url: String) {
        binding.apply {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
        }

        val cachedFile = videoCacheManager.getCachedVideo(url)
        if (cachedFile != null) {
            playVideo(cachedFile.absolutePath) // Use video from cache
        } else {
            // Upload and cache video
            lifecycleScope.launch {
                val downloadedFile = videoCacheManager.downloadAndCacheVideo(url)
                if (downloadedFile != null) {
                    playVideo(downloadedFile.absolutePath)
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun playVideo(filePath: String) {
        binding.videoView.apply {
            if (File(filePath).exists()) {
                binding.videoView.setVideoPath(filePath)
                setOnPreparedListener { mediaPlayer ->
                    binding.progressBar.visibility = View.GONE
                    mediaPlayer.isLooping = true
                    start()
                }
                setOnErrorListener { _, _, _ ->
                    binding.progressBar.visibility = View.GONE
                    true
                }
            } else {
                Log.e("VideoError", "File not found\": $filePath")
                binding.progressBar.visibility = View.GONE
            }

        }
    }

}