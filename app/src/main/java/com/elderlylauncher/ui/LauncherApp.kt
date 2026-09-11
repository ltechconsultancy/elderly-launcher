package com.elderlylauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
import com.elderlylauncher.util.BrightnessHelper
import com.elderlylauncher.util.BrightnessLockWatcher
import com.elderlylauncher.ui.apps.AppsScreen
import com.elderlylauncher.ui.games.GamesScreen
import com.elderlylauncher.ui.home.HomeScreen
import com.elderlylauncher.ui.photos.PhotoCarouselScreen
import com.elderlylauncher.ui.settings.SettingsScreen
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.ui.volume.VolumeScreen

@Composable
fun LauncherApp(viewModel: LauncherViewModel = viewModel()) {
    val totalPages = 6
    var savedPage by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = savedPage,
        pageCount = { totalPages }
    )
    val coroutineScope = rememberCoroutineScope()

    // Save current page across config changes
    LaunchedEffect(pagerState.currentPage) {
        savedPage = pagerState.currentPage
    }

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
        // Main content with horizontal paging
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> HomeScreen()
                1 -> AppsScreen()
                2 -> GamesScreen()
                3 -> PhotoCarouselScreen()
                4 -> VolumeScreen()
                5 -> SettingsScreen()
            }
        }

        // Page indicator with numbers
        PageIndicator(
            currentPage = pagerState.currentPage,
            totalPages = totalPages,
            onPageClick = { page ->
                coroutineScope.launch { pagerState.animateScrollToPage(page) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 16.dp)
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

    Row(
        modifier = modifier.semantics { contentDescription = pageDescription },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (page in 0 until totalPages) {
            val isSelected = page == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(48.dp)
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
