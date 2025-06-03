import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun scheduleReminder(context: Context, intervalMillis: Long) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", false)

        if (!notificationsEnabled) {
            WorkManager.getInstance(context).cancelUniqueWork("budget_reminder")
            return // Stop scheduling if notifications are off
        }

        val intervalMinutes = TimeUnit.MILLISECONDS.toMinutes(intervalMillis).coerceAtLeast(15) // Minimum WorkManager interval
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            intervalMinutes, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "budget_reminder",
            ExistingPeriodicWorkPolicy.UPDATE, // Updates frequency if changed
            workRequest
        )
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("budget_reminder")
    }
}