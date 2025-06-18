package com.example.myapp.repository

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
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

    private val urlCache = mutableMapOf<Int, String>()

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

    fun setPlaylist(songs: List<Song>, startPosition: Int = 0) {
        _playlist.value = songs
        currentIndex = startPosition
        preloadUrls(songs)  // preload all song URLs
        playSongAtIndex(currentIndex)
    }

    private fun preloadUrls(songs: List<Song>) {
        viewModelScope.launch {
            for (song in songs) {
                if (!urlCache.containsKey(song.id)) {
                    try {
                        val response = RetrofitClient.apiService.getLink(song.id)
                        val url = response.data.url.replace(" ", "%20")
                        urlCache[song.id] = url
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun playSongAtIndex(index: Int) {
        val list = _playlist.value ?: return
        if (index !in list.indices) return

        currentIndex = index
        val song = list[index]
        _currentSong.value = song

        viewModelScope.launch {
            try {
                _isPlaying.value = false

                val url = urlCache[song.id] ?: run {
                    val response = RetrofitClient.apiService.getLink(song.id)
                    val fetchedUrl = response.data.url.replace(" ", "%20")
                    urlCache[song.id] = fetchedUrl
                    fetchedUrl
                }

                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .setTag(song.id)
                    .build()

                exoPlayer.setMediaItem(mediaItem, true)
                exoPlayer.prepare()
                exoPlayer.play()

                preloadNextSong()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playNext() {
        val list = _playlist.value ?: return
        val nextIndex = if (isShuffle) {
            (list.indices - currentIndex).random()
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

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun getPlayer(): ExoPlayer = exoPlayer

    fun updateCurrentSong(song: Song) {
        _currentSong.value = song
    }

    fun releasePlayer() {
        exoPlayer.release()
    }

    private fun preloadNextSong() {
        val list = _playlist.value ?: return
        val nextIndex = if (isShuffle) {
            (list.indices - currentIndex).random()
        } else {
            (currentIndex + 1) % list.size
        }
        val nextSong = list[nextIndex]
        if (!urlCache.containsKey(nextSong.id)) {
            viewModelScope.launch {
                try {
                    val response = RetrofitClient.apiService.getLink(nextSong.id)
                    val url = response.data.url.replace(" ", "%20")
                    urlCache[nextSong.id] = url
                } catch (_: Exception) {}
            }
        }
    }
}
