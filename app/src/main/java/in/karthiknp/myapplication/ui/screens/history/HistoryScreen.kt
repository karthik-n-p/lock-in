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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.karthiknp.myapplication.data.local.entity.DailyLog
import `in`.karthiknp.myapplication.ui.theme.*

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
                title = { Text("HISTORY", fontWeight = FontWeight.Black, letterSpacing = 3.sp, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmberBlack, titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = EmberBlack
    ) { padding ->
        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏋️", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No workouts yet", color = TextSecondary, fontSize = 14.sp)
                    Text("Start your journey!", color = TextTertiary, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) { items(logs) { MinimalLogRow(it) } }
        }
    }
}

@Composable
private fun MinimalLogRow(log: DailyLog) {
    val hasPU = log.pushupCount > 0
    val hasPL = log.plankSeconds > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EmberSurface, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Date ──────────────────────────────────────────────────────────────
        val parts = log.date.split("-")
        val months = listOf("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val day = if (parts.size == 3) parts[2] else "?"
        val mon = if (parts.size == 3) months.getOrElse(parts[1].toIntOrNull() ?: 0) { "" } else ""

        Column(modifier = Modifier.width(40.dp)) {
            Text(day, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black, lineHeight = 22.sp)
            Text(mon, color = TextTertiary, fontSize = 10.sp)
        }

        // ── Red accent bar ────────────────────────────────────────────────────
        Box(
            Modifier
                .width(3.dp)
                .height(32.dp)
                .background(
                    Brush.verticalGradient(listOf(CherryRed, CherryRed.copy(0.3f))),
                    RoundedCornerShape(2.dp)
                )
        )

        Spacer(Modifier.width(12.dp))

        // ── Metrics ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasPU) {
                Text("${log.pushupCount}", color = CherryRed, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(3.dp))
                Text("reps", color = TextTertiary, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            }
            if (hasPU && hasPL) {
                Spacer(Modifier.width(14.dp))
                Box(Modifier.width(1.dp).height(18.dp).background(TextTertiary.copy(0.2f)))
                Spacer(Modifier.width(14.dp))
            }
            if (hasPL) {
                val m = log.plankSeconds / 60; val s = log.plankSeconds % 60
                val plankStr = if (m > 0) "${m}m${s}s" else "${s}s"
                Text(plankStr, color = WarmAmber, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(3.dp))
                Text("plank", color = TextTertiary, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            }
            if (!hasPU && !hasPL) {
                Text("Rest day", color = TextTertiary, fontSize = 12.sp)
            }
        }
    }
}
