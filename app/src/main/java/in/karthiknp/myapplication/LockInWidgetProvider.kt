package `in`.karthiknp.myapplication

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import `in`.karthiknp.myapplication.data.local.FitnessDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LockInWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scope.launch {
            val dao = FitnessDatabase.getDatabase(context).workoutDao()
            val today = LocalDate.now()
            val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Today's data
            val todayLog = dao.getDailyLog(todayStr)
            val todayPushups = todayLog?.pushupCount ?: 0
            val todayPlankSec = todayLog?.plankSeconds ?: 0
            
            val plankStr = if (todayPlankSec < 60) "${todayPlankSec}s" else {
                val m = todayPlankSec / 60
                val s = todayPlankSec % 60
                if (s == 0) "${m}m" else "${m}m${s}s"
            }

            // Goal completion % (default: 30 pushups + 60s plank = 100%)
            val pushupGoal = todayLog?.pushupGoal ?: 30
            val plankGoal = todayLog?.plankGoal ?: 60
            val pushupPct = (todayPushups.toFloat() / pushupGoal).coerceIn(0f, 1f)
            val plankPct = (todayPlankSec.toFloat() / plankGoal).coerceIn(0f, 1f)
            val goalPct = ((pushupPct + plankPct) / 2f * 100).toInt()

            // Calculate streak
            val allLogs = dao.getAllDailyLogs().first()
            val logMap = allLogs.associateBy { it.date }
            val fmt = DateTimeFormatter.ISO_LOCAL_DATE
            var streak = 0
            var cursor = today

            val todayActive = isActive(logMap[today.format(fmt)])
            val yesterdayActive = isActive(logMap[today.minusDays(1).format(fmt)])

            if (todayActive || yesterdayActive) {
                while (true) {
                    val dateStr = cursor.format(fmt)
                    if (isActive(logMap[dateStr])) {
                        streak++
                        cursor = cursor.minusDays(1)
                    } else if (cursor == today && yesterdayActive) {
                        cursor = cursor.minusDays(1)
                    } else {
                        break
                    }
                }
            }

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                views.setTextViewText(R.id.widget_streak, "🔥 $streak")
                views.setTextViewText(R.id.widget_pushups, "🏋️ $todayPushups")
                views.setTextViewText(R.id.widget_plank, "⏱️ $plankStr")
                views.setTextViewText(R.id.widget_goal_pct, "$goalPct%")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun isActive(log: `in`.karthiknp.myapplication.data.local.entity.DailyLog?): Boolean {
        if (log == null) return false
        return log.pushupCount > 0 || log.plankSeconds > 0 || log.isStreakFix
    }
}
