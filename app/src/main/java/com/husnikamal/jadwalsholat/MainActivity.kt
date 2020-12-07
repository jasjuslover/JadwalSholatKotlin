package com.husnikamal.jadwalsholat

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.databinding.DataBindingUtil
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.husnikamal.jadwalsholat.BuildConfig.APPLICATION_ID
import com.husnikamal.jadwalsholat.model.SholatTimeModel
import com.husnikamal.jadwalsholat.databinding.ActivityMainBinding
import com.husnikamal.jadwalsholat.utils.PrayTime
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    var pendingIntent: PendingIntent? = null

    private val TAG = "Huwiw MainActivity"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var binding: ActivityMainBinding

    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.textViewDate.text =
            SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
                .format(Date())

        initAlarmSwitcher()
    }

    private fun initAlarmSwitcher() {
        binding.apply {
            switchMagrib.isChecked = true
        }
    }

    private fun calculatePrayTimes(): List<SholatTimeModel> {
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
            intArrayOf(2, 2, 4, 2, 0, 8, 3)
        prayScheduler.tune(offsets)

        val now = Date()
        val cal = Calendar.getInstance()
        cal.time = now

        val prayTimes = prayScheduler.getPrayerTimes(
            cal,
            latitude!!, longitude!!,
            timezone
        )

        val prayNames = prayScheduler.timeNames
        val today = arrayListOf<SholatTimeModel>()

        for ((index, value) in prayTimes.withIndex()) {
            if (prayNames[index] != "Sunrise" && prayNames[index] != "Sunset") {
                today.add(SholatTimeModel(prayNames[index], value))

                if (prayNames[index].equals("Maghrib")) {
                    val hour = value.split(":")[0]
                    val minute = value.split(":")[1]
                    enableReminder(hour.toInt(), minute.toInt(), 0, true)
                }
            }
        }

        binding.schedule = today

        return today
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

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     *
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnCompleteListener { taskLocation ->
                if (taskLocation.isSuccessful && taskLocation.result != null) {

                    val location = taskLocation.result

                    latitude = location?.latitude
                    longitude = location?.longitude

                    Log.d(TAG, "getLastLocation: lat: $latitude, lng: $longitude")

                    calculatePrayTimes()

                } else {
                    Log.w(TAG, "getLastLocation:exception", taskLocation.exception)
                    showSnackbar(R.string.no_location_detected)
                }
            }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    ACCESS_FINE_LOCATION
                ) == PERMISSION_GRANTED


    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                ACCESS_COARSE_LOCATION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)
        ) {
            // Provide an additional rationale to the user. This would happen if the user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale, android.R.string.ok, View.OnClickListener {
                // Request permission
                startLocationPermissionRequest()
            })

        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    private fun showSnackbar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content), getString(snackStrId),
            LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")

                // Permission granted.
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> getLastLocation()

                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                else -> {
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        })
                }
            }
        }
    }
}
