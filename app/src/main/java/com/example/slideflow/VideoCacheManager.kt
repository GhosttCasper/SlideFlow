package com.example.slideflow

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class VideoCacheManager(private val context: Context) {

    private val client = OkHttpClient()
    private val cacheDir = File(context.cacheDir, "video_cache")

    init {
        if (!cacheDir.exists()) cacheDir.mkdirs() // Create a cache folder if it is not exist
    }

    // Check if there is a video file in cache
    fun getCachedVideo(url: String): File? {
        val fileName = url.substringAfterLast("/")
        val cachedFile = File(cacheDir, fileName)
        return if (cachedFile.exists()) cachedFile else null
    }

    // Upload video and save to cache
    suspend fun downloadAndCacheVideo(url: String): File? {
        return withContext(Dispatchers.IO) { // Switch to background
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("DownloadError", "Error HTTP: ${response.code()}")
                    return@withContext null
                }

                val fileName = url.substringAfterLast("/")
                val cachedFile = File(cacheDir, fileName)
                if (cachedFile.exists()) {
                    Log.d(
                        "VideoCacheManager",
                        "Video successfully saved: ${cachedFile.absolutePath}"
                    )
                } else {
                    Log.e("VideoCacheManager", "Failed to save video: $url")
                }
                FileOutputStream(cachedFile).use { outputStream ->
                    response.body()?.byteStream()?.copyTo(outputStream)
                }
                cachedFile
            } catch (e: Exception) {
                Log.e("VideoCacheManager", "Download Error: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    // Clean out old files from cache
    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
