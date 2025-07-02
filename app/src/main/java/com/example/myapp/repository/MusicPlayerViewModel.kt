package com.example.myapp.repository

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.Song
import kotlinx.coroutines.launch

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _playlist = MutableLiveData<List<Song>>()
    val playlist: LiveData<List<Song>> get() = _playlist

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> get() = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private var currentIndex = 0
    private var isShuffle = false

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    playNext()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("MusicPlayer", "Playback error: ${error.errorCodeName} - ${error.message}", error)
            }
        })
    }

    fun setPlaylist(songs: List<Song>, startPosition: Int = 0, autoPlay: Boolean = true) {
        _playlist.value = songs
        currentIndex = startPosition
        viewModelScope.launch {
            playSongAtIndexFromList(songs, currentIndex, autoPlay)
        }
    }

    private fun playSongAtIndexFromList(
        list: List<Song>,
        index: Int,
        autoPlay: Boolean = true
    ) {
        if (index !in list.indices) return

        currentIndex = index
        val song = list[index]

        viewModelScope.launch {
            try {
                _isPlaying.value = false

                exoPlayer.stop()
                exoPlayer.clearMediaItems()

                val url = song.url
                if (url.isNullOrBlank()) {
                    return@launch
                }

                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .setTag(song.id)
                    .build()

                exoPlayer.setMediaItem(mediaItem, true)
                exoPlayer.prepare()

                _currentSong.postValue(song)
                launch {
                    try {
                        RetrofitClient.apiService.postPlaySong(song.id)
                    } catch (_: Exception) {
                    }
                }
                if (autoPlay) {
                    exoPlayer.playWhenReady = true
                    exoPlayer.play()
                }
            } catch (e: Exception) {
            }
        }
    }




    private fun playSongAtIndex(index: Int, autoPlay: Boolean = true) {
        val list = _playlist.value ?: return
        playSongAtIndexFromList(list, index, autoPlay)
    }

    fun playNext() {
        val list = _playlist.value ?: return
        val nextIndex = if (isShuffle && list.size > 1) {
            var newIndex: Int
            do {
                newIndex = list.indices.random()
            } while (newIndex == currentIndex)
            newIndex
        } else {
            (currentIndex + 1) % list.size
        }
        playSongAtIndex(nextIndex)
    }

    fun playPrevious() {
        val list = _playlist.value ?: return
        val previousIndex = if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1
        playSongAtIndex(previousIndex)
    }

    fun toggleShuffle() {
        isShuffle = !isShuffle
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun rePlay() {
        playSongAtIndex(currentIndex)
    }

    fun getPlayer(): ExoPlayer = exoPlayer

}