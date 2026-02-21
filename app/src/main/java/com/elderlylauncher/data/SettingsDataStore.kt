package com.elderlylauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

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
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    // Default values
    companion object {
        const val DEFAULT_PASSWORD = "1234"
        const val DEFAULT_LANGUAGE = "nl"
        const val DEFAULT_EMERGENCY_NUMBER = "112"
        const val DEFAULT_PRIMARY_COLOR = "blue"
    }

    // Password
    val password: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PASSWORD] ?: DEFAULT_PASSWORD }

    suspend fun setPassword(password: String) {
        context.dataStore.edit { it[Keys.PASSWORD] = password }
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

    // Visible apps (package names)
    val visibleApps: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.VISIBLE_APPS] ?: emptySet() }

    suspend fun setVisibleApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.VISIBLE_APPS] = apps }
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
