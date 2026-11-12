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
import org.json.JSONObject
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
        val BRIGHTNESS_MIN_PERCENT = intPreferencesKey("brightness_min_percent")
        val BRIGHTNESS_LOCKED = booleanPreferencesKey("brightness_locked")
        val NOTIFICATION_EXTRA = stringSetPreferencesKey("notification_extra_apps")
        val PAGE_ORDER = stringPreferencesKey("page_order")
        val HOME_BUTTON_PAGE = intPreferencesKey("home_button_page")
        val WEB_TILES_SEEDED = stringSetPreferencesKey("web_tiles_seeded")
        val PUIK_HOME_ADDED = booleanPreferencesKey("puik_home_added")
        val WEB_TILES_DISABLED = stringSetPreferencesKey("web_tiles_disabled")
        val NOTIFICATION_HOME_SEEDED = booleanPreferencesKey("notification_home_seeded")
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
    val brightnessMinPercent: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map {
            BrightnessHelper.clampMinimum(it[Keys.BRIGHTNESS_MIN_PERCENT] ?: BrightnessHelper.DEFAULT_MIN_PERCENT)
        }

    suspend fun setBrightnessMinPercent(percent: Int) {
        val clamped = BrightnessHelper.clampMinimum(percent)
        context.dataStore.edit { prefs ->
            prefs[Keys.BRIGHTNESS_MIN_PERCENT] = clamped
            val current = prefs[Keys.BRIGHTNESS_PERCENT] ?: DEFAULT_BRIGHTNESS_PERCENT
            if (current < clamped) prefs[Keys.BRIGHTNESS_PERCENT] = clamped
        }
    }

    val brightnessPercent: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map {
            val min = BrightnessHelper.clampMinimum(
                it[Keys.BRIGHTNESS_MIN_PERCENT] ?: BrightnessHelper.DEFAULT_MIN_PERCENT
            )
            (it[Keys.BRIGHTNESS_PERCENT] ?: DEFAULT_BRIGHTNESS_PERCENT)
                .coerceIn(min, BrightnessHelper.MAX_PERCENT)
        }

    suspend fun setBrightnessPercent(percent: Int) {
        context.dataStore.edit { prefs ->
            val min = BrightnessHelper.clampMinimum(
                prefs[Keys.BRIGHTNESS_MIN_PERCENT] ?: BrightnessHelper.DEFAULT_MIN_PERCENT
            )
            prefs[Keys.BRIGHTNESS_PERCENT] = percent.coerceIn(min, BrightnessHelper.MAX_PERCENT)
        }
    }

    /** Web tiles that were already placed on Home once (never re-added after removal). */
    val webTilesSeeded: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.WEB_TILES_SEEDED] ?: emptySet() }

    suspend fun setWebTilesSeeded(keys: Set<String>) {
        context.dataStore.edit { it[Keys.WEB_TILES_SEEDED] = keys }
    }

    val brightnessLocked: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.BRIGHTNESS_LOCKED] ?: false }

    /** One-time migration: Puik tile placed on Home once. */
    val puikHomeAdded: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PUIK_HOME_ADDED] ?: false }

    suspend fun setPuikHomeAdded() {
        context.dataStore.edit { it[Keys.PUIK_HOME_ADDED] = true }
    }

    /** Website tiles (Puik, Puzzelwoorden) the caregiver switched off. */
    val webTilesDisabled: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.WEB_TILES_DISABLED] ?: emptySet() }

    /** Switch a website tile on or off. Off also removes it from Home and the Apps page. */
    suspend fun setWebTileEnabled(key: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val off = prefs[Keys.WEB_TILES_DISABLED] ?: emptySet()
            prefs[Keys.WEB_TILES_DISABLED] = if (enabled) off - key else off + key
            if (!enabled) {
                prefs[Keys.VISIBLE_APPS] = (prefs[Keys.VISIBLE_APPS] ?: emptySet()).filterNot { json ->
                    try {
                        JSONObject(json).getString("packageName") == key
                    } catch (e: Exception) {
                        false
                    }
                }.toSet()
                prefs[Keys.APPS_PAGE_ALLOWED] = (prefs[Keys.APPS_PAGE_ALLOWED] ?: emptySet()) - key
                prefs[Keys.NOTIFICATION_EXTRA] = (prefs[Keys.NOTIFICATION_EXTRA] ?: emptySet()) - key
            }
        }
    }

    suspend fun setBrightnessLocked(locked: Boolean) {
        context.dataStore.edit { it[Keys.BRIGHTNESS_LOCKED] = locked }
    }

    suspend fun toggleNotificationExtra(packageName: String) {
        context.dataStore.edit { prefs ->
            if (packageName in (prefs[Keys.APPS_PAGE_ALLOWED] ?: emptySet())) return@edit
            seedHomeNotifications(prefs)
            val current = prefs[Keys.NOTIFICATION_EXTRA] ?: emptySet()
            prefs[Keys.NOTIFICATION_EXTRA] = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    /** Apps on the Apps page: their notifications are always on and cannot be switched off. */
    val notificationAlwaysOnApps: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.APPS_PAGE_ALLOWED] ?: emptySet() }

    /** Extra apps whose notifications are on: chosen in Settings, or put on Home (default on). */
    val notificationExtraApps: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> extraWithHome(prefs) }

    /**
     * Apps whose notifications are shown in Klaro and not muted: the Apps page plus the
     * extra apps. Every other app is muted.
     */
    val notificationAllowedApps: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> (prefs[Keys.APPS_PAGE_ALLOWED] ?: emptySet()) + extraWithHome(prefs) }

    /** Turn notifications on for an app that is put on Home. The caregiver can switch it off. */
    suspend fun enableNotificationsForHomeApp(packageName: String) {
        if (WebTiles.isWebTile(packageName)) return
        context.dataStore.edit { prefs ->
            seedHomeNotifications(prefs)
            prefs[Keys.NOTIFICATION_EXTRA] = (prefs[Keys.NOTIFICATION_EXTRA] ?: emptySet()) + packageName
        }
    }

    /** Until the first change, apps already on Home count as on (older versions showed them). */
    private fun extraWithHome(prefs: Preferences): Set<String> {
        val extra = prefs[Keys.NOTIFICATION_EXTRA] ?: emptySet()
        if (prefs[Keys.NOTIFICATION_HOME_SEEDED] == true) return extra
        return extra + homePackages(prefs[Keys.VISIBLE_APPS] ?: emptySet())
    }

    private fun seedHomeNotifications(prefs: MutablePreferences) {
        if (prefs[Keys.NOTIFICATION_HOME_SEEDED] == true) return
        prefs[Keys.NOTIFICATION_EXTRA] = extraWithHome(prefs)
        prefs[Keys.NOTIFICATION_HOME_SEEDED] = true
    }

    private fun homePackages(jsonSet: Set<String>): Set<String> =
        jsonSet.mapNotNull { json ->
            try {
                JSONObject(json).getString("packageName")
            } catch (e: Exception) {
                null
            }
        }.filterNot { WebTiles.isWebTile(it) }.toSet()

    /** Page number (1-based, in the current page order) the Android Home button opens. */
    val homeButtonPage: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.HOME_BUTTON_PAGE] ?: 1 }

    suspend fun setHomeButtonPage(number: Int) {
        context.dataStore.edit { it[Keys.HOME_BUTTON_PAGE] = number.coerceAtLeast(1) }
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
