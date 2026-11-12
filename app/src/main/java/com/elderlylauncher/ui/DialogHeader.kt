package com.elderlylauncher.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elderlylauncher.R
import com.elderlylauncher.ui.theme.LauncherColors

/**
 * Title row of a selection dialog: title, an (i) button for the explanation, and Close.
 * The explanation is never a block of text above the list: the list needs the room.
 */
@Composable
fun DialogHeader(
    title: String,
    info: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showInfo by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = LauncherColors.Gray800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (info != null) {
            IconButton(onClick = { showInfo = true }, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.dialog_info),
                    tint = LauncherColors.Blue500,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.cancel),
                tint = LauncherColors.Gray600
            )
        }
    }
    if (showInfo && info != null) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(title) },
            text = { Text(info, fontSize = 18.sp) },
            confirmButton = {
                Button(
                    onClick = { showInfo = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.dialog_info_ok)) }
            }
        )
    }
}
