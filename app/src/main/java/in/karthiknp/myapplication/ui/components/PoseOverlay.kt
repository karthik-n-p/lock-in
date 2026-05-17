package `in`.karthiknp.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType

@Composable
fun PoseOverlay(
    pose: Pose?,
    imageWidth: Int,
    imageHeight: Int,
    isFrontCamera: Boolean = true,
    elbowAngle: Double = 180.0,
    bodyAngle: Double = 180.0,
    shoulderAngle: Double = 90.0,
    mode: WorkoutType = WorkoutType.PUSHUP,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cW = size.width
        val cH = size.height

        // ── Plank Guide Line ──────────────────────────────────────────────────
        // Draw a horizontal dashed line as a positional reference for plank
        if (mode == WorkoutType.PLANK) {
            val guideY = cH * 0.6f
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(0f, guideY),
                end = Offset(cW, guideY),
                strokeWidth = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )
        }

        if (pose == null || imageWidth == 0 || imageHeight == 0) return@Canvas

        fun lm(id: Int) = pose.getPoseLandmark(id)

        fun mapX(x: Float): Float {
            val ratio = x / imageWidth
            return if (isFrontCamera) cW * (1f - ratio) else cW * ratio
        }
        fun mapY(y: Float): Float = cH * (y / imageHeight)

        fun isVisible(lm: PoseLandmark?) = lm != null && lm.inFrameLikelihood >= 0.5f

        // ── Arm color ─────────────────────────────────────────────────────────
        // PUSHUP: green when extended (angle > 155°), red when bent (< 90°)
        // PLANK:  green when shoulder angle in valid range (55–130°), amber otherwise
        val armColor = when (mode) {
            WorkoutType.PUSHUP -> {
                val t = ((elbowAngle - 90.0) / 70.0).coerceIn(0.0, 1.0).toFloat()
                Color(red = 1f - t, green = t, blue = 0.2f, alpha = 0.9f)
            }
            WorkoutType.PLANK -> {
                val inRange = shoulderAngle in 55.0..130.0
                if (inRange) Color(0xFFFF6142).copy(alpha = 0.85f)
                else         Color(0xFFFFAB40).copy(alpha = 0.9f) // amber = elbows misaligned
            }
        }

        // ── Torso/spine color ─────────────────────────────────────────────────
        // PLANK:  gradient from red (body sagging) to green (body straight)
        // PUSHUP: soft blue
        val torsoColor = when (mode) {
            WorkoutType.PLANK -> {
                val t = ((bodyAngle - 145.0) / 35.0).coerceIn(0.0, 1.0).toFloat()
                Color(red = 1f - t, green = t, blue = 0.2f, alpha = 0.9f)
            }
            WorkoutType.PUSHUP -> Color(0xFFFF2A42).copy(alpha = 0.85f)
        }

        val legColor   = Color(0xFFFFD700.toInt()).copy(alpha = 0.85f)
        val headColor  = Color.White.copy(alpha = 0.8f)
        val lineWidth  = (cW / 100f).coerceIn(4f, 10f)
        val dotRadius  = lineWidth * 1.4f

        data class Conn(val a: Int, val b: Int, val color: Color)
        val connections = listOf(
            Conn(PoseLandmark.NOSE, PoseLandmark.LEFT_EYE_INNER,   headColor),
            Conn(PoseLandmark.NOSE, PoseLandmark.RIGHT_EYE_INNER,  headColor),
            Conn(PoseLandmark.LEFT_EYE_INNER,  PoseLandmark.LEFT_EAR,  headColor),
            Conn(PoseLandmark.RIGHT_EYE_INNER, PoseLandmark.RIGHT_EAR, headColor),
            Conn(PoseLandmark.LEFT_SHOULDER,  PoseLandmark.RIGHT_SHOULDER, torsoColor),
            Conn(PoseLandmark.LEFT_SHOULDER,  PoseLandmark.LEFT_ELBOW,  armColor),
            Conn(PoseLandmark.LEFT_ELBOW,     PoseLandmark.LEFT_WRIST,  armColor),
            Conn(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, armColor),
            Conn(PoseLandmark.RIGHT_ELBOW,    PoseLandmark.RIGHT_WRIST, armColor),
            Conn(PoseLandmark.LEFT_SHOULDER,  PoseLandmark.LEFT_HIP,   torsoColor),
            Conn(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP,  torsoColor),
            Conn(PoseLandmark.LEFT_HIP,       PoseLandmark.RIGHT_HIP,  torsoColor),
            Conn(PoseLandmark.LEFT_HIP,   PoseLandmark.LEFT_KNEE,   legColor),
            Conn(PoseLandmark.LEFT_KNEE,  PoseLandmark.LEFT_ANKLE,  legColor),
            Conn(PoseLandmark.RIGHT_HIP,  PoseLandmark.RIGHT_KNEE,  legColor),
            Conn(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE, legColor),
        )

        connections.forEach { conn ->
            val a = lm(conn.a)
            val b = lm(conn.b)
            if (isVisible(a) && isVisible(b)) {
                drawLine(
                    color       = conn.color,
                    start       = Offset(mapX(a!!.position.x), mapY(a.position.y)),
                    end         = Offset(mapX(b!!.position.x), mapY(b.position.y)),
                    strokeWidth = lineWidth,
                    cap         = StrokeCap.Round
                )
            }
        }

        val highlightedJoints = setOf(
            PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_ELBOW,    PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.LEFT_WRIST,    PoseLandmark.RIGHT_WRIST,
            PoseLandmark.LEFT_HIP,      PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_KNEE,     PoseLandmark.RIGHT_KNEE,
            PoseLandmark.LEFT_ANKLE,    PoseLandmark.RIGHT_ANKLE
        )

        pose.allPoseLandmarks.forEach { lmk ->
            if (lmk.inFrameLikelihood < 0.5f) return@forEach
            val cx = mapX(lmk.position.x)
            val cy = mapY(lmk.position.y)
            val isKey = lmk.landmarkType in highlightedJoints

            drawCircle(
                color  = Color.White.copy(alpha = 0.6f),
                radius = if (isKey) dotRadius else dotRadius * 0.6f,
                center = Offset(cx, cy)
            )
            val fillColor = when (lmk.landmarkType) {
                PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW -> armColor
                PoseLandmark.LEFT_HIP,   PoseLandmark.RIGHT_HIP   -> torsoColor
                in highlightedJoints -> Color.White
                else -> Color.White.copy(alpha = 0.3f)
            }
            drawCircle(
                color  = fillColor,
                radius = if (isKey) dotRadius * 0.55f else dotRadius * 0.3f,
                center = Offset(cx, cy)
            )
        }
    }
}
