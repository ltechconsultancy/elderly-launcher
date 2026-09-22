package com.elderlylauncher.util

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InboxNotification(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long
)

/**
 * Active notifications published by [ElderlyNotificationListener].
 * The UI filters this list with the caregiver blocklist.
 */
object NotificationInbox {
    private const val TAG = "NotificationInbox"

    private val _items = MutableStateFlow<List<InboxNotification>>(emptyList())
    val items: StateFlow<List<InboxNotification>> = _items.asStateFlow()

    @Volatile
    var service: NotificationListenerService? = null
        private set

    fun attach(listener: NotificationListenerService) {
        service = listener
    }

    fun detach(listener: NotificationListenerService) {
        if (service === listener) service = null
    }

    fun replace(active: List<StatusBarNotification>) {
        _items.value = active.mapNotNull { it.toInbox() }
            .sortedByDescending { it.postedAt }
    }

    fun upsert(sbn: StatusBarNotification) {
        val item = sbn.toInbox() ?: run {
            remove(sbn.key)
            return
        }
        _items.value = (_items.value.filter { it.key != item.key } + item)
            .sortedByDescending { it.postedAt }
    }

    fun remove(key: String) {
        _items.value = _items.value.filter { it.key != key }
    }

    fun dismiss(key: String) {
        try {
            service?.cancelNotification(key)
        } catch (e: Exception) {
            Log.e(TAG, "Could not dismiss notification", e)
        }
        remove(key)
    }

    fun dismissAll(keys: List<String>) {
        val listener = service
        keys.forEach { key ->
            try {
                listener?.cancelNotification(key)
            } catch (e: Exception) {
                Log.e(TAG, "Could not dismiss notification", e)
            }
        }
        val dropping = keys.toSet()
        _items.value = _items.value.filter { it.key !in dropping }
    }
}

private fun StatusBarNotification.toInbox(): InboxNotification? {
    if (packageName == "com.elderlylauncher") return null
    val notice = notification ?: return null
    if (notice.flags and Notification.FLAG_GROUP_SUMMARY != 0) return null
    val extras = notice.extras
    val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
    val text = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        ?: extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        ?: ""
    if (title.isBlank() && text.isBlank()) return null
    return InboxNotification(
        key = key,
        packageName = packageName,
        title = title.ifBlank { text },
        text = if (title.isBlank()) "" else text,
        postedAt = postTime
    )
}
