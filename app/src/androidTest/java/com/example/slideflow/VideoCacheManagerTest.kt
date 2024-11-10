package com.example.slideflow

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoCacheManagerInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val videoCacheManager = VideoCacheManager(context)

    @Test
    fun downloadAndCacheVideo_savesFileLocally() = runTest {
        val testUrl = "https://test.onsignage.com/PlayerBackend/creative/get/28a3cbb9-2622-475b-99ba-b39f04a8bfc2.mp4"
        val file = videoCacheManager.downloadAndCacheVideo(testUrl)
        assertNotNull(file)
        assert(file!!.exists())
    }
}