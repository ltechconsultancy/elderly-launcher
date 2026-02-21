package com.elderlylauncher.data

import android.graphics.drawable.Drawable

/**
 * Represents an installed app
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isSystemApp: Boolean = false
)

/**
 * Represents a quick-dial contact
 */
data class QuickContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val relation: String = "" // e.g., "Zoon", "Dochter"
)

/**
 * App categories for the launcher
 */
enum class AppCategory {
    PHONE,
    MESSAGES,
    CAMERA,
    PHOTOS,
    BROWSER,
    SETTINGS,
    OTHER
}
