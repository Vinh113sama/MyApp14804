package com.example.myapp.process.getplaylist

import com.example.myapp.process.getsong.Song


data class CreatePlaylistRequest(
    val name: String
)

data class UpdatePlaylistRequest(
    val name: String
)

data class AddSongToPlaylistRequest(
    val songId: Int
)

data class RemoveSongFromPlaylistRequest(
    val songId: Int
)

data class PlaylistResponse(
    val id: Int,
    val name: String
)

data class PlaylistListResponse(
    val data: List<PlaylistResponse>
)

data class PlaylistSongsResponse(
    val data: List<PlaylistSongItem>,
    val playlistId: Int
)

data class PlaylistSongItem(
    val song: Song
)

data class AddSongResponse(
    val message: String,
    val data: AddSongData
)

data class AddSongData(
    val playlistId: Int,
    val songId: Int,
    val addedAt: String
)

data class RemoveSongResponse(
    val message: String,
    val data: RemoveSongData
)

data class RemoveSongData(
    val playlistId: Int,
    val songId: Int,
    val addedAt: String
)