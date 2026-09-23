package com.elderlylauncher.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutosizeTest {
    @Test
    fun tallPhoneUsesFourRowsInsideTheCap() {
        val fit = volumeStackFit(width = 360.dp, height = 640.dp)
        assertEquals(1, fit.columns)
        assertTrue(fit.item >= 96.dp && fit.item <= 148.dp)
        assertTrue(fit.gap in 10.dp..16.dp)
    }

    @Test
    fun shortLandscapePhoneUsesTwoColumns() {
        val fit = volumeStackFit(width = 720.dp, height = 240.dp)
        assertEquals(2, fit.columns)
        assertTrue(fit.item >= 88.dp)
    }

    @Test
    fun veryTallPhoneDoesNotGrowCardsWithoutLimit() {
        val fit = volumeStackFit(width = 360.dp, height = 1100.dp)
        assertEquals(1, fit.columns)
        assertEquals(148.dp, fit.item)
        assertTrue(fit.gap <= 16.dp)
    }

    @Test
    fun tabletUsesTwoCappedRows() {
        val fit = volumeStackFit(width = 800.dp, height = 1200.dp, tablet = true)
        assertEquals(2, fit.columns)
        assertEquals(220.dp, fit.item)
        assertTrue(fit.gap <= 28.dp)
    }

    @Test
    fun phoneHomeKeepsFourAppsOnOnePageWithoutStretching() {
        val fit = homeGridFit(360.dp, 520.dp, count = 4, landscape = false, tablet = false)
        assertEquals(2, fit.columns)
        assertEquals(2, fit.rowsPerPage)
        assertTrue(fit.tile <= 176.dp)
    }

    @Test
    fun shortHomePagesInsteadOfCrushingTiles() {
        val fit = homeGridFit(360.dp, 280.dp, count = 8, landscape = false, tablet = false)
        assertEquals(2, fit.columns)
        assertTrue(fit.rowsPerPage * fit.columns < 8)
        assertTrue(fit.tile >= 108.dp)
    }

    @Test
    fun listPagesWhenRowsWouldSitUnderThePager() {
        assertEquals(1, fittedPageSize(height = 200.dp, row = 88.dp, gap = 8.dp, count = 6))
        assertTrue(fittedPageSize(height = 700.dp, row = 88.dp, gap = 8.dp, count = 4) >= 4)
    }
}
