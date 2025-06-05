package com.example.prog_poe_2025

import NotificationWorker
import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

data class NotificationSettingsDTO(
    var isEnabled: Boolean = false,
    var frequency: String = "DAILY",
    var customIntervalMillis: Long? = null,
    var inactivityThresholdMillis: Long = 3 * 24 * 60 * 60 * 1000,
    var lastTransactionDate: String? = null,
    var lastReminderSent: String? = null
)

class Settings : AppCompatActivity() {

    private lateinit var logOut: LinearLayout
    private lateinit var notificationSwitch: Switch
    private lateinit var radioGroup: RadioGroup
    private lateinit var alterProfile: LinearLayout

    private lateinit var firestore: FirebaseFirestore
    private val PREFS_NAME = "user_prefs"
    private val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private val KEY_NOTIFICATION_FREQUENCY = "notification_frequency"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        firestore = FirebaseFirestore.getInstance()

        logOut = findViewById(R.id.llLogout)
        alterProfile = findViewById(R.id.subProfile)
        notificationSwitch = findViewById(R.id.switchNotify)
        radioGroup = findViewById(R.id.radioGroup)

        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = sharedPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
        notificationSwitch.isChecked = isEnabled

        val savedFreq = sharedPrefs.getString(KEY_NOTIFICATION_FREQUENCY, "1_min")
        when (savedFreq) {
            "1_min" -> radioGroup.check(R.id.oneMinute)
            "6_hr" -> radioGroup.check(R.id.sixHours)
            "12_hr" -> radioGroup.check(R.id.twelveHours)
            "24_hr" -> radioGroup.check(R.id.twentyFourHours)
        }

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked).apply()
            Log.d("Settings", "Notification toggle changed: $isChecked")

            val freq = sharedPrefs.getString(KEY_NOTIFICATION_FREQUENCY, "1_min") ?: "1_min"

            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                    return@setOnCheckedChangeListener
                }
                scheduleNotificationWorker(this, freq)
            } else {
                WorkManager.getInstance(this).cancelAllWorkByTag("budget_notify")
            }

            saveSettingsToFirebase(isChecked, freq)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val frequency = when (checkedId) {
                R.id.oneMinute -> "1_min"
                R.id.sixHours -> "6_hr"
                R.id.twelveHours -> "12_hr"
                R.id.twentyFourHours -> "24_hr"
                else -> "1_min"
            }
            sharedPrefs.edit().putString(KEY_NOTIFICATION_FREQUENCY, frequency).apply()
            Log.d("Settings", "Frequency changed: $frequency")

            if (notificationSwitch.isChecked) {
                scheduleNotificationWorker(this, frequency)
            }

            saveSettingsToFirebase(notificationSwitch.isChecked, frequency)
        }

        alterProfile.setOnClickListener {
            startActivity(Intent(this@Settings, ProfileSettings::class.java))
            finish()
        }

        logOut.setOnClickListener {
            startActivity(Intent(this@Settings, MainActivity::class.java))
            finish()
        }
    }

    private fun scheduleNotificationWorker(context: Context, frequencyKey: String) {
        val interval = when (frequencyKey) {
            "1_min" -> 15L to TimeUnit.MINUTES  // Min value for PeriodicWorkRequest
            "6_hr" -> 6L to TimeUnit.HOURS
            "12_hr" -> 12L to TimeUnit.HOURS
            "24_hr" -> 24L to TimeUnit.HOURS
            else -> 15L to TimeUnit.MINUTES
        }

        Log.d("NotificationWorker", "Scheduling notification worker: $frequencyKey (${interval.first} ${interval.second})")

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            interval.first, interval.second
        )
            .setInitialDelay(interval.first, interval.second)
            .addTag("budget_notify")
            .build()

        WorkManager.getInstance(context).cancelAllWorkByTag("budget_notify")
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "budget_notify",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun saveSettingsToFirebase(isEnabled: Boolean, frequency: String) {
        val settings = NotificationSettingsDTO(
            isEnabled = isEnabled,
            frequency = frequency
        )

        val userId = SessionManager.getUserId(applicationContext)

        if (userId.isNullOrBlank()) {
            Toast.makeText(this, "User ID not found. Cannot save settings.", Toast.LENGTH_LONG).show()
            Log.e("FirebaseSave", "User ID is null or blank. Cannot save.")
            return
        }

        Log.d("FirebaseSave", "Saving settings: $settings for user: $userId")

        firestore.collection("notification_settings")
            .document(userId)
            .set(settings)
            .addOnSuccessListener {
                Toast.makeText(this, "Settings saved to Firebase", Toast.LENGTH_SHORT).show()
                Log.d("FirebaseSave", "Settings successfully saved for user: $userId")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FirebaseSave", "Error saving settings", e)
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val freq = sharedPrefs.getString(KEY_NOTIFICATION_FREQUENCY, "1_min") ?: "1_min"
            scheduleNotificationWorker(this, freq)
            saveSettingsToFirebase(true, freq)
        }
    }
}
