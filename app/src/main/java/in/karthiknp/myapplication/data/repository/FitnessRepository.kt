package `in`.karthiknp.myapplication.data.repository

import `in`.karthiknp.myapplication.data.local.dao.WorkoutDao
import `in`.karthiknp.myapplication.data.local.entity.Achievement
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import `in`.karthiknp.myapplication.data.local.entity.WorkoutSession
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

class FitnessRepository(private val dao: WorkoutDao) {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    // ─── Reads ────────────────────────────────────────────────────────────────

    fun getAllDailyLogs(): Flow<List<DailyLog>> = dao.getAllDailyLogs()
    fun getLast30Days(): Flow<List<DailyLog>>   = dao.getLast30Days()
    fun getLast7Days(): Flow<List<DailyLog>>    = dao.getLast7Days()
    fun getLast14Days(): Flow<List<DailyLog>>   = dao.getLast14Days()
    fun getAllSessions(): Flow<List<WorkoutSession>> = dao.getAllSessions()
    fun getAllAchievements(): Flow<List<Achievement>> = dao.getAllAchievements()

    fun getTotalPushups(): Flow<Int>        = dao.getTotalPushups()
    fun getTotalPlankSeconds(): Flow<Int>   = dao.getTotalPlankSeconds()
    fun getMaxPushupsInDay(): Flow<Int>     = dao.getMaxPushupsInDay()
    fun getLongestPlankSeconds(): Flow<Int> = dao.getLongestPlankSeconds()
    fun getAvgPushups(): Flow<Double>       = dao.getAvgPushups()
    fun getAvgPlankSeconds(): Flow<Double>  = dao.getAvgPlankSeconds()
    fun getTotalActiveDays(): Flow<Int>     = dao.getTotalActiveDays()

    // ─── Streak Logic ─────────────────────────────────────────────────────────

    fun getCurrentStreak(): Flow<Int> = dao.getAllDailyLogs().map { logs ->
        if (logs.isEmpty()) return@map 0

        val logMap = logs.associateBy { it.date }
        var streak = 0
        val today = LocalDate.now()
        var currentDate = today

        // If today is empty and yesterday is empty, streak is 0
        val todayActive = isActive(logMap[today.format(fmt)])
        val yesterdayActive = isActive(logMap[today.minusDays(1).format(fmt)])

        if (!todayActive && !yesterdayActive) {
            return@map 0
        }

        // Count backward from today
        while (true) {
            val dateStr = currentDate.format(fmt)
            if (isActive(logMap[dateStr])) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                // If it's today and we haven't worked out yet, we don't break the streak
                if (currentDate == today && yesterdayActive) {
                    currentDate = currentDate.minusDays(1)
                } else {
                    break
                }
            }
        }
        streak
    }

    private fun isActive(log: DailyLog?): Boolean {
        if (log == null) return false
        return log.pushupCount > 0 || log.plankSeconds > 0 || log.isStreakFix
    }

    suspend fun applyStreakFix(missedDateStr: String) {
        val existing = dao.getDailyLog(missedDateStr)
        if (existing == null) {
            dao.upsertDailyLog(
                DailyLog(
                    date = missedDateStr,
                    isStreakFix = true
                )
            )
        } else {
            dao.upsertDailyLog(existing.copy(isStreakFix = true))
        }
    }

    // ─── Writes ───────────────────────────────────────────────────────────────

    suspend fun saveWorkoutSession(
        type: WorkoutType,
        startMs: Long,
        endMs: Long,
        reps: Int,
        durationSeconds: Int
    ) {
        val today = LocalDate.now().format(fmt)

        // 1. Save the session record
        dao.insertSession(
            WorkoutSession(
                id = UUID.randomUUID().toString(),
                type = type,
                date = today,
                startTimeMs = startMs,
                endTimeMs = endMs,
                totalReps = reps,
                durationSeconds = durationSeconds
            )
        )

        // 2. Upsert the daily aggregate
        val existing = dao.getDailyLog(today)
        val updatedLog = if (existing != null) {
            existing.copy(
                pushupCount  = existing.pushupCount  + (if (type == WorkoutType.PUSHUP) reps else 0),
                plankSeconds = existing.plankSeconds + (if (type == WorkoutType.PLANK) durationSeconds else 0)
            )
        } else {
            DailyLog(
                date         = today,
                pushupCount  = if (type == WorkoutType.PUSHUP) reps else 0,
                plankSeconds = if (type == WorkoutType.PLANK) durationSeconds else 0
            )
        }
        dao.upsertDailyLog(updatedLog)

        // 3. Check achievements
        checkAndUnlockAchievements(type)
    }

    private suspend fun checkAndUnlockAchievements(type: WorkoutType) {
        val now = System.currentTimeMillis()

        if (type == WorkoutType.PUSHUP) {
            val total = dao.getTotalPushups().first()
            listOf("pushup_50" to 50, "pushup_100" to 100, "pushup_200" to 200,
                   "pushup_500" to 500, "pushup_1000" to 1000).forEach { (id, threshold) ->
                val a = dao.getAchievement(id)
                if (a != null && a.unlockedAt == null && total >= threshold) {
                    dao.upsertAchievement(a.copy(unlockedAt = now))
                }
            }
        } else {
            val longest = dao.getLongestPlankSeconds().first()
            listOf("plank_30" to 30, "plank_60" to 60, "plank_120" to 120,
                   "plank_300" to 300).forEach { (id, threshold) ->
                val a = dao.getAchievement(id)
                if (a != null && a.unlockedAt == null && longest >= threshold) {
                    dao.upsertAchievement(a.copy(unlockedAt = now))
                }
            }
        }
    }
}
