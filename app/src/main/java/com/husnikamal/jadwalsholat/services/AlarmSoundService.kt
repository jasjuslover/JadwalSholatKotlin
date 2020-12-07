package com.husnikamal.jadwalsholat.services

import android.app.Service
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.media.MediaPlayer
import android.os.IBinder
import com.husnikamal.jadwalsholat.R
import com.husnikamal.jadwalsholat.RingAlarmActivity

class AlarmSoundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

//        startActivity(
//            Intent(applicationContext, RingAlarmActivity::class.java).setFlags(
//                FLAG_ACTIVITY_NEW_TASK
//            )
//        )
    }
}