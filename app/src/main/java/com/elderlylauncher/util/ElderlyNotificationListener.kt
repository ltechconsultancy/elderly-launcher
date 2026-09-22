package com.elderlylauncher.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class ElderlyNotificationListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationInbox.attach(this)
        NotificationInbox.replace(activeNotifications?.toList().orEmpty())
    }

    override fun onListenerDisconnected() {
        NotificationInbox.detach(this)
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationInbox.upsert(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationInbox.remove(sbn.key)
    }

    companion object {
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
