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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import `in`.karthiknp.myapplication.ui.theme.*
import kotlin.math.*

private val BentoShape = RoundedCornerShape(22.dp)
private val BentoGap   = 10.dp

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
        Modifier.fillMaxSize().background(EmberBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("PROGRESS", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        Text("Your fitness evolution", color = TextTertiary, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))

        // ── Bento: Avatar ─────────────────────────────────────────────────────
        AvatarBentoCard(totalPushups, totalPlankSec)
        Spacer(Modifier.height(BentoGap))

        // ── Bento Row: Personal Records ───────────────────────────────────────
        SectionLabel("PERSONAL RECORDS")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(BentoGap)) {
            PRCard("Best Day", "$maxPushupsDay reps", CherryRed, Modifier.weight(1f))
            PRCard("Longest Plank", fmtTime(longestPlankSec), WarmAmber, Modifier.weight(1f))
        }
        Spacer(Modifier.height(BentoGap))
        Row(horizontalArrangement = Arrangement.spacedBy(BentoGap)) {
            PRCard("Daily Avg", "%.1f reps".format(avgPushups), SoftCoral, Modifier.weight(1f))
            PRCard("Avg Plank", fmtTime(avgPlankSec.toInt()), SoftAmber, Modifier.weight(1f))
        }

        Spacer(Modifier.height(BentoGap + 4.dp))

        // ── Bento: Charts ─────────────────────────────────────────────────────
        SectionLabel("PUSHUPS — LAST 14 DAYS")
        Spacer(Modifier.height(8.dp))
        BarChartBento(last14, { it.pushupCount }, CherryRed, { "$it" })
        Spacer(Modifier.height(BentoGap))
        SectionLabel("PLANK — LAST 14 DAYS")
        Spacer(Modifier.height(8.dp))
        BarChartBento(last14, { it.plankSeconds }, WarmAmber, { if (it < 60) "${it}s" else "${it/60}m" })

        Spacer(Modifier.height(BentoGap + 4.dp))

        // ── Bento: Consistency Ring ───────────────────────────────────────────
        SectionLabel("30-DAY CONSISTENCY")
        Spacer(Modifier.height(8.dp))
        ConsistencyBento(activeDays, 30, last30)

        Spacer(Modifier.height(28.dp))
    }
}

// ─── Avatar Card ──────────────────────────────────────────────────────────────

