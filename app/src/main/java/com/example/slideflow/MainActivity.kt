package com.example.slideflow

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.slideflow.databinding.ActivityMainBinding
import com.example.slideflow.network.PlaylistItem
import com.example.slideflow.viewmodels.SlideFlowViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: SlideFlowViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.mediaFiles.observe(this) { mediaFiles ->
            mediaFiles?.let { files ->
                startSlideshow(files)
            }
        }

        viewModel.getPlaylists()
    }

    private fun startSlideshow(files: List<PlaylistItem>) {
        if (files.isEmpty()) return

        lifecycleScope.launch {
            var currentIndex = 0

            while (true) { // Loop for a Looping Slide Show
                val currentItem = files[currentIndex]

                // cross-fade transition animation
                binding.imageView.animate()
                    .alpha(0f) // Исчезновение текущего изображения
                    .setDuration(500)
                    .withEndAction {
                        loadMedia(currentItem)
                        binding.imageView.animate().alpha(1f).setDuration(500).start()
                    }
                    .start()

                // Delay for the current slide duration
                delay(currentItem.duration * 1000L)

                // Jump to next slide
                currentIndex = (currentIndex + 1) % files.size
            }
        }
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
        binding.videoView.visibility = View.GONE
        binding.imageView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE

        Glide.with(this)
            .load(url)
/*            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }
            })*/
            .into(binding.imageView)
    }

    private fun showVideo(url: String) {
        binding.imageView.visibility = View.GONE
        binding.videoView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE

        binding.videoView.setVideoPath(url)
        binding.videoView.setOnPreparedListener { mediaPlayer ->
            binding.progressBar.visibility = View.GONE
            mediaPlayer.isLooping = true
            binding.videoView.start()
        }
        binding.videoView.setOnErrorListener { _, _, _ ->
            binding.progressBar.visibility = View.GONE
            true
        }
    }
}