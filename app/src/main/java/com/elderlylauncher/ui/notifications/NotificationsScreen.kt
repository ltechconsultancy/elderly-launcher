package com.elderlylauncher.ui.notifications

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
import com.elderlylauncher.data.AppInfo
import com.elderlylauncher.ui.ButtonPagedColumn
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.rememberDeviceLayout
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.util.ElderlyNotificationListener
import com.elderlylauncher.util.InboxNotification
import com.elderlylauncher.util.NotificationInbox
import java.text.DateFormat
import java.util.Date

@Composable
fun NotificationsScreen(viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val layout = rememberDeviceLayout()
    val blocked by viewModel.notificationBlockedApps.collectAsState()
    val inbox by NotificationInbox.items.collectAsState()
    var accessEnabled by remember { mutableStateOf(ElderlyNotificationListener.isEnabled(context)) }
    var confirmAll by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessEnabled = ElderlyNotificationListener.isEnabled(context)
                if (accessEnabled && NotificationInbox.service == null) {
                    ElderlyNotificationListener.requestRebind(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val visible = remember(inbox, blocked) {
        inbox.filter { it.packageName !in blocked }
    }
    val pageSize = when {
        layout.isTablet && !layout.isLandscape -> 3
        layout.isTablet -> 2
        layout.isLandscape -> 1
        else -> 2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.notifications_title),
                style = MaterialTheme.typography.headlineLarge,
                color = LauncherColors.Gray800
            )
            Text(
                text = stringResource(R.string.notifications_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = LauncherColors.Gray600,
                fontSize = 18.sp
            )
        }

        if (!accessEnabled) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = LauncherColors.Orange500,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.notifications_access_needed),
                    style = MaterialTheme.typography.titleLarge,
                    color = LauncherColors.Gray800,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Orange500)
                ) {
                    Text(
                        text = stringResource(R.string.notifications_access_button),
                        fontSize = 22.sp
                    )
                }
            }
        } else if (visible.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.notifications_empty),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LauncherColors.Gray500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            ButtonPagedColumn(
                items = visible,
                pageSize = pageSize,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) { item ->
                NotificationCard(
                    item = item,
                    onDelete = { NotificationInbox.dismiss(item.key) }
                )
            }

            Button(
                onClick = { confirmAll = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(72.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Red500)
            ) {
                Text(
                    text = stringResource(R.string.notifications_clear_all),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (confirmAll) {
        AlertDialog(
            onDismissRequest = { confirmAll = false },
            title = {
                Text(
                    text = stringResource(R.string.notifications_clear_all_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.notifications_clear_all_message),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmAll = false
                        NotificationInbox.dismissAll(visible.map { it.key })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Red500)
                ) {
                    Text(
                        text = stringResource(R.string.notifications_clear_all),
                        fontSize = 18.sp
                    )
                }
            },
            dismissButton = {
                Button(onClick = { confirmAll = false }) {
                    Text(text = stringResource(R.string.cancel), fontSize = 18.sp)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun NotificationCard(
    item: InboxNotification,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val icon = remember(item.packageName) {
        try {
            context.packageManager.getApplicationIcon(item.packageName)
                .toBitmap(128, 128)
                .asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
    val appName = remember(item.packageName) {
        try {
            val info = context.packageManager.getApplicationInfo(item.packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            item.packageName
        }
    }
    val time = remember(item.postedAt) {
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.postedAt))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LauncherColors.Orange50)
            .border(2.dp, LauncherColors.Orange200, RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    color = LauncherColors.Orange700,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.title,
                    color = LauncherColors.Gray800,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.text.isNotBlank()) {
                    Text(
                        text = item.text,
                        color = LauncherColors.Gray600,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = time,
                color = LauncherColors.Gray500,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Red500)
        ) {
            Text(
                text = stringResource(R.string.notifications_delete),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NotificationAppsDialog(
    installedApps: List<AppInfo>,
    blockedApps: Set<String>,
    onDismiss: () -> Unit,
    onToggleApp: (String) -> Unit
) {
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
                        text = stringResource(R.string.settings_notifications),
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
                    text = stringResource(R.string.settings_notifications_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LauncherColors.Gray600
                )
                Spacer(modifier = Modifier.height(12.dp))
                ButtonPagedColumn(
                    items = installedApps,
                    pageSize = 6,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) { app ->
                    val shown = app.packageName !in blockedApps
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (shown) LauncherColors.Green50 else LauncherColors.Gray100)
                            .clickable { onToggleApp(app.packageName) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (shown) LauncherColors.Gray800 else LauncherColors.Gray500,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = if (shown) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (shown) LauncherColors.Green500 else LauncherColors.Gray400
                        )
                    }
                }
            }
        }
    }
}
