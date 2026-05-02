package `in`.karthiknp.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WorkoutType { PUSHUP, PLANK }

/** One record per calendar day — aggregated totals. */
@Entity(tableName = "daily_log")
data class DailyLog(
    @PrimaryKey
    val date: String,           // "YYYY-MM-DD"
    val pushupCount: Int = 0,
    val plankSeconds: Int = 0,
    val pushupGoal: Int = 30,
    val plankGoal: Int = 60,
    val streakDay: Int = 0,     // consecutive days active up to this date
    @ColumnInfo(defaultValue = "0")
    val isStreakFix: Boolean = false // True if this day was bridged using a Streak Fix
)

/** One record per finished workout session. */
@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey
    val id: String,
    val type: WorkoutType,
    val date: String,           // "YYYY-MM-DD"
    val startTimeMs: Long,
    val endTimeMs: Long,
    val totalReps: Int = 0,     // pushups
    val durationSeconds: Int = 0// plank
)

/** One row per achievement milestone — unlocked or not. */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,             // e.g. "pushup_100"
    val title: String,
    val description: String,
    val type: WorkoutType,
    val threshold: Int,         // reps or seconds required
    val unlockedAt: Long? = null// null = locked
)
