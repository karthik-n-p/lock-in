package `in`.karthiknp.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lockin_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_STREAK_FIXES_USED = "streak_fixes_used"
        private const val KEY_LAST_FIX_MONTH = "last_fix_month"
        private const val KEY_DAY_END_HOUR = "day_end_hour"
        private const val KEY_LAST_WORKOUT_HOUR = "last_workout_hour"
        const val MAX_FIXES_PER_MONTH = 3

        /** Motivational quotes for notifications and celebrations */
        val MOTIVATION_QUOTES = listOf(
            "You're a champion! 🏆 Keep crushing it!",
            "Legends are built one rep at a time. You're doing it! ⚡",
            "Your future self will thank you for today! 💎",
            "Discipline beats motivation every single day. You're proof! 🔥",
            "Champions don't skip days. You didn't! 👑",
            "Your consistency is your superpower! ⚡",
            "Every rep is a vote for the person you're becoming! 🎯",
            "You're in the top 1% who actually show up! 💪",
            "Rest is earned. You've earned it today! 🌟",
            "The grind never stops. Neither do you! 🚀",
            "Pain is temporary. Glory is forever! ⚡",
            "You're rewriting your limits right now! 💎",
            "Consistency is the mother of mastery! 🏆",
            "You didn't come this far to only come this far! 🔥",
            "Your body is a machine. Fuel it with effort! 💪"
        )

        val STREAK_QUOTES = listOf(
            "Your streak is on fire! Don't let it die! 🔥",
            "⚡ Warriors don't break chains. Protect your streak!",
            "🏆 Your streak is your trophy. Keep polishing it!",
            "💎 Every day you show up, you level up!",
            "🔥 You're building something unstoppable!",
            "⚡ Streak = Discipline = Power!",
            "👑 Kings and Queens don't miss days!"
        )

        val BADGE_QUOTES = listOf(
            "🏆 Badge unlocked! You're ascending to greatness!",
            "💎 New achievement! The grind is paying off!",
            "⚡ Level up! You're becoming a legend!",
            "🌟 Another milestone crushed! Unstoppable!",
            "👑 Achievement unlocked! You're royalty now!"
        )

        val PB_QUOTES = listOf(
            "🔥 NEW PERSONAL BEST! You're rewriting history!",
            "⚡ RECORD BROKEN! You're evolving!",
            "💎 NEW PB! Yesterday's limit is today's warmup!",
            "🏆 PERSONAL RECORD! Champions keep raising the bar!",
            "👑 NEW BEST! The only competition is yourself — and you won!"
        )
    }

    /** Returns how many fixes have been used this month */
    fun getStreakFixesUsedThisMonth(): Int {
        val currentMonth = getCurrentMonthString()
        val lastMonthSaved = prefs.getString(KEY_LAST_FIX_MONTH, "")

        if (currentMonth != lastMonthSaved) {
            // New month! Reset counter
            resetFixesCounter()
            return 0
        }
        return prefs.getInt(KEY_STREAK_FIXES_USED, 0)
    }

    /** Increments the usage count of streak fixes for the current month */
    fun useStreakFix(): Boolean {
        val used = getStreakFixesUsedThisMonth()
        if (used < MAX_FIXES_PER_MONTH) {
            val currentMonth = getCurrentMonthString()
            prefs.edit()
                .putInt(KEY_STREAK_FIXES_USED, used + 1)
                .putString(KEY_LAST_FIX_MONTH, currentMonth)
                .apply()
            return true
        }
        return false
    }

    // ─── Day End Hour ────────────────────────────────────────────────────────
    /** Hour (0-23) when the user's "day" ends. Default 0 = midnight. */
    fun getDayEndHour(): Int = prefs.getInt(KEY_DAY_END_HOUR, 0)

    fun setDayEndHour(hour: Int) {
        prefs.edit().putInt(KEY_DAY_END_HOUR, hour.coerceIn(0, 6)).apply()
    }

    /**
     * Returns the effective "today" date string, accounting for the custom day-end hour.
     * If current time is before dayEndHour, the effective date is yesterday.
     */
    fun getEffectiveToday(): String {
        val now = LocalDateTime.now()
        val dayEnd = getDayEndHour()
        val effectiveDate = if (now.hour < dayEnd) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        return effectiveDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // ─── Last Workout Hour (for smart notification timing) ───────────────────
    fun getLastWorkoutHour(): Int = prefs.getInt(KEY_LAST_WORKOUT_HOUR, -1)

    fun setLastWorkoutHour(hour: Int) {
        prefs.edit().putInt(KEY_LAST_WORKOUT_HOUR, hour).apply()
    }

    private fun resetFixesCounter() {
        prefs.edit()
            .putInt(KEY_STREAK_FIXES_USED, 0)
            .putString(KEY_LAST_FIX_MONTH, getCurrentMonthString())
            .apply()
    }

    private fun getCurrentMonthString(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }
}
