package `in`.karthiknp.myapplication.ui.screens.workout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import `in`.karthiknp.myapplication.ui.theme.*

@Composable
fun WorkoutScreen(
    initialMode: WorkoutType = WorkoutType.PUSHUP,
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
    val plankFormState  by viewModel.plankFormState.collectAsState()
    val isNewPB         by viewModel.isNewPB.collectAsState()
    val pbMessage       by viewModel.pbMessage.collectAsState()
    val currentPose     by viewModel.currentPose.collectAsState()
    val imageWidth      by viewModel.imageWidth.collectAsState()
    val imageHeight     by viewModel.imageHeight.collectAsState()
    val elbowAngle      by viewModel.elbowAngle.collectAsState()
    val bodyAngle       by viewModel.bodyAngle.collectAsState()
    val shoulderAngle   by viewModel.shoulderAngle.collectAsState()
    val plankFormIssue  by viewModel.plankFormIssue.collectAsState()
    val pushupIsDown    by viewModel.pushupIsDown.collectAsState()

    val analyzer = remember {
        PoseAnalyzer { pose, w, h -> viewModel.processPose(pose, w, h) }
    }

    LaunchedEffect(workoutSaved) { if (workoutSaved) onFinish() }
    LaunchedEffect(initialMode) { viewModel.setMode(initialMode) }

    val feedbackBg by animateColorAsState(
        targetValue = when {
            feedback.contains("Rep")   -> CherryRed
            feedback.contains("Hold")  -> WarmAmber
            feedback.contains("Great") -> FormGreen
            !formOk                    -> FormRed
            else                       -> CherryRed
        },
        animationSpec = tween(250),
        label = "fbColor"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            useFrontCamera = true,
            analyzer = analyzer
        )

        GhostPostureOverlay(
            pose = currentPose, imageWidth = imageWidth, imageHeight = imageHeight,
            isFrontCamera = true, formOk = formOk, mode = mode,
            formIssue = plankFormIssue, pushupIsDown = pushupIsDown,
            modifier = Modifier.fillMaxSize()
        )

        PoseOverlay(
            pose = currentPose, imageWidth = imageWidth, imageHeight = imageHeight,
            isFrontCamera = true, elbowAngle = elbowAngle, bodyAngle = bodyAngle,
            shoulderAngle = shoulderAngle, mode = mode,
            modifier = Modifier.fillMaxSize()
        )

        // Top scrim
        Box(Modifier.fillMaxWidth().height(160.dp)
            .background(Brush.verticalGradient(listOf(Color.Black.copy(0.75f), Color.Transparent))))
        // Bottom scrim
        Box(Modifier.fillMaxWidth().height(200.dp).align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)))))

        // ── Top Bar ──────────────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.finishWorkout() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            ModeChips(mode) { viewModel.setMode(it) }
            Spacer(Modifier.weight(1f))
        }

        // ── Central Counter ──────────────────────────────────────────────────
        Column(Modifier.align(Alignment.Center).offset(y = (-20).dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            val v = if (mode == WorkoutType.PUSHUP) "$reps" else fmtTime(plankSec)
            Text(v, color = Color.White, fontSize = 84.sp, fontWeight = FontWeight.Black, letterSpacing = (-2).sp)
            Text(
                if (mode == WorkoutType.PUSHUP) "PUSHUPS" else "PLANK",
                color = Color.White.copy(0.55f), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 5.sp
            )
            if (mode == WorkoutType.PLANK) {
                Spacer(Modifier.height(10.dp))
                PlankFormChip(plankFormState)
            }
        }

        // Angle chips
        if (mode == WorkoutType.PUSHUP) {
            AngleChip("${elbowAngle.toInt()}°", "elbow",
                lerp(1f, 0f, ((elbowAngle - 90) / 70).coerceIn(0.0, 1.0).toFloat()),
                Modifier.align(Alignment.CenterEnd).padding(end = 12.dp))
        } else {
            Column(Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AngleChip("${bodyAngle.toInt()}°", "body",
                    lerp(1f, 0f, ((bodyAngle - 145) / 35).coerceIn(0.0, 1.0).toFloat()))
                AngleChip("${shoulderAngle.toInt()}°", "shoulder",
                    if (shoulderAngle in 55.0..130.0) 0f else 1f)
            }
        }

        // Feedback bar
        Box(
            Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 104.dp)
                .background(feedbackBg.copy(0.9f), RoundedCornerShape(50))
                .padding(horizontal = 18.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(feedback, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }

        // Ghost legend
        if (!formOk) {
            Box(Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 68.dp)
                .background(Color.White.copy(0.12f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("- - - ideal posture", color = Color.White.copy(0.6f), fontSize = 10.sp) }
        }

        // PB Celebration
        AnimatedVisibility(
            visible = isNewPB,
            enter = scaleIn(initialScale = 0.3f, animationSpec = spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) { PBOverlay(pbMessage) { viewModel.dismissPB() } }

        // Finish button
        Button(
            onClick = { viewModel.finishWorkout() },
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 28.dp)
                .fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CherryRed)
        ) { Text("FINISH", fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp) }
    }
}

