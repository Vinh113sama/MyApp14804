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
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage


    @OptIn(UnstableApi::class)
    fun loadSongs(
        type: SongType,
        keyword: String? = null,
        isRefresh: Boolean = false,
        playlistId: Int? = null
    ) {
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
                    SongType.PLAYLISTSONG -> {
                        val id = playlistId ?: return@launch
                        repository.getPlaylistSongs(id, currentPage)
                    }
                }
                val updatedList = if (isRefresh || currentPage == 1) {
                    result
                } else {
                    _songs.value + result
                }
                _songs.value = updatedList
                Log.d(
                    "Pagination",
                    "Page: $currentPage, ResultSize: ${result.size}, TotalItems: ${_songs.value.size}"
                )
                if (result.isEmpty()) _isLastPage.value = true
                else currentPage++

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(
        song: Song,
        isFromFavoriteScreen: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
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

    fun refresh(type: SongType, keyword: String? = null, playlistId: Int? = null) {
        currentPage = 1
        _isLastPage.value = false
        loadSongs(type, keyword = keyword, isRefresh = true, playlistId = playlistId)
    }

    fun clearSongs() {
        _songs.value = emptyList()
    }

    suspend fun clearFavoriteSong(id: Int) {
        return repository.deleteFavorite(id)
    }

    suspend fun getUserInformation(): UserResponse {
        return repository.getUserInfor()
    }

    suspend fun isFavorite(songId: Int): Boolean {
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

    suspend fun getPlaylist(userId: Int): List<PlaylistResponse> {
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

    fun deletePlaylistSong(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            repository.deletePlaylistSong(playlistId, songId)

            loadSongsInPlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(
        playlistId: Int,
        songId: Int,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                repository.addSongToPlaylist(playlistId, songId)
                onSuccess?.invoke()
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 500) {
                    onError?.invoke("Song already exists in the playlist")
                } else {
                    onError?.invoke("Unexpected error: ${e.code()}")
                }
            } catch (e: Exception) {
                onError?.invoke("Failed to add song to playlist")
            }
        }
    }

    fun updatePlaylistName(
        playlistId: Int,
        name: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updatePlaylist(playlistId, name)
                _errorMessage.value = null
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                val message = if (e.code() == 400) "Invalid name" else "Name already exists"
                _errorMessage.value = message
                onError(message)
            } catch (e: Exception) {
                _errorMessage.value = "Network error"
                onError("Network error")
            }
        }
    }


    fun createPlaylist(name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.createPlaylist(name)
                _errorMessage.value = null
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                val msg = if (e.code() == 400) extractMessage(e) ?: "Invalid playlist name"
                else "Name already exists"
                _errorMessage.value = msg
                onError(msg)
            } catch (e: Exception) {
                _errorMessage.value = "Network error"
                onError("Network error")
            }
        }
    }

    private fun extractMessage(e: retrofit2.HttpException): String? {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            val json = org.json.JSONObject(errorBody ?: "")
            json.getString("message")
        } catch (ex: Exception) {
            null
        }
    }

    fun deletePlaylist(playlistId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deletePlaylist(playlistId)
                onSuccess()
            } catch (_: Exception) {
            }
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}