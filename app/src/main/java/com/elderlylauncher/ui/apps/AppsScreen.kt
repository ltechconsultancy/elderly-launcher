package com.elderlylauncher.ui.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
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
import com.elderlylauncher.ui.ButtonPagedGrid
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.rememberDeviceLayout
import com.elderlylauncher.ui.theme.LauncherColors

@Composable
fun AppsScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val context = LocalContext.current
    val layout = rememberDeviceLayout()
    val installedApps by viewModel.installedApps.collectAsState()
    val allowedApps by viewModel.appsPageAllowed.collectAsState()

    val visibleApps = remember(installedApps, allowedApps) {
        installedApps.filter { it.packageName in allowedApps }
    }
    val columns = layout.appGridColumns
    val rows = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.apps_title),
                style = MaterialTheme.typography.headlineLarge,
                color = LauncherColors.Gray800
            )
            Text(
                text = stringResource(R.string.apps_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = LauncherColors.Gray600,
                fontSize = 18.sp
            )
        }

        if (visibleApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = LauncherColors.Gray300,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.apps_empty),
                        style = MaterialTheme.typography.titleLarge,
                        color = LauncherColors.Gray500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.apps_empty_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LauncherColors.Gray400,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            ButtonPagedGrid(
                items = visibleApps,
                columns = columns,
                rows = rows,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) { app ->
                AppGridItem(
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

@Composable
fun AppGridItem(
    appInfo: AppInfo,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LauncherColors.Gray50)
            .border(1.dp, LauncherColors.Gray200, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = appInfo.icon.toBitmap(128, 128).asImageBitmap(),
            contentDescription = appInfo.label,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

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