// ─── Components ──────────────────────────────────────────────────────────────

@Composable
private fun PBOverlay(message: String, onDismiss: () -> Unit) {
    val p = rememberInfiniteTransition(label = "pb")
    val s by p.animateFloat(1f, 1.08f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "ps")
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(3000); onDismiss() }
    Box(Modifier.scale(s)
        .background(Brush.radialGradient(listOf(GoldReward.copy(0.3f), Color.Transparent)), CircleShape)
        .padding(40.dp), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚡", fontSize = 44.sp)
            Spacer(Modifier.height(6.dp))
            Text("NEW PERSONAL BEST!", color = GoldReward, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(4.dp))
            Text(message, color = Color.White.copy(0.8f), fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ModeChips(current: WorkoutType, onSelect: (WorkoutType) -> Unit) {
    Row(Modifier.background(Color.White.copy(0.1f), RoundedCornerShape(50)).padding(3.dp)) {
        listOf(WorkoutType.PUSHUP to "🏋️ PUSH", WorkoutType.PLANK to "⏱️ PLANK").forEach { (t, l) ->
            val sel = current == t
            Surface(onClick = { onSelect(t) }, color = if (sel) Color.White else Color.Transparent,
                shape = RoundedCornerShape(50), modifier = Modifier.padding(1.dp)
            ) {
                Text(l, color = if (sel) Color.Black else Color.White,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
private fun PlankFormChip(state: PlankFormState) {
    val col by animateColorAsState(when (state) {
        PlankFormState.GOOD -> FormGreen; PlankFormState.WARNING -> FormYellow; PlankFormState.PAUSED -> FormRed
    }, tween(300), label = "fc")
    val pulse = rememberInfiniteTransition(label = "fp")
    val pa by pulse.animateFloat(0.6f, 1f, infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "fpa")
    val a = if (state == PlankFormState.WARNING) pa else 1f
    val txt = when (state) {
        PlankFormState.GOOD -> "● HOLDING"; PlankFormState.WARNING -> "⚠ FIX FORM"; PlankFormState.PAUSED -> "◼ PAUSED"
    }
    Box(Modifier.alpha(a).background(col.copy(0.18f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 5.dp)) {
        Text(txt, color = col, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AngleChip(value: String, label: String, redFraction: Float = 0f, modifier: Modifier = Modifier) {
    val c = Color(red = redFraction.coerceIn(0f,1f), green = (1f - redFraction).coerceIn(0f,1f), blue = 0.15f, alpha = 1f)
    Column(modifier.background(Color.Black.copy(0.5f), RoundedCornerShape(10.dp))
        .padding(horizontal = 8.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = c, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(0.4f), fontSize = 9.sp)
    }
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
private fun fmtTime(s: Int): String { val m = s / 60; val r = s % 60; return "%d:%02d".format(m, r) }
