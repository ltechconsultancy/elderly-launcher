package com.elderlylauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
import com.elderlylauncher.ui.photos.PhotoCarouselScreen
import com.elderlylauncher.ui.settings.SettingsScreen
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.ui.volume.VolumeScreen
import com.elderlylauncher.util.BrightnessHelper
import com.elderlylauncher.util.BrightnessLockWatcher

@Composable
fun LauncherApp(viewModel: LauncherViewModel = viewModel()) {
    val totalPages = 6
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val pageStateHolder = rememberSaveableStateHolder()

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
        if (brightnessLocked) {
            BrightnessHelper.apply(context, brightnessPercent)
            watcher.start()
        } else {
            BrightnessHelper.clearWindow(context)
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && brightnessLocked) {
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
            pageStateHolder.SaveableStateProvider(currentPage) {
                when (currentPage) {
                    0 -> HomeScreen()
                    1 -> AppsScreen()
                    2 -> GamesScreen()
                    3 -> PhotoCarouselScreen()
                    4 -> VolumeScreen()
                    5 -> SettingsScreen()
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
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

    Column(
        modifier = modifier.semantics { contentDescription = pageDescription },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onPageClick(currentPage - 1) },
                enabled = currentPage > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LauncherColors.Gray800,
                    disabledContainerColor = LauncherColors.Gray200,
                    disabledContentColor = LauncherColors.Gray500
                )
            ) {
                Text(
                    text = stringResource(R.string.nav_previous),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            Button(
                onClick = { onPageClick(currentPage + 1) },
                enabled = currentPage < totalPages - 1,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LauncherColors.Gray800,
                    disabledContainerColor = LauncherColors.Gray200,
                    disabledContentColor = LauncherColors.Gray500
                )
            ) {
                Text(
                    text = stringResource(R.string.nav_next),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (page in 0 until totalPages) {
                val isSelected = page == currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(44.dp)
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
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) LauncherColors.White
                        else LauncherColors.Gray500
                    )
                }
            }
        }
    }
}
