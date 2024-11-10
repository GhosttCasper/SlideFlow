package com.example.slideflow.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slideflow.network.PlaylistItem
import com.example.slideflow.network.SlideFlowApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val SCREEN_KEY = "e490b14d-987d-414f-a822-1e7703b37ce4"

class SlideFlowViewModel : ViewModel() {

    private val _mediaFiles = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val mediaFiles = _mediaFiles.asStateFlow()

    private var lastModified: Long = 0

    fun getPlaylists() {
        viewModelScope.launch {
            try {
                val response = SlideFlowApi.retrofitService.getPlaylists(SCREEN_KEY)
                if (response.modified != lastModified) {
                    lastModified = response.modified
                    val newItems = response.playlists.flatMap { it.playlistItems }
                    _mediaFiles.value = newItems
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePlaylistsPeriodically() {
        viewModelScope.launch {
            while (true) {
                getPlaylists()
                delay(30000L) // Check updates every 30 seconds
            }
        }
    }

    private fun isValidItem(item: PlaylistItem): Boolean {
        val currentDate = "9999-12-30 00:00:00" // Example: always active
        return item.startDate <= currentDate && item.expireDate >= currentDate
    }
}
