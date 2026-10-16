package com.danyal.expensetracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Brand colours used across the app. Indigo is the main colour, teal and coral are accents. */
object Brand {
    val Indigo900 = Color(0xFF1E1B4B)
    val Indigo800 = Color(0xFF312E81)
    val Indigo700 = Color(0xFF3730A3)
    val Indigo600 = Color(0xFF4F46E5)
    val Indigo500 = Color(0xFF6366F1)
    val Indigo100 = Color(0xFFE0E7FF)
    val Teal = Color(0xFF14B8A6)
    val TealDark = Color(0xFF0D9488)
    val Coral = Color(0xFFFB7185)
    val Amber = Color(0xFFF59E0B)
    val Danger = Color(0xFFEF4444)

    val Income = Color(0xFF14B8A6)
    val Expense = Color(0xFFFB7185)

    /** The signature indigo-to-teal gradient used in headers and buttons. */
    val HeaderBrush = Brush.linearGradient(listOf(Indigo800, Indigo600, TealDark))
    val ButtonBrush = Brush.horizontalGradient(listOf(Indigo600, Indigo500, Teal))
}

private val LightColors = lightColorScheme(
    primary = Brand.Indigo600,
    onPrimary = Color.White,
    primaryContainer = Brand.Indigo100,
    onPrimaryContainer = Brand.Indigo900,
    secondary = Brand.TealDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF042F2E),
    tertiary = Brand.Coral,
    background = Color(0xFFF4F5FF),
    onBackground = Color(0xFF1B1B2F),
    surface = Color.White,
    onSurface = Color(0xFF1B1B2F),
    surfaceVariant = Color(0xFFE8EAFE),
    onSurfaceVariant = Color(0xFF5B5E7A),
    outline = Color(0xFFC3C6E0),
    error = Brand.Danger
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8B8FFF),
    onPrimary = Brand.Indigo900,
    primaryContainer = Brand.Indigo700,
    onPrimaryContainer = Brand.Indigo100,
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Brand.Coral,
    background = Color(0xFF0F0E1F),
    onBackground = Color(0xFFE8E8F5),
    surface = Color(0xFF1A1833),
    onSurface = Color(0xFFE8E8F5),
    surfaceVariant = Color(0xFF28254A),
    onSurfaceVariant = Color(0xFFB4B6D4),
    outline = Color(0xFF47496B),
    error = Color(0xFFFF6B6B)
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

/** Currency symbol for the whole UI, provided once in MainActivity. */
val LocalCurrency = compositionLocalOf { "Rs" }

@Composable
fun ExpenseTrackerTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
