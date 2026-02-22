package com.elderlylauncher.ui.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
import com.elderlylauncher.data.AppInfo
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.theme.LauncherColors

@Composable
fun GamesScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val context = LocalContext.current
    val installedApps by viewModel.installedApps.collectAsState()
    val gameApps by viewModel.gameApps.collectAsState()

    // Filter to only show apps marked as games
    val gamesList = remember(installedApps, gameApps) {
        installedApps.filter { it.packageName in gameApps }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.games_title),
                style = MaterialTheme.typography.headlineLarge,
                color = LauncherColors.Gray800
            )
            Text(
                text = stringResource(R.string.games_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = LauncherColors.Gray600,
                fontSize = 18.sp
            )
        }

        if (gamesList.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = LauncherColors.Gray300,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.games_empty),
                        style = MaterialTheme.typography.titleLarge,
                        color = LauncherColors.Gray500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.games_empty_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LauncherColors.Gray400,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Games grid - auto-scaling based on count
            val columns = when {
                gamesList.size <= 4 -> 2
                gamesList.size <= 9 -> 3
                else -> 4
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gamesList) { app ->
                    GameGridItem(
                        appInfo = app,
                        onClick = {
                            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (intent != null) {
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GameGridItem(
    appInfo: AppInfo,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LauncherColors.Purple50)
            .border(1.dp, LauncherColors.Purple200, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App icon - large for elderly
        Image(
            bitmap = appInfo.icon.toBitmap(128, 128).asImageBitmap(),
            contentDescription = appInfo.label,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // App name
        Text(
            text = appInfo.label,
            style = MaterialTheme.typography.bodyMedium,
            color = LauncherColors.Gray800,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
