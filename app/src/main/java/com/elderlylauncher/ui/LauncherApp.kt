package com.elderlylauncher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.ui.home.HomeScreen
import com.elderlylauncher.ui.volume.VolumeScreen
import com.elderlylauncher.ui.settings.SettingsScreen
import com.elderlylauncher.ui.theme.LauncherColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherApp(viewModel: LauncherViewModel = viewModel()) {
    val totalPages = 3
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { totalPages }
    )

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
                1 -> VolumeScreen()
                2 -> SettingsScreen()
            }
        }

        // Page indicator with numbers
        PageIndicator(
            currentPage = pagerState.currentPage,
            totalPages = totalPages,
            modifier = Modifier
                .fillMaxWidth()
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
    val pageDescription = "Pagina ${currentPage + 1} van $totalPages"

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
