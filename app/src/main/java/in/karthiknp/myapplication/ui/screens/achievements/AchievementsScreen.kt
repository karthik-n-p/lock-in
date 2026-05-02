package `in`.karthiknp.myapplication.ui.screens.achievements

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.karthiknp.myapplication.data.local.entity.Achievement
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType

private val BG      = Color(0xFF0A0A0F)
private val CARD    = Color(0xFF14141E)
private val LOCKED  = Color(0xFF1E1E2E)
private val GOLD    = Color(0xFFFFD700)
private val GREEN   = Color(0xFF00E676)

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
                        Text("ACHIEVEMENTS", fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                        Text("$unlocked / ${achievements.size} unlocked",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BG,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = BG
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(achievements) { a -> BadgeCard(a) }
        }
    }
}

@Composable
private fun BadgeCard(achievement: Achievement) {
    val isUnlocked = achievement.unlockedAt != null
    val cardBg     = if (isUnlocked) CARD else LOCKED
    val emoji      = if (achievement.type == WorkoutType.PUSHUP) "💪" else "🧘"
    val accentColor = when {
        !isUnlocked                   -> Color.White.copy(alpha = 0.2f)
        achievement.type == WorkoutType.PUSHUP -> GOLD
        else                          -> GREEN
    }

    Card(
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text     = if (isUnlocked) emoji else "🔒",
                fontSize = 36.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = achievement.title,
                color      = accentColor,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                maxLines   = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = achievement.description,
                color    = Color.White.copy(alpha = if (isUnlocked) 0.5f else 0.2f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            if (isUnlocked) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text("✓ UNLOCKED", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
