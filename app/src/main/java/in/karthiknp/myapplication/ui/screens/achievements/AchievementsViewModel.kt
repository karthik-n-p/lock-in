package `in`.karthiknp.myapplication.ui.screens.achievements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.karthiknp.myapplication.data.local.FitnessDatabase
import `in`.karthiknp.myapplication.data.local.entity.Achievement
import `in`.karthiknp.myapplication.data.repository.FitnessRepository
import kotlinx.coroutines.flow.*

class AchievementsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FitnessRepository(FitnessDatabase.getDatabase(app).workoutDao())

    val achievements: StateFlow<List<Achievement>> = repo.getAllAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
