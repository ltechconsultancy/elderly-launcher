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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
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

    // Save current page across config changes
    LaunchedEffect(pagerState.currentPage) {
        savedPage = pagerState.currentPage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
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
    modifier: Modifier = Modifier
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) LauncherColors.Gray800
                        else LauncherColors.Gray200
                    ),
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
