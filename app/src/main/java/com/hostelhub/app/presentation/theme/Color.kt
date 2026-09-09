package com.hostelhub.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// HOSTELHUB DARK NAVY / SLATE DESIGN SYSTEM
// ==========================================

// 1. Core Page & Surface Colors
val DarkNavyBackground = Color(0xFF0F172A)   // Page Canvas Background (#0F172A)
val SurfaceCard = Color(0xFF1E293B)          // Elevated Card Surface (#1E293B)
val SurfaceSecondary = Color(0xFF162235)     // Secondary Surface Container (#162235)
val SurfaceCardBorder = Color(0xFF334155)    // Card & Component Border (#334155)
val SurfaceContainer = Color(0xFF1E293B)
val SurfaceContainerHigh = Color(0xFF24344D)
val SurfaceVariant = Color(0xFF162235)
val SurfaceWhite = Color(0xFF1E293B)         // Unified card surface

// 2. High-Contrast Text Tokens
val TextPrimary = Color(0xFFF8FAFC)          // Primary Text (#F8FAFC)
val TextSecondary = Color(0xFFCBD5E1)        // Secondary Text (#CBD5E1)
val TextMuted = Color(0xFF94A3B8)            // Muted Subtitle / Timestamp (#94A3B8)
val OnBackground = Color(0xFFF8FAFC)
val OnSurface = Color(0xFFF8FAFC)
val OnSurfaceVariant = Color(0xFFCBD5E1)

// 3. Borders & Outlines
val OutlineColor = Color(0xFF94A3B8)
val OutlineVariant = Color(0xFF334155)
val BorderSubtle = Color(0xFF334155)

// 4. Primary Brand Accents (Vibrant Blue / Indigo)
val PrimaryNavy = Color(0xFF38BDF8)          // Vibrant Sky Blue
val PrimaryIndigo = Color(0xFF60A5FA)        // Vibrant Blue
val PrimaryLight = Color(0xFF1E3A8A)
val PrimaryContainer = Color(0xFF1E3A8A)     // Dark Blue Container
val OnPrimary = Color(0xFF0B132B)
val OnPrimaryContainer = Color(0xFFDBEAFE)

// 5. Secondary Accents (Vibrant Teal)
val SecondaryTeal = Color(0xFF2DD4BF)        // Radiant Teal
val SecondaryDark = Color(0xFF0D9488)
val SecondaryContainer = Color(0xFF0F766E)   // Dark Teal Container
val OnSecondary = Color(0xFF0B132B)
val OnSecondaryContainer = Color(0xFFCCFBF1)

// 6. Tertiary Accents (Warm Gold / Amber)
val TertiaryAmber = Color(0xFFFBBF24)        // Warm Amber
val TertiaryDark = Color(0xFFD97706)
val TertiaryContainer = Color(0xFF78350F)    // Dark Amber Container
val OnTertiary = Color(0xFF0B132B)
val OnTertiaryContainer = Color(0xFFFEF3C7)

// 7. Neutral Canvas Background Aliases
val BackgroundCool = DarkNavyBackground
val StudentBackground = DarkNavyBackground
val HostBackground = DarkNavyBackground
val AdminBackground = DarkNavyBackground

// 8. Functional & Semantic Status Colors
val StatusSuccess = Color(0xFF4ADE80)        // Vibrant Emerald Green
val StatusSuccessBg = Color(0xFF064E3B)      // Dark Green Container
val StatusWarning = Color(0xFFFBBF24)        // Vibrant Amber
val StatusWarningBg = Color(0xFF78350F)      // Dark Amber Container
val StatusError = Color(0xFFF87171)          // Vibrant Coral Red
val StatusErrorBg = Color(0xFF7F1D1D)        // Dark Red Container
val StatusInfo = Color(0xFF38BDF8)           // Vibrant Sky Blue
val StatusInfoBg = Color(0xFF0C4A6E)         // Dark Blue Container

// ==========================================
// DISTINCT HERO & ROLE PALETTES
// ==========================================

// Student Hero
val StudentHeroBg = Color(0xFF0369A1)
val StudentHeroGradientEnd = Color(0xFF0284C7)
val StudentAccent = Color(0xFF38BDF8)
val StudentAccentContainer = Color(0xFF0C4A6E)
val StudentOnAccentContainer = Color(0xFFBAE6FD)
val StudentCardBg = SurfaceCard
val StudentBadgeBg = Color(0xFF0C4A6E)
val StudentBadgeText = Color(0xFF38BDF8)

// Hostel Owner / Host Hero
val HostHeroBg = Color(0xFF065F46)
val HostHeroGradientEnd = Color(0xFF059669)
val HostAccent = Color(0xFF34D399)
val HostAccentContainer = Color(0xFF064E3B)
val HostOnAccentContainer = Color(0xFFA7F3D0)
val HostCardBg = SurfaceCard
val HostBadgeBg = Color(0xFF064E3B)
val HostBadgeText = Color(0xFF34D399)

// Associative Head / Admin Hero
val AdminHeroBg = Color(0xFF581C87)
val AdminHeroGradientEnd = Color(0xFF7C3AED)
val AdminAccent = Color(0xFFC084FC)
val AdminAccentContainer = Color(0xFF3B0764)
val AdminOnAccentContainer = Color(0xFFE9D5FF)
val AdminCardBg = SurfaceCard
val AdminBadgeBg = Color(0xFF3B0764)
val AdminBadgeText = Color(0xFFC084FC)

// Dark Theme Overrides (kept consistent)
val PrimaryNavyDark = Color(0xFF60A5FA)
val SecondaryTealDark = Color(0xFF2DD4BF)
val BackgroundDark = DarkNavyBackground
val SurfaceDark = SurfaceCard
val SurfaceVariantDark = SurfaceSecondary
val OnBackgroundDark = TextPrimary
val OnSurfaceDark = TextPrimary
val OnSurfaceVariantDark = TextSecondary

// Colorful Vibrant Badges & Accents (Dark-Theme High-Contrast)
val ColorTagCyan = Color(0xFF38BDF8)
val ColorTagCyanBg = Color(0xFF0C4A6E)
val ColorTagGreen = Color(0xFF4ADE80)
val ColorTagGreenBg = Color(0xFF064E3B)
val ColorTagPurple = Color(0xFFC084FC)
val ColorTagPurpleBg = Color(0xFF3B0764)
val ColorTagAmber = Color(0xFFFBBF24)
val ColorTagAmberBg = Color(0xFF78350F)
val ColorTagRose = Color(0xFFFB7185)
val ColorTagRoseBg = Color(0xFF881337)
val ColorTagIndigo = Color(0xFF818CF8)
val ColorTagIndigoBg = Color(0xFF312E81)

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
