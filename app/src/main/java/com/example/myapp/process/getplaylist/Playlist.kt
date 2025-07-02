package com.example.myapp.process.getplaylist

import android.os.Parcelable
import com.example.myapp.process.getsong.Song
import kotlinx.android.parcel.Parcelize

data class PlaylistResponse(
    val statusCode: Int,
    val message: String,
    val data: PlaylistData
)

data class PlaylistData(
    val playlists: List<Playlist>
)

@Suppress("DEPRECATED_ANNOTATION")
@Parcelize
data class Playlist(
    val id: Int,
    val name: String
) : Parcelable

data class PlaylistSongsResponse(
    val statusCode: Int,
    val message: String,
    val data: List<PlaylistSongEntry>
)

data class PlaylistSongEntry(
    val playlistId: Int,
    val songId: Int,
    val addedAt: String,
    val song: Song
)

data class NamePlaylistRequest(
    val name: String
)

data class IdSong(
    val songId: Int
)
data class PlaylistAllResponse(
   // val statusCode: Int,
    val message: String
)
