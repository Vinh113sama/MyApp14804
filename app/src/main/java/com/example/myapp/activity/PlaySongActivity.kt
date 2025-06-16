package com.example.myapp.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import com.bumptech.glide.Glide
import com.example.myapp.R
import com.example.myapp.databinding.ActivityPlaySongBinding
import com.example.myapp.process.getsong.Song
import com.example.myapp.util.AppViewModelProvider
import java.util.*


class PlaySongActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaySongBinding
    private val musicViewModel = AppViewModelProvider.provideMusicPlayerViewModel()
    private val handler = Handler(Looper.getMainLooper())
    private var seekBarRunnable: Runnable = Runnable {}
    private var isShuffle = false
    private var rotationAnimator: ObjectAnimator? = null
    private var currentRotation = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val playlist = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("playlist", Song::class.java) ?: arrayListOf()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("playlist") ?: arrayListOf()
        }
        val position = intent.getIntExtra("position", 0)


        if (!intent.getBooleanExtra("fromMiniPlayer", false)) {
            musicViewModel.setPlaylist(playlist, position)
        }
        observeViewModel()
        setupPlayerListener()
        startSeekBarUpdate()
        setupEvents()
    }

    private fun observeViewModel() {
        musicViewModel.currentSong.observe(this) { song ->
            if (song != null) {
                updateSongUI(song)
                startSeekBarUpdate()
                startOrStopRotation(musicViewModel.getPlayer().isPlaying)
            }
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

        Glide.with(this)
            .load(song.imageUrl)
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
            binding.imgbtnPlay.setImageResource(
                if (musicViewModel.getPlayer().isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
            startOrStopRotation(musicViewModel.getPlayer().isPlaying)
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
            rotationAnimator = ObjectAnimator.ofFloat(binding.imgSong, View.ROTATION, currentRotation, currentRotation + 360f).apply {
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

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
