package com.example.myapp.process.getsong

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Int,
    val title: String,
    val duration: Int,
    val url: String,
    val imageUrl: String?,
    val artist: Artist,
) : Parcelable

@Parcelize
data class Artist(
    val id: Int,
    val name: String,
) : Parcelable

data class SongUrlResponse(
    val data: SongUrl
)

data class SongUrl(
    val url: String
)


data class HistoryResponse(
    val data: List<PlayedSong>
)

data class PlayedSong(
    val id: Int,
    val song: Song
)

@Parcelize
data class FavoriteResponse(
    val data: List<Song>
): Parcelable

data class FavoriteRequest(
    val songId : Int
)

data class BaseResponse(
    val message: String
)