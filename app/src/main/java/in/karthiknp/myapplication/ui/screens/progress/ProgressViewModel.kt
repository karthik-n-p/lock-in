package `in`.karthiknp.myapplication.ui.screens.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import `in`.karthiknp.myapplication.data.local.FitnessDatabase
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import `in`.karthiknp.myapplication.data.repository.FitnessRepository
import kotlinx.coroutines.flow.Flow

data class ProgressStats(
    val totalPushups: Int     = 0,
    val totalPlankSec: Int    = 0,
    val maxPushupsDay: Int    = 0,
    val longestPlankSec: Int  = 0,
    val avgPushups: Double    = 0.0,
    val avgPlankSec: Double   = 0.0,
    val activeDays: Int       = 0
)

class ProgressViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FitnessRepository(
        FitnessDatabase.getDatabase(app).workoutDao()
    )

    val last30Days: Flow<List<DailyLog>> = repo.getLast30Days()
    val last14Days: Flow<List<DailyLog>> = repo.getLast14Days()
    val last7Days:  Flow<List<DailyLog>> = repo.getLast7Days()

    val totalPushups:    Flow<Int>    = repo.getTotalPushups()
    val totalPlankSec:   Flow<Int>    = repo.getTotalPlankSeconds()
    val maxPushupsDay:   Flow<Int>    = repo.getMaxPushupsInDay()
    val longestPlankSec: Flow<Int>    = repo.getLongestPlankSeconds()
    val avgPushups:      Flow<Double> = repo.getAvgPushups()
    val avgPlankSec:     Flow<Double> = repo.getAvgPlankSeconds()
    val activeDays:      Flow<Int>    = repo.getTotalActiveDays()
}
