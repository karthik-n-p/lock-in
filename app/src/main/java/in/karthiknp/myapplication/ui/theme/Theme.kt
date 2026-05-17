package `in`.karthiknp.myapplication.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EmberColorScheme = darkColorScheme(
    primary       = CherryRed,
    secondary     = WarmAmber,
    tertiary      = SoftCoral,
    background    = EmberBlack,
    surface       = EmberSurface,
    onPrimary     = Color.White,
    onSecondary   = EmberBlack,
    onBackground  = TextPrimary,
    onSurface     = TextPrimary
)

@Composable
fun FitnessAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = EmberColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
