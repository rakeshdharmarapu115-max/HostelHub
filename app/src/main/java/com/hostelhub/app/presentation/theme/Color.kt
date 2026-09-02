package com.hostelhub.app.presentation.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Colors (Deep Navy / Indigo)
val PrimaryNavy = Color(0xFF1A237E)
val PrimaryIndigo = Color(0xFF3F51B5)
val PrimaryLight = Color(0xFFDEE0FF)
val PrimaryContainer = Color(0xFFE9EDFF)
val OnPrimary = Color(0xFFFFFFFF)
val OnPrimaryContainer = Color(0xFF00105C)

// Secondary Colors (Teal Accent)
val SecondaryTeal = Color(0xFF00897B)
val SecondaryDark = Color(0xFF006A60)
val SecondaryContainer = Color(0xFF85F6E5)
val OnSecondary = Color(0xFFFFFFFF)
val OnSecondaryContainer = Color(0xFF007166)

// Tertiary / Accent Colors (Warm Amber / Gold)
val TertiaryAmber = Color(0xFFF59E0B)
val TertiaryDark = Color(0xFF603B00)
val TertiaryContainer = Color(0xFFFFC988)
val OnTertiary = Color(0xFFFFFFFF)
val OnTertiaryContainer = Color(0xFF2A1700)

// Neutral & Background Colors (Clean Light Theme with High Contrast Text)
val BackgroundCool = Color(0xFFF8FAFC)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceContainer = Color(0xFFF1F5F9)
val SurfaceContainerHigh = Color(0xFFE2E8F0)
val SurfaceVariant = Color(0xFFF1F5F9)
val OnBackground = Color(0xFF0F172A)
val OnSurface = Color(0xFF0F172A)
val OnSurfaceVariant = Color(0xFF475569)

// Outline & Borders
val OutlineColor = Color(0xFF94A3B8)
val OutlineVariant = Color(0xFFE2E8F0)
val BorderSubtle = Color(0xFFE2E8F0)

// Functional & Semantic Colors
val StatusSuccess = Color(0xFF16A34A)
val StatusSuccessBg = Color(0xFFDCFCE7)
val StatusWarning = Color(0xFFD97706)
val StatusWarningBg = Color(0xFFFEF3C7)
val StatusError = Color(0xFFDC2626)
val StatusErrorBg = Color(0xFFFEE2E2)
val StatusInfo = Color(0xFF2563EB)
val StatusInfoBg = Color(0xFFDBEAFE)

// ==========================================
// DISTINCT DASHBOARD THEMED COLOR PALETTES
// ==========================================

// 1. Student Dashboard (Ocean Blue & Modern Cyan/Teal)
val StudentHeroBg = Color(0xFF0C2340)
val StudentHeroGradientEnd = Color(0xFF0284C7)
val StudentAccent = Color(0xFF0284C7)
val StudentAccentContainer = Color(0xFFE0F2FE)
val StudentOnAccentContainer = Color(0xFF0369A1)
val StudentBackground = Color(0xFFF8FAFC)
val StudentCardBg = Color(0xFFFFFFFF)
val StudentBadgeBg = Color(0xFFE0F7FA)
val StudentBadgeText = Color(0xFF00695C)

// 2. Host / Warden Dashboard (Emerald Green & Deep Forest)
val HostHeroBg = Color(0xFF064E3B)
val HostHeroGradientEnd = Color(0xFF059669)
val HostAccent = Color(0xFF059669)
val HostAccentContainer = Color(0xFFD1FAE5)
val HostOnAccentContainer = Color(0xFF047857)
val HostBackground = Color(0xFFF8FAFC)
val HostCardBg = Color(0xFFFFFFFF)
val HostBadgeBg = Color(0xFFDCFCE7)
val HostBadgeText = Color(0xFF166534)

// 3. Admin / Council Dashboard (Royal Violet & Midnight Plum)
val AdminHeroBg = Color(0xFF311042)
val AdminHeroGradientEnd = Color(0xFF7C3AED)
val AdminAccent = Color(0xFF7C3AED)
val AdminAccentContainer = Color(0xFFEDE9FE)
val AdminOnAccentContainer = Color(0xFF5B21B6)
val AdminBackground = Color(0xFFF8FAFC)
val AdminCardBg = Color(0xFFFFFFFF)
val AdminBadgeBg = Color(0xFFF3E8FF)
val AdminBadgeText = Color(0xFF6B21A8)

// Dark Theme Overrides
val PrimaryNavyDark = Color(0xFF1E3A8A)
val SecondaryTealDark = Color(0xFF0D9488)
val BackgroundDark = Color(0xFFF8FAFC)
val SurfaceDark = Color(0xFFFFFFFF)
val SurfaceVariantDark = Color(0xFFF1F5F9)
val OnBackgroundDark = Color(0xFF0F172A)
val OnSurfaceDark = Color(0xFF0F172A)
val OnSurfaceVariantDark = Color(0xFF475569)

// Colorful Vibrant Badges & Accents (with guaranteed high-contrast visible text)
val ColorTagCyan = Color(0xFF0284C7)
val ColorTagCyanBg = Color(0xFFE0F2FE)
val ColorTagGreen = Color(0xFF16A34A)
val ColorTagGreenBg = Color(0xFFDCFCE7)
val ColorTagPurple = Color(0xFF7C3AED)
val ColorTagPurpleBg = Color(0xFFEDE9FE)
val ColorTagAmber = Color(0xFFD97706)
val ColorTagAmberBg = Color(0xFFFEF3C7)
val ColorTagRose = Color(0xFFE11D48)
val ColorTagRoseBg = Color(0xFFFFE4E6)
val ColorTagIndigo = Color(0xFF4F46E5)
val ColorTagIndigoBg = Color(0xFFEEF2FF)

val VibrantAccents = listOf(
    Pair(ColorTagCyan, ColorTagCyanBg),
    Pair(ColorTagGreen, ColorTagGreenBg),
    Pair(ColorTagPurple, ColorTagPurpleBg),
    Pair(ColorTagAmber, ColorTagAmberBg),
    Pair(ColorTagRose, ColorTagRoseBg),
    Pair(ColorTagIndigo, ColorTagIndigoBg)
)

fun getVibrantAccent(indexOrSeed: Any): Pair<Color, Color> {
    val hash = kotlin.math.abs(indexOrSeed.hashCode())
    return VibrantAccents[hash % VibrantAccents.size]
}
