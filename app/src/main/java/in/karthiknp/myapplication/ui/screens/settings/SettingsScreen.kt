package `in`.karthiknp.myapplication.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.karthiknp.myapplication.data.local.PreferencesManager
import `in`.karthiknp.myapplication.ui.theme.*

private val BentoShape = RoundedCornerShape(22.dp)

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var selectedHour by remember { mutableIntStateOf(prefs.getDayEndHour()) }

    Column(
        Modifier.fillMaxSize().background(EmberBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("SETTINGS", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        Text("Customize your experience", color = TextTertiary, fontSize = 12.sp)
        Spacer(Modifier.height(20.dp))

        // ── Day End ───────────────────────────────────────────────────────────
        Card(shape = BentoShape, colors = CardDefaults.cardColors(containerColor = EmberSurface)) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌙", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Day End Time", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Workouts after this hour count for today",
                            color = TextTertiary, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..6).forEach { h ->
                        val sel = selectedHour == h
                        val bg by animateColorAsState(
                            if (sel) CherryRed else Color.Transparent, tween(200), label = "c$h")
                        Box(
                            Modifier.weight(1f)
                                .background(bg, RoundedCornerShape(10.dp))
                                .border(1.dp, if (sel) CherryRed else TextTertiary.copy(0.25f), RoundedCornerShape(10.dp))
                                .clickable { selectedHour = h; prefs.setDayEndHour(h) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (h == 0) "12AM" else "${h}AM",
                                color = if (sel) Color.White else TextTertiary,
                                fontSize = 9.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (selectedHour == 0) "Standard — resets at midnight"
                    else "Night owl — before ${selectedHour}AM counts as yesterday",
                    color = CherryRed.copy(0.7f), fontSize = 10.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Notifications ─────────────────────────────────────────────────────
        Card(shape = BentoShape, colors = CardDefaults.cardColors(containerColor = EmberSurface)) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔔", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Smart Notifications", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Reminders based on your patterns", color = TextTertiary, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                NotifRow("⚡", "Morning Boost", "Daily at 9 AM")
                Spacer(Modifier.height(6.dp))
                NotifRow("🔥", "Streak Warning", "8 PM if inactive")
                Spacer(Modifier.height(6.dp))
                NotifRow("🏆", "Achievements", "On badge unlock")
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Streak Fixes ──────────────────────────────────────────────────────
        val fixesUsed = remember { prefs.getStreakFixesUsedThisMonth() }
        Card(shape = BentoShape, colors = CardDefaults.cardColors(containerColor = EmberSurface)) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔧", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Streak Fixes", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("${PreferencesManager.MAX_FIXES_PER_MONTH - fixesUsed} of ${PreferencesManager.MAX_FIXES_PER_MONTH} remaining this month",
                            color = TextTertiary, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(PreferencesManager.MAX_FIXES_PER_MONTH) { i ->
                        Box(
                            Modifier.size(28.dp)
                                .background(
                                    if (i < fixesUsed) TextTertiary.copy(0.2f) else WarmAmber.copy(0.8f),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (i < fixesUsed) "✗" else "✓",
                                color = if (i < fixesUsed) TextTertiary else EmberBlack,
                                fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── About ─────────────────────────────────────────────────────────────
        Card(shape = BentoShape, colors = CardDefaults.cardColors(containerColor = EmberSurface)) {
            Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LOCK IN", color = CherryRed, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Spacer(Modifier.height(3.dp))
                Text("100% Offline • Zero Data Shared", color = TextTertiary, fontSize = 10.sp)
                Spacer(Modifier.height(6.dp))
                Text("Your data never leaves your device.",
                    color = TextTertiary, fontSize = 10.sp, textAlign = TextAlign.Center)
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun NotifRow(emoji: String, title: String, desc: String) {
    Row(
        Modifier.fillMaxWidth().background(EmberBlack.copy(0.5f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = TextTertiary, fontSize = 9.sp)
        }
    }
}
