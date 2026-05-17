package `in`.karthiknp.myapplication.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType
import `in`.karthiknp.myapplication.pose.PlankDetector

/**
 * Ghost Posture Overlay — draws a semi-transparent "ideal skeleton" on top of the camera feed.
 *
 * The ghost is ANCHORED to the user's detected hip center so it stays contextually close
 * to where the user actually is. When no pose is detected, a centered default ghost is shown.
 *
 * Visibility logic:
 *   - When formOk == true  → ghost fades OUT (user has matched the ideal pose)
 *   - When formOk == false → ghost fades IN  (guides the user toward correct position)
 *
 * Ghost style: hollow white dashed lines at 45% alpha, clearly distinct from the
 * solid real-skeleton drawn by PoseOverlay.
 */
@Composable
fun GhostPostureOverlay(
    pose: Pose?,
    imageWidth: Int,
    imageHeight: Int,
    isFrontCamera: Boolean = true,
    formOk: Boolean,
    mode: WorkoutType,
    formIssue: PlankDetector.FormIssue = PlankDetector.FormIssue.NONE,
    pushupIsDown: Boolean = false,       // true = show DOWN target; false = show UP target
    modifier: Modifier = Modifier
) {
    // Animate alpha: visible when form is bad, hidden when form is good
    val alpha by animateFloatAsState(
        targetValue = if (formOk) 0f else 0.55f,
        animationSpec = tween(durationMillis = 400),
        label = "ghostAlpha"
    )

    Canvas(modifier = modifier) {
        if (alpha <= 0.01f) return@Canvas

        val cW = size.width
        val cH = size.height

        // ── Coordinate mapping helpers ────────────────────────────────────────
        fun mapX(x: Float): Float {
            if (imageWidth == 0) return cW / 2f
            val ratio = x / imageWidth
            return if (isFrontCamera) cW * (1f - ratio) else cW * ratio
        }
        fun mapY(y: Float): Float {
            if (imageHeight == 0) return cH / 2f
            return cH * (y / imageHeight)
        }

        // ── Find anchor: detected hip center, or screen center if no pose ─────
        val lHip = pose?.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rHip = pose?.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        val anchorX: Float
        val anchorY: Float
        val scale: Float  // scale ghost to match apparent body size

        if (lHip != null && rHip != null &&
            lHip.inFrameLikelihood >= 0.4f && rHip.inFrameLikelihood >= 0.4f) {
            anchorX = (mapX(lHip.position.x) + mapX(rHip.position.x)) / 2f
            anchorY = (mapY(lHip.position.y) + mapY(rHip.position.y)) / 2f
            // Use shoulder-to-hip distance as scale reference if available
            val lShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
            val rShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
            scale = if (lShoulder != null && rShoulder != null) {
                val detectedHipY  = anchorY
                val detectedShoulderY = (mapY(lShoulder.position.y) + mapY(rShoulder.position.y)) / 2f
                val span = Math.abs(detectedShoulderY - detectedHipY).coerceIn(cH * 0.05f, cH * 0.35f)
                span / (cH * 0.18f)  // normalize against default ghost scale
            } else {
                1.0f
            }
        } else {
            // No detected pose — draw ghost at center of screen
            anchorX = cW / 2f
            anchorY = cH * 0.55f
            scale   = 1.0f
        }

        // ── Draw the appropriate ghost ────────────────────────────────────────
        when (mode) {
            WorkoutType.PLANK  -> drawPlankGhost(anchorX, anchorY, scale, alpha, cW, cH, formIssue)
            WorkoutType.PUSHUP -> drawPushupGhost(anchorX, anchorY, scale, alpha, cW, cH, pushupIsDown)
        }
    }
}

// ─── Plank Ghost ─────────────────────────────────────────────────────────────

