package com.elderlylauncher.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.layout.widthIn
import com.elderlylauncher.R
import com.elderlylauncher.ui.DialogHeader
import com.elderlylauncher.data.AppInfo
import com.elderlylauncher.ui.ButtonPagedColumn
import com.elderlylauncher.ui.theme.LauncherColors

/**
 * Caregiver view: search every installed app and open it, or open the Play Store.
 * Only reachable from Settings, after the PIN.
 */
@Composable
fun AllAppsDialog(
    installedApps: List<AppInfo>,
    onDismiss: () -> Unit,
    onOpenApp: (String) -> Unit
) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(installedApps, query) {
        val q = query.trim()
        if (q.isEmpty()) installedApps
        else installedApps.filter {
            it.label.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DialogHeader(
                    title = stringResource(R.string.all_apps_title),
                    info = null,
                    onClose = onDismiss
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.all_apps_search)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.all_apps_clear)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() })
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { openPlayStore(context, query.trim()) },
                        modifier = Modifier.heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Green500)
                    ) {
                        Icon(Icons.Default.Shop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.all_apps_play_store),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (results.isEmpty()) {
                    Text(
                        text = stringResource(R.string.all_apps_none),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LauncherColors.Gray600,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    ButtonPagedColumn(
                        items = results,
                        pageSize = 10,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LauncherColors.Gray100)
                                .clickable { onOpenApp(app.packageName) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = LauncherColors.Gray800,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.all_apps_open),
                                style = MaterialTheme.typography.titleSmall,
                                color = LauncherColors.Green500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Open the Play Store app, on the search results when there is a query. Falls back to the website. */
private fun openPlayStore(context: Context, query: String) {
    val pm = context.packageManager
    val intent = if (query.isEmpty()) {
        pm.getLaunchIntentForPackage("com.android.vending")
            ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps"))
    } else {
        Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + Uri.encode(query) + "&c=apps"))
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val web = Uri.parse(
            if (query.isEmpty()) "https://play.google.com/store/apps"
            else "https://play.google.com/store/search?q=" + Uri.encode(query) + "&c=apps"
        )
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, web).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            // No store and no browser: nothing to open.
        }
    }
}
