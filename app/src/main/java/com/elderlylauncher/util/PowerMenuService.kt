package com.elderlylauncher.util

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

/**
 * Only used to open the system power menu (Uit / Opnieuw opstarten) from Settings.
 * A normal app may not switch the device off itself. It reads no screen content.
 */
class PowerMenuService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (instance === this) instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    companion object {
        @Volatile
        private var instance: PowerMenuService? = null

        fun isEnabled(context: Context): Boolean {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val mine = ComponentName(context, PowerMenuService::class.java)
            return flat.split(":").any {
                it.equals(mine.flattenToString(), ignoreCase = true) ||
                    it.equals(mine.flattenToShortString(), ignoreCase = true)
            }
        }

        /** Opens the system power menu. False when the service is not running. */
        fun openPowerMenu(): Boolean =
            instance?.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG) ?: false
    }
}
