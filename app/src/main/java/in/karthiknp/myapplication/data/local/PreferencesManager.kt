package `in`.karthiknp.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lockin_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_STREAK_FIXES_USED = "streak_fixes_used"
        private const val KEY_LAST_FIX_MONTH = "last_fix_month"
        const val MAX_FIXES_PER_MONTH = 2
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
