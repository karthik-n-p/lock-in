package `in`.karthiknp.myapplication.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import `in`.karthiknp.myapplication.MainActivity
import `in`.karthiknp.myapplication.data.local.FitnessDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dao = FitnessDatabase.getDatabase(appContext).workoutDao()
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        val todayLog = dao.getDailyLog(todayStr)
        val hasWorkedOut = (todayLog?.pushupCount ?: 0) > 0 || (todayLog?.plankSeconds ?: 0) > 0

        if (!hasWorkedOut) {
            showNotification()
        }

        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun showNotification() {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "lockin_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to keep your streak alive"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using a built-in icon for now
            .setContentTitle("Lock In! 🔥")
            .setContentText("Don't break your streak! You haven't worked out today.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
