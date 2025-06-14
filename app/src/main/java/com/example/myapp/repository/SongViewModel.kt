package com.example.myapp.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.process.getsong.Song
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

    fun refresh(type: SongType, keyword: String? = null) {
        currentPage = 1
        _isLastPage.value = false
        loadSongs(type, keyword = keyword, isRefresh = true)
    }
    fun clearSongs() {
        _songs.value = emptyList()
    }
}