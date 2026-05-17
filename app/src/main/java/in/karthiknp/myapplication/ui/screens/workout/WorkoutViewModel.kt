package `in`.karthiknp.myapplication.ui.screens.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.pose.Pose
import `in`.karthiknp.myapplication.data.local.FitnessDatabase
import `in`.karthiknp.myapplication.data.local.PreferencesManager
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType
import `in`.karthiknp.myapplication.data.repository.FitnessRepository
import `in`.karthiknp.myapplication.pose.PlankDetector
import `in`.karthiknp.myapplication.pose.PushupDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Form state for plank — drives the Green/Yellow/Red indicator system.
 * GOOD   = timer running, form is correct (Green glow)
 * WARNING = form broken <2s, timer still running (Yellow pulse — "fix it NOW!")
 * PAUSED  = form broken >2s, timer paused (Red — must re-engage)
 */
enum class PlankFormState { GOOD, WARNING, PAUSED }

class WorkoutViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FitnessRepository(
        FitnessDatabase.getDatabase(app).workoutDao()
    )
    private val prefs = PreferencesManager(app)

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

    /** Plank form state: GOOD / WARNING / PAUSED — drives UI color indicators */
    private val _plankFormState = MutableStateFlow(PlankFormState.PAUSED)
    val plankFormState: StateFlow<PlankFormState> = _plankFormState.asStateFlow()

    // ─── Shared feedback ──────────────────────────────────────────────────────
    private val _feedback = MutableStateFlow("Get into position")
    val feedback: StateFlow<String> = _feedback.asStateFlow()

    private val _formOk = MutableStateFlow(false)
    val formOk: StateFlow<Boolean> = _formOk.asStateFlow()

    private val _workoutSaved = MutableStateFlow(false)
    val workoutSaved: StateFlow<Boolean> = _workoutSaved.asStateFlow()

    // ─── Personal Best detection ──────────────────────────────────────────────
    private val _isNewPB = MutableStateFlow(false)
    val isNewPB: StateFlow<Boolean> = _isNewPB.asStateFlow()

    private val _pbMessage = MutableStateFlow("")
    val pbMessage: StateFlow<String> = _pbMessage.asStateFlow()

    private var previousBestPushups = 0
    private var previousBestPlankSec = 0

    // ─── Motivational quote for completion ────────────────────────────────────
    private val _completionQuote = MutableStateFlow("")
    val completionQuote: StateFlow<String> = _completionQuote.asStateFlow()

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

    // ─── Plank grace period tracking ──────────────────────────────────────────
    private var formBreakStartMs: Long = 0L
    private val GRACE_PERIOD_MS = 2000L  // 2-second grace period

    init {
        // Load previous bests for PB detection
        viewModelScope.launch {
            previousBestPushups = repo.getMaxSingleSessionPushups().first()
            previousBestPlankSec = repo.getMaxSingleSessionPlankSec().first()
        }
    }

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
        _plankFormState.value = PlankFormState.PAUSED
        _isNewPB.value       = false
        _pbMessage.value     = ""
        formBreakStartMs     = 0L
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
        val prevReps = pushupDetector.reps
        pushupDetector.process(pose)
        _reps.value         = pushupDetector.reps
        _feedback.value     = pushupDetector.feedback
        _formOk.value       = pushupDetector.formOk
        _elbowAngle.value   = pushupDetector.elbowAngleDeg
        // Expose whether arms are in DOWN state for ghost posture target selection
        _pushupIsDown.value = !pushupDetector.formOk || pushupDetector.elbowAngleDeg < 115.0

        // Check for new PB on each new rep
        if (pushupDetector.reps > prevReps && pushupDetector.reps > previousBestPushups) {
            _isNewPB.value = true
            _pbMessage.value = PreferencesManager.PB_QUOTES.random()
        }
    }

    private fun processPlank(pose: Pose) {
        plankDetector.process(pose)
        _feedback.value      = plankDetector.feedback
        val nowHolding       = plankDetector.isHolding
        _formOk.value        = nowHolding
        _bodyAngle.value     = plankDetector.bodyAngleDeg
        _shoulderAngle.value = plankDetector.shoulderAngleDeg
        _plankFormIssue.value = plankDetector.formIssue

        // ── Plank form state machine with 2-second grace period ──────────────
        if (nowHolding) {
            // Form is GOOD — reset break timer
            formBreakStartMs = 0L
            _plankFormState.value = PlankFormState.GOOD
            _isPlankActive.value = true

            // Start or continue the plank timer
            if (plankTimerJob == null) {
                plankTimerJob = viewModelScope.launch {
                    while (true) {
                        delay(1000L)
                        // Only tick when in GOOD or WARNING state
                        val state = _plankFormState.value
                        if (state == PlankFormState.GOOD || state == PlankFormState.WARNING) {
                            _plankSeconds.value++
                            // Check for new PB
                            if (_plankSeconds.value > previousBestPlankSec && previousBestPlankSec > 0) {
                                _isNewPB.value = true
                                _pbMessage.value = PreferencesManager.PB_QUOTES.random()
                                previousBestPlankSec = _plankSeconds.value // prevent repeated triggers
                            }
                        }
                    }
                }
            }
        } else {
            // Form broken
            if (_isPlankActive.value) {
                if (formBreakStartMs == 0L) {
                    // First frame of break — start grace timer
                    formBreakStartMs = System.currentTimeMillis()
                    _plankFormState.value = PlankFormState.WARNING
                } else {
                    val elapsed = System.currentTimeMillis() - formBreakStartMs
                    if (elapsed > GRACE_PERIOD_MS) {
                        // Grace period exceeded — PAUSE the timer
                        _plankFormState.value = PlankFormState.PAUSED
                        _isPlankActive.value = false
                        plankTimerJob?.cancel()
                        plankTimerJob = null
                    } else {
                        // Still within grace — WARNING state (timer continues)
                        _plankFormState.value = PlankFormState.WARNING
                    }
                }
            } else {
                _plankFormState.value = PlankFormState.PAUSED
            }
        }
    }

    fun finishWorkout() {
        val endMs = System.currentTimeMillis()
        // Record the workout hour for smart notification timing
        val hour = java.time.LocalDateTime.now().hour
        prefs.setLastWorkoutHour(hour)

        _completionQuote.value = PreferencesManager.MOTIVATION_QUOTES.random()

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

    fun dismissPB() {
        _isNewPB.value = false
        _pbMessage.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        plankTimerJob?.cancel()
    }
}
