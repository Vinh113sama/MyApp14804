package com.example.myapp.repository

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.myapp.process.getplaylist.PlaylistResponse
import com.example.myapp.process.getsong.Song
import com.example.myapp.process.getsong.UserResponse
import com.example.myapp.process.login.SongType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SongViewModel(private val repository: SongRepository) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private var currentPage = 1
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> get() = _isLastPage
    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> get() = _searchResults

    private val _playlistSongs = MutableStateFlow<List<Song>>(emptyList())
    val playlistSongs: StateFlow<List<Song>> get() = _playlistSongs

    fun loadSongs(type: SongType, keyword: String? = null, isRefresh: Boolean = false) {
        if (_isLoading.value || _isLastPage.value) return
        _isLoading.value = true

        if (isRefresh) {
            currentPage = 1
            _isLastPage.value = false
        }

        viewModelScope.launch {
            try {
                val result = when (type) {
                    SongType.ALL -> repository.getSongs(keyword, currentPage)
                    SongType.FAVORITE -> repository.getFavoriteSongs(currentPage)
                    SongType.HISTORY -> repository.getHistorySongs(currentPage)
                    SongType.PLAYLISTSONG -> repository.getPlaylistSongs(playlistId = 1, currentPage)
                }
                val updatedList = if (isRefresh || currentPage == 1) {
                    result
                } else {
                    _songs.value + result
                }
                _songs.value = updatedList

                if (result.isEmpty()) _isLastPage.value = true
                else currentPage++

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(song: Song, isFromFavoriteScreen: Boolean = false, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                if (song.isFavorite) {
                    repository.deleteFavorite(song.id)
                    song.isFavorite = false
                } else {
                    repository.postFavoriteSong(song.id)
                    song.isFavorite = true
                }

                _songs.value = _songs.value.map {
                    if (it.id == song.id) song else it
                }

                if (isFromFavoriteScreen) {
                    refresh(SongType.FAVORITE)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete?.invoke()
            }
        }
    }

    fun refresh(type: SongType, keyword: String? = null) {
        currentPage = 1
        _isLastPage.value = false
        loadSongs(type, keyword = keyword, isRefresh = true)
    }

    fun clearSongs() {
        _songs.value = emptyList()
    }

    suspend fun clearFavoriteSong(id: Int) {
        return repository.deleteFavorite(id)
    }

    suspend fun getuserInformation(): UserResponse {
        return repository.getUserInfor()
    }

    suspend fun isFavorite(songId : Int): Boolean {
        val result = repository.getFavoriteSongs(currentPage, 30)
        return result.any { it.id == songId }
    }

    @OptIn(UnstableApi::class)
    fun searchSong(query: String, page: Int = 1, limit: Int = 30) {
        viewModelScope.launch {
            try {
                val result = repository.getSongs(query, page, limit)
                _searchResults.value = result
            } catch (e: Exception) {
                Log.e("Search", "Error: ${e.message}")
            }
        }
    }

    suspend fun getPlaylist(userId : Int): List<PlaylistResponse> {
        return repository.getPlaylistList(userId)
    }

    fun loadSongsInPlaylist(playlistId: Int) {
        viewModelScope.launch {
            try {
                val result = repository.getPlaylistSongs(playlistId, currentPage)
                _playlistSongs.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}