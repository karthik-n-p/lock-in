package `in`.karthiknp.myapplication.ui.screens.progress

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import kotlin.math.*

private val BG     = Color(0xFF0A0A0F)
private val CARD   = Color(0xFF14141E)
private val ACCENT = Color(0xFF6C63FF)
private val GREEN  = Color(0xFF00E676)
private val ORANGE = Color(0xFFFFAB40)

@Composable
fun ProgressScreen(vm: ProgressViewModel = viewModel()) {
    val totalPushups    by vm.totalPushups.collectAsState(0)
    val totalPlankSec   by vm.totalPlankSec.collectAsState(0)
    val maxPushupsDay   by vm.maxPushupsDay.collectAsState(0)
    val longestPlankSec by vm.longestPlankSec.collectAsState(0)
    val avgPushups      by vm.avgPushups.collectAsState(0.0)
    val avgPlankSec     by vm.avgPlankSec.collectAsState(0.0)
    val activeDays      by vm.activeDays.collectAsState(0)
    val last30          by vm.last30Days.collectAsState(emptyList())
    val last14          by vm.last14Days.collectAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Text("PROGRESS", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
        Text("Your fitness evolution", color = Color.White.copy(0.4f), fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        // ── 3D Evolving Avatar ────────────────────────────────────────────────
        FitnessAvatarCard(totalPushups = totalPushups, totalPlankSec = totalPlankSec)

        Spacer(Modifier.height(20.dp))

        // ── Weekly Pushup Bar Chart ───────────────────────────────────────────
        SectionLabel("PUSHUPS — LAST 14 DAYS")
        Spacer(Modifier.height(8.dp))
        BarChartCard(logs = last14, selector = { it.pushupCount }, color = ACCENT, unit = "reps")

        Spacer(Modifier.height(16.dp))

        // ── Weekly Plank Trend ────────────────────────────────────────────────
        SectionLabel("PLANK — LAST 14 DAYS")
        Spacer(Modifier.height(8.dp))
        BarChartCard(logs = last14, selector = { it.plankSeconds }, color = GREEN, unit = "sec")

        Spacer(Modifier.height(20.dp))

        // ── Personal Records ──────────────────────────────────────────────────
        SectionLabel("PERSONAL RECORDS")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PRCard("Best Day",     "$maxPushupsDay reps",        ACCENT, Modifier.weight(1f))
            PRCard("Longest Plank", fmtTime(longestPlankSec),   GREEN,  Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PRCard("Daily Avg",    "%.1f reps".format(avgPushups),     ORANGE,           Modifier.weight(1f))
            PRCard("Avg Plank",    fmtTime(avgPlankSec.toInt()),        Color(0xFFE040FB), Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        // ── Consistency Ring ──────────────────────────────────────────────────
        SectionLabel("30-DAY CONSISTENCY")
        Spacer(Modifier.height(10.dp))
        ConsistencyRingCard(activeDays = activeDays, totalDays = 30, logs = last30)

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Fitness Avatar ───────────────────────────────────────────────────────────

@Composable
fun FitnessAvatarCard(totalPushups: Int, totalPlankSec: Int) {
    // Fitness level 0.0–1.0 based on cumulative effort
    val pushupLevel = (totalPushups / 1000f).coerceIn(0f, 1f)
    val plankLevel  = (totalPlankSec / 1800f).coerceIn(0f, 1f)  // 30 min max
    val fitnessLevel = (pushupLevel * 0.6f + plankLevel * 0.4f)

    val levelName = when {
        fitnessLevel < 0.1f -> "Beginner"
        fitnessLevel < 0.25f -> "Novice"
        fitnessLevel < 0.45f -> "Intermediate"
        fitnessLevel < 0.65f -> "Advanced"
        fitnessLevel < 0.85f -> "Elite"
        else -> "Legend"
    }
    val levelColor = when {
        fitnessLevel < 0.1f  -> Color(0xFF9E9E9E)
        fitnessLevel < 0.25f -> Color(0xFF4CAF50)
        fitnessLevel < 0.45f -> Color(0xFF2196F3)
        fitnessLevel < 0.65f -> Color(0xFF9C27B0)
        fitnessLevel < 0.85f -> Color(0xFFFF9800)
        else                  -> Color(0xFFFFD700)
    }

    // Pulse animation for aura
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulse by pulseAnim.animateFloat(
        initialValue = 0.85f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Card(
        shape  = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CARD),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("YOUR AVATAR", color = Color.White.copy(0.4f), fontSize = 11.sp, letterSpacing = 2.sp)
                    Text(levelName, color = levelColor, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
                // Mini stats
                Column(horizontalAlignment = Alignment.End) {
                    Text("$totalPushups pushups", color = ACCENT, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${fmtTime(totalPlankSec)} plank", color = GREEN, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Avatar canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                drawFitnessAvatar(
                    level      = fitnessLevel,
                    levelColor = levelColor,
                    pulse      = pulse,
                    cx         = size.width / 2f,
                    cy         = size.height / 2f + 10f,
                    baseUnit   = size.height * 0.28f
                )
            }

            Spacer(Modifier.height(8.dp))

            // Level progress bar
            val nextLevel = when {
                fitnessLevel < 0.1f  -> "Novice"
                fitnessLevel < 0.25f -> "Intermediate"
                fitnessLevel < 0.45f -> "Advanced"
                fitnessLevel < 0.65f -> "Elite"
                fitnessLevel < 0.85f -> "Legend"
                else                  -> "MAX"
            }
            Text("Progress to $nextLevel", color = Color.White.copy(0.45f), fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            LinearProgressBar(progress = fitnessLevel, color = levelColor)
        }
    }
}

private fun DrawScope.drawFitnessAvatar(
    level: Float,
    levelColor: Color,
    pulse: Float,
    cx: Float,
    cy: Float,
    baseUnit: Float
) {
    val u = baseUnit  // base unit = ~60dp

    // Muscle scale: 1.0 (thin) → 2.2 (buff) based on level
    val muscleScale = 1f + level * 1.2f
    // Arm width: thin at 0 → thick at 1
    val armW = (u * 0.10f * muscleScale).coerceIn(u * 0.08f, u * 0.32f)
    // Shoulder width expands with fitness
    val shoulderSpread = u * (0.55f + level * 0.35f)
    // Body definition
    val torsoW = u * (0.28f + level * 0.15f)

    // ── Glow aura ────────────────────────────────────────────────────────────
    val auraRadius = u * 1.1f * pulse
    drawCircle(
        brush = Brush.radialGradient(
            listOf(levelColor.copy(alpha = 0.18f * level), Color.Transparent),
            center = Offset(cx, cy - u * 0.1f),
            radius = auraRadius
        ),
        radius = auraRadius,
        center = Offset(cx, cy - u * 0.1f)
    )

    // ── Head ─────────────────────────────────────────────────────────────────
    val headY = cy - u * 1.05f
    val headR  = u * 0.20f
    drawCircle(
        brush  = Brush.radialGradient(
            listOf(levelColor.copy(0.6f), Color(0xFF2A2A40)),
            center = Offset(cx, headY)
        ),
        radius = headR,
        center = Offset(cx, headY)
    )
    drawCircle(
        color  = levelColor.copy(0.9f),
        radius = headR,
        center = Offset(cx, headY),
        style  = Stroke(width = u * 0.025f)
    )

    // ── Neck ─────────────────────────────────────────────────────────────────
    val neckTop = headY + headR
    val neckBot = cy - u * 0.75f
    drawLine(levelColor.copy(0.7f), Offset(cx, neckTop), Offset(cx, neckBot), u * 0.08f, StrokeCap.Round)

    // ── Shoulders (width scales with level) ───────────────────────────────────
    val shoulderY  = cy - u * 0.70f
    val lShoulderX = cx - shoulderSpread
    val rShoulderX = cx + shoulderSpread

    // Shoulder trapezoid
    val shoulderPath = Path().apply {
        moveTo(cx - u * 0.1f, neckBot)
        lineTo(lShoulderX, shoulderY)
        lineTo(rShoulderX, shoulderY)
        lineTo(cx + u * 0.1f, neckBot)
        close()
    }
    drawPath(
        shoulderPath,
        brush = Brush.verticalGradient(
            listOf(levelColor.copy(0.5f), levelColor.copy(0.2f)),
            startY = neckBot, endY = shoulderY
        )
    )
    drawPath(shoulderPath, levelColor.copy(0.8f), style = Stroke(u * 0.022f))

    // Shoulder dots
    drawCircle(levelColor.copy(0.9f), u * 0.10f, Offset(lShoulderX, shoulderY))
    drawCircle(levelColor.copy(0.9f), u * 0.10f, Offset(rShoulderX, shoulderY))

    // ── Arms (thickness = muscle scale) ───────────────────────────────────────
    val elbowY  = cy + u * 0.10f
    val lElbowX = cx - shoulderSpread - u * 0.05f
    val rElbowX = cx + shoulderSpread + u * 0.05f
    val wristY  = cy + u * 0.55f
    val lWristX = cx - shoulderSpread * 0.75f
    val rWristX = cx + shoulderSpread * 0.75f

    // Upper arm (shoulder → elbow) with bulge for bicep
    fun drawArm(sx: Float, sy: Float, ex: Float, ey: Float, wx: Float, wy: Float, side: Int) {
        val bulgeX = sx + (ex - sx) * 0.5f + side * u * 0.04f * level
        val bulgeY = sy + (ey - sy) * 0.5f

        val upperPath = Path().apply {
            val perp = armW * 0.5f
            moveTo(sx - side * perp * 0.4f, sy)
            quadraticBezierTo(bulgeX + side * perp, bulgeY, ex - side * perp * 0.3f, ey)
            quadraticBezierTo(bulgeX - side * perp * 0.3f, bulgeY, sx + side * perp * 0.4f, sy)
            close()
        }
        drawPath(upperPath, brush = Brush.linearGradient(
            listOf(levelColor.copy(0.55f), levelColor.copy(0.25f)),
            Offset(sx, sy), Offset(ex, ey)
        ))
        drawPath(upperPath, levelColor.copy(0.75f), style = Stroke(u * 0.018f))

        // Forearm
        val forearmPath = Path().apply {
            val fw = armW * 0.42f
            moveTo(ex - side * fw, ey)
            lineTo(wx - side * fw * 0.7f, wy)
            lineTo(wx + side * fw * 0.7f, wy)
            lineTo(ex + side * fw, ey)
            close()
        }
        drawPath(forearmPath, levelColor.copy(0.3f))
        drawPath(forearmPath, levelColor.copy(0.65f), style = Stroke(u * 0.016f))

        // Elbow joint
        drawCircle(levelColor.copy(0.8f), armW * 0.38f, Offset(ex, ey))
        // Wrist
        drawCircle(levelColor.copy(0.6f), armW * 0.25f, Offset(wx, wy))
    }

    drawArm(lShoulderX, shoulderY, lElbowX, elbowY, lWristX, wristY, -1)
    drawArm(rShoulderX, shoulderY, rElbowX, elbowY, rWristX, wristY, 1)

    // ── Torso ─────────────────────────────────────────────────────────────────
    val torsoTop  = shoulderY + u * 0.02f
    val torsoBot  = cy + u * 0.35f
    val waistW    = torsoW * (1f - level * 0.15f)  // slight V-taper at high levels

    val torsoPath = Path().apply {
        moveTo(cx - torsoW, torsoTop)
        lineTo(cx + torsoW, torsoTop)
        lineTo(cx + waistW, torsoBot)
        lineTo(cx - waistW, torsoBot)
        close()
    }
    drawPath(torsoPath, brush = Brush.verticalGradient(
        listOf(levelColor.copy(0.35f), levelColor.copy(0.12f)),
        startY = torsoTop, endY = torsoBot
    ))
    drawPath(torsoPath, levelColor.copy(0.7f), style = Stroke(u * 0.022f))

    // Abs definition (appears above level 0.4)
    if (level > 0.4f) {
        val absAlpha = ((level - 0.4f) / 0.6f).coerceIn(0f, 1f) * 0.6f
        val absCount = 3
        val absSpacing = (torsoBot - torsoTop - u * 0.1f) / absCount
        repeat(absCount) { i ->
            val absY = torsoTop + u * 0.08f + absSpacing * i + absSpacing * 0.5f
            val absW = (torsoW * 0.35f) * (1f - i * 0.08f)
            drawLine(levelColor.copy(absAlpha), Offset(cx - absW, absY), Offset(cx + absW, absY), u * 0.018f, StrokeCap.Round)
        }
        // Center line
        drawLine(levelColor.copy(absAlpha * 0.7f), Offset(cx, torsoTop + u * 0.06f), Offset(cx, torsoBot - u * 0.04f), u * 0.012f)
    }

    // ── Legs ─────────────────────────────────────────────────────────────────
    val legTopY = torsoBot
    val legW    = u * (0.13f + level * 0.06f)
    val kneeY   = cy + u * 0.75f
    val ankleY  = cy + u * 1.10f
    val lLegX = cx - u * 0.22f
    val rLegX = cx + u * 0.22f

    fun drawLeg(lx: Float, side: Int) {
        // Thigh
        drawLine(levelColor.copy(0.6f), Offset(lx, legTopY), Offset(lx + side * u * 0.03f, kneeY), legW, StrokeCap.Round)
        // Shin
        drawLine(levelColor.copy(0.45f), Offset(lx + side * u * 0.03f, kneeY), Offset(lx + side * u * 0.01f, ankleY), legW * 0.8f, StrokeCap.Round)
        // Knee
        drawCircle(levelColor.copy(0.7f), legW * 0.6f, Offset(lx + side * u * 0.03f, kneeY))
    }

    drawLeg(lLegX, -1)
    drawLeg(rLegX, 1)

    // Hip connector
    drawLine(levelColor.copy(0.5f), Offset(cx - waistW * 0.8f, torsoBot), Offset(cx + waistW * 0.8f, torsoBot), u * 0.06f, StrokeCap.Round)
}

// ─── Bar Chart ────────────────────────────────────────────────────────────────

@Composable
private fun BarChartCard(
    logs: List<DailyLog>,
    selector: (DailyLog) -> Int,
    color: Color,
    unit: String
) {
    // Build last-14-day grid with zeros for missing days
    val today = java.time.LocalDate.now()
    val days  = (13 downTo 0).map { today.minusDays(it.toLong()).toString() }
    val logMap = logs.associateBy { it.date }
    val values = days.map { selector(logMap[it] ?: DailyLog(it)) }
    val maxVal = values.maxOrNull()?.coerceAtLeast(1) ?: 1

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CARD)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chart
            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                val barCount = values.size
                val spacing  = 4.dp.toPx()
                val barW     = (size.width - spacing * (barCount - 1)) / barCount
                values.forEachIndexed { i, v ->
                    val barH = (v.toFloat() / maxVal) * size.height * 0.88f
                    val x    = i * (barW + spacing)
                    val y    = size.height - barH
                    // Background bar
                    drawRoundRect(
                        color  = color.copy(0.1f),
                        topLeft = Offset(x, 0f),
                        size   = androidx.compose.ui.geometry.Size(barW, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                    )
                    if (v > 0) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(color, color.copy(0.5f)),
                                startY = y, endY = size.height
                            ),
                            topLeft = Offset(x, y),
                            size    = androidx.compose.ui.geometry.Size(barW, barH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            // Day labels (show Mon/Wed/Fri/Sun for 14 days)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.filterIndexed { i, _ -> i % 2 == 0 }.forEach { d ->
                    val dow = java.time.LocalDate.parse(d).dayOfWeek.name.take(1)
                    Text(dow, color = Color.White.copy(0.3f), fontSize = 9.sp, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Max: ${maxVal} $unit", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Total: ${values.sum()} $unit", color = Color.White.copy(0.5f), fontSize = 11.sp)
            }
        }
    }
}

// ─── Consistency Ring ─────────────────────────────────────────────────────────

@Composable
private fun ConsistencyRingCard(activeDays: Int, totalDays: Int, logs: List<DailyLog>) {
    val fraction = (activeDays.toFloat() / totalDays).coerceIn(0f, 1f)
    val sweepAnim by animateFloatAsState(fraction * 360f, tween(900, easing = EaseOutCubic), label = "ring")

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CARD)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Ring
            Canvas(modifier = Modifier.size(90.dp)) {
                val stroke = 14.dp.toPx()
                drawArc(Color.White.copy(0.07f), 0f, 360f, false, style = Stroke(stroke))
                drawArc(
                    brush = Brush.sweepGradient(listOf(ACCENT, GREEN, ACCENT)),
                    startAngle = -90f, sweepAngle = sweepAnim,
                    useCenter = false, style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("$activeDays / $totalDays days active", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${(fraction * 100).toInt()}% consistency", color = ACCENT, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                // Mini streak heatmap (last 30 days, 5 rows × 6 cols)
                val logMap = logs.associateBy { it.date }
                val today  = java.time.LocalDate.now()
                val dates  = (29 downTo 0).map { today.minusDays(it.toLong()).toString() }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    dates.chunked(10).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            row.forEach { d ->
                                val log = logMap[d]
                                val active = (log?.pushupCount ?: 0) > 0 || (log?.plankSeconds ?: 0) > 0
                                Box(Modifier.size(10.dp).background(if (active) ACCENT else Color.White.copy(0.07f), RoundedCornerShape(2.dp)))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = Color.White.copy(0.45f), fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun PRCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(72.dp), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CARD)) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(0.45f), fontSize = 11.sp)
            Text(value,  color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun LinearProgressBar(progress: Float, color: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
        drawRoundRect(Color.White.copy(0.1f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(color.copy(0.7f), color)),
            size  = androidx.compose.ui.geometry.Size(size.width * progress, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )
    }
}

private fun fmtTime(seconds: Int): String {
    if (seconds < 60) return "${seconds}s"
    val m = seconds / 60; val s = seconds % 60
    return if (s == 0) "${m}m" else "${m}m${s}s"
}
