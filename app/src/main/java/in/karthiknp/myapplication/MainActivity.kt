package `in`.karthiknp.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import `in`.karthiknp.myapplication.data.local.entity.WorkoutType
import `in`.karthiknp.myapplication.ui.screens.achievements.AchievementsScreen
import `in`.karthiknp.myapplication.ui.screens.history.HistoryScreen
import `in`.karthiknp.myapplication.ui.screens.home.HomeScreen
import `in`.karthiknp.myapplication.ui.screens.progress.ProgressScreen
import `in`.karthiknp.myapplication.ui.screens.settings.SettingsScreen
import `in`.karthiknp.myapplication.ui.screens.workout.WorkoutScreen
import `in`.karthiknp.myapplication.ui.theme.*
import `in`.karthiknp.myapplication.util.ReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWorkManager()
        setContent { FitnessAppTheme { AppRoot() } }
    }

    private fun setupWorkManager() {
        val morningRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayToHour(9), TimeUnit.MILLISECONDS)
            .addTag("morning_motivation").build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "morning_motivation", ExistingPeriodicWorkPolicy.KEEP, morningRequest)

        val eveningRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayToHour(20), TimeUnit.MILLISECONDS)
            .addTag("evening_streak_warning").build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "evening_streak_warning", ExistingPeriodicWorkPolicy.KEEP, eveningRequest)
    }

    private fun calculateDelayToHour(targetHour: Int): Long {
        val now = java.time.LocalDateTime.now()
        var t = now.withHour(targetHour).withMinute(0).withSecond(0).withNano(0)
        if (now.isAfter(t)) t = t.plusDays(1)
        return java.time.temporal.ChronoUnit.MILLIS.between(now, t)
    }
}

@Composable
private fun AppRoot() {
    var cameraGranted by remember { mutableStateOf(false) }
    var notifGranted by remember { mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        cameraGranted = p[Manifest.permission.CAMERA] ?: cameraGranted
        notifGranted = p[Manifest.permission.POST_NOTIFICATIONS] ?: notifGranted
    }

    val ctx = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val req = mutableListOf<String>()
        cameraGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!cameraGranted) req.add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!notifGranted) req.add(Manifest.permission.POST_NOTIFICATIONS)
        } else notifGranted = true
        if (req.isNotEmpty()) launcher.launch(req.toTypedArray())
    }

    if (!cameraGranted) {
        PermissionScreen {
            val p = mutableListOf(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifGranted)
                p.add(Manifest.permission.POST_NOTIFICATIONS)
            launcher.launch(p.toTypedArray())
        }
        return
    }
    MainNavGraph()
}

@Composable
private fun MainNavGraph() {
    val nav = rememberNavController()
    var pendingType by remember { mutableStateOf(WorkoutType.PUSHUP) }

    val items = listOf(
        NavItem("home", "Home", Icons.Default.Home),
        NavItem("progress", "Progress", Icons.Default.DateRange),
        NavItem("achievements", "Badges", Icons.Default.Star),
        NavItem("settings", "Settings", Icons.Default.Settings)
    )
    val current by nav.currentBackStackEntryAsState()
    val showBar = current?.destination?.route != "workout"

    Scaffold(
        containerColor = EmberBlack,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = NavDark, tonalElevation = 0.dp) {
                    items.forEach { item ->
                        val sel = current?.destination?.route == item.route
                        NavigationBarItem(
                            selected = sel,
                            onClick = {
                                nav.navigate(item.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CherryRed,
                                selectedTextColor = CherryRed,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = CherryRed.copy(0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(nav, "home", Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(
                    onStartPushups = { pendingType = WorkoutType.PUSHUP; nav.navigate("workout") },
                    onStartPlank   = { pendingType = WorkoutType.PLANK;  nav.navigate("workout") }
                )
            }
            composable("workout") { WorkoutScreen(initialMode = pendingType, onFinish = { nav.popBackStack() }) }
            composable("history") { HistoryScreen(onBack = { nav.popBackStack() }) }
            composable("progress") { ProgressScreen() }
            composable("achievements") { AchievementsScreen(onBack = { nav.popBackStack() }) }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
private fun PermissionScreen(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().background(EmberBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📷", fontSize = 56.sp)
            Spacer(Modifier.height(14.dp))
            Text("Camera Permission Required", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Pose detection needs camera access", color = TextTertiary, fontSize = 13.sp)
            Spacer(Modifier.height(28.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = CherryRed)) {
                Text("Grant Permission", color = TextPrimary)
            }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)
