//package com.example.myapp.repository
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.Service
//import android.content.Intent
//import androidx.media3.common.MediaItem
//import android.os.Build
//import android.os.IBinder
//import androidx.core.app.NotificationCompat
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.exoplayer.ExoPlayer
//import com.example.myapp.R
//import com.example.myapp.activity.PlaySongActivity
//
//@UnstableApi
//class MusicService : Service() {
//
//    private lateinit var player: ExoPlayer
//    private val CHANNEL_ID = "music_channel"
//    private val NOTIFICATION_ID = 1
//
//    override fun onCreate() {
//        super.onCreate()
//        createNotificationChannel()
//        player = ExoPlayer.Builder(this).build()
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val url = intent?.getStringExtra("song_url") ?: return START_NOT_STICKY
//        val position = intent.getLongExtra("position", 0L)
//        val playWhenReady = intent.getBooleanExtra("is_playing", true)
//
//        player.setMediaItem(MediaItem.fromUri(url))
//        player.prepare()
//        player.seekTo(position)
//        player.playWhenReady = playWhenReady
//
//        startForeground(NOTIFICATION_ID, createNotification())
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        player.release()
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    private fun createNotification(): Notification {
//        val intent = Intent(this, PlaySongActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        return NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle("Đang phát nhạc")
//            .setContentText("Nhấn để quay lại ứng dụng")
//            .setSmallIcon(R.drawable.ic_music_note)
//            .setContentIntent(pendingIntent)
//            .setOngoing(true)
//            .build()
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Phát nhạc nền",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//    }
//}
