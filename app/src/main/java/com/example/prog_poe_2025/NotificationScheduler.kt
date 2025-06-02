import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun scheduleReminder(context: Context, intervalMillis: Long) {
        val intervalHours = TimeUnit.MILLISECONDS.toHours(intervalMillis)
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            intervalHours, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "budget_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("budget_reminder")
    }
}
