package `in`.karthiknp.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Ember Red-Orange Palette ────────────────────────────────────────────────
// Warm blacks with red undertone — feels alive, not cold
val EmberBlack       = Color(0xFF0C0808)   // deepest background — warm void
val EmberSurface     = Color(0xFF161010)   // bento card surface
val EmberSurfaceHi   = Color(0xFF1E1515)   // elevated card / hover
val NavDark          = Color(0xFF0E0A0A)   // bottom nav

// ─── Primary: Cherry Red ────────────────────────────────────────────────────
// Red = urgency + passion + heart-rate elevation → drives daily return
val CherryRed        = Color(0xFFE63946)   // primary action, CTAs
val CherryDim        = Color(0xFFBF2D38)   // pressed / muted red

// ─── Secondary: Warm Amber ──────────────────────────────────────────────────
// Orange/amber = enthusiasm + warmth + energy → feels rewarding
val WarmAmber        = Color(0xFFFF8C42)   // streaks, progress, warmth
val SoftAmber        = Color(0xFFFFAB6B)   // lighter amber highlights

// ─── Accent: Coral & Rose ───────────────────────────────────────────────────
val SoftCoral        = Color(0xFFFF6B6B)   // soft accent, badges
val DeepRose         = Color(0xFFC2185B)   // premium / rare achievements

// ─── Form Indicators ────────────────────────────────────────────────────────
val FormGreen        = Color(0xFF4CAF50)   // ✅ good form — earthy green
val FormYellow       = Color(0xFFFFCA28)   // ⚠️ warning — warm yellow
val FormRed          = Color(0xFFEF5350)   // ❌ paused — soft red

// ─── Reward & Gold ──────────────────────────────────────────────────────────
val GoldReward       = Color(0xFFFFB74D)   // achievements, PBs
val GoldBright       = Color(0xFFFFD54F)   // celebration highlight

// ─── Text ───────────────────────────────────────────────────────────────────
val TextPrimary      = Color(0xFFFAF0E6)   // linen white — warm, easy on eyes
val TextSecondary    = Color(0xFF9E8E82)   // warm muted
val TextTertiary     = Color(0xFF5A4A42)   // very subtle

// ─── Legacy aliases ─────────────────────────────────────────────────────────
val DarkBackground   = EmberBlack
val DarkSurface      = EmberSurface
val PrimaryAccent    = CherryRed
