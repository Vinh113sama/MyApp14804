package com.example.myapp.repository

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
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
        })
    }

    fun setPlaylist(songs: List<Song>, startPosition: Int = 0) {
        _playlist.value = songs
        currentIndex = startPosition
        playSongAtIndex(currentIndex)
    }

    private fun playSongAtIndex(index: Int) {
        val list = _playlist.value ?: return
        if (index !in list.indices) return
        currentIndex = index
        val song = list[index]
        _currentSong.value = song

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getLink(song.id)
                val url = response.data.url.replace(" ", "%20")
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                val mediaItem = MediaItem.fromUri(url)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playNext() {
        val list = _playlist.value ?: return
        val nextIndex = if (isShuffle) {
            (list.indices).random()
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
    fun releasePlayer() {
        exoPlayer.release()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun getPlayer(): ExoPlayer = exoPlayer

}
