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
    val OPTIONS_MS = listOf(
        30_000,
        60_000,
        120_000,
        300_000,
        600_000,
        1_800_000
    )

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

    fun apply(context: Context, millis: Int): Boolean {
        if (millis !in OPTIONS_MS) return false
        if (!canWriteSettings(context)) return false
        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                millis
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "WRITE_SETTINGS not granted", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting screen timeout", e)
            false
        }
    }
}
