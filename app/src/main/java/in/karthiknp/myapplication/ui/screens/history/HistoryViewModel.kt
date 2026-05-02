package `in`.karthiknp.myapplication.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.karthiknp.myapplication.data.local.FitnessDatabase
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import `in`.karthiknp.myapplication.data.repository.FitnessRepository
import kotlinx.coroutines.flow.*

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FitnessRepository(FitnessDatabase.getDatabase(app).workoutDao())

    val logs: StateFlow<List<DailyLog>> = repo.getAllDailyLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
