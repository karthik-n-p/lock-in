package `in`.karthiknp.myapplication.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.karthiknp.myapplication.data.local.entity.DailyLog

private val BG     = Color(0xFF0A0A0F)
private val CARD   = Color(0xFF14141E)
private val ACCENT = Color(0xFF6C63FF)
private val GREEN  = Color(0xFF00E676)
private val ORANGE = Color(0xFFFFAB40)

@Composable
fun HomeScreen(
    onStartPushups: () -> Unit,
    onStartPlank: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val stats     by viewModel.stats.collectAsState()
    val last30    by viewModel.last30Days.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val showStreakFix by viewModel.showStreakFixDialog.collectAsState()

    if (showStreakFix != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissStreakFixDialog() },
            title = { Text("Streak in Danger!", color = ORANGE, fontWeight = FontWeight.Bold) },
            text = { Text("You missed yesterday's workout. Use 1 of your 2 monthly Streak Fixes to save your streak?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = { viewModel.applyStreakFix() }) {
                    Text("Use Fix", color = GREEN, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissStreakFixDialog() }) {
                    Text("Let it burn", color = Color.Gray)
                }
            },
            containerColor = CARD,
            titleContentColor = ORANGE,
            textContentColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Text("LOCK IN", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp)
        Text("Offline Fitness Tracker", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)

        Spacer(Modifier.height(28.dp))

        // ── Quick Start Buttons ───────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StartButton(
                label = "PUSHUPS",
                emoji = "💪",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF6C63FF), Color(0xFF3A36DB))),
                modifier = Modifier.weight(1f),
                onClick = onStartPushups
            )
            StartButton(
                label = "PLANK",
                emoji = "🧘",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF00C853), Color(0xFF007B33))),
                modifier = Modifier.weight(1f),
                onClick = onStartPlank
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── Stats Grid ────────────────────────────────────────────────────────
        Text("LIFETIME STATS", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Total Pushups", "${stats.totalPushups}", ACCENT, Modifier.weight(1f))
            StatCard("Plank Time", formatTime(stats.totalPlankSec), GREEN, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Best Day", "${stats.maxPushupsDay} reps", ORANGE, Modifier.weight(1f))
            StatCard("Active Days", "${stats.activeDays}", Color(0xFFFF6B6B), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Daily Avg", "%.1f reps".format(stats.avgPushups), Color(0xFF40C4FF), Modifier.weight(1f))
            StatCard("Longest Plank", formatTime(stats.longestPlankSec), Color(0xFFE040FB), Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))

        // ── Streak Heatmap ────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("CURRENT STREAK", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Text("🔥 $currentStreak Days", color = ORANGE, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(10.dp))
        StreakHeatmap(logs = last30)
    }
}

@Composable
private fun StartButton(
    label: String, emoji: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .background(gradient, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            color  = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(emoji, fontSize = 28.sp)
                Spacer(Modifier.height(4.dp))
                Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CARD)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            Text(value, color = accentColor, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun StreakHeatmap(logs: List<DailyLog>) {
    val logMap = logs.associateBy { it.date }

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CARD)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Show 5 weeks × 6 columns (30 days) grid
            val today = java.time.LocalDate.now()
            val dates = (29 downTo 0).map { today.minusDays(it.toLong()).toString() }

            dates.chunked(6).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { date ->
                        val log   = logMap[date]
                        val hasPU = (log?.pushupCount ?: 0) > 0
                        val hasPL = (log?.plankSeconds ?: 0) > 0
                        val color = when {
                            hasPU && hasPL -> ACCENT
                            hasPU          -> GREEN
                            hasPL          -> ORANGE
                            else           -> Color.White.copy(alpha = 0.07f)
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(color, RoundedCornerShape(6.dp))
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(GREEN,  "Pushups")
                LegendDot(ORANGE, "Plank")
                LegendDot(ACCENT, "Both")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}

private fun formatTime(seconds: Int): String {
    if (seconds < 60) return "${seconds}s"
    val m = seconds / 60; val s = seconds % 60
    return if (s == 0) "${m}m" else "${m}m${s}s"
}
