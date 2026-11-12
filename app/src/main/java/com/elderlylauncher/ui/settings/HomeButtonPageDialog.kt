package com.elderlylauncher.ui.settings

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elderlylauncher.R
import com.elderlylauncher.ui.ButtonPagedColumn
import com.elderlylauncher.ui.DialogHeader
import com.elderlylauncher.ui.LauncherPage
import com.elderlylauncher.ui.theme.LauncherColors

/** Choose the page the Android Home button opens. Numbers follow the current page order. */
@Composable
fun HomeButtonPageDialog(
    pages: List<LauncherPage>,
    selectedNumber: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
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
                DialogHeader(
                    title = stringResource(R.string.settings_home_button),
                    info = stringResource(R.string.settings_home_button_hint),
                    onClose = onDismiss
                )
                Spacer(modifier = Modifier.height(8.dp))
                ButtonPagedColumn(
                    items = pages.indices.toList(),
                    pageSize = 7,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) { index ->
                    val number = index + 1
                    val selected = number == selectedNumber
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .background(
                                if (selected) LauncherColors.Green50 else LauncherColors.Gray100,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                2.dp,
                                if (selected) LauncherColors.Green500 else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelect(number) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = number.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .background(LauncherColors.Gray800, CircleShape)
                                .padding(top = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = pageLabel(pages[index]),
                            style = MaterialTheme.typography.titleMedium,
                            color = LauncherColors.Gray800,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = LauncherColors.Green500,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
