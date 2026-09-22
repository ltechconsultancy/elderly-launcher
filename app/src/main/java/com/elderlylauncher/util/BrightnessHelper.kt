package com.elderlylauncher.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import kotlin.math.roundToInt

private const val TAG = "BrightnessHelper"

/**
 * System brightness control for the elderly launcher.
 *
 * Locking cannot hide the notification-shade slider, but it:
 * - turns auto-brightness off
 * - sets a stored brightness
 * - restores that value if something else changes it
 */
object BrightnessHelper {
    /** Lowest value an admin may choose. Not the brightness the screen is stuck at. */
    const val ABSOLUTE_MIN_PERCENT = 10
    /** Highest minimum, so the plus button still has room. */
    const val MAX_OF_MINIMUM = 80
    const val DEFAULT_MIN_PERCENT = 40
    const val MAX_PERCENT = 100
    const val STEP = 10
    const val DEFAULT_PERCENT = 100

    fun clampMinimum(minPercent: Int): Int {
        return minPercent.coerceIn(ABSOLUTE_MIN_PERCENT, MAX_OF_MINIMUM)
    }

    fun canWriteSettings(context: Context): Boolean = Settings.System.canWrite(context)

    fun requestWriteSettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun percentToSystem(percent: Int, minPercent: Int = DEFAULT_MIN_PERCENT): Int {
        val clamped = percent.coerceIn(clampMinimum(minPercent), MAX_PERCENT)
        return ((clamped / 100f) * 255f).roundToInt().coerceIn(1, 255)
    }

    fun readPercent(context: Context, minPercent: Int = DEFAULT_MIN_PERCENT): Int {
        return try {
            val value = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            ((value / 255f) * 100f).roundToInt().coerceIn(clampMinimum(minPercent), MAX_PERCENT)
        } catch (e: Exception) {
            Log.w(TAG, "Could not read brightness", e)
            DEFAULT_PERCENT
        }
    }

    fun apply(context: Context, percent: Int, minPercent: Int = DEFAULT_MIN_PERCENT) {
        val clamped = percent.coerceIn(clampMinimum(minPercent), MAX_PERCENT)
        applyWindow(context, clamped)
        if (!canWriteSettings(context)) return

        val resolver = context.contentResolver
        val value = percentToSystem(clamped, minPercent)
        try {
            val current = Settings.System.getInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value
            )
            val mode = Settings.System.getInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            if (mode != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                Settings.System.putInt(
                    resolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
            }
            if (current != value) {
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, value)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "WRITE_SETTINGS not granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying brightness", e)
        }
    }

    fun applyWindow(context: Context, percent: Int) {
        val activity = context.findActivity() ?: return
        val lp = activity.window.attributes
        lp.screenBrightness = percent.coerceIn(ABSOLUTE_MIN_PERCENT, MAX_PERCENT) / 100f
        activity.window.attributes = lp
    }

    /**
     * Stop overriding window brightness so the system slider works again.
     */
    fun clearWindow(context: Context) {
        val activity = context.findActivity() ?: return
        val lp = activity.window.attributes
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.window.attributes = lp
    }
}

class BrightnessLockWatcher(
    private val context: Context,
    private val isLocked: () -> Boolean,
    private val lockedPercent: () -> Int,
    private val minPercent: () -> Int = { BrightnessHelper.DEFAULT_MIN_PERCENT }
) : ContentObserver(Handler(Looper.getMainLooper())) {

    private var registered = false

    fun start() {
        if (registered) return
        val resolver = context.contentResolver
        resolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
            false,
            this
        )
        resolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
            false,
            this
        )
        registered = true
        enforce()
    }

    fun stop() {
        if (!registered) return
        context.contentResolver.unregisterContentObserver(this)
        registered = false
    }

    override fun onChange(selfChange: Boolean) {
        enforce()
    }

    private fun enforce() {
        if (isLocked()) {
            BrightnessHelper.apply(context, lockedPercent(), minPercent())
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
