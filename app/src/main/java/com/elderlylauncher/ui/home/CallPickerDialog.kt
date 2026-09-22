package com.elderlylauncher.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elderlylauncher.R
import com.elderlylauncher.data.QuickContact
import com.elderlylauncher.ui.ButtonPagedColumn
import com.elderlylauncher.ui.theme.LauncherColors

@Composable
fun CallPickerDialog(
    favorites: List<QuickContact>,
    contacts: List<QuickContact>,
    onDismiss: () -> Unit,
    onCall: (String) -> Unit
) {
    var number by rememberSaveable { mutableStateOf("") }
    var typing by rememberSaveable { mutableStateOf(false) }
    val favoriteIds = favorites.map { it.id }.toSet()
    val others = contacts.filter { it.id !in favoriteIds }
    val rows = buildList {
        if (favorites.isNotEmpty()) {
            add(CallRow.Header(R.string.call_favorites))
            favorites.forEach { add(CallRow.Person(it, pinned = true)) }
        }
        if (others.isNotEmpty()) {
            add(CallRow.Header(R.string.call_others))
            others.forEach { add(CallRow.Person(it, pinned = false)) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
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
                        text = stringResource(R.string.call_title),
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
                if (typing) {
                    DialPad(
                        number = number,
                        onNumberChange = { number = it },
                        onOpen = {
                            if (number.isNotBlank()) onCall(number)
                        }
                    )
                } else {
                    Button(
                        onClick = { typing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Green500)
                    ) {
                        Text(
                            text = stringResource(R.string.call_dial_self),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (rows.isEmpty()) {
                        Text(
                            text = stringResource(R.string.call_empty),
                            color = LauncherColors.Gray600,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    } else {
                        ButtonPagedColumn(
                            items = rows,
                            pageSize = 5,
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) { row ->
                            when (row) {
                                is CallRow.Header -> Text(
                                    text = stringResource(row.label),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LauncherColors.Gray600
                                )
                                is CallRow.Person -> ContactCallRow(row.contact, row.pinned) {
                                    onCall(row.contact.phoneNumber)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class CallRow {
    data class Header(val label: Int) : CallRow()
    data class Person(val contact: QuickContact, val pinned: Boolean) : CallRow()
}

@Composable
private fun ContactCallRow(contact: QuickContact, pinned: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (pinned) LauncherColors.Green50 else LauncherColors.Gray100)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (pinned) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = LauncherColors.Green700,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = LauncherColors.Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contact.phoneNumber,
                fontSize = 16.sp,
                color = LauncherColors.Gray600,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DialPad(number: String, onNumberChange: (String) -> Unit, onOpen: () -> Unit) {
    Text(
        text = number.ifBlank { " " },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = LauncherColors.Gray800,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            when (key) {
                                "" -> Unit
                                "⌫" -> if (number.isNotEmpty()) onNumberChange(number.dropLast(1))
                                else -> if (number.length < 15) onNumberChange(number + key)
                            }
                        },
                        enabled = key.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LauncherColors.Gray100,
                            contentColor = LauncherColors.Gray800
                        )
                    ) {
                        Text(text = key, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = onOpen,
        enabled = number.isNotBlank(),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LauncherColors.Green500)
    ) {
        Text(
            text = stringResource(R.string.call_open_phone),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
