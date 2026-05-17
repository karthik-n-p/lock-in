package `in`.karthiknp.myapplication.data.local.dao

import androidx.room.*
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import `in`.karthiknp.myapplication.data.local.entity.WorkoutSession
import `in`.karthiknp.myapplication.data.local.entity.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ─── Daily Log ────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyLog(log: DailyLog)

    @Query("SELECT * FROM daily_log WHERE date = :date LIMIT 1")
    suspend fun getDailyLog(date: String): DailyLog?

    @Query("SELECT * FROM daily_log ORDER BY date DESC")
    fun getAllDailyLogs(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log ORDER BY date DESC LIMIT 30")
    fun getLast30Days(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log ORDER BY date DESC LIMIT 7")
    fun getLast7Days(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log ORDER BY date DESC LIMIT 14")
    fun getLast14Days(): Flow<List<DailyLog>>

    // ─── Sessions ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY startTimeMs DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    // ─── Lifetime Stats ───────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(pushupCount), 0) FROM daily_log")
    fun getTotalPushups(): Flow<Int>

    @Query("SELECT COALESCE(SUM(plankSeconds), 0) FROM daily_log")
    fun getTotalPlankSeconds(): Flow<Int>

    @Query("SELECT COALESCE(MAX(pushupCount), 0) FROM daily_log")
    fun getMaxPushupsInDay(): Flow<Int>

    @Query("SELECT COALESCE(MAX(plankSeconds), 0) FROM daily_log")
    fun getLongestPlankSeconds(): Flow<Int>

    @Query("SELECT COALESCE(AVG(pushupCount), 0.0) FROM daily_log WHERE pushupCount > 0")
    fun getAvgPushups(): Flow<Double>

    @Query("SELECT COALESCE(AVG(plankSeconds), 0.0) FROM daily_log WHERE plankSeconds > 0")
    fun getAvgPlankSeconds(): Flow<Double>

    @Query("SELECT COUNT(*) FROM daily_log WHERE pushupCount > 0 OR plankSeconds > 0")
    fun getTotalActiveDays(): Flow<Int>

    // ─── Single-Session Records ───────────────────────────────────────────

    @Query("SELECT COALESCE(MAX(totalReps), 0) FROM workout_sessions WHERE type = 'PUSHUP'")
    fun getMaxSingleSessionPushups(): Flow<Int>

    @Query("SELECT COALESCE(MAX(durationSeconds), 0) FROM workout_sessions WHERE type = 'PLANK'")
    fun getMaxSingleSessionPlankSec(): Flow<Int>

    // ─── Best Day (date + value) ──────────────────────────────────────────

    @Query("SELECT date FROM daily_log ORDER BY pushupCount DESC LIMIT 1")
    fun getBestPushupDay(): Flow<String?>

    @Query("SELECT date FROM daily_log ORDER BY plankSeconds DESC LIMIT 1")
    fun getBestPlankDay(): Flow<String?>

    // ─── Achievements ─────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAchievement(achievement: Achievement)

    @Query("SELECT * FROM achievements ORDER BY type, threshold ASC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getAchievement(id: String): Achievement?
}
