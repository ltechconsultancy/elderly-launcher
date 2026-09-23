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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elderlylauncher.R
import com.elderlylauncher.ui.theme.LauncherColors

@Composable
fun NavIconButton(
    previous: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    contentDescription: String? = null
) {
    val label = contentDescription ?: stringResource(
        if (previous) R.string.nav_previous else R.string.nav_next
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LauncherColors.Gray800,
            disabledContainerColor = LauncherColors.Gray200,
            disabledContentColor = LauncherColors.Gray400
        )
    ) {
        Icon(
            imageVector = if (previous) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
            contentDescription = label,
            modifier = Modifier.size(size * 0.62f)
        )
    }
}

/**
 * Content uses the full width. Previous/Next are icon buttons under the content,
 * never large text buttons on the sides.
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

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
        ) {
            content()
        }
        if (showNav) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIconButton(
                    previous = true,
                    enabled = canPrev,
                    contentDescription = previousLabel,
                    onClick = {
                        val previous = if (currentPage > 0) currentPage - 1 else pageCount - 1
                        onPageChange(previous)
                    }
                )
                Spacer(modifier = Modifier.size(24.dp))
                NavIconButton(
                    previous = false,
                    enabled = canNext,
                    contentDescription = nextLabel,
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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = verticalArrangement
        ) {
            pageItems.forEach { item ->
                itemContent(item)
            }
            if (fillRemaining) {
                Spacer(modifier = Modifier.weight(1f))
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
            if (fillCells) {
                repeat((rows - rowChunks.size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
