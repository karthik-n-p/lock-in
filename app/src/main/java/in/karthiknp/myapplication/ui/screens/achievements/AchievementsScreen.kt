package `in`.karthiknp.myapplication.ui.screens.achievements

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.karthiknp.myapplication.data.local.entity.Achievement
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType
import `in`.karthiknp.myapplication.ui.theme.*

private val BentoShape = RoundedCornerShape(22.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = viewModel()
) {
    val achievements by viewModel.achievements.collectAsState()
    val unlocked = achievements.count { it.unlockedAt != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BADGES", fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                        Text("$unlocked / ${achievements.size} unlocked", fontSize = 11.sp, color = TextTertiary)
                    }
                },
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) { items(achievements) { BadgeCard(it) } }
    }
}

@Composable
private fun BadgeCard(a: Achievement) {
    val unlocked = a.unlockedAt != null

    val glow = rememberInfiniteTransition(label = "glow")
    val ga by glow.animateFloat(0.2f, 0.5f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "ga")

    val accent = when {
        !unlocked -> TextTertiary
        a.type == WorkoutType.PUSHUP -> GoldReward
        else -> WarmAmber
    }
    val emoji = when {
        !unlocked -> "🔒"
        a.type == WorkoutType.PUSHUP -> "🏋️"
        else -> "⏱️"
    }

    val scale by animateFloatAsState(if (unlocked) 1f else 0.85f,
        spring(Spring.DampingRatioMediumBouncy), label = "bs")

    Card(
        shape = BentoShape,
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) EmberSurface else Color(0xFF0E0A0A)
        ),
        modifier = Modifier.aspectRatio(0.88f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .then(if (unlocked) Modifier.background(
                    Brush.radialGradient(listOf(accent.copy(ga * 0.12f), Color.Transparent))
                ) else Modifier)
        ) {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(emoji, fontSize = 32.sp, modifier = Modifier.scale(scale))
                Spacer(Modifier.height(8.dp))
                Text(a.title, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, maxLines = 2)
                Spacer(Modifier.height(3.dp))
                Text(a.description, color = if (unlocked) TextSecondary else TextTertiary,
                    fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 2)
                if (unlocked) {
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.background(accent.copy(0.15f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 3.dp)) {
                        Text("✓ UNLOCKED", color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
