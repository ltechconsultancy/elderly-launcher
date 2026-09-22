package com.elderlylauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.elderlylauncher.ui.LauncherPage
import com.elderlylauncher.util.BrightnessHelper
import java.io.IOException
import java.security.MessageDigest

// Extension property for DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_settings")

/**
 * DataStore for launcher settings
 */
class SettingsDataStore(private val context: Context) {

    // Keys
    private object Keys {
        val PASSWORD = stringPreferencesKey("settings_password")
        val LANGUAGE = stringPreferencesKey("language")
        val EMERGENCY_NUMBER = stringPreferencesKey("emergency_number")
        val PRIMARY_COLOR = stringPreferencesKey("primary_color")
        val QUICK_CONTACTS = stringSetPreferencesKey("quick_contacts")
        val VISIBLE_APPS = stringSetPreferencesKey("visible_apps")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val APPS_PAGE_ALLOWED = stringSetPreferencesKey("apps_page_allowed")
        val GAME_APPS = stringSetPreferencesKey("game_apps")
        val CAROUSEL_PHOTOS = stringSetPreferencesKey("carousel_photos")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val BRIGHTNESS_PERCENT = intPreferencesKey("brightness_percent")
        val BRIGHTNESS_LOCKED = booleanPreferencesKey("brightness_locked")
        val NOTIFICATION_BLOCKED = stringSetPreferencesKey("notification_blocked_apps")
        val NOTIFICATION_EXTRA = stringSetPreferencesKey("notification_extra_apps")
        val PAGE_ORDER = stringPreferencesKey("page_order")
    }

