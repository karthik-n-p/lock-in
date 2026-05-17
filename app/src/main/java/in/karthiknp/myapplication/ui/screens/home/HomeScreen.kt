package `in`.karthiknp.myapplication.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import `in`.karthiknp.myapplication.data.local.PreferencesManager
import `in`.karthiknp.myapplication.ui.theme.*

private val BentoShape = RoundedCornerShape(22.dp)
private val BentoGap   = 10.dp

@Composable
fun HomeScreen(
    onStartPushups: () -> Unit,
    onStartPlank: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val stats         by viewModel.stats.collectAsState()
    val last30        by viewModel.last30Days.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val showStreakFix by viewModel.showStreakFixDialog.collectAsState()

    val dailyQuote = remember { PreferencesManager.MOTIVATION_QUOTES.random() }

    // ── Streak Fix Dialog ─────────────────────────────────────────────────────
    if (showStreakFix != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissStreakFixDialog() },
            title = { Text("🔥 Streak in Danger!", color = WarmAmber, fontWeight = FontWeight.Bold) },
            text  = { Text("You missed yesterday. Use 1 of your 3 monthly Streak Fixes?", color = TextPrimary) },
            confirmButton = {
                TextButton(onClick = { viewModel.applyStreakFix() }) {
                    Text("Save Streak", color = CherryRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissStreakFixDialog() }) {
                    Text("Let it go", color = TextSecondary)
                }
            },
            containerColor = EmberSurface,
            shape = BentoShape
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmberBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Text(
            "LOCK IN",
            color = CherryRed,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(2.dp))
        Text("Offline Fitness Tracker", color = TextTertiary, fontSize = 12.sp)

        Spacer(Modifier.height(16.dp))

        // ═══════════════════════════════════════════════════════════════════════
        // BENTO ROW 1: Streak (tall left) + Two stacked start buttons (right)
        // ═══════════════════════════════════════════════════════════════════════
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(BentoGap)
        ) {
            // ── Streak Card (tall) ────────────────────────────────────────────
            BentoCard(
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("STREAK", color = TextTertiary, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Column {
                        AnimatedStreakFire(streak = currentStreak)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (currentStreak > 0) "Keep it alive!" else "Start today!",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // ── Start Buttons (stacked) ───────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(BentoGap)
            ) {
                StartBentoButton(
                    emoji = "🏋️",
                    label = "PUSHUPS",
                    brush = Brush.horizontalGradient(listOf(CherryRed, DeepRose)),
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    onClick = onStartPushups
                )
                StartBentoButton(
                    emoji = "⏱️",
                    label = "PLANK",
                    brush = Brush.horizontalGradient(listOf(WarmAmber, Color(0xFFE67E22))),
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    onClick = onStartPlank
                )
            }
        }

        Spacer(Modifier.height(BentoGap))

        // ═══════════════════════════════════════════════════════════════════════
        // BENTO ROW 2: Today's Stats — two equal cards
        // ═══════════════════════════════════════════════════════════════════════
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BentoGap)
        ) {
            BentoStatCard(
                label = "TODAY",
                emoji = "🏋️",
                value = "${stats.todayPushups}",
                unit  = "reps",
                color = CherryRed,
                modifier = Modifier.weight(1f)
            )
            BentoStatCard(
                label = "TODAY",
                emoji = "⏱️",
                value = formatTime(stats.todayPlankSec),
                unit  = "plank",
                color = WarmAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(BentoGap))

        // ═══════════════════════════════════════════════════════════════════════
        // BENTO ROW 3: Daily Goal — full width
        // ═══════════════════════════════════════════════════════════════════════
        val pushupPct = (stats.todayPushups / 30f).coerceIn(0f, 1f)
        val plankPct  = (stats.todayPlankSec / 60f).coerceIn(0f, 1f)
        val isComplete = pushupPct >= 1f && plankPct >= 1f

        BentoGoalCard(pushupPct = pushupPct, plankPct = plankPct, isComplete = isComplete)

        Spacer(Modifier.height(BentoGap))

        // ═══════════════════════════════════════════════════════════════════════
        // BENTO ROW 4: Quote — full width, minimal
        // ═══════════════════════════════════════════════════════════════════════
        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(CherryRed.copy(0.04f), WarmAmber.copy(0.04f))
                        )
                    )
                    .padding(18.dp)
            ) {
                Text(
                    "\"$dailyQuote\"",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(BentoGap))

        // ═══════════════════════════════════════════════════════════════════════
        // BENTO ROW 5: Streak Calendar — full width
        // ═══════════════════════════════════════════════════════════════════════
        Text("ACTIVITY", color = TextTertiary, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        StreakCalendar(logs = last30)

        Spacer(Modifier.height(24.dp))
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// BENTO BUILDING BLOCKS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun BentoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.clip(BentoShape),
        shape = BentoShape,
        color = EmberSurface
    ) {
        content()
    }
}

