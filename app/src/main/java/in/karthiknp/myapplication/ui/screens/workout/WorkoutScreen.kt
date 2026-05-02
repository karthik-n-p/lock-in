package `in`.karthiknp.myapplication.ui.screens.workout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.karthiknp.myapplication.camera.CameraPreview
import `in`.karthiknp.myapplication.camera.PoseAnalyzer
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType
import `in`.karthiknp.myapplication.ui.components.GhostPostureOverlay
import `in`.karthiknp.myapplication.ui.components.PoseOverlay

@Composable
fun WorkoutScreen(
    onFinish: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val mode            by viewModel.mode.collectAsState()
    val reps            by viewModel.reps.collectAsState()
    val plankSec        by viewModel.plankSeconds.collectAsState()
    val isPlankOn       by viewModel.isPlankActive.collectAsState()
    val feedback        by viewModel.feedback.collectAsState()
    val formOk          by viewModel.formOk.collectAsState()
    val workoutSaved    by viewModel.workoutSaved.collectAsState()

    // Overlay state
    val currentPose     by viewModel.currentPose.collectAsState()
    val imageWidth      by viewModel.imageWidth.collectAsState()
    val imageHeight     by viewModel.imageHeight.collectAsState()
    val elbowAngle      by viewModel.elbowAngle.collectAsState()
    val bodyAngle       by viewModel.bodyAngle.collectAsState()
    val shoulderAngle   by viewModel.shoulderAngle.collectAsState()
    val plankFormIssue  by viewModel.plankFormIssue.collectAsState()
    val pushupIsDown    by viewModel.pushupIsDown.collectAsState()

    // Create the analyzer once
    val analyzer = remember {
        PoseAnalyzer { pose, w, h -> viewModel.processPose(pose, w, h) }
    }

    LaunchedEffect(workoutSaved) { if (workoutSaved) onFinish() }

    val feedbackBgColor by animateColorAsState(
        targetValue = when {
            feedback.contains("Rep")   -> Color(0xFF00C853)
            feedback.contains("Hold")  -> Color(0xFF6C63FF)
            feedback.contains("Great") -> Color(0xFF6C63FF)
            !formOk                    -> Color(0xFFFF1744)
            else                       -> Color(0xFF6C63FF)
        },
        animationSpec = tween(250),
        label = "feedbackColor"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── Camera Feed ───────────────────────────────────────────────────────
        CameraPreview(
            modifier       = Modifier.fillMaxSize(),
            useFrontCamera = true,
            analyzer       = analyzer
        )

        // ── Ghost Posture Overlay (below real skeleton so ghost is behind) ────
        GhostPostureOverlay(
            pose          = currentPose,
            imageWidth    = imageWidth,
            imageHeight   = imageHeight,
            isFrontCamera = true,
            formOk        = formOk,
            mode          = mode,
            formIssue     = plankFormIssue,
            pushupIsDown  = pushupIsDown,
            modifier      = Modifier.fillMaxSize()
        )

        // ── Real Skeleton Overlay ─────────────────────────────────────────────
        PoseOverlay(
            pose          = currentPose,
            imageWidth    = imageWidth,
            imageHeight   = imageHeight,
            isFrontCamera = true,
            elbowAngle    = elbowAngle,
            bodyAngle     = bodyAngle,
            shoulderAngle = shoulderAngle,
            mode          = mode,
            modifier      = Modifier.fillMaxSize()
        )

        // ── Top gradient scrim ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Black.copy(0.75f), Color.Transparent)
                    )
                )
        )

        // ── Bottom gradient scrim ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(0.85f))
                    )
                )
        )

        // ── Top Bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.finishWorkout() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            // Mode tab selector
            ModeSelector(currentMode = mode) { viewModel.setMode(it) }
            Spacer(Modifier.weight(1f))
        }

        // ── Central Stats Display ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-20).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val mainValue = if (mode == WorkoutType.PUSHUP) "$reps" else formatTime(plankSec)
            Text(
                text       = mainValue,
                color      = Color.White,
                fontSize   = 88.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-2).sp
            )
            Text(
                text      = if (mode == WorkoutType.PUSHUP) "PUSHUPS" else "PLANK",
                color     = Color.White.copy(alpha = 0.65f),
                fontSize  = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp
            )

            if (mode == WorkoutType.PLANK) {
                Spacer(Modifier.height(10.dp))
                PlankIndicator(isActive = isPlankOn)
            }
        }

        // ── Angle Chips ───────────────────────────────────────────────────────
        if (mode == WorkoutType.PUSHUP) {
            ElbowAngleChip(
                angle    = elbowAngle,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            )
        } else if (mode == WorkoutType.PLANK) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                BodyAngleChip(angle = bodyAngle)
                ShoulderAngleChip(angle = shoulderAngle)
            }
        }

        // ── Live Feedback Bar ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 108.dp)
                .background(feedbackBgColor.copy(alpha = 0.92f), RoundedCornerShape(50))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = feedback,
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
        }

        // ── Ghost Legend Badge ────────────────────────────────────────────────
        // Shown when ghost is visible (form is bad) — explains the dashed overlay
        if (!formOk) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 72.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "- - - ideal posture",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Finish Button ─────────────────────────────────────────────────────
        Button(
            onClick = { viewModel.finishWorkout() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 32.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
        ) {
            Text("FINISH", fontSize = 17.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        }
    }
}

