//package com.example.myapp.repository
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.Service
//import android.content.Intent
//import android.os.Build
//import android.os.IBinder
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import androidx.media3.common.MediaItem
//import androidx.media3.common.MimeTypes
//import androidx.media3.exoplayer.ExoPlayer
//import com.example.myapp.R
//
//class MusicService : Service() {
//
//    private lateinit var player: ExoPlayer
//    private val CHANNEL_ID = "MusicChannel"
//
//    companion object {
//        const val ACTION_PLAY_URL = "ACTION_PLAY_URL"
//        const val ACTION_PLAY = "ACTION_PLAY"
//        const val ACTION_PAUSE = "ACTION_PAUSE"
//        const val ACTION_TOGGLE = "ACTION_TOGGLE"
//        const val ACTION_STOP = "ACTION_STOP"
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        Log.d("MusicService", "Service created")
//
//        player = ExoPlayer.Builder(this).build()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Music Playback",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
//        }
//
//        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle("Đang phát nhạc")
//            .setContentText("Ứng dụng của bạn")
//            .setSmallIcon(R.drawable.ic_music_note)
//            .build()
//
//        startForeground(1, notification)
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        when (intent?.action) {
//            ACTION_PLAY -> player.play()
//            ACTION_PAUSE -> player.pause()
//            ACTION_TOGGLE -> {
//                if (player.isPlaying) {
//                    player.pause()
//                } else {
//                    player.play()
//                }
//            }
//            ACTION_STOP -> {
//                player.stop()
//                stopSelf()
//                return START_NOT_STICKY
//            }
//            ACTION_PLAY_URL -> {
//                val url = intent.getStringExtra("url") ?: return START_NOT_STICKY
//                val mediaItem = MediaItem.Builder()
//                    .setUri(url)
//                    .setMimeType(MimeTypes.APPLICATION_M3U8)
//                    .build()
//                player.setMediaItem(mediaItem)
//                player.prepare()
//                player.playWhenReady = true
//            }
//        }
//
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        player.release()
//        super.onDestroy()
//    }
//
//    override fun onBind(intent: Intent?): IBinder? = null
//}
