package com.example.myapp.repository

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.myapp.process.getplaylist.Playlist
import com.example.myapp.process.getsong.Song
import com.example.myapp.process.getsong.UserResponse
import com.example.myapp.process.login.SongType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject


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
                val result: List<Song> = when (type) {
                    SongType.ALL -> repository.getAllSongs(currentPage)
                    SongType.VN -> repository.getByGenre("VN", currentPage)
                    SongType.USUK -> repository.getByGenre("US-UK", currentPage)
                    SongType.KPOP -> repository.getByGenre("K-POP", currentPage)
                    SongType.FAVORITE -> repository.getFavoriteSongs(currentPage)
                    SongType.HISTORY -> repository.getHistorySongs(currentPage)
                    SongType.PLAYLISTSONG -> {
                        val id = playlistId ?: return@launch
                        repository.getPlaylistSongs(id, currentPage)
                    }
                }
                val isFirstPage = isRefresh || currentPage == 1
                when (type) {
                    SongType.PLAYLISTSONG -> {
                        val updatedList = if (isFirstPage) result else _playlistSongs.value + result
                        _playlistSongs.value = updatedList
                    }
                    else -> {
                        val updatedList = if (isFirstPage) result else _songs.value + result
                        _songs.value = updatedList
                    }
                }
                Log.d("Pagination", "Page: $currentPage, ResultSize: ${result.size}, TotalItems: ${
                        when (type) {
                            SongType.PLAYLISTSONG -> _playlistSongs.value.size
                            else -> _songs.value.size
                        }
                    }"
                )
                if (result.isEmpty()) {
                    _isLastPage.value = true
                } else {
                    currentPage++
                }

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

    fun refresh(type: SongType, playlistId: Int? = null) {
        currentPage = 1
        _isLastPage.value = false
        loadSongs(type,  isRefresh = true, playlistId = playlistId)
    }

    fun clearSongs() {
        _songs.value = emptyList()
    }

    fun clearFavoriteSong(
        songId: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.deleteFavorite(songId)
                val message = extractMessage(response)
                if (response.isSuccessful) onSuccess(message)
                else onError(message)
            } catch (_: Exception) {
                onError("Failed to connect to the server.")
            }
        }
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

    suspend fun getPlaylist(): List<Playlist> {
        return repository.getPlaylistList()
    }

    fun deletePlaylistSong(
        playlistId: Int,
        songId: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.deletePlaylistSong(playlistId, songId)
                val message = extractMessage(response)
                if (response.isSuccessful) {
                    onSuccess(message)
                    loadSongs(SongType.PLAYLISTSONG, playlistId = playlistId, isRefresh = true)
                } else {
                    onError(message)
                }
            } catch (_: Exception) {
                onError("Failed to connect to the server.")
            }
        }
    }

    fun addSongToPlaylist(
        playlistId: Int,
        songId: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.addSongToPlaylist(playlistId, songId)
                onResult(response.isSuccessful, extractMessage(response))
            } catch (_: Exception) {
                onResult(false, "Failed to connect to the server.")
            }
        }
    }

    fun updatePlaylistName(
        playlistId: Int,
        name: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.updatePlaylist(playlistId, name)
                val message = extractMessage(response)
                if (response.isSuccessful) onSuccess(message)
                else onError(message)
            } catch (_: Exception) {
                onError("Failed to connect to the server.")
            }
        }
    }

    fun createPlaylist(name: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.createPlaylist(name)
                val message = extractMessage(response)
                if (response.isSuccessful) onSuccess(message)
                else onError(message)
            } catch (_: Exception) {
                onError("Failed to connect to the server.")
            }
        }
    }

    fun deletePlaylist(
        playlistId: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.deletePlaylist(playlistId)
                val message = extractMessage(response)
                if (response.isSuccessful) onSuccess(message)
                else onError(message)
            } catch (_: Exception) {
                onError("Failed to connect to the server.")
            }
        }
    }

    private fun extractMessage(response: retrofit2.Response<*>): String {
        return if (response.isSuccessful) {
            try {
                val body = response.body()
                val message = when (body) {
                    is com.example.myapp.process.getplaylist.PlaylistAllResponse -> body.message
                    else -> "Success"
                }
                message.ifBlank { "Success" }
            } catch (_: Exception) {
                "Success"
            }
        } else {
            try {
                val errorText = response.errorBody()?.string()
                JSONObject(errorText ?: "{}")
                    .optString("message")
                    .ifBlank { "Unknown error." }
            } catch (_: Exception) {
                "Unknown error."
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


