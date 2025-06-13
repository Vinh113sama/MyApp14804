package com.example.myapp.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import com.bumptech.glide.Glide
import com.example.myapp.R
import com.example.myapp.databinding.ActivityPlaySongBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.Song
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.example.myapp.process.getsong.FavoriteRequest
import kotlinx.coroutines.launch
import java.util.*

@androidx.media3.common.util.UnstableApi
class PlaySongActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaySongBinding
    private lateinit var playlist: ArrayList<Song>
    private var currentPosition: Int = 0
    private var exoPlayer: ExoPlayer? = null
    private var isShuffle = false
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable = Runnable {}
    private val randomList = Stack<Int>()
    private var favoriteSongIds = mutableListOf<Int>()
    private var isFavoriteProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getFavoriteSongs()
                val favoriteSongs = response.data
                favoriteSongIds.clear()
                favoriteSongIds.addAll(favoriteSongs.map { it.id })
                setupUI()
            } catch (e: Exception) {
                e.printStackTrace()
                setupUI()
            }
        }

        initPlaylist()
        setupEvents()
    }

    private fun initPlaylist() {
        playlist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("playlist", Song::class.java) ?: arrayListOf()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("playlist") ?: arrayListOf()
        }
        currentPosition = intent.getIntExtra("position", 0)
        randomList.push(currentPosition)

    }

    private fun setupUI() {
        if (playlist.isNotEmpty()) {
            loadSong(currentPosition)
        } else {
            binding.tvSongName.text = getString(R.string.no_song)
        }
    }

    private fun setupEvents() {
        binding.imgbtnNext.setOnClickListener { nextSong() }

        binding.imgbtnReplay.setOnClickListener { loadSong(currentPosition) }

        binding.imgbtnShuffle.setOnClickListener {
            isShuffle = !isShuffle
            binding.imgbtnShuffle.setImageResource(
                if (isShuffle) R.drawable.ic_shuffle else R.drawable.ic_circuit
            )
        }

        binding.imgbtnBack.setOnClickListener { finish() }

        binding.imgbtnPlay.setOnClickListener {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    binding.imgbtnPlay.setImageResource(R.drawable.ic_play)
                } else {
                    player.play()
                    binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
                }
            }
        }

        binding.imgbtnPlayback.setOnClickListener { previousSong() }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer?.seekTo(progress * 1000L)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(runnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                startSeekBarUpdate()
            }
        })
        binding.imgbtnFavorite.setOnClickListener {
            if (isFavoriteProcessing) return@setOnClickListener
            isFavoriteProcessing = true

            val song = playlist[currentPosition]
            val songId = song.id

            lifecycleScope.launch {
                try {
                    if (favoriteSongIds.contains(songId)) {
                        val response =
                            RetrofitClient.apiService.deleteFavoriteSong(FavoriteRequest(songId))
                        if (response.isSuccessful) {
                            favoriteSongIds.remove(songId)
                            binding.imgbtnFavorite.setImageResource(R.drawable.ic_add_favorite)
                        }
                    } else {
                        val response =
                            RetrofitClient.apiService.postFavoriteSong(FavoriteRequest(songId))
                        if (response.isSuccessful) {
                            favoriteSongIds.add(songId)
                            binding.imgbtnFavorite.setImageResource(R.drawable.ic_delete_favorite)
                        }
                    }
                    checkFavorite(currentPosition)
                } catch (e: Exception) {
                    e.printStackTrace()
                }finally {
                    isFavoriteProcessing = false
                    binding.imgbtnFavorite.isEnabled = true
                }
            }
        }
    }

    private fun loadSong(position: Int) {
        if(position >= playlist.size && playlist.isNotEmpty()) {
            currentPosition = 0
        }
        else {
            currentPosition = position
        }
        val currentSong = playlist[position]

        checkFavorite(position)
        binding.tvSongName.text = currentSong.title
        binding.tvArtistName.text = currentSong.artist.name
        binding.tvArtistName.isSelected = true

        Glide.with(this)
            .load(currentSong.imageUrl)
            .placeholder(R.drawable.ic_music_note)
            .error(R.drawable.img_avatar_default)
            .into(binding.imgSong)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getLink(currentSong.id)
                val url = response.data.url.replace(" ", "%20")
                Log.d("HLS_URL", url)
                playHlsSong(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playHlsSong(url: String) {
        handler.removeCallbacks(runnable)
        exoPlayer?.release()

        val player = ExoPlayer.Builder(this).build()
        val dataSourceFactory = DefaultDataSource.Factory(this)
        val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))

        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()

        exoPlayer = player
        binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    handler.removeCallbacks(runnable)
                    if (isShuffle) {
                        if (playlist.size == 1) return
                        var randomPosition: Int
                        do {
                            randomPosition = (0 until playlist.size).random()
                        } while (randomPosition == currentPosition)
                        randomList.push(randomPosition)
                        loadSong(randomPosition)
                    } else {
                        randomList.push(currentPosition)
                        nextSong()
                    }
                }
            }
        })

        startSeekBarUpdate()
    }

    private fun nextSong() {
        if (playlist.isNotEmpty()) {
            currentPosition = (currentPosition + 1) % playlist.size
            loadSong(currentPosition)
        }
    }

    private fun previousSong() {
        if (randomList.isNotEmpty()) {
            loadSong(randomList.pop())
        } else {
            currentPosition =
                if (currentPosition - 1 < 0) playlist.size - 1 else currentPosition - 1
            loadSong(currentPosition)
        }
    }

    private fun startSeekBarUpdate() {
        exoPlayer?.let { player ->
            val durationMs = player.duration
            val durationSec = if (durationMs != C.TIME_UNSET && durationMs > 0) {
                (durationMs / 1000).toInt()
            } else {
                0
            }

            binding.tvTimeMax.text = formatDuration(durationSec)
            binding.seekBar.max = durationSec

            runnable = object : Runnable {
                override fun run() {
                    val currentPosMs = player.currentPosition
                    val currentPosSec = if (currentPosMs > 0) {
                        (currentPosMs / 1000).toInt()
                    } else {
                        0
                    }

                    binding.seekBar.progress = currentPosSec
                    binding.tvTimeCurrent.text = formatDuration(currentPosSec)
                    handler.postDelayed(this, 1000)
                }
            }

            handler.post(runnable)
        }
    }


    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }

    private fun checkFavorite(position: Int) {
        val songId = playlist[position].id
        val isFavorite = favoriteSongIds.contains(songId)
        binding.imgbtnFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_delete_favorite
            else R.drawable.ic_add_favorite
        )
    }
}
