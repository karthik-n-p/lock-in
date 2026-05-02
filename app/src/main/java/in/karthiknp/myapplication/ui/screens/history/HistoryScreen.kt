package `in`.karthiknp.myapplication.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.karthiknp.myapplication.data.local.entity.DailyLog

private val BG   = Color(0xFF0A0A0F)
private val CARD = Color(0xFF14141E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val logs by viewModel.logs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HISTORY", fontWeight = FontWeight.Black, letterSpacing = 3.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = BG,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = BG
    ) { padding ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏋️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No workouts yet", color = Color.White.copy(alpha = 0.4f), fontSize = 16.sp)
                    Text("Complete your first session!", color = Color.White.copy(alpha = 0.25f), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(logs) { log -> DailyLogCard(log) }
            }
        }
    }
}

@Composable
private fun DailyLogCard(log: DailyLog) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CARD)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date column
            Column(modifier = Modifier.weight(1f)) {
                val parts = log.date.split("-")
                if (parts.size == 3) {
                    val months = listOf("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                    val mon = parts[1].toIntOrNull() ?: 0
                    Text(parts[2], color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("${months.getOrElse(mon) { "" }} ${parts[0]}", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                } else {
                    Text(log.date, color = Color.White, fontSize = 14.sp)
                }
            }

            // Pushups
            if (log.pushupCount > 0) {
                MetricChip("💪", "${log.pushupCount}", "reps", Color(0xFF6C63FF))
                Spacer(Modifier.width(8.dp))
            }

            // Plank
            if (log.plankSeconds > 0) {
                val m = log.plankSeconds / 60; val s = log.plankSeconds % 60
                MetricChip("🧘", if (m > 0) "${m}m${s}s" else "${s}s", "plank", Color(0xFF00C853))
            }
        }
    }
}

@Composable
private fun MetricChip(emoji: String, value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
    }
}
