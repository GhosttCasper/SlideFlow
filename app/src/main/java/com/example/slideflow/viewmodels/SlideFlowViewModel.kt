package com.example.slideflow.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slideflow.network.SlideFlowApi
import kotlinx.coroutines.launch

private const val SCREEN_KEY = "e490b14d-987d-414f-a822-1e7703b37ce4"

class SlideFlowViewModel : ViewModel() {

    private val _mediaFiles = MutableLiveData<List<String>>()
    val mediaFiles: LiveData<List<String>> get() = _mediaFiles

    fun getPlaylists() {
        viewModelScope.launch {
            try {
                val response =
                    SlideFlowApi.retrofitService.getPlaylists(SCREEN_KEY)
                val files = response.playlists.flatMap { it.playlistItems }.map { it.fileKey }
                _mediaFiles.postValue(files)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}