@Composable
private fun AvatarBentoCard(totalPushups: Int, totalPlankSec: Int) {
    val pushupLvl = (totalPushups / 1000f).coerceIn(0f, 1f)
    val plankLvl  = (totalPlankSec / 1800f).coerceIn(0f, 1f)
    val fitness   = pushupLvl * 0.6f + plankLvl * 0.4f

    val lvlName = when {
        fitness < 0.1f -> "Beginner"; fitness < 0.25f -> "Novice"
        fitness < 0.45f -> "Intermediate"; fitness < 0.65f -> "Advanced"
        fitness < 0.85f -> "Elite"; else -> "Legend"
    }
    val lvlColor = when {
        fitness < 0.1f -> TextSecondary; fitness < 0.25f -> FormGreen
        fitness < 0.45f -> WarmAmber; fitness < 0.65f -> SoftCoral
        fitness < 0.85f -> CherryRed; else -> GoldReward
    }

    val pAnim = rememberInfiniteTransition(label = "p")
    val pulse by pAnim.animateFloat(0.85f, 1f,
        infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse), label = "pv")

    Card(shape = BentoShape, colors = CardDefaults.cardColors(containerColor = EmberSurface),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("YOUR AVATAR", color = TextTertiary, fontSize = 10.sp, letterSpacing = 2.sp)
                    Text(lvlName, color = lvlColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("🏋️ $totalPushups", color = CherryRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("⏱️ ${fmtTime(totalPlankSec)}", color = WarmAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            Canvas(Modifier.fillMaxWidth().height(200.dp)) {
                drawFitnessAvatar(fitness, lvlColor, pulse, size.width / 2f, size.height / 2f + 10f, size.height * 0.28f)
            }
            Spacer(Modifier.height(8.dp))
            val next = when {
                fitness < 0.1f -> "Novice"; fitness < 0.25f -> "Intermediate"
                fitness < 0.45f -> "Advanced"; fitness < 0.65f -> "Elite"
                fitness < 0.85f -> "Legend"; else -> "MAX"
            }
            Text("Progress to $next", color = TextTertiary, fontSize = 10.sp)
            Spacer(Modifier.height(5.dp))
            Canvas(Modifier.fillMaxWidth().height(5.dp)) {
                drawRoundRect(Color.White.copy(0.08f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(lvlColor.copy(0.6f), lvlColor)),
                    size = androidx.compose.ui.geometry.Size(size.width * fitness, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                )
            }
        }
    }
}

private fun DrawScope.drawFitnessAvatar(level: Float, c: Color, pulse: Float, cx: Float, cy: Float, u: Float) {
    val muscleScale = 1f + level * 1.2f
    val armW = (u * 0.10f * muscleScale).coerceIn(u * 0.08f, u * 0.32f)
    val shoulderSpread = u * (0.55f + level * 0.35f)
    val torsoW = u * (0.28f + level * 0.15f)

    drawCircle(Brush.radialGradient(listOf(c.copy(0.18f * level), Color.Transparent),
        center = Offset(cx, cy - u * 0.1f), radius = u * 1.1f * pulse),
        radius = u * 1.1f * pulse, center = Offset(cx, cy - u * 0.1f))

    val headY = cy - u * 1.05f; val headR = u * 0.20f
    drawCircle(Brush.radialGradient(listOf(c.copy(0.6f), Color(0xFF1A1210)), center = Offset(cx, headY)),
        radius = headR, center = Offset(cx, headY))
    drawCircle(c.copy(0.9f), headR, Offset(cx, headY), style = Stroke(u * 0.025f))

    val neckBot = cy - u * 0.75f
    drawLine(c.copy(0.7f), Offset(cx, headY + headR), Offset(cx, neckBot), u * 0.08f, StrokeCap.Round)

    val shY = cy - u * 0.70f; val lSX = cx - shoulderSpread; val rSX = cx + shoulderSpread
    val sp = Path().apply { moveTo(cx - u * 0.1f, neckBot); lineTo(lSX, shY); lineTo(rSX, shY); lineTo(cx + u * 0.1f, neckBot); close() }
    drawPath(sp, Brush.verticalGradient(listOf(c.copy(0.5f), c.copy(0.2f)), neckBot, shY))
    drawPath(sp, c.copy(0.8f), style = Stroke(u * 0.022f))
    drawCircle(c.copy(0.9f), u * 0.10f, Offset(lSX, shY)); drawCircle(c.copy(0.9f), u * 0.10f, Offset(rSX, shY))

    val eY = cy + u * 0.10f; val wY = cy + u * 0.55f
    fun arm(sx: Float, ex: Float, wx: Float, s: Int) {
        val bx = sx + (ex - sx) * 0.5f + s * u * 0.04f * level; val by2 = shY + (eY - shY) * 0.5f
        val p = armW * 0.5f
        val upper = Path().apply { moveTo(sx - s * p * 0.4f, shY); quadraticBezierTo(bx + s * p, by2, ex - s * p * 0.3f, eY); quadraticBezierTo(bx - s * p * 0.3f, by2, sx + s * p * 0.4f, shY); close() }
        drawPath(upper, Brush.linearGradient(listOf(c.copy(0.55f), c.copy(0.25f)), Offset(sx, shY), Offset(ex, eY)))
        drawPath(upper, c.copy(0.75f), style = Stroke(u * 0.018f))
        val fw = armW * 0.42f
        val fore = Path().apply { moveTo(ex - s * fw, eY); lineTo(wx - s * fw * 0.7f, wY); lineTo(wx + s * fw * 0.7f, wY); lineTo(ex + s * fw, eY); close() }
        drawPath(fore, c.copy(0.3f)); drawPath(fore, c.copy(0.65f), style = Stroke(u * 0.016f))
        drawCircle(c.copy(0.8f), armW * 0.38f, Offset(ex, eY)); drawCircle(c.copy(0.6f), armW * 0.25f, Offset(wx, wY))
    }
    arm(lSX, cx - shoulderSpread - u * 0.05f, cx - shoulderSpread * 0.75f, -1)
    arm(rSX, cx + shoulderSpread + u * 0.05f, cx + shoulderSpread * 0.75f, 1)

    val tBot = cy + u * 0.35f; val waistW = torsoW * (1f - level * 0.15f)
    val tp = Path().apply { moveTo(cx - torsoW, shY + u * 0.02f); lineTo(cx + torsoW, shY + u * 0.02f); lineTo(cx + waistW, tBot); lineTo(cx - waistW, tBot); close() }
    drawPath(tp, Brush.verticalGradient(listOf(c.copy(0.35f), c.copy(0.12f)), shY, tBot))
    drawPath(tp, c.copy(0.7f), style = Stroke(u * 0.022f))
    if (level > 0.4f) {
        val aa = ((level - 0.4f) / 0.6f).coerceIn(0f, 1f) * 0.6f
        val sp2 = (tBot - shY - u * 0.1f) / 3f
        repeat(3) { i -> val ay = shY + u * 0.1f + sp2 * i + sp2 * 0.5f; val aw = torsoW * 0.35f * (1f - i * 0.08f)
            drawLine(c.copy(aa), Offset(cx - aw, ay), Offset(cx + aw, ay), u * 0.018f, StrokeCap.Round) }
        drawLine(c.copy(aa * 0.7f), Offset(cx, shY + u * 0.08f), Offset(cx, tBot - u * 0.04f), u * 0.012f)
    }

    val legW = u * (0.13f + level * 0.06f); val kY = cy + u * 0.75f; val aY = cy + u * 1.10f
    fun leg(x: Float, s: Int) {
        drawLine(c.copy(0.6f), Offset(x, tBot), Offset(x + s * u * 0.03f, kY), legW, StrokeCap.Round)
        drawLine(c.copy(0.45f), Offset(x + s * u * 0.03f, kY), Offset(x + s * u * 0.01f, aY), legW * 0.8f, StrokeCap.Round)
        drawCircle(c.copy(0.7f), legW * 0.6f, Offset(x + s * u * 0.03f, kY))
    }
    leg(cx - u * 0.22f, -1); leg(cx + u * 0.22f, 1)
    drawLine(c.copy(0.5f), Offset(cx - waistW * 0.8f, tBot), Offset(cx + waistW * 0.8f, tBot), u * 0.06f, StrokeCap.Round)
}

// ─── Bar Chart ────────────────────────────────────────────────────────────────

@Composable
private fun BarChartBento(logs: List<DailyLog>, sel: (DailyLog) -> Int, color: Color, fmt: (Int) -> String) {
    val today = java.time.LocalDate.now()
    val days = (13 downTo 0).map { today.minusDays(it.toLong()) }
    val map = logs.associateBy { it.date }
    val vals = days.map { sel(map[it.toString()] ?: DailyLog(it.toString())) }
    val mx = vals.maxOrNull()?.coerceAtLeast(1) ?: 1
    val tm = rememberTextMeasurer()

    Card(shape = BentoShape, colors = CardDefaults.cardColors(containerColor = EmberSurface)) {
        Column(Modifier.padding(14.dp)) {
            Canvas(Modifier.fillMaxWidth().height(100.dp)) {
                val sp = 3.dp.toPx(); val bw = (size.width - sp * (vals.size - 1)) / vals.size
                vals.forEachIndexed { i, v ->
                    val bh = (v.toFloat() / mx) * size.height * 0.78f
                    val x = i * (bw + sp); val y = size.height - bh
                    drawRoundRect(color.copy(0.06f), Offset(x, 0f),
                        androidx.compose.ui.geometry.Size(bw, size.height),
                        androidx.compose.ui.geometry.CornerRadius(5f))
                    if (v > 0) {
                        drawRoundRect(Brush.verticalGradient(listOf(color, color.copy(0.3f)), y, size.height),
                            Offset(x, y), androidx.compose.ui.geometry.Size(bw, bh),
                            androidx.compose.ui.geometry.CornerRadius(5f))
                        val tl = tm.measure(fmt(v), TextStyle(fontSize = 7.sp, color = color, fontWeight = FontWeight.Bold))
                        drawText(tl, topLeft = Offset(x + (bw - tl.size.width) / 2f, y - tl.size.height - 1.dp.toPx()))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: ${fmt(vals.sum())}", color = TextTertiary, fontSize = 10.sp)
                Text("Max: ${fmt(mx)}", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Consistency Ring ─────────────────────────────────────────────────────────

@Composable
private fun ConsistencyBento(active: Int, total: Int, logs: List<DailyLog>) {
    val f = (active.toFloat() / total).coerceIn(0f, 1f)
    val sweep by animateFloatAsState(f * 360f, tween(900, easing = EaseOutCubic), label = "r")

    Card(shape = BentoShape, colors = CardDefaults.cardColors(containerColor = EmberSurface)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(80.dp)) {
                val s = 12.dp.toPx()
                drawArc(Color.White.copy(0.06f), 0f, 360f, false, style = Stroke(s))
                drawArc(Brush.sweepGradient(listOf(CherryRed, WarmAmber, CherryRed)),
                    -90f, sweep, false, style = Stroke(s, cap = StrokeCap.Round))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("$active / $total days", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("${(f * 100).toInt()}% consistency", color = CherryRed, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                val lm = logs.associateBy { it.date }; val td = java.time.LocalDate.now()
                val ds = (29 downTo 0).map { td.minusDays(it.toLong()).toString() }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ds.chunked(10).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            row.forEach { d ->
                                val l = lm[d]; val a = (l?.pushupCount ?: 0) > 0 || (l?.plankSeconds ?: 0) > 0 || l?.isStreakFix == true
                                Box(Modifier.size(9.dp).background(if (a) CherryRed else Color.White.copy(0.04f), RoundedCornerShape(2.dp)))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(t: String) {
    Text(t, color = TextTertiary, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp))
}

@Composable
private fun PRCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(68.dp), shape = BentoShape,
        colors = CardDefaults.cardColors(containerColor = EmberSurface)) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextTertiary, fontSize = 10.sp)
            Text(value, color = color, fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
    }
}

private fun fmtTime(s: Int): String {
    if (s < 60) return "${s}s"; val m = s / 60; val r = s % 60
    return if (r == 0) "${m}m" else "${m}m${r}s"
}
