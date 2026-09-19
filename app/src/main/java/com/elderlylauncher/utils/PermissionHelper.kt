package com.elderlylauncher.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

/**
 * Helper class for runtime permissions
 */
object PermissionHelper {

    // Permission groups for the launcher
    val PHONE_PERMISSIONS = arrayOf(
        Manifest.permission.CALL_PHONE
    )

    val CONTACTS_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS
    )

    val CAMERA_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )

    val MEDIA_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    /**
     * Check if a permission is granted
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if all permissions in an array are granted
     */
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }

    /**
     * Check if phone call permission is granted
     */
    fun canMakePhoneCalls(context: Context): Boolean {
        return hasPermissions(context, PHONE_PERMISSIONS)
    }

    /**
     * Check if contacts permission is granted
     */
    fun canReadContacts(context: Context): Boolean {
        return hasPermissions(context, CONTACTS_PERMISSIONS)
    }

    /**
     * Check if camera permission is granted
     */
    fun canUseCamera(context: Context): Boolean {
        return hasPermissions(context, CAMERA_PERMISSIONS)
    }

    /**
     * Check if media/photos permission is granted
     */
    fun canReadMedia(context: Context): Boolean {
        return hasPermissions(context, MEDIA_PERMISSIONS)
    }

    /**
     * Wi‑Fi tablets (POCO Pad and similar) have no cellular radio.
     */
    fun hasTelephony(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    /**
     * True only when a SIM is ready for calls/SMS.
     * Wi-Fi tablets, missing SIMs, PIN/PUK locks, and unknown errors stay false
     * so calling UI is hidden unless we know the radio can be used.
     */
    fun hasActiveSim(context: Context): Boolean {
        if (!hasTelephony(context)) return false
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return false
        return try {
            telephony.simState == TelephonyManager.SIM_STATE_READY
        } catch (_: SecurityException) {
            false
        }
    }

    /**
     * Get all required permissions that are not yet granted.
     * Skip CALL_PHONE and READ_CONTACTS when the device cannot place cellular calls.
     */
    fun getMissingPermissions(context: Context): List<String> {
        val phone = if (hasActiveSim(context)) PHONE_PERMISSIONS + CONTACTS_PERMISSIONS else emptyArray()
        val allPermissions = phone + CAMERA_PERMISSIONS + MEDIA_PERMISSIONS
        return allPermissions.filter { !hasPermission(context, it) }
    }
}
