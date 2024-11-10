package com.example.slideflow.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slideflow.network.PlaylistItem
import com.example.slideflow.network.SlideFlowApi
import kotlinx.coroutines.launch

private const val SCREEN_KEY = "e490b14d-987d-414f-a822-1e7703b37ce4"

class SlideFlowViewModel : ViewModel() {

    private val _mediaFiles = MutableLiveData<List<PlaylistItem>>()
    val mediaFiles: LiveData<List<PlaylistItem>> get() = _mediaFiles

    fun getPlaylists() {
        viewModelScope.launch {
            try {
                val response =
                    SlideFlowApi.retrofitService.getPlaylists(SCREEN_KEY)
                val validItems = response.playlists.flatMap { it.playlistItems }
                    .filter { isValidItem(it) } // Check the slide
                _mediaFiles.postValue(validItems)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isValidItem(item: PlaylistItem): Boolean {
        val currentDate = "9999-12-30 00:00:00" // Example: always active
        return item.startDate <= currentDate && item.expireDate >= currentDate
    }
}
