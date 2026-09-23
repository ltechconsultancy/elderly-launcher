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
