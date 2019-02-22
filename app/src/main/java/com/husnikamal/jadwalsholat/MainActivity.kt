package com.husnikamal.jadwalsholat

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.husnikamal.jadwalsholat.model.SholatTimeModel
import com.husnikamal.jadwalsholat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val mWorker = WorkManager.getInstance()
        val oneTimeRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
        val oneTimeWorkRequest = oneTimeRequest.build()

        mWorker.enqueue(oneTimeWorkRequest)

        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)

        val today = arrayOfNulls<SholatTimeModel>(5 )
        today[0] = SholatTimeModel("Subuh", "04:36")
        today[1] = SholatTimeModel("Zuhur", "12:03")
        today[2] = SholatTimeModel("Ashar", "15:11")
        today[3] = SholatTimeModel("Magrib", "18:11")
        today[4] = SholatTimeModel("Isya", "19:22")

        binding.schedule = today
    }
}