@Composable
private fun ModeSelector(currentMode: WorkoutType, onSelect: (WorkoutType) -> Unit) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        listOf(WorkoutType.PUSHUP to "💪 PUSHUPS", WorkoutType.PLANK to "🧘 PLANK").forEach { (type, label) ->
            val selected = currentMode == type
            Surface(
                onClick  = { onSelect(type) },
                color    = if (selected) Color.White else Color.Transparent,
                shape    = RoundedCornerShape(50),
                modifier = Modifier.padding(2.dp)
            ) {
                Text(
                    text       = label,
                    color      = if (selected) Color.Black else Color.White,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun PlankIndicator(isActive: Boolean) {
    val color = if (isActive) Color(0xFF00E676) else Color(0xFFFFAB40)
    val label = if (isActive) "● HOLDING" else "○ NOT DETECTED"
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ElbowAngleChip(angle: Double, modifier: Modifier = Modifier) {
    val t       = ((angle - 90.0) / 70.0).coerceIn(0.0, 1.0).toFloat()
    val color   = Color(red = 1f - t, green = t, blue = 0.2f)
    Column(
        modifier = modifier
            .background(Color.Black.copy(0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = "${angle.toInt()}°",
            color      = color,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text    = "elbow",
            color   = Color.White.copy(0.5f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun BodyAngleChip(angle: Double, modifier: Modifier = Modifier) {
    val t       = ((angle - 145.0) / 35.0).coerceIn(0.0, 1.0).toFloat()
    val color   = Color(red = 1f - t, green = t, blue = 0.2f)
    Column(
        modifier = modifier
            .background(Color.Black.copy(0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = "${angle.toInt()}°",
            color      = color,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text    = "body",
            color   = Color.White.copy(0.5f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ShoulderAngleChip(angle: Double, modifier: Modifier = Modifier) {
    // 55–130° is the valid elbow-under-shoulder range; color accordingly
    val inRange = angle in 55.0..130.0
    val color   = if (inRange) Color(0xFF00E676) else Color(0xFFFFAB40)
    Column(
        modifier = modifier
            .background(Color.Black.copy(0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = "${angle.toInt()}°",
            color      = color,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text    = "shoulder",
            color   = Color.White.copy(0.5f),
            fontSize = 10.sp
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60; val s = seconds % 60
    return "%d:%02d".format(m, s)
}
