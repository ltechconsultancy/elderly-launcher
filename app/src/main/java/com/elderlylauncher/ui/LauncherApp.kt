package com.elderlylauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
import com.elderlylauncher.ui.apps.AppsScreen
import com.elderlylauncher.ui.games.GamesScreen
import com.elderlylauncher.ui.home.HomeScreen
import com.elderlylauncher.ui.notifications.NotificationsScreen
import com.elderlylauncher.ui.photos.PhotoCarouselScreen
import com.elderlylauncher.ui.settings.SettingsScreen
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.ui.volume.VolumeScreen
import com.elderlylauncher.util.BrightnessHelper
import com.elderlylauncher.util.BrightnessLockWatcher

@Composable
fun LauncherApp(viewModel: LauncherViewModel = viewModel()) {
    val pageOrder by viewModel.pageOrder.collectAsState()
    val pages = if (pageOrder.isEmpty()) LauncherPage.DEFAULT else pageOrder
    val totalPages = pages.size
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val pageStateHolder = rememberSaveableStateHolder()
    if (currentPage > pages.lastIndex) currentPage = pages.lastIndex.coerceAtLeast(0)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val brightnessLocked by viewModel.brightnessLocked.collectAsState()
    val brightnessPercent by viewModel.brightnessPercent.collectAsState()

    DisposableEffect(brightnessLocked, brightnessPercent, lifecycleOwner) {
        val watcher = BrightnessLockWatcher(
            context = context,
            isLocked = { brightnessLocked },
            lockedPercent = { brightnessPercent }
        )
        BrightnessHelper.apply(context, brightnessPercent)
        if (brightnessLocked) {
            watcher.start()
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                BrightnessHelper.apply(context, brightnessPercent)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            watcher.stop()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
            .statusBarsPadding()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            pageStateHolder.SaveableStateProvider(pages[currentPage].name) {
                when (pages[currentPage]) {
                    LauncherPage.HOME -> HomeScreen()
                    LauncherPage.APPS -> AppsScreen()
                    LauncherPage.GAMES -> GamesScreen()
                    LauncherPage.PHOTOS -> PhotoCarouselScreen()
                    LauncherPage.VOLUME -> VolumeScreen()
                    LauncherPage.NOTIFICATIONS -> NotificationsScreen()
                    LauncherPage.SETTINGS -> SettingsScreen()
                }
            }
        }

        PageIndicator(
            currentPage = currentPage,
            totalPages = totalPages,
            onPageClick = { page -> currentPage = page },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
    onPageClick: (Int) -> Unit = {}
) {
    val pageDescription = stringResource(R.string.home_page, currentPage + 1, totalPages)
    val widthDp = LocalConfiguration.current.screenWidthDp
    val numberSize = when {
        widthDp < 360 -> 26.dp
        widthDp < 480 -> 30.dp
        widthDp < 700 -> 36.dp
        else -> 48.dp
    }
    val chevronSize = when {
        widthDp < 360 -> 40.dp
        widthDp < 480 -> 48.dp
        widthDp < 700 -> 56.dp
        else -> 72.dp
    }
    val numberPad = if (widthDp < 700) 0.dp else 4.dp

    Row(
        modifier = modifier.semantics { contentDescription = pageDescription },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavIconButton(
            previous = true,
            enabled = currentPage > 0,
            size = chevronSize,
            onClick = { onPageClick(currentPage - 1) }
        )
        for (page in 0 until totalPages) {
            val isSelected = page == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = numberPad)
                    .size(numberSize)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) LauncherColors.Gray800
                        else LauncherColors.Gray200
                    )
                    .clickable { onPageClick(page) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (page + 1).toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = when {
                        widthDp < 360 -> 14.sp
                        widthDp < 480 -> 16.sp
                        widthDp < 700 -> 18.sp
                        else -> 22.sp
                    },
                    color = if (isSelected) LauncherColors.White
                    else LauncherColors.Gray500
                )
            }
        }
        NavIconButton(
            previous = false,
            enabled = currentPage < totalPages - 1,
            size = chevronSize,
            onClick = { onPageClick(currentPage + 1) }
        )
    }
}
