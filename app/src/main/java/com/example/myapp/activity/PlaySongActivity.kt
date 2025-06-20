package com.example.myapp.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import com.bumptech.glide.Glide
import com.example.myapp.R
import com.example.myapp.databinding.ActivityPlaySongBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.Song
import com.example.myapp.repository.SongRepository
import com.example.myapp.repository.SongViewModel
import com.example.myapp.repository.AppViewModelProvider
import kotlinx.coroutines.launch
import java.util.*
import kotlin.getValue


@Suppress("UNCHECKED_CAST")
class PlaySongActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaySongBinding
    private val musicViewModel = AppViewModelProvider.provideMusicPlayerViewModel()
    private val handler = Handler(Looper.getMainLooper())
    private var seekBarRunnable: Runnable = Runnable {}
    private var isShuffle = false
    private var rotationAnimator: ObjectAnimator? = null
    private var currentRotation = 0f
    private var lastSongId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val playlist =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("playlist", Song::class.java) ?: arrayListOf()
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("playlist") ?: arrayListOf()
            }
        val position = intent.getIntExtra("position", 0)


        if (!intent.getBooleanExtra("fromMiniPlayer", false)) {
            musicViewModel.setPlaylist(playlist, position, autoPlay = true)
        }
        observeViewModel()
        setupPlayerListener()
        startSeekBarUpdate()
        setupEvents()
    }

    private val viewModel: SongViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = SongRepository(RetrofitClient.apiService)
                return SongViewModel(repository) as T
            }
        }
    }

    private fun observeViewModel() {
        musicViewModel.currentSong.observe(this) { song ->
            if (song != null && song.id != lastSongId) {
                lastSongId = song.id
                updateSongUI(song)
                startSeekBarUpdate()
                startOrStopRotation(musicViewModel.getPlayer().isPlaying)
            }
        }
        musicViewModel.isPlaying.observe(this) { isPlaying ->
            updatePlayPauseIcon(isPlaying)
        }
    }

    private fun setupPlayerListener() {
        musicViewModel.getPlayer().addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    startSeekBarUpdate()
                    startOrStopRotation(musicViewModel.getPlayer().isPlaying)
                }
            }
        })
    }

    private fun updateSongUI(song: Song) {
        binding.tvSongName.text = song.title
        binding.tvArtistName.text = song.artist.name
        binding.tvArtistName.isSelected = true
        processFavorite()
        Glide.with(this)
            .load(song.imageUrl)
            .override(600,600)
            .placeholder(R.drawable.ic_music_note)
            .error(R.drawable.img_avatar_default)
            .into(binding.imgSong)

        binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
    }

    private fun setupEvents() {
        binding.imgbtnNext.setOnClickListener {
            musicViewModel.playNext()
        }

        binding.imgbtnPlayback.setOnClickListener {
            musicViewModel.playPrevious()
        }

        binding.imgbtnPlay.setOnClickListener {
            musicViewModel.togglePlayPause()
        }


        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicViewModel.getPlayer().seekTo(progress * 1000L)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(seekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                startSeekBarUpdate()
            }
        })

        binding.imgbtnShuffle.setOnClickListener {
            isShuffle = !isShuffle
            binding.imgbtnShuffle.setImageResource(
                if (isShuffle) R.drawable.ic_shuffle else R.drawable.ic_circuit
            )
            musicViewModel.toggleShuffle()
        }

        binding.imgbtnDown.setOnClickListener {
            finish()
        }

        binding.imgbtnReplay.setOnClickListener {
            musicViewModel.rePlay()
        }

        binding.imgbtnFavorite.setOnClickListener {
            val song = musicViewModel.currentSong.value
            if (song != null) {
                viewModel.toggleFavorite(song, onComplete = {
                    processFavorite()
                })
            }
        }
    }

    private fun startSeekBarUpdate() {
        val player = musicViewModel.getPlayer()
        val durationMs = player.duration
        val durationSec = if (durationMs > 0) (durationMs / 1000).toInt() else 0

        binding.seekBar.max = durationSec
        binding.tvTimeMax.text = formatDuration(durationSec)

        seekBarRunnable = object : Runnable {
            override fun run() {
                val positionSec = (player.currentPosition / 1000).toInt()
                binding.seekBar.progress = positionSec
                binding.tvTimeCurrent.text = formatDuration(positionSec)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(seekBarRunnable)
    }

    private fun startOrStopRotation(isPlaying: Boolean) {
        if (rotationAnimator == null) {
            rotationAnimator = ObjectAnimator.ofFloat(
                binding.imgSong,
                View.ROTATION,
                currentRotation,
                currentRotation + 360f
            ).apply {
                duration = 10000
                interpolator = LinearInterpolator()
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener {
                    currentRotation = binding.imgSong.rotation % 360f
                }
            }
        } else {
            rotationAnimator?.setFloatValues(currentRotation, currentRotation + 360f)
        }

        if (isPlaying) {
            rotationAnimator?.start()
        } else {
            rotationAnimator?.pause()
        }
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val sec = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, sec)
    }

    private fun isFavorite(isFav: Boolean) {
        if (isFav) {
            binding.imgbtnFavorite.setImageResource(R.drawable.ic_delete_favorite)
        } else {
            binding.imgbtnFavorite.setImageResource(R.drawable.ic_add_favorite)
        }
    }

    private fun processFavorite() {
        val song = musicViewModel.currentSong.value ?: return
        lifecycleScope.launch {
            val isFav = viewModel.isFavorite(song.id)
            isFavorite(isFav)
        }
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        binding.imgbtnPlay.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
        startOrStopRotation(isPlaying)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
