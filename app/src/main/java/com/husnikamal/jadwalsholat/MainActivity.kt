package com.husnikamal.jadwalsholat

import android.app.PendingIntent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.husnikamal.jadwalsholat.model.SholatTimeModel
import com.husnikamal.jadwalsholat.databinding.ActivityMainBinding
import com.husnikamal.jadwalsholat.utils.PrayTime
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        val latitude = -6.9175
        val longitude = 107.6191

        val defaultTimeZone = TimeZone.getTimeZone("GMT+7")
        val defaultCalendar = Calendar.getInstance(defaultTimeZone)

        // get offset from UTC, accounting for DST
        val defaultTzOffsetMs =
            defaultCalendar.get(Calendar.ZONE_OFFSET) + defaultCalendar.get(Calendar.DST_OFFSET)
        val timezone: Double = (defaultTzOffsetMs / (1000 * 60 * 60)).toDouble()

        val prayScheduler = PrayTime()
        prayScheduler.timeFormat = PrayTime.TIME_24
        prayScheduler.calcMethod = PrayTime.CUSTOM // SIHAT KEMENAG
        prayScheduler.asrJuristic = PrayTime.SHAFII
        prayScheduler.adjustHighLats = PrayTime.ANGLE_BASED

        val offsets =
            intArrayOf(2, 2, 4, 2, 0, 8, 3) // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
        prayScheduler.tune(offsets)

        val now = Date()
        val cal = Calendar.getInstance()
        cal.time = now

        val prayTimes = prayScheduler.getPrayerTimes(
            cal,
            latitude, longitude,
            timezone
        )
        val prayNames = prayScheduler.timeNames

        var today = arrayListOf<SholatTimeModel>()

        for ((index, value) in prayTimes.withIndex()) {
            Log.d("Huwiw ${prayNames[index]}", value)
            if (!prayNames[index].equals("Sunrise") && !prayNames[index].equals("Sunset"))
                today.add(SholatTimeModel(prayNames[index], value))
        }

        binding.schedule = today

        switch_subuh.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                enableReminder(4, 33, 0, true)
            else
                enableReminder(4, 33, 0, false)
        }

        switch_zuhur.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                enableReminder(12, 3, 1, true)
            else
                enableReminder(12, 3, 1, false)
        }

        switch_ashar.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                enableReminder(15, 11, 2, true)
            else
                enableReminder(15, 11, 2, false)
        }

        switch_magrib.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                enableReminder(18, 11, 3, true)
            else
                enableReminder(18, 11, 3, false)
        }

        switch_isya.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                enableReminder(19, 22, 4, true)
            else
                enableReminder(19, 22, 4, false)
        }
    }

    fun enableReminder(hours: Int, minutes: Int, reqCode: Int, enable: Boolean) {
        val data = Data.Builder()
            .putInt("hours", hours)
            .putInt("minutes", minutes)
            .putInt("reqCode", reqCode)
            .putBoolean("enable", enable)
            .build()

        val mWorker = WorkManager.getInstance()
        val oneTimeRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
        val oneTimeWorkRequest = oneTimeRequest.setInputData(data)
            .build()

        mWorker.enqueue(oneTimeWorkRequest)
    }
}