@Composable
private fun StartBentoButton(
    emoji: String,
    label: String,
    brush: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.clip(BentoShape),
        shape = BentoShape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(emoji, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    label,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun BentoStatCard(
    label: String,
    emoji: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    BentoCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = TextTertiary, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(emoji, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Black)
            }
            Text(unit, color = TextTertiary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun BentoGoalCard(pushupPct: Float, plankPct: Float, isComplete: Boolean) {
    val combinedPct = ((pushupPct + plankPct) / 2f)

    val glowAnim = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnim.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "glowA"
    )

    BentoCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isComplete) Modifier.border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(GoldReward.copy(alpha = glowAlpha), WarmAmber.copy(alpha = glowAlpha))
                    ),
                    BentoShape
                ) else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DAILY GOAL", color = TextTertiary, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                if (isComplete) {
                    Text("🎯 CRUSHED!", color = GoldReward, fontSize = 12.sp, fontWeight = FontWeight.Black)
                } else {
                    Text("${(combinedPct * 100).toInt()}%", color = CherryRed, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(14.dp))

            GoalRow(label = "Pushups", pct = pushupPct, target = "30", color = CherryRed)
            Spacer(Modifier.height(10.dp))
            GoalRow(label = "Plank",   pct = plankPct,  target = "60s", color = WarmAmber)
        }
    }
}

@Composable
private fun GoalRow(label: String, pct: Float, target: String, color: Color) {
    val animPct by animateFloatAsState(
        targetValue = pct,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "goalPct"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, fontSize = 11.sp)
            Text("${(pct * 100).toInt()}% of $target", color = TextTertiary, fontSize = 10.sp)
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(Color.White.copy(0.06f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animPct)
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(listOf(color.copy(0.5f), color)),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

// ─── Animated Streak ──────────────────────────────────────────────────────────

@Composable
private fun AnimatedStreakFire(streak: Int) {
    if (streak > 0) {
        val pulse = rememberInfiniteTransition(label = "fire")
        val s by pulse.animateFloat(
            1f, 1.15f,
            infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "fScale"
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text("🔥", fontSize = 28.sp, modifier = Modifier.scale(s))
            Spacer(Modifier.width(6.dp))
            Text("$streak", color = WarmAmber, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(4.dp))
            Text("days", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
    } else {
        Text("🔥 0 days", color = TextTertiary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

// ─── Real Calendar Streak Map ─────────────────────────────────────────────────

@Composable
private fun StreakCalendar(logs: List<DailyLog>) {
    val logMap = logs.associateBy { it.date }
    val today  = java.time.LocalDate.now()

    // Show current month as a real calendar
    val firstOfMonth = today.withDayOfMonth(1)
    val lastDay = today.month.length(today.isLeapYear)
    val monthName = today.month.name.lowercase().replaceFirstChar { it.uppercase() }

    // What weekday does the 1st fall on? (Mon=1 … Sun=7)
    val startDow = firstOfMonth.dayOfWeek.value  // ISO: Mon=1

    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Month + Year header
            Text(
                "$monthName ${today.year}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Day-of-week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach { d ->
                    Text(
                        d, color = TextTertiary, fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            // Build calendar grid: fill empty slots before day 1 and after last day
            val totalSlots = startDow - 1 + lastDay
            val totalRows = (totalSlots + 6) / 7  // ceil division

            for (row in 0 until totalRows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val slotIndex = row * 7 + col
                        val dayNum = slotIndex - (startDow - 1) + 1

                        if (dayNum < 1 || dayNum > lastDay) {
                            // Empty cell
                            Box(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date = firstOfMonth.plusDays((dayNum - 1).toLong())
                            val ds = date.toString()
                            val log = logMap[ds]
                            val hasPU = (log?.pushupCount ?: 0) > 0
                            val hasPL = (log?.plankSeconds ?: 0) > 0
                            val isFix = log?.isStreakFix == true
                            val isActive = hasPU || hasPL || isFix
                            val isToday = date == today
                            val isFuture = date.isAfter(today)
                            val isPast = date.isBefore(today)

                            // Unified colors: all active = CherryRed, missed past = subtle dark
                            val cellColor = when {
                                isFuture       -> Color.Transparent
                                isActive       -> CherryRed
                                isPast         -> CherryRed.copy(alpha = 0.08f) // missed day
                                else           -> Color.Transparent // today, no workout yet
                            }

                            val textColor = when {
                                isFuture       -> TextTertiary.copy(alpha = 0.25f)
                                isActive       -> Color.White
                                isPast         -> TextTertiary.copy(alpha = 0.4f)
                                else           -> TextSecondary
                            }

                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(1.5.dp)
                                    .then(
                                        if (isToday) Modifier.border(
                                            1.5.dp, CherryRed, RoundedCornerShape(6.dp)
                                        ) else Modifier
                                    )
                                    .background(cellColor, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$dayNum",
                                    color = textColor,
                                    fontSize = 10.sp,
                                    fontWeight = if (isToday) FontWeight.Black else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
            }

            // Legend
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(CherryRed, "Active")
                LegendDot(CherryRed.copy(0.08f), "Missed")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(3.dp))
                .then(
                    if (color.alpha < 0.15f) Modifier.border(
                        0.5.dp, TextTertiary.copy(0.3f), RoundedCornerShape(3.dp)
                    ) else Modifier
                )
        )
        Spacer(Modifier.width(5.dp))
        Text(label, color = TextTertiary, fontSize = 10.sp)
    }
}

private fun formatTime(seconds: Int): String {
    if (seconds < 60) return "${seconds}s"
    val m = seconds / 60; val s = seconds % 60
    return if (s == 0) "${m}m" else "${m}m${s}s"
}
