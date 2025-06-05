import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit


class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val lastNotificationTime = prefs.getLong("last_notification_time", 0)
        val currentTime = System.currentTimeMillis()
        val intervalMillis = getFrequencyMillis(prefs.getString("notification_frequency", "1_min") ?: "1_min")
        val budgetEnabled = prefs.getBoolean("budget_notifications_enabled", false)
        val transactionEnabled = prefs.getBoolean("transaction_notifications_enabled", false)

        if (currentTime - lastNotificationTime < intervalMillis) {
            Log.d("NotificationWorker", "Skipping notifications: Interval not reached")
            return Result.success()
        }

        if (budgetEnabled) showBudgetReminder()
        if (transactionEnabled) showQuizReminder()

        prefs.edit().putLong("last_notification_time", currentTime).apply()
        return Result.success()
    }

    private fun getFrequencyMillis(frequencyKey: String): Long {
        return when (frequencyKey) {
            "15_min" -> TimeUnit.MINUTES.toMillis(15)  // 🔹 Lowest interval: 15 minutes
            "1_hr" -> TimeUnit.HOURS.toMillis(1)      // 🔹 1 hour
            "6_hr" -> TimeUnit.HOURS.toMillis(6)      // 🔹 6 hours
            "12_hr" -> TimeUnit.HOURS.toMillis(12)    // 🔹 12 hours
            else -> TimeUnit.HOURS.toMillis(1)        // 🔹 Default to 1 hour if invalid key
        }
    }

    private fun showBudgetReminder() {
        saveNotification("Budget Reminder")
        sendNotification("Budget Reminder", "Don't forget to log a transaction!", "default_channel", 1)
    }

    private fun showQuizReminder() {
        saveNotification("Quiz Reminder")
        sendNotification("Quiz Reminder", "It's time to play your daily quiz!", "quiz_channel", 2)
    }

    private fun sendNotification(title: String, message: String, channelId: String, notificationId: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun saveNotification(title: String) {
        val prefs = applicationContext.getSharedPreferences("notification_history", Context.MODE_PRIVATE)
        val history = prefs.getStringSet("notifications", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        // 🔹 Add timestamp to the notification
        val timestamp = System.currentTimeMillis()
        val newNotification = "$title | $timestamp"

        // 🔹 Convert to a sorted list to keep the latest notifications
        val updatedHistory = history.toList().sortedByDescending {
            it.split(" | ").getOrNull(1)?.toLongOrNull() ?: 0L
        }.toMutableList()

        // 🔹 Ensure only the last 5 notifications are retained
        updatedHistory.add(newNotification)
        while (updatedHistory.size > 5) {
            updatedHistory.removeAt(0) // Remove the oldest entry
        }

        prefs.edit().putStringSet("notifications", updatedHistory.toSet()).apply()
    }
}
/**
 * Source: https://developer.android.com/topic/libraries/architecture/workmanager/how-to/define-work
 * Author: Android Developers (Google)
 * License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
 * Adapted by: Reaobaka Ntoagae & Ade-Eza Silongo for Pennywise
 * Purpose: Implements a Worker that creates and displays notifications in the background
 * Modifications:
 * - Customized notification content and channels
 * - Added support for specific app notification logic
 */