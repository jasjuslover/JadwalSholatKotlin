package com.husnikamal.jadwalsholat

import android.app.PendingIntent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.husnikamal.jadwalsholat.model.SholatTimeModel
import com.husnikamal.jadwalsholat.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)

        val today = arrayOfNulls<SholatTimeModel>(5 )
        today[0] = SholatTimeModel("Subuh", "04:36")
        today[1] = SholatTimeModel("Zuhur", "12:03")
        today[2] = SholatTimeModel("Ashar", "15:11")
        today[3] = SholatTimeModel("Magrib", "18:11")
        today[4] = SholatTimeModel("Isya", "19:22")

        binding.schedule = today

        switch_subuh.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                enableReminder(4, 36, 0, true)
            else
                enableReminder(4, 36,0, false)
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
