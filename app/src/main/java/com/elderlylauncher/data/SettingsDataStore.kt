package com.elderlylauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        val GAME_APPS = stringSetPreferencesKey("game_apps")
        val CAROUSEL_PHOTOS = stringSetPreferencesKey("carousel_photos")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val BRIGHTNESS_PERCENT = intPreferencesKey("brightness_percent")
        val BRIGHTNESS_LOCKED = booleanPreferencesKey("brightness_locked")
    }

    // Default values
    companion object {
        const val DEFAULT_PASSWORD = "1234"
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

    // Password (stored as SHA-256 hash)
    val passwordHash: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PASSWORD] ?: hashPassword(DEFAULT_PASSWORD) }

    suspend fun setPassword(password: String) {
        context.dataStore.edit { it[Keys.PASSWORD] = hashPassword(password) }
    }

    /**
     * Validate a plaintext password against the stored hash.
     * Uses MessageDigest.isEqual for constant-time comparison to prevent timing attacks.
     */
    suspend fun validatePassword(input: String): Boolean {
        val storedHash = passwordHash.first()
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

    suspend fun toggleAppVisibility(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_APPS] ?: emptySet()
            prefs[Keys.HIDDEN_APPS] = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
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
