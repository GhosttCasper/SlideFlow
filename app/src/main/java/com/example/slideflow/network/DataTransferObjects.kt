package com.example.slideflow.network

data class PlaylistResponse(
    val screenKey: String,
    val breakpointInterval: Int,
    val playlists: List<Playlist>
)

data class Playlist(
    val channelTime: Int,
    val playlistItems: List<PlaylistItem>
)

data class PlaylistItem(
    val fileKey: String
)
