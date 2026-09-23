package com.elderlylauncher.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Intent
import net.sourceforge.solitaire_cg_re.SolitaireCGRE
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
fun GamesScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val context = LocalContext.current
    val layout = rememberDeviceLayout()
    val installedApps by viewModel.installedApps.collectAsState()
    val gameApps by viewModel.gameApps.collectAsState()

    val gamesList = remember(installedApps, gameApps) {
        installedApps.filter { it.packageName in gameApps }
    }

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
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    context.startActivity(
                        Intent(context, SolitaireCGRE::class.java)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (layout.isTablet) 88.dp else 64.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.solitaire_play),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (gamesList.isEmpty()) {
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                val fit = appGridFit(
                    width = maxWidth,
                    height = maxHeight,
                    count = gamesList.size,
                    tablet = layout.isTablet
                )
                ButtonPagedGrid(
                    items = gamesList,
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
