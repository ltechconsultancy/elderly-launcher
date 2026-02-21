package com.elderlylauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Color definitions
object LauncherColors {
    // Grayscale
    val White = Color(0xFFFFFFFF)
    val Gray50 = Color(0xFFF9FAFB)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray200 = Color(0xFFE5E7EB)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    val Gray600 = Color(0xFF4B5563)
    val Gray700 = Color(0xFF374151)
    val Gray800 = Color(0xFF1F2937)

    // Green (Call)
    val Green50 = Color(0xFFF0FDF4)
    val Green200 = Color(0xFFBBF7D0)
    val Green500 = Color(0xFF22C55E)
    val Green600 = Color(0xFF16A34A)
    val Green700 = Color(0xFF15803D)

    // Blue (Messages)
    val Blue50 = Color(0xFFEFF6FF)
    val Blue100 = Color(0xFFDBEAFE)
    val Blue200 = Color(0xFFBFDBFE)
    val Blue500 = Color(0xFF3B82F6)
    val Blue600 = Color(0xFF2563EB)
    val Blue700 = Color(0xFF1D4ED8)

    // Orange (Notifications)
    val Orange50 = Color(0xFFFFF7ED)
    val Orange200 = Color(0xFFFED7AA)
    val Orange500 = Color(0xFFF97316)
    val Orange700 = Color(0xFFC2410C)

    // Red (Emergency)
    val Red50 = Color(0xFFFEF2F2)
    val Red200 = Color(0xFFFECACA)
    val Red500 = Color(0xFFEF4444)
    val Red600 = Color(0xFFDC2626)
    val Red700 = Color(0xFFB91C1C)

    // Purple (Camera)
    val Purple50 = Color(0xFFFAF5FF)
    val Purple200 = Color(0xFFE9D5FF)
    val Purple500 = Color(0xFFA855F7)
    val Purple700 = Color(0xFF7E22CE)

    // Pink (Photos)
    val Pink50 = Color(0xFFFDF2F8)
    val Pink200 = Color(0xFFFBCFE8)
    val Pink500 = Color(0xFFEC4899)
    val Pink700 = Color(0xFFBE185D)

    // Teal
    val Teal500 = Color(0xFF14B8A6)
}

// Theme accent color - can be changed by user
val LocalAccentColor = compositionLocalOf { LauncherColors.Blue500 }

fun getAccentColor(colorName: String): Color {
    return when (colorName) {
        "blue" -> LauncherColors.Blue500
        "green" -> LauncherColors.Green500
        "purple" -> LauncherColors.Purple500
        "orange" -> LauncherColors.Orange500
        "red" -> LauncherColors.Red500
        "teal" -> LauncherColors.Teal500
        else -> LauncherColors.Blue500
    }
}

private fun createColorScheme(primaryColor: Color) = lightColorScheme(
    primary = primaryColor,
    onPrimary = Color.White,
    secondary = LauncherColors.Green500,
    onSecondary = Color.White,
    background = LauncherColors.White,
    onBackground = LauncherColors.Gray800,
    surface = LauncherColors.White,
    onSurface = LauncherColors.Gray800,
    error = LauncherColors.Red500,
    onError = Color.White,
)

@Composable
fun ElderlyLauncherTheme(
    primaryColor: String = "blue",
    content: @Composable () -> Unit
) {
    val accentColor = getAccentColor(primaryColor)

    CompositionLocalProvider(LocalAccentColor provides accentColor) {
        MaterialTheme(
            colorScheme = createColorScheme(accentColor),
            typography = LauncherTypography,
            content = content
        )
    }
}