    // Default values
    companion object {
        const val DEFAULT_LANGUAGE = "nl"
        const val DEFAULT_EMERGENCY_NUMBER = "112"
        const val DEFAULT_PRIMARY_COLOR = "blue"
        const val DEFAULT_BRIGHTNESS_PERCENT = BrightnessHelper.DEFAULT_PERCENT

        /**
         * Hash a password using SHA-256.
         * Returns the hex-encoded hash string.
         */
        fun hashPassword(password: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }

    // Password (stored as SHA-256 hash). Empty until chosen on first unlock.
    val passwordHash: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PASSWORD] ?: "" }

    val hasPassword: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { !it[Keys.PASSWORD].isNullOrBlank() }

    suspend fun setPassword(password: String) {
        context.dataStore.edit { it[Keys.PASSWORD] = hashPassword(password) }
    }

    /**
     * Validate a plaintext password against the stored hash.
     * Uses MessageDigest.isEqual for constant-time comparison to prevent timing attacks.
     */
    suspend fun validatePassword(input: String): Boolean {
        val storedHash = context.dataStore.data.first()[Keys.PASSWORD]
        if (storedHash.isNullOrBlank()) return false
        val inputHash = hashPassword(input)
        return MessageDigest.isEqual(
            storedHash.toByteArray(Charsets.UTF_8),
            inputHash.toByteArray(Charsets.UTF_8)
        )
    }

    // Language
    val language: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.LANGUAGE] ?: DEFAULT_LANGUAGE }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    // Emergency number
    val emergencyNumber: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.EMERGENCY_NUMBER] ?: DEFAULT_EMERGENCY_NUMBER }

    suspend fun setEmergencyNumber(number: String) {
        context.dataStore.edit { it[Keys.EMERGENCY_NUMBER] = number }
    }

    // Primary color theme
    val primaryColor: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PRIMARY_COLOR] ?: DEFAULT_PRIMARY_COLOR }

    suspend fun setPrimaryColor(color: String) {
        context.dataStore.edit { it[Keys.PRIMARY_COLOR] = color }
    }

    // Quick contacts (stored as JSON strings)
    val quickContacts: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.QUICK_CONTACTS] ?: emptySet() }

    suspend fun setQuickContacts(contacts: Set<String>) {
        context.dataStore.edit { it[Keys.QUICK_CONTACTS] = contacts }
    }

    suspend fun addQuickContact(contactJson: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.QUICK_CONTACTS] ?: emptySet()
            prefs[Keys.QUICK_CONTACTS] = current + contactJson
        }
    }

    suspend fun removeQuickContact(contactJson: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.QUICK_CONTACTS] ?: emptySet()
            prefs[Keys.QUICK_CONTACTS] = current - contactJson
        }
    }

    /**
     * Atomically read and transform the quick contacts set within a single DataStore edit.
     * Prevents race conditions from separate read-then-write operations.
     */
    suspend fun editQuickContacts(transform: (Set<String>) -> Set<String>) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.QUICK_CONTACTS] ?: emptySet()
            prefs[Keys.QUICK_CONTACTS] = transform(current)
        }
    }

    // Visible apps (package names) - for home screen
    val visibleApps: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.VISIBLE_APPS] ?: emptySet() }

    suspend fun setVisibleApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.VISIBLE_APPS] = apps }
    }

    // Hidden apps (package names) - for apps page
    val hiddenApps: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.HIDDEN_APPS] ?: emptySet() }

    suspend fun setHiddenApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.HIDDEN_APPS] = apps }
    }

    val appsPageAllowed: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.APPS_PAGE_ALLOWED] ?: emptySet() }

    suspend fun setAppsPageAllowed(apps: Set<String>) {
        context.dataStore.edit { it[Keys.APPS_PAGE_ALLOWED] = apps }
    }

    suspend fun toggleAppVisibility(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.APPS_PAGE_ALLOWED] ?: emptySet()
            if (packageName in current) {
                prefs[Keys.APPS_PAGE_ALLOWED] = current - packageName
            } else {
                prefs[Keys.APPS_PAGE_ALLOWED] = current + packageName
                val extra = prefs[Keys.NOTIFICATION_EXTRA] ?: emptySet()
                prefs[Keys.NOTIFICATION_EXTRA] = extra + packageName
            }
        }
    }

    // Game apps (package names for games page)
    val gameApps: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.GAME_APPS] ?: emptySet() }

    suspend fun setGameApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.GAME_APPS] = apps }
    }

    suspend fun toggleGameApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.GAME_APPS] ?: emptySet()
            prefs[Keys.GAME_APPS] = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    // Carousel photos (URIs for photo carousel page)
    val carouselPhotos: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.CAROUSEL_PHOTOS] ?: emptySet() }

    suspend fun setCarouselPhotos(photos: Set<String>) {
        context.dataStore.edit { it[Keys.CAROUSEL_PHOTOS] = photos }
    }

    suspend fun addCarouselPhoto(photoUri: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CAROUSEL_PHOTOS] ?: emptySet()
            prefs[Keys.CAROUSEL_PHOTOS] = current + photoUri
        }
    }

    suspend fun removeCarouselPhoto(photoUri: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CAROUSEL_PHOTOS] ?: emptySet()
            prefs[Keys.CAROUSEL_PHOTOS] = current - photoUri
        }
    }

    // Brightness (percent 40-100) and lock
    val brightnessPercent: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map {
            (it[Keys.BRIGHTNESS_PERCENT] ?: DEFAULT_BRIGHTNESS_PERCENT)
                .coerceIn(BrightnessHelper.MIN_PERCENT, BrightnessHelper.MAX_PERCENT)
        }

    suspend fun setBrightnessPercent(percent: Int) {
        val clamped = percent.coerceIn(BrightnessHelper.MIN_PERCENT, BrightnessHelper.MAX_PERCENT)
        context.dataStore.edit { it[Keys.BRIGHTNESS_PERCENT] = clamped }
    }

    val brightnessLocked: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.BRIGHTNESS_LOCKED] ?: false }

    suspend fun setBrightnessLocked(locked: Boolean) {
        context.dataStore.edit { it[Keys.BRIGHTNESS_LOCKED] = locked }
    }

    /** Apps whose notifications stay out of the overview. Empty means show all. */
    val notificationBlockedApps: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.NOTIFICATION_BLOCKED] ?: emptySet() }

    suspend fun toggleNotificationBlocked(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.NOTIFICATION_BLOCKED] ?: emptySet()
            prefs[Keys.NOTIFICATION_BLOCKED] = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    /** Extra apps shown in notifications, beyond the Apps page. Empty means only the Apps page. */
    val notificationExtraApps: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.NOTIFICATION_EXTRA] ?: emptySet() }

    suspend fun toggleNotificationExtra(packageName: String) {
        context.dataStore.edit { prefs ->
            val onAppsPage = packageName in (prefs[Keys.APPS_PAGE_ALLOWED] ?: emptySet())
            if (onAppsPage) return@edit
            val current = prefs[Keys.NOTIFICATION_EXTRA] ?: emptySet()
            prefs[Keys.NOTIFICATION_EXTRA] = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    val pageOrder: Flow<List<LauncherPage>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { LauncherPage.decode(it[Keys.PAGE_ORDER]) }

    suspend fun setPageOrder(pages: List<LauncherPage>) {
        context.dataStore.edit { it[Keys.PAGE_ORDER] = LauncherPage.encode(pages) }
    }

    // First launch flag
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.FIRST_LAUNCH] ?: true }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { it[Keys.FIRST_LAUNCH] = false }
    }

    // Clear all settings
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
