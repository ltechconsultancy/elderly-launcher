package com.elderlylauncher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elderlylauncher.R
import com.elderlylauncher.data.QuickContact
import com.elderlylauncher.ui.PagedSideNav
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
                    if (favorites.isEmpty() && others.isEmpty()) {
                        Text(
                            text = stringResource(R.string.call_empty),
                            color = LauncherColors.Gray600,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 8.dp)
                        )
                    } else {
                        CallContactPages(
                            favorites = favorites,
                            others = others,
                            onCall = onCall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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
                }
            }
        }
    }
}

@Composable
private fun ContactCallRow(
    contact: QuickContact,
    pinned: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
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

private sealed interface CallLine {
    data class Label(val textRes: Int) : CallLine
    data class Person(val contact: QuickContact, val pinned: Boolean) : CallLine
}

@Composable
private fun CallContactPages(
    favorites: List<QuickContact>,
    others: List<QuickContact>,
    onCall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val pages = callPages(favorites, others, maxHeight)
        var page by rememberSaveable { mutableIntStateOf(0) }
        val safePage = page.coerceIn(0, pages.lastIndex.coerceAtLeast(0))
        PagedSideNav(
            currentPage = safePage,
            pageCount = pages.size,
            onPageChange = { page = it },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pages[safePage].forEach { line ->
                    when (line) {
                        is CallLine.Label -> Text(
                            text = stringResource(line.textRes),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LauncherColors.Gray600
                        )
                        is CallLine.Person -> Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            ContactCallRow(
                                contact = line.contact,
                                pinned = line.pinned,
                                modifier = Modifier.fillMaxSize(),
                                onClick = { onCall(line.contact.phoneNumber) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun callPages(
    favorites: List<QuickContact>,
    others: List<QuickContact>,
    maxHeight: Dp
): List<List<CallLine>> {
    val minRow = 80.dp
    val gap = 8.dp
    val label = 28.dp
    val nav = 68.dp
    val people = buildList {
        favorites.forEach { add(it to true) }
        others.forEach { add(it to false) }
    }
    if (people.isEmpty()) return listOf(emptyList())
    val height = if (maxHeight.value.isFinite() && maxHeight > 0.dp) maxHeight else 480.dp
    val pages = mutableListOf<List<CallLine>>()
    var index = 0
    while (index < people.size) {
        val rest = people.size - index
        val withoutNav = packCallCount(people, index, height, minRow, gap, label)
        val count = if (pages.isNotEmpty() || withoutNav < rest) {
            packCallCount(people, index, height - nav, minRow, gap, label).coerceAtLeast(1)
        } else {
            withoutNav.coerceAtLeast(1)
        }
        pages.add(callLines(people.subList(index, (index + count).coerceAtMost(people.size))))
        index += count
    }
    return pages
}

private fun packCallCount(
    people: List<Pair<QuickContact, Boolean>>,
    start: Int,
    budget: Dp,
    minRow: Dp,
    gap: Dp,
    label: Dp
): Int {
    if (budget <= minRow) return 1
    var used = 0.dp
    var count = 0
    var sawFavorite = false
    var sawOther = false
    while (start + count < people.size && count < 8) {
        val pinned = people[start + count].second
        var add = minRow
        if (used > 0.dp) add += gap
        val needsLabel = (pinned && !sawFavorite) || (!pinned && !sawOther)
        if (needsLabel) {
            if (used > 0.dp) add += gap
            add += label
        }
        if (count > 0 && used + add > budget) break
        used += add
        if (pinned) sawFavorite = true else sawOther = true
        count++
        if (used > budget) break
    }
    return count
}

private fun callLines(people: List<Pair<QuickContact, Boolean>>): List<CallLine> {
    val lines = mutableListOf<CallLine>()
    var sawFavorite = false
    var sawOther = false
    people.forEach { (contact, pinned) ->
        if (pinned && !sawFavorite) {
            lines.add(CallLine.Label(R.string.call_favorites))
            sawFavorite = true
        }
        if (!pinned && !sawOther) {
            lines.add(CallLine.Label(R.string.call_others))
            sawOther = true
        }
        lines.add(CallLine.Person(contact, pinned))
    }
    return lines
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
