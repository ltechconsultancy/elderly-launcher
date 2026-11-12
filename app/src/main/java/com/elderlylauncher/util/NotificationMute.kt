package com.elderlylauncher.util

import android.app.Notification
import android.content.Context
import android.os.Build
import android.provider.Telephony
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager

/**
 * Decides which notifications Klaro removes because their app is not approved.
 * Approved = on the Apps page, on Home, or an extra app chosen in Settings.
 * Calls, alarms, emergency alerts and the system are never removed.
 */
object NotificationMute {
    private const val OWN_PACKAGE = "com.elderlylauncher"

    private val SYSTEM_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.android.phone",
        "com.android.server.telecom",
        "com.android.settings",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.deskclock",
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.android.emergency",
        "com.google.android.apps.safetyhub"
    )

    private val KEEP_CATEGORIES: Set<String> = buildSet {
        add(Notification.CATEGORY_CALL)
        add(Notification.CATEGORY_ALARM)
        add(Notification.CATEGORY_SYSTEM)
        add(Notification.CATEGORY_TRANSPORT)
        add(Notification.CATEGORY_NAVIGATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(Notification.CATEGORY_STOPWATCH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Notification.CATEGORY_MISSED_CALL)
    }

    /** Default phone and SMS apps: calls and text messages must always reach the user. */
    fun protectedPackages(context: Context): Set<String> {
        val result = mutableSetOf<String>()
        try {
            context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage?.let { result += it }
        } catch (e: Exception) {
            // Ignore: fall back to the fixed list.
        }
        try {
            Telephony.Sms.getDefaultSmsPackage(context)?.let { result += it }
        } catch (e: Exception) {
            // Ignore: fall back to the fixed list.
        }
        return result
    }

    fun shouldMute(
        sbn: StatusBarNotification,
        allowed: Set<String>,
        exempt: Set<String>
    ): Boolean {
        val notice = sbn.notification ?: return false
        return shouldMute(
            packageName = sbn.packageName,
            category = notice.category,
            clearable = sbn.isClearable,
            ongoing = sbn.isOngoing ||
                notice.flags and Notification.FLAG_FOREGROUND_SERVICE != 0,
            allowed = allowed,
            exempt = exempt
        )
    }

    fun shouldMute(
        packageName: String,
        category: String?,
        clearable: Boolean,
        ongoing: Boolean,
        allowed: Set<String>,
        exempt: Set<String>
    ): Boolean {
        if (!clearable || ongoing) return false
        if (packageName == OWN_PACKAGE) return false
        if (packageName in allowed || packageName in exempt) return false
        if (packageName in SYSTEM_PACKAGES) return false
        if (packageName.contains("cellbroadcast", ignoreCase = true)) return false
        if (category != null && category in KEEP_CATEGORIES) return false
        return true
    }
}
