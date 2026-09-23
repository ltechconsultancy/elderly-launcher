package com.elderlylauncher.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.max
import kotlin.math.min

data class IconColors(
    val background: Color,
    val border: Color,
    val accent: Color
)

private val fallbackColors = IconColors(
    background = Color(0xFFF3F4F6),
    border = Color(0xFFD1D5DB),
    accent = Color(0xFF374151)
)

/**
 * Card colors taken from the icon itself. A green WhatsApp icon becomes a green card.
 * Near-white and near-black pixels are ignored so a logo on a white plate still wins.
 */
fun iconColors(drawable: Drawable): IconColors {
    val bitmap = try {
        drawable.toBitmap(32, 32)
    } catch (_: Exception) {
        return fallbackColors
    }
    return iconColors(bitmap)
}

fun iconColors(bitmap: Bitmap): IconColors {
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    return iconColorsFromPixels(pixels)
}

internal fun iconColorsFromPixels(pixels: IntArray): IconColors {
    data class Sample(val r: Int, val g: Int, val b: Int, val saturation: Float)

    val samples = ArrayList<Sample>(pixels.size)
    for (pixel in pixels) {
        if (android.graphics.Color.alpha(pixel) < 80) continue
        val r = android.graphics.Color.red(pixel)
        val g = android.graphics.Color.green(pixel)
        val b = android.graphics.Color.blue(pixel)
        val hi = max(r, max(g, b))
        val lo = min(r, min(g, b))
        if (hi < 28) continue
        if (hi > 242 && lo > 220) continue
        val saturation = if (hi == 0) 0f else (hi - lo) / hi.toFloat()
        samples += Sample(r, g, b, saturation)
    }
    val colored = samples.filter { it.saturation >= 0.18f }.ifEmpty { samples }
    if (colored.isEmpty()) return fallbackColors
    val r = colored.sumOf { it.r } / colored.size
    val g = colored.sumOf { it.g } / colored.size
    val b = colored.sumOf { it.b } / colored.size
    return IconColors(
        background = mixWhite(r, g, b, 0.20f),
        border = mixWhite(r, g, b, 0.48f),
        accent = darken(r, g, b)
    )
}

private fun mixWhite(r: Int, g: Int, b: Int, amount: Float): Color {
    val keep = 1f - amount
    return Color(
        red = (255f * keep + r * amount) / 255f,
        green = (255f * keep + g * amount) / 255f,
        blue = (255f * keep + b * amount) / 255f
    )
}

private fun darken(r: Int, g: Int, b: Int): Color {
    val hi = max(r, max(g, b)).coerceAtLeast(1)
    val scale = 130f / hi
    return Color(
        red = (r * scale).coerceIn(0f, 255f) / 255f,
        green = (g * scale).coerceIn(0f, 255f) / 255f,
        blue = (b * scale).coerceIn(0f, 255f) / 255f
    )
}
