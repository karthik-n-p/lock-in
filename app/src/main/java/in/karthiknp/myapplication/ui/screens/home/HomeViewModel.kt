package `in`.karthiknp.myapplication.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.karthiknp.myapplication.data.local.FitnessDatabase
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import `in`.karthiknp.myapplication.data.repository.FitnessRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeStats(
    val totalPushups: Int      = 0,
    val totalPlankSec: Int     = 0,
    val maxPushupsDay: Int     = 0,
    val longestPlankSec: Int   = 0,
    val avgPushups: Double     = 0.0,
    val avgPlankSec: Double    = 0.0,
    val activeDays: Int        = 0,
    // New fields
    val bestSinglePushup: Int  = 0,
    val bestSinglePlankSec: Int = 0,
    val todayPushups: Int      = 0,
    val todayPlankSec: Int     = 0,
    val bestPushupDay: String? = null,
    val bestPlankDay: String?  = null
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FitnessRepository(
        FitnessDatabase.getDatabase(app).workoutDao()
    )

    // Split into typed combine blocks to avoid vararg Array<Any> ClassCastException
    private val intStats: Flow<IntArray> = combine(
        repo.getTotalPushups(),
        repo.getTotalPlankSeconds(),
        repo.getMaxPushupsInDay(),
        repo.getLongestPlankSeconds(),
        repo.getTotalActiveDays()
    ) { total, plank, maxDay, longest, days ->
        intArrayOf(total, plank, maxDay, longest, days)
    }

    private val doubleStats: Flow<DoubleArray> = combine(
        repo.getAvgPushups(),
        repo.getAvgPlankSeconds()
    ) { avgPU, avgPL -> doubleArrayOf(avgPU, avgPL) }

    private val singleSessionStats: Flow<IntArray> = combine(
        repo.getMaxSingleSessionPushups(),
        repo.getMaxSingleSessionPlankSec()
    ) { maxPU, maxPL -> intArrayOf(maxPU, maxPL) }

    private val bestDayStats: Flow<Pair<String?, String?>> = combine(
        repo.getBestPushupDay(),
        repo.getBestPlankDay()
    ) { puDay, plDay -> Pair(puDay, plDay) }

    // Today's log
    private val _todayLog = MutableStateFlow(DailyLog(date = java.time.LocalDate.now().toString()))
    val todayLog: StateFlow<DailyLog> = _todayLog.asStateFlow()

    init {
        viewModelScope.launch {
            val todayStr = java.time.LocalDate.now().toString()
            repo.getAllDailyLogs().collect { logs ->
                val log = logs.firstOrNull { it.date == todayStr }
                _todayLog.value = log ?: DailyLog(date = todayStr)
            }
        }
    }

    val stats: StateFlow<HomeStats> = combine(
        intStats, doubleStats, singleSessionStats, bestDayStats, _todayLog
    ) { ints, doubles, singles, bestDays, today ->
        HomeStats(
            totalPushups      = ints[0],
            totalPlankSec     = ints[1],
            maxPushupsDay     = ints[2],
            longestPlankSec   = ints[3],
            activeDays        = ints[4],
            avgPushups        = doubles[0],
            avgPlankSec       = doubles[1],
            bestSinglePushup  = singles[0],
            bestSinglePlankSec = singles[1],
            todayPushups      = today.pushupCount,
            todayPlankSec     = today.plankSeconds,
            bestPushupDay     = bestDays.first,
            bestPlankDay      = bestDays.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeStats())

    val currentStreak: StateFlow<Int> = repo.getCurrentStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val last30Days: StateFlow<List<DailyLog>> = repo.getLast30Days()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val prefs = `in`.karthiknp.myapplication.data.local.PreferencesManager(app)
    
    private val _showStreakFixDialog = MutableStateFlow<String?>(null)
    val showStreakFixDialog: StateFlow<String?> = _showStreakFixDialog.asStateFlow()

    init {
        checkStreakFixEligible()
    }

    private fun checkStreakFixEligible() {
        viewModelScope.launch {
            val logs = repo.getAllDailyLogs().first()
            if (logs.isEmpty()) return@launch

            val logMap = logs.associateBy { it.date }
            val today = java.time.LocalDate.now()
            val fmt = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
            val yesterdayStr = today.minusDays(1).format(fmt)
            
            val todayLog = logMap[today.format(fmt)]
            val yesterdayLog = logMap[yesterdayStr]
            
            val yesterdayActive = (yesterdayLog?.pushupCount ?: 0) > 0 || (yesterdayLog?.plankSeconds ?: 0) > 0 || (yesterdayLog?.isStreakFix == true)

            // If we missed yesterday, but we have older active days, we might want to fix it.
            // Let's see if day before yesterday was active to know if there's a streak worth fixing.
            val dayBeforeStr = today.minusDays(2).format(fmt)
            val dayBeforeLog = logMap[dayBeforeStr]
            val dayBeforeActive = (dayBeforeLog?.pushupCount ?: 0) > 0 || (dayBeforeLog?.plankSeconds ?: 0) > 0 || (dayBeforeLog?.isStreakFix == true)

            if (!yesterdayActive && dayBeforeActive) {
                // We missed yesterday, but we had a streak going!
                if (prefs.getStreakFixesUsedThisMonth() < `in`.karthiknp.myapplication.data.local.PreferencesManager.MAX_FIXES_PER_MONTH) {
                    _showStreakFixDialog.value = yesterdayStr
                }
            }
        }
    }

    fun dismissStreakFixDialog() {
        _showStreakFixDialog.value = null
    }

    fun applyStreakFix() {
        val dateToFix = _showStreakFixDialog.value ?: return
        viewModelScope.launch {
            if (prefs.useStreakFix()) {
                repo.applyStreakFix(dateToFix)
                _showStreakFixDialog.value = null
            }
        }
    }
}
