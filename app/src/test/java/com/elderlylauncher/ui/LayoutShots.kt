package com.elderlylauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.ScreenOrientation
import android.graphics.drawable.ColorDrawable
import com.elderlylauncher.data.AppInfo
import com.elderlylauncher.data.QuickContact
import com.elderlylauncher.ui.home.CallContactPages
import com.elderlylauncher.ui.notifications.NotificationCard
import com.elderlylauncher.ui.settings.AppSlotItem
import com.elderlylauncher.util.InboxNotification
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.ui.volume.VolumeCardGrid
import com.elderlylauncher.ui.volume.VolumeControl
import org.junit.Rule
import org.junit.Test

class LayoutShots {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
        showSystemUi = false
    )

    @Test
    fun volumePhonePortrait() = volume(DeviceConfig.PIXEL_5, tablet = false, landscape = false, "volume-phone-portrait")

    @Test
    fun volumePhoneLandscape() = volume(DeviceConfig.PIXEL_5.landscape(), tablet = false, landscape = true, "volume-phone-landscape")

    @Test
    fun volumeTabletPortrait() = volume(DeviceConfig.NEXUS_10.portrait(), tablet = true, landscape = false, "volume-tablet-portrait")

    @Test
    fun volumeTabletLandscape() = volume(DeviceConfig.NEXUS_10, tablet = true, landscape = true, "volume-tablet-landscape")

    @Test
    fun callsPhonePortrait() = calls(DeviceConfig.PIXEL_5, "calls-phone-portrait")

    @Test
    fun callsPhoneLandscape() = calls(DeviceConfig.PIXEL_5.landscape(), "calls-phone-landscape")

    @Test
    fun homeSlotsPhonePortrait() = slots(DeviceConfig.PIXEL_5, "home-slots-phone-portrait")

    @Test
    fun homeSlotsPhoneLandscape() = slots(DeviceConfig.PIXEL_5.landscape(), "home-slots-phone-landscape")

    @Test
    fun homeTilesPhonePortrait() = tiles(DeviceConfig.PIXEL_5, tablet = false, landscape = false, "home-tiles-phone-portrait")

    @Test
    fun homeTilesPhoneLandscape() = tiles(DeviceConfig.PIXEL_5.landscape(), tablet = false, landscape = true, "home-tiles-phone-landscape")

    @Test
    fun notificationCardPhone() {
        paparazzi.unsafeUpdateConfig(deviceConfig = DeviceConfig.PIXEL_5.copy(locale = "nl"))
        paparazzi.snapshot("notification-open-app") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                NotificationCard(
                    item = InboxNotification(
                        key = "1",
                        packageName = "com.example.messages",
                        title = "Nieuw bericht",
                        text = "Kom je zo eten?",
                        postedAt = 1_700_000_000_000L
                    ),
                    onOpen = {},
                    onDelete = {}
                )
            }
        }
    }

    private fun volume(device: DeviceConfig, tablet: Boolean, landscape: Boolean, name: String) {
        paparazzi.unsafeUpdateConfig(deviceConfig = device)
        paparazzi.snapshot(name) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Text(
                        text = "Geluid",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = LauncherColors.Gray800
                    )
                    Text(
                        text = "Pas volume en helderheid aan",
                        fontSize = 18.sp,
                        color = LauncherColors.Gray600
                    )
                }
                VolumeCardGrid(
                    tablet = tablet,
                    landscape = landscape,
                    brightness = { mod ->
                        sampleCard(mod, tablet, "Helderheid", "Hoe licht het scherm is, minstens 10%.", Icons.Default.BrightnessHigh, LauncherColors.Green50, LauncherColors.Green200, LauncherColors.Green500, LauncherColors.Green700, 10, 100)
                    },
                    media = { mod ->
                        sampleCard(mod, tablet, "Media", "Geluid van muziek en video's.", Icons.Default.MusicNote, LauncherColors.Blue50, LauncherColors.Blue200, LauncherColors.Blue500, LauncherColors.Blue700, 12, 15)
                    },
                    notification = { mod ->
                        sampleCard(mod, tablet, "Meldingen", "Geluid van berichten en apps.", Icons.Default.Notifications, LauncherColors.Orange50, LauncherColors.Orange200, LauncherColors.Orange500, LauncherColors.Orange700, 0, 15)
                    },
                    alarm = { mod ->
                        sampleCard(mod, tablet, "Alarm", "Geluid van de wekker.", Icons.Default.Alarm, LauncherColors.Red50, LauncherColors.Red200, LauncherColors.Red500, LauncherColors.Red700, 6, 15)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◀   1  2  3  4  5  6  7   ▶", fontSize = 18.sp)
                }
            }
        }
    }

    private fun calls(device: DeviceConfig, name: String) {
        paparazzi.unsafeUpdateConfig(deviceConfig = device)
        val favorites = listOf(
            QuickContact("1", "Bart Luttels", "+31 6 81482521"),
            QuickContact("2", "Bas Meuter", "+31 6 23562132")
        )
        val others = listOf(
            QuickContact("3", "Alexander Hendriks", "+31 6 83672111"),
            QuickContact("4", "Andrea van Langen", "+31 6 51164528"),
            QuickContact("5", "Anaelo", "+31 6 10000005")
        )
        paparazzi.snapshot(name) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bellen", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        CallContactPages(
                            favorites = favorites,
                            others = others,
                            onCall = {},
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(LauncherColors.Green500, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Zelf een nummer", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    private fun slots(device: DeviceConfig, name: String) {
        paparazzi.unsafeUpdateConfig(deviceConfig = device)
        paparazzi.snapshot(name) {
            val icon = ColorDrawable(0xFF16A34A.toInt())
            val app = AppInfo("demo.app", "Camera", icon)
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Text("Startscherm", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                    BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val columns = if (maxWidth > maxHeight && maxWidth >= 520.dp) 2 else 1
                    ButtonPagedColumn(
                        items = (0 until 8).toList(),
                        pageSize = 12,
                        fitToHeight = true,
                        rowHeight = 88.dp,
                        columns = columns,
                        modifier = Modifier.fillMaxSize()
                    ) { index ->
                        AppSlotItem(
                            slotName = "Plek ${index + 1}",
                            appInfo = app,
                            onClick = {},
                            onClear = {}
                        )
                    }
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(LauncherColors.Green500, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("App toevoegen", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    private fun tiles(device: DeviceConfig, tablet: Boolean, landscape: Boolean, name: String) {
        paparazzi.unsafeUpdateConfig(deviceConfig = device)
        paparazzi.snapshot(name) {
            Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
                Text("12:30", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val fit = homeGridFit(maxWidth, maxHeight, 8, landscape, tablet)
                    val pageSize = fit.rowsPerPage * fit.columns
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(fit.gap, Alignment.CenterVertically)
                    ) {
                        (0 until minOf(8, pageSize)).toList().chunked(fit.columns).forEach { row ->
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.fillMaxWidth().height(fit.tile),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(fit.gap)
                            ) {
                                row.forEach { index ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .background(LauncherColors.Blue50, RoundedCornerShape(24.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("App ${index + 1}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(LauncherColors.Red500, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Noodgeval", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun DeviceConfig.landscape(): DeviceConfig = copy(orientation = ScreenOrientation.LANDSCAPE)

private fun DeviceConfig.portrait(): DeviceConfig = copy(orientation = ScreenOrientation.PORTRAIT)

@androidx.compose.runtime.Composable
private fun sampleCard(
    modifier: Modifier,
    tablet: Boolean,
    title: String,
    subtitle: String,
    icon: ImageVector,
    background: Color,
    border: Color,
    accent: Color,
    text: Color,
    current: Int,
    max: Int
) {
    VolumeControl(
        title = title,
        subtitle = subtitle,
        icon = icon,
        backgroundColor = background,
        borderColor = border,
        iconBackgroundColor = accent,
        accentColor = accent,
        textColor = text,
        currentVolume = current,
        maxVolume = max,
        decreaseDescription = "Minder",
        increaseDescription = "Meer",
        modifier = modifier,
        compact = !tablet,
        onVolumeUp = {},
        onVolumeDown = {}
    )
}
