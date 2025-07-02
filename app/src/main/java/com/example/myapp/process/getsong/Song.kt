package com.example.myapp.process.getsong

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Song(
    val id: Int,
    val title: String,
    val duration: Int,
    val url: String?,
    val imageUrl: String?,
    val artist: Artist,
    var isFavorite: Boolean = false
) : Parcelable

data class SongResponse(
    val statusCode: Int,
    val message: String,
    val data: List<Song>
)


data class TopSongResponse(
    val id: Int,
    val title: String,
    val titleNormalized: String,
    val genre: String,
    val duration: Int,
    val url: String,
    val imageUrl: String,
    val artistId: Int,
    val createdAt: String,
    val artist: ArtistTop,
    val listenCount: Int
)

data class ArtistTop(
    val id: Int,
    val name: String,
    val nameNormalized: String,
    val imageUrl: String,
    val createdAt: String
)

data class TopSongListResponse(
    val data: List<TopSongResponse>
)

@Parcelize
data class Artist(
    val id: Int,
    val name: String,
) : Parcelable

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
) : Parcelable

data class FavoriteRequest(
    val songId: Int
)

data class UserResponse(
    val name: String,
    val id: Int
)