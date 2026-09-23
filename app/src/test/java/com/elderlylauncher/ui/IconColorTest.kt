package com.elderlylauncher.ui

import org.junit.Assert.assertTrue
import org.junit.Test

class IconColorTest {
    @Test
    fun greenIconBecomesGreenCard() {
        val colors = iconColorsFromPixels(IntArray(16) { 0xFF25D366.toInt() })
        assertTrue(colors.accent.green > colors.accent.red)
        assertTrue(colors.accent.green > colors.accent.blue)
        assertTrue(colors.background.green > colors.background.red)
        assertTrue(colors.background.green > colors.background.blue)
    }

    @Test
    fun whiteIconDoesNotInventAColor() {
        val colors = iconColorsFromPixels(IntArray(16) { 0xFFFFFFFF.toInt() })
        val spread = maxOf(colors.accent.red, colors.accent.green, colors.accent.blue) -
            minOf(colors.accent.red, colors.accent.green, colors.accent.blue)
        assertTrue(spread < 0.08f)
    }
}
