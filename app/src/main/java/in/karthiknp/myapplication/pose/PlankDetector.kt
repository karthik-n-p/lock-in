package `in`.karthiknp.myapplication.pose

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs

/**
 * Plank posture validator — FRONT CAMERA, portrait mode.
 *
 * Camera: phone propped in front of user at ~chest height, user faces camera.
 *
 * ── Key insight for front-camera geometry ────────────────────────────────────
 * When the user is in PLANK (body horizontal, facing camera):
 *   • Their shoulders and hips appear at SIMILAR Y positions in the image.
 *     The body extends backward (depth), so the torso looks "compact" vertically.
 *   • Ratio = |shoulderY - hipY| / shoulderSpanX  →  typically 0.1 – 0.9
 *
 * When the user is STANDING:
 *   • The hips are far BELOW the shoulders in image Y.
 *   • Ratio = |shoulderY - hipY| / shoulderSpanX  →  typically 1.5 – 3.0
 *
 * This shoulder-hip compactness ratio is the reliable front-camera anti-stand check.
 * It uses only shoulders + hips (always visible) instead of ankles (often hidden).
 *
 * Secondary check: body angle (shoulder→hip→foot) ≥ BODY_ANGLE_MIN, when feet visible.
 *
 * Shoulder/elbow angle: informational only (colors skeleton), never blocks timer.
 */
class PlankDetector {

    companion object {
        private const val MIN_CONFIDENCE     = 0.45f
        private const val BODY_ANGLE_MIN     = 140.0

        // Front-camera anti-stand: shoulder-to-hip Y delta / shoulder span.
        // Plank = compact (< threshold), Standing = tall (> threshold).
        private const val COMPACTNESS_MAX    = 1.1f  // > this = standing / bad position

        // Shoulder angle range — INFORMATIONAL ONLY
        private const val SHOULDER_ANGLE_MIN = 15.0
        private const val SHOULDER_ANGLE_MAX = 165.0

        private const val HOLD_FRAMES_START  = 3   // good frames to start
        private const val HOLD_FRAMES_STOP   = 3   // bad frames to stop (fast response)
    }

    var isHolding:        Boolean   = false; private set
    var feedback:         String    = "👀 Get in front of camera"; private set
    var bodyAngleDeg:     Double    = 180.0; private set
    var shoulderAngleDeg: Double    = 90.0;  private set
    var isBodyFlat:       Boolean   = false; private set
    var formIssue:        FormIssue = FormIssue.NOT_IN_FRAME; private set

    private var goodFrames = 0
    private var badFrames  = 0

    enum class FormIssue {
        NONE, NOT_IN_FRAME, HIPS_TOO_HIGH, HIPS_TOO_LOW,
        ELBOWS_FORWARD, ELBOWS_BACK, STANDING
    }

    fun reset() {
        isHolding        = false
        goodFrames       = 0
        badFrames        = 0
        feedback         = "👀 Get in front of camera"
        bodyAngleDeg     = 180.0
        shoulderAngleDeg = 90.0
        isBodyFlat       = false
        formIssue        = FormIssue.NOT_IN_FRAME
    }

    fun process(pose: Pose) {
        val lShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val lElbow    = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rElbow    = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val lHip      = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rHip      = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val lAnkle    = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rAnkle    = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        val lKnee     = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rKnee     = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)

        // ── 1. Require shoulders + hips ──────────────────────────────────────
        val required = listOf(lShoulder, rShoulder, lHip, rHip)
        if (required.any { it == null || it.inFrameLikelihood < MIN_CONFIDENCE }) {
            handleBadFrame("Move shoulders & hips into frame", FormIssue.NOT_IN_FRAME)
            return
        }

        val shoulderY  = (lShoulder!!.position.y + rShoulder!!.position.y) / 2f
        val hipY       = (lHip!!.position.y       + rHip!!.position.y)      / 2f
        val shoulderSpanX = abs(lShoulder.position.x - rShoulder.position.x).coerceAtLeast(1f)

