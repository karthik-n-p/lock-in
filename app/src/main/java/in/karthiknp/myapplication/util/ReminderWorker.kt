package `in`.karthiknp.myapplication.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import `in`.karthiknp.myapplication.MainActivity
import `in`.karthiknp.myapplication.data.local.FitnessDatabase
import `in`.karthiknp.myapplication.data.local.PreferencesManager
import java.time.LocalDate
import java.time.LocalDateTime
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

        val currentHour = LocalDateTime.now().hour
        val isMorning = currentHour in 7..12
        val isEvening = currentHour in 18..22

        when {
            // Morning motivation (even if they've already worked out — reward their habits)
            isMorning -> {
                if (!hasWorkedOut) {
                    showMotivationNotification()
                }
            }
            // Evening streak warning (only if they haven't worked out)
            isEvening && !hasWorkedOut -> {
                showStreakWarningNotification()
            }
            // Default: generic check
            !hasWorkedOut -> {
                showMotivationNotification()
            }
        }

        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun showMotivationNotification() {
        val quote = PreferencesManager.MOTIVATION_QUOTES.random()
        showNotification(
            title = "⚡ Lock In — Rise & Grind!",
            body = quote,
            notificationId = 1001
        )
    }

    @SuppressLint("MissingPermission")
    private fun showStreakWarningNotification() {
        val quote = PreferencesManager.STREAK_QUOTES.random()
        showNotification(
            title = "🔥 Your Streak Is at Risk!",
            body = quote,
            notificationId = 1002
        )
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, body: String, notificationId: Int) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "lockin_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lock In Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Smart reminders to keep you on track"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(soundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
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
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