private fun DrawScope.drawPlankGhost(
    anchorX: Float,
    anchorY: Float,
    scale: Float,
    alpha: Float,
    cW: Float,
    cH: Float,
    issue: PlankDetector.FormIssue
) {
    val ghostColor  = Color.White.copy(alpha = alpha)
    // Highlight the problematic body part in amber to give directional cue
    val issueColor  = Color(0xFFFFAB40).copy(alpha = alpha * 1.2f)
    val dashEffect  = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
    val strokeW     = (cW / 80f * scale).coerceIn(3f, 8f)
    val dotR        = strokeW * 1.3f

    // ── Ghost plank geometry (front camera, user horizontal) ─────────────────
    // In front view, the body spans horizontally across the image.
    // Anchor = hip center.
    val unit = cW * 0.10f * scale  // base unit = 10% of screen width × scale

    // Key body points relative to anchor (hip center)
    // The user is seen from front, so horizontal extent = left/right in screen
    // Shoulders are ABOVE the hip in front-camera plank (user's torso toward camera top)
    val shoulderX = anchorX
    val shoulderY = anchorY - unit * 1.5f   // shoulders above hips

    val headX = shoulderX
    val headY = shoulderY - unit * 0.7f

    val ankleX = anchorX
    val ankleY = anchorY + unit * 2.0f      // ankles below hips

    val kneeX  = anchorX
    val kneeY  = anchorY + unit * 1.0f

    // Arms: in forearm plank, elbows extend forward (toward camera = upward in front view)
    // Left/right spread for visual clarity
    val lShoulderX = shoulderX - unit * 0.8f
    val rShoulderX = shoulderX + unit * 0.8f
    val lElbowX = lShoulderX - unit * 0.2f
    val rElbowX = rShoulderX + unit * 0.2f
    val elbowY  = shoulderY + unit * 0.9f   // elbows below shoulders in front view
    val lWristX = lElbowX - unit * 0.1f
    val rWristX = rElbowX + unit * 0.1f
    val wristY  = elbowY + unit * 0.5f

    val lHipX = anchorX - unit * 0.5f
    val rHipX = anchorX + unit * 0.5f

    // ── Determine per-segment colors (highlight problem area) ─────────────────
    val torsoLineColor = when (issue) {
        PlankDetector.FormIssue.HIPS_TOO_HIGH,
        PlankDetector.FormIssue.HIPS_TOO_LOW -> issueColor
        else -> ghostColor
    }
    val armColor = when (issue) {
        PlankDetector.FormIssue.ELBOWS_FORWARD,
        PlankDetector.FormIssue.ELBOWS_BACK -> issueColor
        else -> ghostColor
    }

    // ── Draw ghost skeleton ───────────────────────────────────────────────────
    // Spine line (shoulder to hip to knee to ankle)
    listOf(
        Pair(Offset(shoulderX, shoulderY), Offset(anchorX, anchorY)),
        Pair(Offset(anchorX, anchorY),     Offset(kneeX, kneeY)),
        Pair(Offset(kneeX, kneeY),         Offset(ankleX, ankleY))
    ).forEach { (s, e) ->
        drawLine(torsoLineColor, s, e, strokeW, StrokeCap.Round, pathEffect = dashEffect)
    }

    // Head
    drawLine(ghostColor, Offset(headX, headY), Offset(shoulderX, shoulderY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)
    drawCircle(ghostColor.copy(alpha = alpha * 0.5f), dotR * 1.2f, Offset(headX, headY))

    // Shoulder crossbar
    drawLine(torsoLineColor, Offset(lShoulderX, shoulderY), Offset(rShoulderX, shoulderY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)

    // Hip crossbar
    drawLine(torsoLineColor, Offset(lHipX, anchorY), Offset(rHipX, anchorY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)

    // Left arm
    drawLine(armColor, Offset(lShoulderX, shoulderY), Offset(lElbowX, elbowY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)
    drawLine(armColor, Offset(lElbowX, elbowY), Offset(lWristX, wristY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)

    // Right arm
    drawLine(armColor, Offset(rShoulderX, shoulderY), Offset(rElbowX, elbowY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)
    drawLine(armColor, Offset(rElbowX, elbowY), Offset(rWristX, wristY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)

    // Key joint dots
    listOf(
        Offset(lShoulderX, shoulderY), Offset(rShoulderX, shoulderY),
        Offset(lElbowX, elbowY),       Offset(rElbowX, elbowY),
        Offset(lHipX, anchorY),        Offset(rHipX, anchorY),
        Offset(kneeX, kneeY),          Offset(ankleX, ankleY)
    ).forEach { pt ->
        drawCircle(ghostColor.copy(alpha = alpha * 0.6f), dotR * 0.7f, pt)
    }

    // ── "IDEAL" label arc above ghost ────────────────────────────────────────
    // (Cannot draw text in Canvas without nativeCanvas — draw a small ✓ hint dot)
    drawCircle(Color(0xFFFF6142).copy(alpha = alpha * 0.8f), dotR * 1.0f, Offset(shoulderX, headY - unit * 0.5f))
}

// ─── Pushup Ghost ────────────────────────────────────────────────────────────

private fun DrawScope.drawPushupGhost(
    anchorX: Float,
    anchorY: Float,
    scale: Float,
    alpha: Float,
    cW: Float,
    cH: Float,
    isDown: Boolean
) {
    val ghostColor = Color.White.copy(alpha = alpha)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
    val strokeW    = (cW / 80f * scale).coerceIn(3f, 8f)
    val dotR       = strokeW * 1.3f
    val unit       = cW * 0.10f * scale

    // Pushup from front camera: arms visible as two points (elbows closer/farther from body)
    val shoulderY = anchorY - unit * 1.5f
    val headY     = shoulderY - unit * 0.7f
    val kneeY     = anchorY + unit * 1.0f
    val ankleY    = anchorY + unit * 2.0f

    val lShoulderX = anchorX - unit * 0.8f
    val rShoulderX = anchorX + unit * 0.8f
    val lHipX      = anchorX - unit * 0.5f
    val rHipX      = anchorX + unit * 0.5f

    // Elbow position depends on UP vs DOWN state
    // UP  = arms extended → elbows far from body in depth → appear near shoulder in front view
    // DOWN = arms bent → elbows spread wider / lower
    val elbowYOffset = if (isDown) unit * 1.4f else unit * 0.6f
    val elbowXSpread = if (isDown) unit * 1.2f else unit * 0.9f
    val lElbowX = anchorX - elbowXSpread
    val rElbowX = anchorX + elbowXSpread
    val elbowY  = shoulderY + elbowYOffset

    val lWristX = lElbowX - unit * 0.1f
    val rWristX = rElbowX + unit * 0.1f
    val wristY  = elbowY + unit * 0.5f

    // Target color: green for UP (extend!), amber for DOWN (lower!)
    val targetColor = if (isDown)
        Color(0xFFFFAB40).copy(alpha = alpha)
    else
        Color(0xFFFF6142).copy(alpha = alpha)

    // Spine
    listOf(
        Pair(Offset(anchorX, shoulderY), Offset(anchorX, anchorY)),
        Pair(Offset(anchorX, anchorY),   Offset(anchorX, kneeY)),
        Pair(Offset(anchorX, kneeY),     Offset(anchorX, ankleY))
    ).forEach { (s, e) ->
        drawLine(ghostColor, s, e, strokeW, StrokeCap.Round, pathEffect = dashEffect)
    }

    // Head
    drawLine(ghostColor, Offset(anchorX, headY), Offset(anchorX, shoulderY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)
    drawCircle(ghostColor.copy(alpha = alpha * 0.4f), dotR * 1.2f, Offset(anchorX, headY))

    // Shoulder bar
    drawLine(ghostColor, Offset(lShoulderX, shoulderY), Offset(rShoulderX, shoulderY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)
    // Hip bar
    drawLine(ghostColor, Offset(lHipX, anchorY), Offset(rHipX, anchorY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)

    // Arms — colored by target state
    drawLine(targetColor, Offset(lShoulderX, shoulderY), Offset(lElbowX, elbowY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)
    drawLine(targetColor, Offset(lElbowX, elbowY), Offset(lWristX, wristY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)
    drawLine(targetColor, Offset(rShoulderX, shoulderY), Offset(rElbowX, elbowY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)
    drawLine(targetColor, Offset(rElbowX, elbowY), Offset(rWristX, wristY),
        strokeW, StrokeCap.Round, pathEffect = dashEffect)

    // Key joint dots
    listOf(
        Offset(lShoulderX, shoulderY), Offset(rShoulderX, shoulderY),
        Offset(lElbowX, elbowY),       Offset(rElbowX, elbowY),
        Offset(lHipX, anchorY),        Offset(rHipX, anchorY),
        Offset(anchorX, kneeY),        Offset(anchorX, ankleY)
    ).forEach { pt ->
        drawCircle(targetColor.copy(alpha = alpha * 0.6f), dotR * 0.7f, pt)
    }
}
