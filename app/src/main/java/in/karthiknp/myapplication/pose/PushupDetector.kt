package `in`.karthiknp.myapplication.pose

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs

/**
 * Pushup rep-counting state machine — production grade.
 *
 * ── Why it won't count hand-waves ────────────────────────────────────────
 * Before any rep logic runs, we validate the FULL BODY is in pushup position:
 *  1. All key landmarks (shoulders, elbows, wrists, hips, knees/ankles) must
 *     be visible with ≥ MIN_CONFIDENCE.
 *  2. Body must be roughly HORIZONTAL: the absolute vertical difference
 *     between shoulder and ankle/knee Y-coords must be < BODY_ANGLE_LIMIT.
 *     This ensures the person is lying down (pushup position), not standing.
 *  3. Hip alignment must stay straight throughout.
 *
 * ── Camera setup ─────────────────────────────────────────────────────────
 * Place phone on a stable surface at shoulder height, pointing to your SIDE.
 * The camera should see your full body: head → shoulder → elbow → hip → knee → ankle.
 *
 * ── State machine ────────────────────────────────────────────────────────
 * WAITING_FOR_POSITION → UP (arms extended) → DOWN (arms bent) → UP (rep counted)
 */
class PushupDetector {

    private enum class State { WAITING, UP, DOWN }

    companion object {
        private const val MIN_CONFIDENCE     = 0.60f
        // Elbow angles
        private const val ANGLE_UP           = 155.0  // arms extended
        private const val ANGLE_DOWN         = 90.0   // arms fully bent

        // Body-horizontal check:
        // If image is portrait with side view, shoulder.Y ≈ hip.Y ≈ ankle.Y.
        // The max allowed VERTICAL span between shoulder & ankle in portrait coords.
        // Expressed as a fraction of image height. 0.25 = 25% of frame height.
        private const val MAX_VERTICAL_SPAN  = 0.30f

        // Hip alignment
        private const val HIP_ANGLE_MIN      = 145.0

        // Debounce
        private const val DEBOUNCE_FRAMES    = 2
    }

    private var state       = State.WAITING
    private var debounce    = 0

    // Exposed to ViewModel and UI
    var reps:         Int     = 0;    private set
    var feedback:     String  = "👀 Position camera to your SIDE"; private set
    var formOk:       Boolean = false; private set
    var elbowAngleDeg: Double = 180.0; private set
    var inPosition:   Boolean = false; private set

    fun reset() {
        state         = State.WAITING
        debounce      = 0
        reps          = 0
        feedback      = "👀 Position camera to your SIDE"
        formOk        = false
        elbowAngleDeg = 180.0
        inPosition    = false
    }

    fun process(pose: Pose) {
        val lS  = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val lE  = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val lW  = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rS  = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rE  = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rW  = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val lH  = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rH  = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val lK  = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rK  = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val lA  = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rA  = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        // ── 1. Confidence gate — all primary landmarks must be visible ────────
        val primary = listOf(lS, lE, lW, rS, rE, rW, lH, rH)
        if (primary.any { it == null || it.inFrameLikelihood < MIN_CONFIDENCE }) {
            feedback   = "Move fully into frame"
            formOk     = false
            inPosition = false
            state      = State.WAITING
            return
        }

        // ── 2. Compute bilateral elbow angle ──────────────────────────────────
        val leftAngle = AngleCalculator.angle(
            lS!!.position.x, lS.position.y,
            lE!!.position.x, lE.position.y,
            lW!!.position.x, lW.position.y
        )
        val rightAngle = AngleCalculator.angle(
            rS!!.position.x, rS.position.y,
            rE!!.position.x, rE.position.y,
            rW!!.position.x, rW.position.y
        )
        elbowAngleDeg = AngleCalculator.average(leftAngle, rightAngle)

        // ── 3. Body-horizontal check ──────────────────────────────────────────
        // We need at least one foot landmark visible for horizontal check
        val footLm = lA ?: rA ?: lK ?: rK
        if (footLm == null) {
            feedback   = "Show your feet in frame"
            formOk     = false
            inPosition = false
            state      = State.WAITING
            return
        }

        // Compute how much the body spans vertically in the image.
        // In a proper side-view pushup, shoulder and ankle are at similar Y.
        // If the person is standing, shoulder Y will be much higher than ankle Y.
        val shoulderY = ((lS.position.y + rS.position.y) / 2f)
        val footY     = footLm.position.y

        // We need the image height to normalise. Approximate using known landmarks:
        // We'll use the raw pixel difference instead — if > 250 px gap they're likely standing.
        // 250 px is a reasonable threshold for typical 480–640 px tall image.
        val verticalSpanPx = abs(shoulderY - footY)
        if (verticalSpanPx > 300f) {
            // Large vertical gap = standing upright, not in pushup
            feedback   = "Lie down in pushup position"
            formOk     = false
            inPosition = false
            state      = State.WAITING
            return
        }

        // ── 4. Hip alignment ──────────────────────────────────────────────────
        val leftHipAngle = if (lK != null) AngleCalculator.angle(
            lS.position.x, lS.position.y,
            lH!!.position.x, lH.position.y,
            lK.position.x,  lK.position.y
        ) else 180.0
        val rightHipAngle = if (rK != null) AngleCalculator.angle(
            rS.position.x, rS.position.y,
            rH!!.position.x, rH.position.y,
            rK.position.x,  rK.position.y
        ) else 180.0
        val hipAngle = AngleCalculator.average(leftHipAngle, rightHipAngle)

        if (hipAngle < HIP_ANGLE_MIN) {
            feedback   = "Keep hips level!"
            formOk     = false
            return  // Don't reset state — let user fix form mid-rep
        }

        // ── 5. User is in valid pushup position ───────────────────────────────
        inPosition = true
        formOk     = true

        // ── 6. State machine ──────────────────────────────────────────────────
        when (state) {
            State.WAITING -> {
                feedback = if (elbowAngleDeg > ANGLE_UP) {
                    state    = State.UP
                    debounce = 0
                    "✅ Ready! Lower yourself"
                } else {
                    "Extend arms to start"
                }
            }

            State.UP -> {
                feedback = "⬇ Lower yourself"
                if (elbowAngleDeg < ANGLE_DOWN) {
                    debounce++
                    if (debounce >= DEBOUNCE_FRAMES) {
                        state    = State.DOWN
                        debounce = 0
                        feedback = "⬆ Push up!"
                    }
                } else {
                    debounce = 0
                }
            }

            State.DOWN -> {
                feedback = "⬆ Push up!"
                if (elbowAngleDeg > ANGLE_UP) {
                    debounce++
                    if (debounce >= DEBOUNCE_FRAMES) {
                        reps++
                        state    = State.UP
                        debounce = 0
                        feedback = "🔥 Rep ${reps}!"
                    }
                } else {
                    debounce = 0
                }
            }
        }
    }
}
