package com.elderlylauncher.util

import android.content.Context
import android.provider.Settings
import android.util.Log

private const val TAG = "ScreenTimeoutHelper"

/**
 * System screen-off timeout. Same setting on phones and tablets.
 * Needs the WRITE_SETTINGS permission the brightness controls already use.
 */
object ScreenTimeoutHelper {
    const val MIN_MS = 15_000
    const val NEVER_MS = Int.MAX_VALUE
    const val MAX_HOURS = 23

    fun canWriteSettings(context: Context): Boolean = BrightnessHelper.canWriteSettings(context)

    fun requestWriteSettings(context: Context) = BrightnessHelper.requestWriteSettings(context)

    fun readMillis(context: Context): Int {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                60_000
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not read screen timeout", e)
            60_000
        }
    }

    fun isNever(millis: Int): Boolean = millis >= 12 * 60 * 60 * 1000

    fun applyMillis(context: Context, millis: Int): Boolean {
        val value = if (millis >= NEVER_MS) NEVER_MS else millis.coerceAtLeast(MIN_MS)
        if (!canWriteSettings(context)) return false
        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                value
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "WRITE_SETTINGS not granted", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting screen timeout", e)
            false
        }
    }

    fun applyParts(context: Context, hours: Int, minutes: Int, seconds: Int): Boolean {
        val total = hours.coerceIn(0, MAX_HOURS) * 3_600_000 +
            minutes.coerceIn(0, 59) * 60_000 +
            seconds.coerceIn(0, 59) * 1_000
        return applyMillis(context, total)
    }

    fun applyNever(context: Context): Boolean = applyMillis(context, NEVER_MS)
}
