package com.husnikamal.jadwalsholat

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.husnikamal.jadwalsholat.services.AlarmReceiver
import com.husnikamal.jadwalsholat.services.AlarmSoundService
import java.util.*

class ReminderWorker(ctx: Context, params: WorkerParameters): Worker(ctx, params) {
    override fun doWork(): Result {
        val hours = inputData.getInt("hours", 0)
        val minutes = inputData.getInt("minutes", 0)
        val reqCode = inputData.getInt("reqCode", 0)
        val enable = inputData.getBoolean("enable", false)

        if (enable)
            triggerAlarm(hours, minutes, reqCode)
        else
            stopAlarm(hours, minutes, reqCode)

        return Result.success()
    }

    fun triggerAlarm(hours: Int, minutes: Int, reqCode: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, 0)

        val alarm = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hours)
                    set(Calendar.MINUTE, minutes)
                    set(Calendar.SECOND, 0)
                }.timeInMillis,
                PendingIntent.getBroadcast(
                    applicationContext,
                    reqCode,
                    Intent(applicationContext, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        } else {
            alarm.set(
                AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hours)
                    set(Calendar.MINUTE, minutes)
                    set(Calendar.SECOND, 0)
                }.timeInMillis,
                PendingIntent.getBroadcast(
                    applicationContext,
                    reqCode,
                    Intent(applicationContext, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
    }

    fun stopAlarm(hours: Int, minutes: Int, reqCode: Int) {
        val alarm = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(
                PendingIntent.getBroadcast(
                applicationContext,
                reqCode,
                Intent(applicationContext, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
        ))

        applicationContext.stopService(Intent(applicationContext, AlarmSoundService::class.java))
    }
}