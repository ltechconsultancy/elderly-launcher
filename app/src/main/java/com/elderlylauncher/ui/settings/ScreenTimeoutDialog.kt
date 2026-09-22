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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.elderlylauncher.R
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.util.ScreenTimeoutHelper

@Composable
fun ScreenTimeoutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var canWrite by remember { mutableStateOf(ScreenTimeoutHelper.canWriteSettings(context)) }
    var current by remember { mutableIntStateOf(ScreenTimeoutHelper.readMillis(context)) }
    val start = remember {
        val ms = ScreenTimeoutHelper.readMillis(context)
        if (ScreenTimeoutHelper.isNever(ms)) intArrayOf(0, 1, 0) else intArrayOf(
            (ms / 3_600_000).coerceIn(0, ScreenTimeoutHelper.MAX_HOURS),
            ((ms / 60_000) % 60).coerceIn(0, 59),
            ((ms / 1_000) % 60).coerceIn(0, 59)
        )
    }
    var hours by remember { mutableIntStateOf(start[0]) }
    var minutes by remember { mutableIntStateOf(start[1]) }
    var seconds by remember { mutableIntStateOf(start[2]) }

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

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
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
                        text = stringResource(R.string.settings_screen_timeout),
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
                    text = if (ScreenTimeoutHelper.isNever(current)) {
                        stringResource(R.string.screen_timeout_current_never)
                    } else {
                        stringResource(
                            R.string.screen_timeout_current,
                            current / 3_600_000,
                            (current / 60_000) % 60,
                            (current / 1_000) % 60
                        )
                    },
                    color = LauncherColors.Gray600,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (!canWrite) {
                    Button(
                        onClick = { ScreenTimeoutHelper.requestWriteSettings(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_brightness_permission_button),
                            fontSize = 20.sp
                        )
                    }
                } else {
                    TimeoutStepper(
                        label = stringResource(R.string.screen_timeout_hours),
                        value = hours,
                        onChange = { hours = it.coerceIn(0, ScreenTimeoutHelper.MAX_HOURS) }
                    )
                    TimeoutStepper(
                        label = stringResource(R.string.screen_timeout_minutes),
                        value = minutes,
                        onChange = { minutes = it.coerceIn(0, 59) }
                    )
                    TimeoutStepper(
                        label = stringResource(R.string.screen_timeout_seconds),
                        value = seconds,
                        onChange = { seconds = it.coerceIn(0, 59) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (ScreenTimeoutHelper.applyParts(context, hours, minutes, seconds)) {
                                current = ScreenTimeoutHelper.readMillis(context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Blue500)
                    ) {
                        Text(
                            text = stringResource(R.string.screen_timeout_apply),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (ScreenTimeoutHelper.applyNever(context)) {
                                current = ScreenTimeoutHelper.readMillis(context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Gray700)
                    ) {
                        Text(
                            text = stringResource(R.string.screen_timeout_never),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeoutStepper(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = LauncherColors.Gray800
        )
        StepButton("−") { onChange(value - 1) }
        Text(
            text = value.toString(),
            modifier = Modifier.padding(horizontal = 12.dp),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = LauncherColors.Gray800,
            textAlign = TextAlign.Center
        )
        StepButton("+") { onChange(value + 1) }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LauncherColors.Gray100)
    ) {
        Text(text = label, fontSize = 32.sp, color = LauncherColors.Gray800)
    }
}
