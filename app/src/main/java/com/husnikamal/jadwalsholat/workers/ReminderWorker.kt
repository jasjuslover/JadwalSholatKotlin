package com.husnikamal.jadwalsholat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.support.v4.app.NotificationCompat
import androidx.work.Worker

class ReminderWorker: Worker() {
    override fun doWork(): WorkerResult {
        sendNotification()

        return WorkerResult.SUCCESS
    }

    fun sendNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "WorkManager_00"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "WorkManager", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Reminder")
            .setContentText("Waktu sholat telah tiba!")
            .setSmallIcon(R.drawable.ic_launcher_background)

        notificationManager.notify(1, notification.build())
    }
}