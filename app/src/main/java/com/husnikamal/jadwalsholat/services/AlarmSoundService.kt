package com.husnikamal.jadwalsholat.services

import android.app.Service
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.media.MediaPlayer
import android.os.IBinder
import com.husnikamal.jadwalsholat.R
import com.husnikamal.jadwalsholat.RingAlarmActivity

class AlarmSoundService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mediaPlayer = MediaPlayer.create(this, R.raw.azan)
        mediaPlayer?.start()
        mediaPlayer?.isLooping = false

        startActivity(
            Intent(applicationContext, RingAlarmActivity::class.java).setFlags(
                FLAG_ACTIVITY_NEW_TASK
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        }
    }
}