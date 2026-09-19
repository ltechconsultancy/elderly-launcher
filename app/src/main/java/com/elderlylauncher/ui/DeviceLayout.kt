package com.elderlylauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elderlylauncher.utils.PermissionHelper

/**
 * Screen and hardware traits for phone vs tablet (POCO Pad / Pad X1 / M1 / C1).
 *
 * Home uses fewer, larger tiles. The Apps page uses a denser, smaller grid.
 * Landscape is first-class: a tablet on its side is not a stretched phone layout.
 */
data class DeviceLayout(
    val isTablet: Boolean,
    val isLandscape: Boolean,
    val widthDp: Int,
    val heightDp: Int,
    val homeGridColumns: Int,
    val homeGridRows: Int,
    val appGridColumns: Int,
    val appGridRows: Int,
    val contentMaxWidth: Dp,
    /** True only when a usable SIM is present. Wi-Fi tablets stay false. */
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
    val hasTelephony = PermissionHelper.hasActiveSim(context)

    val homeGridColumns: Int
    val homeGridRows: Int
    when {
        isLandscape && isTablet -> {
            homeGridColumns = 4
            homeGridRows = 1
        }
        isLandscape -> {
            homeGridColumns = 3
            homeGridRows = 1
        }
        isTablet -> {
            homeGridColumns = 3
            homeGridRows = 2
        }
        else -> {
            homeGridColumns = 2
            homeGridRows = 2
        }
    }

    val appGridColumns = when {
        widthDp >= 1200 -> 6
        widthDp >= 840 -> 5
        widthDp >= 600 -> 4
        isLandscape -> 4
        else -> 3
    }
    val appGridRows = if (isLandscape) 2 else 3

    return DeviceLayout(
        isTablet = isTablet,
        isLandscape = isLandscape,
        widthDp = widthDp,
        heightDp = heightDp,
        homeGridColumns = homeGridColumns,
        homeGridRows = homeGridRows,
        appGridColumns = appGridColumns,
        appGridRows = appGridRows,
        contentMaxWidth = if (isLandscape) 1100.dp else 720.dp,
        hasTelephony = hasTelephony
    )
}
