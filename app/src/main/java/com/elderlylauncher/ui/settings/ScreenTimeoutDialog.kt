package com.elderlylauncher.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.elderlylauncher.R
import com.elderlylauncher.ui.ButtonPagedColumn
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.util.ScreenTimeoutHelper

@Composable
fun ScreenTimeoutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var canWrite by remember { mutableStateOf(ScreenTimeoutHelper.canWriteSettings(context)) }
    var current by remember { mutableIntStateOf(ScreenTimeoutHelper.readMillis(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canWrite = ScreenTimeoutHelper.canWriteSettings(context)
                current = ScreenTimeoutHelper.readMillis(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
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
                        text = stringResource(R.string.settings_screen_timeout),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LauncherColors.Gray800
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
                    text = stringResource(R.string.settings_screen_timeout_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LauncherColors.Gray600
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.settings_screen_timeout_now,
                        timeoutLabel(current)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = LauncherColors.Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (!canWrite) {
                    Text(
                        text = stringResource(R.string.settings_screen_timeout_permission),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LauncherColors.Gray700,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { ScreenTimeoutHelper.requestWriteSettings(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Blue500)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_brightness_permission_button),
                            fontSize = 18.sp
                        )
                    }
                } else {
                    ButtonPagedColumn(
                        items = ScreenTimeoutHelper.OPTIONS_MS,
                        pageSize = 4,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) { millis ->
                        val selected = millis == current
                        Button(
                            onClick = {
                                if (ScreenTimeoutHelper.apply(context, millis)) {
                                    current = millis
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) LauncherColors.Blue500 else LauncherColors.Gray200,
                                contentColor = if (selected) Color.White else LauncherColors.Gray800
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text(
                                text = timeoutLabel(millis),
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun timeoutLabel(millis: Int): String {
    if (millis < 60_000) {
        val seconds = (millis / 1000).coerceAtLeast(1)
        return pluralStringResource(R.plurals.screen_timeout_seconds, seconds, seconds)
    }
    val minutes = (millis / 60_000).coerceAtLeast(1)
    return pluralStringResource(R.plurals.screen_timeout_minutes, minutes, minutes)
}
