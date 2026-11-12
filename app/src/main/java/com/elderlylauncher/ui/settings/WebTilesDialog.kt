package com.elderlylauncher.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.elderlylauncher.R
import com.elderlylauncher.ui.DialogHeader
import com.elderlylauncher.data.AppInfo
import com.elderlylauncher.ui.theme.LauncherColors

/** Switch the website tiles (Puik, Puzzelwoorden) on or off. Tap the whole row. */
@Composable
fun WebTilesDialog(
    tiles: List<AppInfo>,
    disabled: Set<String>,
    onDismiss: () -> Unit,
    onToggle: (key: String, enabled: Boolean) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DialogHeader(
                    title = stringResource(R.string.settings_web_tiles),
                    info = stringResource(R.string.settings_web_tiles_hint),
                    onClose = onDismiss
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tiles.forEach { tile ->
                        val on = tile.packageName !in disabled
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (on) LauncherColors.Green50 else LauncherColors.Gray100)
                                .clickable { onToggle(tile.packageName, !on) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = tile.icon.toBitmap(96, 96).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tile.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (on) LauncherColors.Gray800 else LauncherColors.Gray500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(if (on) R.string.web_tile_on else R.string.web_tile_off),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (on) LauncherColors.Green500 else LauncherColors.Gray500
                                )
                            }
                            Switch(
                                checked = on,
                                onCheckedChange = { onToggle(tile.packageName, it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = LauncherColors.Green500)
                            )
                        }
                    }
                }
            }
        }
    }
}
