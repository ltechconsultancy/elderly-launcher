package com.elderlylauncher.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.elderlylauncher.data.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Receives every notification on the device. Notifications of approved apps go to
 * [NotificationInbox]; the rest are removed at once, so they do not stay in the
 * status bar and their sound or vibration stops.
 */
class ElderlyNotificationListener : NotificationListenerService() {
    private var scope: CoroutineScope? = null

    /** Null until the settings have loaded: nothing is removed before that. */
    @Volatile
    private var allowed: Set<String>? = null

    @Volatile
    private var protectedApps: Set<String> = emptySet()

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationInbox.attach(this)
        protectedApps = NotificationMute.protectedPackages(this)
        NotificationInbox.replace(activeNotifications?.toList().orEmpty())

        scope?.cancel()
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope = newScope
        newScope.launch {
            SettingsDataStore(applicationContext).notificationAllowedApps.collect { apps ->
                allowed = apps
                muteActive()
            }
        }
    }

    override fun onListenerDisconnected() {
        scope?.cancel()
        scope = null
        allowed = null
        NotificationInbox.detach(this)
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        scope?.cancel()
        scope = null
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (muteIfUnapproved(sbn)) return
        NotificationInbox.upsert(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationInbox.remove(sbn.key)
    }

    private fun muteActive() {
        val active = try {
            activeNotifications?.toList().orEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Could not read active notifications", e)
            return
        }
        protectedApps = NotificationMute.protectedPackages(this)
        active.forEach { muteIfUnapproved(it) }
    }

    /** Removes the notification when its app is not approved. Returns true if removed. */
    private fun muteIfUnapproved(sbn: StatusBarNotification): Boolean {
        val apps = allowed ?: return false
        if (!NotificationMute.shouldMute(sbn, apps, protectedApps)) return false
        return try {
            cancelNotification(sbn.key)
            NotificationInbox.remove(sbn.key)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Could not mute notification", e)
            false
        }
    }

    companion object {
        private const val TAG = "ElderlyNotifications"

        fun isEnabled(context: Context): Boolean {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val mine = ComponentName(context, ElderlyNotificationListener::class.java)
            return flat.split(":").any { entry ->
                entry.equals(mine.flattenToString(), ignoreCase = true) ||
                    entry.contains(context.packageName)
            }
        }

        fun requestRebind(context: Context) {
            NotificationListenerService.requestRebind(
                ComponentName(context, ElderlyNotificationListener::class.java)
            )
        }
    }
}
