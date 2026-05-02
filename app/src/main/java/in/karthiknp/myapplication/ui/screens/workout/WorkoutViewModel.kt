package `in`.karthiknp.myapplication.ui.screens.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.pose.Pose
import `in`.karthiknp.myapplication.data.local.FitnessDatabase
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType
import `in`.karthiknp.myapplication.data.repository.FitnessRepository
import `in`.karthiknp.myapplication.pose.PlankDetector
import `in`.karthiknp.myapplication.pose.PushupDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FitnessRepository(
        FitnessDatabase.getDatabase(app).workoutDao()
    )

    // ─── Mode ─────────────────────────────────────────────────────────────────
    private val _mode = MutableStateFlow(WorkoutType.PUSHUP)
    val mode: StateFlow<WorkoutType> = _mode.asStateFlow()

    // ─── Pushup state ─────────────────────────────────────────────────────────
    private val _reps = MutableStateFlow(0)
    val reps: StateFlow<Int> = _reps.asStateFlow()

    /** True when the pushup is in DOWN state — used to pick ghost posture target */
    private val _pushupIsDown = MutableStateFlow(false)
    val pushupIsDown: StateFlow<Boolean> = _pushupIsDown.asStateFlow()

    // ─── Plank state ──────────────────────────────────────────────────────────
    private val _plankSeconds = MutableStateFlow(0)
    val plankSeconds: StateFlow<Int> = _plankSeconds.asStateFlow()

    private val _isPlankActive = MutableStateFlow(false)
    val isPlankActive: StateFlow<Boolean> = _isPlankActive.asStateFlow()

    // ─── Shared feedback ──────────────────────────────────────────────────────
    private val _feedback = MutableStateFlow("Get into position")
    val feedback: StateFlow<String> = _feedback.asStateFlow()

    private val _formOk = MutableStateFlow(false)
    val formOk: StateFlow<Boolean> = _formOk.asStateFlow()

    private val _workoutSaved = MutableStateFlow(false)
    val workoutSaved: StateFlow<Boolean> = _workoutSaved.asStateFlow()

    // ─── Pose overlay state ───────────────────────────────────────────────────
    private val _currentPose   = MutableStateFlow<Pose?>(null)
    val currentPose: StateFlow<Pose?> = _currentPose.asStateFlow()

    private val _imageWidth    = MutableStateFlow(1)
    val imageWidth: StateFlow<Int> = _imageWidth.asStateFlow()

    private val _imageHeight   = MutableStateFlow(1)
    val imageHeight: StateFlow<Int> = _imageHeight.asStateFlow()

    private val _elbowAngle    = MutableStateFlow(180.0)
    val elbowAngle: StateFlow<Double> = _elbowAngle.asStateFlow()

    private val _bodyAngle     = MutableStateFlow(180.0)
    val bodyAngle: StateFlow<Double> = _bodyAngle.asStateFlow()

    private val _shoulderAngle = MutableStateFlow(90.0)
    val shoulderAngle: StateFlow<Double> = _shoulderAngle.asStateFlow()

    /** Current plank form issue — drives ghost highlight color */
    private val _plankFormIssue = MutableStateFlow(PlankDetector.FormIssue.NOT_IN_FRAME)
    val plankFormIssue: StateFlow<PlankDetector.FormIssue> = _plankFormIssue.asStateFlow()

    // ─── Internal ─────────────────────────────────────────────────────────────
    private val pushupDetector  = PushupDetector()
    private val plankDetector   = PlankDetector()
    private var plankTimerJob: Job? = null
    private var sessionStartMs  = System.currentTimeMillis()

    fun setMode(type: WorkoutType) {
        _mode.value         = type
        pushupDetector.reset()
        plankDetector.reset()
        plankTimerJob?.cancel()
        plankTimerJob        = null
        _reps.value          = 0
        _plankSeconds.value  = 0
        _isPlankActive.value = false
        _pushupIsDown.value  = false
        _feedback.value      = "Get into position"
        _formOk.value        = false
        _currentPose.value   = null
        _plankFormIssue.value = PlankDetector.FormIssue.NOT_IN_FRAME
        sessionStartMs       = System.currentTimeMillis()
    }

    /** Called from PoseAnalyzer with pose + the display-oriented image dimensions. */
    fun processPose(pose: Pose, imgW: Int, imgH: Int) {
        _currentPose.value  = pose
        _imageWidth.value   = imgW
        _imageHeight.value  = imgH

        when (_mode.value) {
            WorkoutType.PUSHUP -> processPushup(pose)
            WorkoutType.PLANK  -> processPlank(pose)
        }
    }

    private fun processPushup(pose: Pose) {
        pushupDetector.process(pose)
        _reps.value         = pushupDetector.reps
        _feedback.value     = pushupDetector.feedback
        _formOk.value       = pushupDetector.formOk
        _elbowAngle.value   = pushupDetector.elbowAngleDeg
        // Expose whether arms are in DOWN state for ghost posture target selection
        _pushupIsDown.value = !pushupDetector.formOk || pushupDetector.elbowAngleDeg < 115.0
    }

    private fun processPlank(pose: Pose) {
        plankDetector.process(pose)
        _feedback.value      = plankDetector.feedback
        val nowHolding       = plankDetector.isHolding
        _isPlankActive.value = nowHolding
        _formOk.value        = nowHolding
        _bodyAngle.value     = plankDetector.bodyAngleDeg
        _shoulderAngle.value = plankDetector.shoulderAngleDeg
        _plankFormIssue.value = plankDetector.formIssue

        if (nowHolding && plankTimerJob == null) {
            plankTimerJob = viewModelScope.launch {
                while (true) {
                    delay(1000L)
                    if (_isPlankActive.value) _plankSeconds.value++
                }
            }
        } else if (!nowHolding) {
            plankTimerJob?.cancel()
            plankTimerJob = null
        }
    }

    fun finishWorkout() {
        val endMs = System.currentTimeMillis()
        viewModelScope.launch {
            repo.saveWorkoutSession(
                type            = _mode.value,
                startMs         = sessionStartMs,
                endMs           = endMs,
                reps            = _reps.value,
                durationSeconds = _plankSeconds.value
            )
            _workoutSaved.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        plankTimerJob?.cancel()
    }
}
