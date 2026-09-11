package com.elderlylauncher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elderlylauncher.R
import com.elderlylauncher.ui.theme.LauncherColors

/**
 * Large Previous / Next buttons. Elderly users tap these instead of swiping.
 */
@Composable
fun BigPrevNextButtons(
    currentPage: Int,
    pageCount: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    wrap: Boolean = false,
    showPageLabel: Boolean = true,
    previousLabel: String = stringResource(R.string.nav_previous),
    nextLabel: String = stringResource(R.string.nav_next)
) {
    if (pageCount <= 1) return

    val canPrev = wrap || currentPage > 0
    val canNext = wrap || currentPage < pageCount - 1

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showPageLabel) {
            Text(
                text = stringResource(R.string.home_page, currentPage + 1, pageCount),
                style = MaterialTheme.typography.titleMedium,
                color = LauncherColors.Gray600,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val previous = if (currentPage > 0) currentPage - 1 else pageCount - 1
                    onPageChange(previous)
                },
                enabled = canPrev,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LauncherColors.Gray800,
                    disabledContainerColor = LauncherColors.Gray200,
                    disabledContentColor = LauncherColors.Gray500
                )
            ) {
                Text(
                    text = previousLabel,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            Button(
                onClick = {
                    onPageChange(
                        if (wrap) (currentPage + 1) % pageCount
                        else (currentPage + 1).coerceAtMost(pageCount - 1)
                    )
                },
                enabled = canNext,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LauncherColors.Gray800,
                    disabledContainerColor = LauncherColors.Gray200,
                    disabledContentColor = LauncherColors.Gray500
                )
            ) {
                Text(
                    text = nextLabel,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun <T> ButtonPagedColumn(
    items: List<T>,
    pageSize: Int,
    modifier: Modifier = Modifier,
    fillRemaining: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    itemContent: @Composable (T) -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pageCount = maxOf(1, (items.size + pageSize - 1) / pageSize)
    LaunchedEffect(items.size, pageSize) {
        if (page >= pageCount) page = pageCount - 1
    }
    val pageItems = items.drop(page * pageSize).take(pageSize)

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .then(if (fillRemaining) Modifier.weight(1f, fill = true) else Modifier)
                .fillMaxWidth(),
            verticalArrangement = verticalArrangement
        ) {
            pageItems.forEach { item ->
                itemContent(item)
            }
        }
        if (items.size > pageSize) {
            Spacer(modifier = Modifier.height(8.dp))
            BigPrevNextButtons(
                currentPage = page,
                pageCount = pageCount,
                onPageChange = { page = it }
            )
        }
    }
}

@Composable
fun <T> ButtonPagedGrid(
    items: List<T>,
    columns: Int,
    rows: Int,
    modifier: Modifier = Modifier,
    fillCells: Boolean = false,
    itemContent: @Composable (T) -> Unit
) {
    val pageSize = (columns * rows).coerceAtLeast(1)
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pageCount = maxOf(1, (items.size + pageSize - 1) / pageSize)
    LaunchedEffect(items.size, pageSize) {
        if (page >= pageCount) page = pageCount - 1
    }
    val pageItems = items.drop(page * pageSize).take(pageSize)
    val rowChunks = pageItems.chunked(columns)

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rowChunks.forEach { rowItems ->
                Row(
                    modifier = if (fillCells) {
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    },
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .then(if (fillCells) Modifier.fillMaxHeight() else Modifier)
                        ) {
                            itemContent(item)
                        }
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        if (items.size > pageSize) {
            Spacer(modifier = Modifier.height(8.dp))
            BigPrevNextButtons(
                currentPage = page,
                pageCount = pageCount,
                onPageChange = { page = it }
            )
        }
    }
}
