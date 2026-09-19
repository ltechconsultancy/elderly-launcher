package com.elderlylauncher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

@Composable
fun SideNavButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .width(88.dp)
            .height(168.dp),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LauncherColors.Gray800,
            disabledContainerColor = LauncherColors.Gray200,
            disabledContentColor = LauncherColors.Gray500
        )
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 22.sp
        )
    }
}

/**
 * Previous / Next on the left and right, vertically centered.
 * Keeps those buttons away from the page-number bar at the bottom.
 */
@Composable
fun PagedSideNav(
    currentPage: Int,
    pageCount: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    wrap: Boolean = false,
    previousLabel: String = stringResource(R.string.nav_previous),
    nextLabel: String = stringResource(R.string.nav_next),
    content: @Composable () -> Unit
) {
    val showNav = pageCount > 1
    val canPrev = wrap || currentPage > 0
    val canNext = wrap || currentPage < pageCount - 1

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showNav) {
            SideNavButton(
                label = previousLabel,
                enabled = canPrev,
                onClick = {
                    val previous = if (currentPage > 0) currentPage - 1 else pageCount - 1
                    onPageChange(previous)
                }
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            content()
        }
        if (showNav) {
            SideNavButton(
                label = nextLabel,
                enabled = canNext,
                onClick = {
                    onPageChange(
                        if (wrap) (currentPage + 1) % pageCount
                        else (currentPage + 1).coerceAtMost(pageCount - 1)
                    )
                }
            )
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

    PagedSideNav(
        currentPage = page,
        pageCount = if (items.size > pageSize) pageCount else 1,
        onPageChange = { page = it },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (fillRemaining) Modifier else Modifier),
            verticalArrangement = verticalArrangement
        ) {
            pageItems.forEach { item ->
                itemContent(item)
            }
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

    PagedSideNav(
        currentPage = page,
        pageCount = if (items.size > pageSize) pageCount else 1,
        onPageChange = { page = it },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
    }
}
