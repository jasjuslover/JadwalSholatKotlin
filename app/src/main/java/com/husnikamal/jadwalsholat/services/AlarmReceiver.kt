package com.husnikamal.jadwalsholat.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.husnikamal.jadwalsholat.R

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startService(Intent(context, AlarmSoundService::class.java))

        sendNotification(context)
    }

    fun sendNotification(context: Context?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pray_reminder"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(channelId, "Pray Reminder", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("Reminder")
                .setContentText("Waktu sholat telah tiba!")
                .setSmallIcon(R.drawable.ic_launcher_background)

        notificationManager.notify(1, notification.build())
    }
}