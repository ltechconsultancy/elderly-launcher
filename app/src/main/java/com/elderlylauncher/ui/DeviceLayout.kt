package com.elderlylauncher.ui

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen and hardware traits for phone vs tablet (POCO Pad / Pad X1 / M1 / C1).
 *
 * Breakpoints follow Material window size classes: compact < 600dp shortest side.
 */
data class DeviceLayout(
    val isTablet: Boolean,
    val isLandscape: Boolean,
    val widthDp: Int,
    val heightDp: Int,
    val appGridColumns: Int,
    val contentMaxWidth: Dp,
    val hasTelephony: Boolean
)

@Composable
fun rememberDeviceLayout(): DeviceLayout {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val shortest = minOf(widthDp, heightDp)
    val isTablet = shortest >= 600
    val isLandscape = widthDp > heightDp
    val hasTelephony = remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }
    val appGridColumns = when {
        widthDp >= 1200 -> 6
        widthDp >= 840 -> 5
        widthDp >= 600 -> 4
        else -> 3
    }
    return DeviceLayout(
        isTablet = isTablet,
        isLandscape = isLandscape,
        widthDp = widthDp,
        heightDp = heightDp,
        appGridColumns = appGridColumns,
        contentMaxWidth = 720.dp,
        hasTelephony = hasTelephony
    )
}
