package com.elderlylauncher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
import com.elderlylauncher.ui.AppIconCard
import com.elderlylauncher.ui.ButtonPagedGrid
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.appGridFit
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (layout.isTablet) 32.dp else 24.dp,
                    vertical = if (layout.isLandscape) 12.dp else 24.dp
                )
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                val fit = appGridFit(
                    width = maxWidth,
                    height = maxHeight,
                    count = visibleApps.size,
                    tablet = layout.isTablet
                )
                ButtonPagedGrid(
                    items = visibleApps,
                    columns = fit.columns,
                    rows = fit.rowsPerPage,
                    modifier = Modifier.fillMaxSize()
                ) { app ->
                    AppIconCard(
                        appInfo = app,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(fit.tile),
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
