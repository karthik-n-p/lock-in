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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import `in`.karthiknp.myapplication.ui.screens.workout.WorkoutScreen
import `in`.karthiknp.myapplication.ui.theme.FitnessAppTheme
import `in`.karthiknp.myapplication.util.ReminderWorker
import java.util.concurrent.TimeUnit

private val BG = Color(0xFF0A0A0F)
private val NAV_BG = Color(0xFF0F0F1A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWorkManager()
        setContent {
            FitnessAppTheme {
                AppRoot()
            }
        }
    }

    private fun setupWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val now = java.time.LocalDateTime.now()
        var targetTime = now.withHour(20).withMinute(0).withSecond(0).withNano(0)
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1)
        }
        return java.time.temporal.ChronoUnit.MILLIS.between(now, targetTime)
    }
}

@Composable
private fun AppRoot() {
    var cameraGranted by remember {
        mutableStateOf(false)
    }
    var notificationGranted by remember {
        mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> 
        cameraGranted = permissions[Manifest.permission.CAMERA] ?: cameraGranted
        notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: notificationGranted
    }

    // Check on composition
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()

        cameraGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!cameraGranted) permissionsToRequest.add(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!notificationGranted) permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notificationGranted = true
        }

        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest.toTypedArray())
        }
    }

    if (!cameraGranted) {
        PermissionDeniedScreen { 
            val perms = mutableListOf(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationGranted) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            launcher.launch(perms.toTypedArray()) 
        }
        return
    }

    MainNavGraph()
}

@Composable
private fun MainNavGraph() {
    val navController = rememberNavController()

    // workout mode passed as nav argument
    var pendingWorkoutType by remember { mutableStateOf(WorkoutType.PUSHUP) }

    val bottomItems = listOf(
        BottomItem("home",         "Home",     Icons.Default.Home),
        BottomItem("progress",     "Progress", Icons.Default.DateRange),
        BottomItem("achievements", "Badges",   Icons.Default.Star)
    )

    val currentRoute by navController.currentBackStackEntryAsState()
    val showBottomBar = currentRoute?.destination?.route != "workout"

    Scaffold(
        containerColor = BG,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = NAV_BG,
                    tonalElevation = 0.dp
                ) {
                    bottomItems.forEach { item ->
                        val selected = currentRoute?.destination?.route == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = Color(0xFF6C63FF),
                                selectedTextColor   = Color(0xFF6C63FF),
                                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                unselectedTextColor = Color.White.copy(alpha = 0.4f),
                                indicatorColor      = Color(0xFF6C63FF).copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = "home",
            modifier         = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    onStartPushups = {
                        pendingWorkoutType = WorkoutType.PUSHUP
                        navController.navigate("workout")
                    },
                    onStartPlank = {
                        pendingWorkoutType = WorkoutType.PLANK
                        navController.navigate("workout")
                    }
                )
            }
            composable("workout") {
                WorkoutScreen(
                    onFinish = { navController.popBackStack() }
                )
            }
            composable("history") {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable("progress") {
                ProgressScreen()
            }
            composable("achievements") {
                AchievementsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun PermissionDeniedScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(BG),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📷", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text("Camera Permission Required",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Pose detection needs camera access",
                color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Text("Grant Permission")
            }
        }
    }
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)