        // ── 2. Front-camera anti-stand: shoulder-hip compactness ─────────────
        // In plank (horizontal body), shoulders and hips appear at similar Y.
        // In standing, hips are far below shoulders → large ratio.
        val compactness = abs(shoulderY - hipY) / shoulderSpanX
        if (compactness > COMPACTNESS_MAX) {
            handleBadFrame("Lie down in plank position", FormIssue.STANDING)
            return
        }
        isBodyFlat = true

        // ── 3. Body angle check (only when leg landmarks available) ──────────
        val footLm = sequenceOf(lAnkle, rAnkle, lKnee, rKnee)
            .firstOrNull { it != null && it.inFrameLikelihood >= MIN_CONFIDENCE }

        if (footLm != null) {
            val leftBodyAngle = AngleCalculator.angle(
                lShoulder.position.x, lShoulder.position.y,
                lHip.position.x,      lHip.position.y,
                footLm.position.x,    footLm.position.y
            )
            val rightBodyAngle = AngleCalculator.angle(
                rShoulder.position.x, rShoulder.position.y,
                rHip.position.x,      rHip.position.y,
                footLm.position.x,    footLm.position.y
            )
            bodyAngleDeg = AngleCalculator.average(leftBodyAngle, rightBodyAngle)

            if (bodyAngleDeg < BODY_ANGLE_MIN) {
                val midY  = (lShoulder.position.y + footLm.position.y) / 2f
                val issue = if (hipY < midY) FormIssue.HIPS_TOO_HIGH else FormIssue.HIPS_TOO_LOW
                val msg   = if (issue == FormIssue.HIPS_TOO_HIGH) "⬇ Lower your hips!" else "⬆ Raise your hips!"
                handleBadFrame(msg, issue)
                return
            }
        } else {
            bodyAngleDeg = 170.0 // good default when no feet visible
        }

        // ── 4. Shoulder/elbow angle — informational only ─────────────────────
        if (lElbow != null && rElbow != null &&
            lElbow.inFrameLikelihood > MIN_CONFIDENCE &&
            rElbow.inFrameLikelihood > MIN_CONFIDENCE) {

            val leftSA = AngleCalculator.angle(
                lElbow.position.x,    lElbow.position.y,
                lShoulder.position.x, lShoulder.position.y,
                lHip.position.x,      lHip.position.y
            )
            val rightSA = AngleCalculator.angle(
                rElbow.position.x,    rElbow.position.y,
                rShoulder.position.x, rShoulder.position.y,
                rHip.position.x,      rHip.position.y
            )
            shoulderAngleDeg = AngleCalculator.average(leftSA, rightSA)
            // Update formIssue for skeleton color — does NOT block timer
            when {
                shoulderAngleDeg < SHOULDER_ANGLE_MIN -> formIssue = FormIssue.ELBOWS_FORWARD
                shoulderAngleDeg > SHOULDER_ANGLE_MAX -> formIssue = FormIssue.ELBOWS_BACK
            }
        }

        // ── 5. Good frame ────────────────────────────────────────────────────
        badFrames  = 0
        goodFrames++
        formIssue  = FormIssue.NONE
        if (!isHolding && goodFrames >= HOLD_FRAMES_START) {
            isHolding = true
            feedback  = "🔥 Hold it!"
        } else if (!isHolding) {
            feedback = "✅ Hold position…"
        } else {
            feedback = "💪 Great form! Keep holding!"
        }
    }

    private fun handleBadFrame(reason: String, issue: FormIssue) {
        goodFrames = 0
        badFrames++
        formIssue  = issue
        if (isHolding && badFrames >= HOLD_FRAMES_STOP) {
            isHolding = false
            feedback  = reason
        } else if (!isHolding) {
            feedback = reason
        }
    }
}
