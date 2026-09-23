package com.elderlylauncher.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How a fixed set of blocks should use the space that is actually on screen.
 * Item height stays between [min] and [max]. Leftover space becomes a small gap,
 * not a stretched card.
 */
data class StackFit(
    val item: Dp,
    val gap: Dp,
    val columns: Int
)

/**
 * Phone volume: four full-width rows when the screen is tall enough.
 * A short landscape phone uses two columns so each card stays readable.
 */
fun volumeStackFit(width: Dp, height: Dp, tablet: Boolean = false): StackFit {
    if (tablet) {
        val minItem = 160.dp
        val maxItem = 220.dp
        val minGap = 16.dp
        val maxGap = 28.dp
        val item = ((height - minGap) / 2f).coerceIn(minItem, maxItem)
        val leftover = (height - item * 2 - minGap).coerceAtLeast(0.dp)
        val gap = if (item >= maxItem && leftover > 0.dp) {
            (minGap + leftover).coerceAtMost(maxGap)
        } else {
            minGap
        }
        return StackFit(item = item, gap = gap, columns = 2)
    }
    val minItem = 96.dp
    val maxItem = 148.dp
    val minGap = 10.dp
    val maxGap = 16.dp
    val stacked = width >= 280.dp && height >= minItem * 4 + minGap * 3
    if (stacked) {
        val item = ((height - minGap * 3) / 4f).coerceIn(minItem, maxItem)
        val leftover = (height - item * 4 - minGap * 3).coerceAtLeast(0.dp)
        val gap = if (item >= maxItem && leftover > 0.dp) {
            (minGap + leftover / 3f).coerceAtMost(maxGap)
        } else {
            minGap
        }
        return StackFit(item = item, gap = gap, columns = 1)
    }
    val gap = 12.dp
    val item = ((height - gap) / 2f).coerceIn(88.dp, 180.dp)
    return StackFit(item = item, gap = gap, columns = 2)
}

data class HomeGridFit(
    val columns: Int,
    val rowsPerPage: Int,
    val tile: Dp,
    val gap: Dp
)

/**
 * Home tiles keep a readable size. Extra apps go to the next page instead of
 * shrinking under the clock, the emergency button, or the page indicator.
 */
fun homeGridFit(
    width: Dp,
    height: Dp,
    count: Int,
    landscape: Boolean,
    tablet: Boolean
): HomeGridFit {
    val gap = 12.dp
    val minTile = if (tablet) 140.dp else 108.dp
    val maxTile = if (tablet) 220.dp else 176.dp
    val minTileWidth = if (tablet) 180.dp else 140.dp
    val safeCount = count.coerceAtLeast(1)
    val columns = when {
        safeCount == 1 -> 1
        landscape && !tablet -> {
            ((width + gap) / (minTileWidth + gap)).toInt().coerceIn(2, 4).coerceAtMost(safeCount)
        }
        tablet && landscape && safeCount <= 4 -> 2
        tablet && landscape -> 4.coerceAtMost(safeCount)
        tablet && safeCount <= 4 -> 2
        tablet -> 3.coerceAtMost(safeCount)
        safeCount == 2 && width >= minTileWidth * 2 -> 2
        else -> 2
    }
    val nav = 64.dp
    fun rowsIn(budget: Dp): Int {
        if (budget <= minTile) return 1
        return ((budget + gap) / (minTile + gap)).toInt().coerceAtLeast(1)
    }
    val rowsNeeded = (safeCount + columns - 1) / columns
    val paging = rowsNeeded > rowsIn(height)
    val rows = if (paging) rowsIn(height - nav) else rowsNeeded
    val budget = height - (if (paging) nav else 0.dp) - gap * (rows - 1).coerceAtLeast(0)
    val tile = (budget / rows).coerceIn(minTile, maxTile)
    return HomeGridFit(columns = columns, rowsPerPage = rows, tile = tile, gap = gap)
}

/** How many uniform rows fit above the pager buttons. */
fun fittedPageSize(height: Dp, row: Dp, gap: Dp, count: Int, nav: Dp = 64.dp): Int {
    if (count <= 0 || row <= 0.dp) return 1
    val stride = row + gap
    val fit = ((height + gap) / stride).toInt().coerceAtLeast(1)
    if (count <= fit) return fit
    val room = height - nav
    if (room <= row) return 1
    return ((room + gap) / stride).toInt().coerceAtLeast(1)
}
