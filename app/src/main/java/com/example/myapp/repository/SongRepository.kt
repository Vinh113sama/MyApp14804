package com.example.myapp.repository

import com.example.myapp.process.getplaylist.IdSong
import com.example.myapp.process.getplaylist.NamePlaylistRequest
import com.example.myapp.process.getplaylist.Playlist
import com.example.myapp.process.getplaylist.PlaylistAllResponse
import com.example.myapp.process.getsong.ApiService
import com.example.myapp.process.getsong.Artist
import com.example.myapp.process.getsong.FavoriteRequest
import com.example.myapp.process.getsong.Song
import com.example.myapp.process.getsong.UserResponse
import retrofit2.Response


class SongRepository(private val apiService: ApiService) {

    suspend fun getSongs(keyword: String?, page: Int, limit: Int = 10): List<Song> {
        return apiService.getSongs(keyword, page, limit).data
    }

    suspend fun getFavoriteSongs(page: Int, limit: Int = 10): List<Song> {
        return apiService.getFavoriteSongs(page, limit).data
    }

    suspend fun postFavoriteSong(songId: Int) {
        apiService.postFavoriteSong(FavoriteRequest(songId))
    }

    suspend fun deleteFavorite(songId: Int): Response<PlaylistAllResponse> {
        return apiService.deleteFavoriteSong(songId)
    }

    suspend fun getHistorySongs(page: Int, limit: Int = 10): List<Song> {
        return apiService.getHistorySongs(page, limit).data.map { it.song }
    }

    suspend fun getUserInfor(): UserResponse {
        return apiService.getUserInfor().data
    }

    suspend fun getPlaylistList(): List<Playlist> {
        return apiService.getPlaylists().body()?.data?.playlists ?: emptyList()
    }

    suspend fun getPlaylistSongs(playlistId: Int, page: Int, limit: Int = 10): List<Song> {
        return apiService.getPlaylistSongs(playlistId, page, limit)
            .data.map { it.song }
    }

    suspend fun deletePlaylistSong(playlistId: Int, songId: Int): Response<PlaylistAllResponse> {
        return apiService.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun addSongToPlaylist(playlistId: Int, songId: Int): Response<PlaylistAllResponse> {
        val request = IdSong(songId)
        return apiService.addSongToPlaylist(playlistId, request)
    }

    suspend fun logout() {
        apiService.logout()
    }

    suspend fun updatePlaylist(playlistId: Int, name: String): Response<PlaylistAllResponse> {
        val request = NamePlaylistRequest(name)
        return apiService.updatePlaylist(playlistId, request)
    }

    suspend fun createPlaylist(name: String): Response<PlaylistAllResponse> {
        val request = NamePlaylistRequest(name)
        return apiService.createPlaylist(request)
    }

    suspend fun deletePlaylist(playlistId: Int): Response<PlaylistAllResponse> {
        return apiService.deletePlaylist(playlistId)
    }

    suspend fun getAllSongs(page: Int, limit: Int = 10): List<Song> {
        return try {
            val response = apiService.getSongs(page = page, limit = limit)
            response.data.map { apiSong ->
                Song(
                    id = apiSong.id,
                    title = apiSong.title,
                    duration = apiSong.duration,
                    url = apiSong.url,
                    imageUrl = apiSong.imageUrl,
                    artist = Artist(
                        id = apiSong.artist.id,
                        name = apiSong.artist.name
                    ),
                    isFavorite = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getByGenre(genreName: String, page: Int, limit: Int = 10): List<Song> {
        return try {
            val response = apiService.getSongsByGenre(genreName, page, limit)
            response.data.map {
                Song(
                    id = it.id,
                    title = it.title,
                    duration = it.duration,
                    url = it.url,
                    imageUrl = it.imageUrl,
                    artist = Artist(it.artist.id, it.artist.name),
                    isFavorite = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
