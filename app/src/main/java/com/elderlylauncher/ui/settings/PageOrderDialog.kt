package com.elderlylauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
import com.elderlylauncher.ui.ButtonPagedColumn
import com.elderlylauncher.ui.LauncherPage
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.theme.LauncherColors

@Composable
fun PageOrderDialog(
    onDismiss: () -> Unit,
    viewModel: LauncherViewModel = viewModel()
) {
    val pages by viewModel.pageOrder.collectAsState()
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_page_order),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LauncherColors.Gray800,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = LauncherColors.Gray600
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.settings_page_order_hint),
                    color = LauncherColors.Gray600,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                ButtonPagedColumn(
                    items = pages,
                    pageSize = 4,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) { page ->
                    val index = pages.indexOf(page)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LauncherColors.Gray100, RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pageLabel(page),
                            modifier = Modifier.weight(1f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LauncherColors.Gray800
                        )
                        IconButton(
                            onClick = { viewModel.movePage(index, -1) },
                            enabled = index > 0,
                            modifier = Modifier
                                .size(56.dp)
                                .background(LauncherColors.Blue500, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.page_order_up),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.movePage(index, 1) },
                            enabled = index < pages.lastIndex,
                            modifier = Modifier
                                .size(56.dp)
                                .background(LauncherColors.Blue500, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.page_order_down),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun pageLabel(page: LauncherPage): String {
    return when (page) {
        LauncherPage.HOME -> stringResource(R.string.page_home)
        LauncherPage.APPS -> stringResource(R.string.apps_title)
        LauncherPage.GAMES -> stringResource(R.string.games_title)
        LauncherPage.VOLUME -> stringResource(R.string.volume_title)
        LauncherPage.NOTIFICATIONS -> stringResource(R.string.notifications_title)
        LauncherPage.PHOTOS -> stringResource(R.string.photos_title)
        LauncherPage.SETTINGS -> stringResource(R.string.settings_title)
    }
}
