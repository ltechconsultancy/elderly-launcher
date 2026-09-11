package com.elderlylauncher.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
     * Get all required permissions that are not yet granted.
     * Skip CALL_PHONE when the device cannot place cellular calls.
     */
    fun getMissingPermissions(context: Context): List<String> {
        val phone = if (hasTelephony(context)) PHONE_PERMISSIONS else emptyArray()
        val allPermissions = phone + CONTACTS_PERMISSIONS +
                CAMERA_PERMISSIONS + MEDIA_PERMISSIONS
        return allPermissions.filter { !hasPermission(context, it) }
    }
}
