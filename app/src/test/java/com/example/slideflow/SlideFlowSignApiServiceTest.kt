package com.example.slideflow

import com.example.slideflow.network.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SlideFlowSignApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetch playlists returns correct data`() = runTest {
        val mockResponse = """
{
  "screenKey": "e490b14d-987d-414f-a822-1e7703b37ce4",
  "company": "Test Company",
  "breakpointInterval": 0,
  "playlists": [
    {
      "channelTime": 0,
      "playlistItems": [
        {
          "creativeRefKey": "ref_123",
          "duration": 10,
          "expireDate": "2023-12-31",
          "startDate": "2023-01-01",
          "collectStatistics": true,
          "creativeLabel": "Label",
          "slidePriority": 1,
          "playlistKey": "playlist_1",
          "creativeKey": "123.jpg",
          "orderKey": 1,
          "eventTypesList": []
        }
      ],
      "playlistKey": "playlist_1"
    }
  ],
  "modified": 1695202956690
}
"""
        mockWebServer.enqueue(MockResponse().setBody(mockResponse))
        println("Mock JSON: $mockResponse")
        val response = apiService.getPlaylists("e490b14d-987d-414f-a822-1e7703b37ce4")

        // Check result
        assertEquals("e490b14d-987d-414f-a822-1e7703b37ce4", response.screenKey)
        assertEquals("Test Company", response.company)
        assertEquals(0, response.breakpointInterval)
        assertEquals(1, response.playlists.size)

        val playlist = response.playlists[0]
        assertEquals(0, playlist.channelTime)
        assertEquals("playlist_1", playlist.playlistKey)

        val item = playlist.playlistItems[0]
        assertEquals("123.jpg", item.creativeKey)
        assertEquals(10, item.duration)
        assertEquals("2023-12-31", item.expireDate)
        assertEquals("2023-01-01", item.startDate)
        assertEquals(true, item.collectStatistics)
        assertEquals("Label", item.creativeLabel)
        assertEquals(1, item.slidePriority)
        assertEquals(1, item.orderKey)
        assertEquals(emptyList<String>(), item.eventTypesList)
    }
}