import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("NotificationWorker", "Worker triggered")

        val prefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("notifications_enabled", false)
        Log.d("NotificationWorker", "Notifications enabled: $enabled")

        if (enabled) {
            showBudgetReminder()
            showQuizReminder()
            Log.d("NotificationWorker", "Notifications shown")
        } else {
            Log.d("NotificationWorker", "Notifications skipped")
        }

        return Result.success()
    }

    private fun showBudgetReminder() {
        val channelId = "default_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Budget Reminder")
            .setContentText("Don't forget to log a transaction!!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun showQuizReminder() {
        val channelId = "quiz_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Quiz Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Quiz Reminder")
            .setContentText("It's time to play your daily quiz! Test your knowledge now.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification) // Unique ID for the quiz notification
    }
}