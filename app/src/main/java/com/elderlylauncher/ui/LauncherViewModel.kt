package com.elderlylauncher.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elderlylauncher.data.*
import com.elderlylauncher.util.BrightnessHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

private const val TAG = "LauncherViewModel"

/**
 * ViewModel for the launcher
 */
class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val contactsRepository = ContactsRepository(application)
    private val settingsDataStore = SettingsDataStore(application)

    // Coroutine exception handler to prevent crashes
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine exception", throwable)
    }

    companion object {
        const val MAX_HOME_APPS = 12
        const val DEFAULT_HOME_SLOTS = 4
    }

    // Loading states
    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _isLoadingContacts = MutableStateFlow(false)
    val isLoadingContacts: StateFlow<Boolean> = _isLoadingContacts.asStateFlow()

    // Apps
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    // Contacts
    private val _contacts = MutableStateFlow<List<QuickContact>>(emptyList())
    val contacts: StateFlow<List<QuickContact>> = _contacts.asStateFlow()

    // Quick contacts (derived from DataStore flow via stateIn)
    val quickContacts: StateFlow<List<QuickContact>> = settingsDataStore.quickContacts
        .map { jsonSet ->
            jsonSet.mapNotNull { json -> parseQuickContact(json) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings (stored password hash)
    val passwordHash: StateFlow<String> = settingsDataStore.passwordHash
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.hashPassword(SettingsDataStore.DEFAULT_PASSWORD))

    val emergencyNumber: StateFlow<String> = settingsDataStore.emergencyNumber
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_EMERGENCY_NUMBER)

    val primaryColor: StateFlow<String> = settingsDataStore.primaryColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_PRIMARY_COLOR)

    val language: StateFlow<String> = settingsDataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_LANGUAGE)

    // Home apps (stored as JSON with position -> packageName)
    val homeApps: StateFlow<Map<Int, String>> = settingsDataStore.visibleApps
        .map { jsonSet ->
            jsonSet.mapNotNull { json -> parseHomeApp(json) }.toMap()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Hidden apps (package names to hide from Apps page)
    val hiddenApps: StateFlow<Set<String>> = settingsDataStore.hiddenApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Game apps (package names for games page)
    val gameApps: StateFlow<Set<String>> = settingsDataStore.gameApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Carousel photos (URIs for photo carousel)
    val carouselPhotos: StateFlow<Set<String>> = settingsDataStore.carouselPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val brightnessPercent: StateFlow<Int> = settingsDataStore.brightnessPercent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_BRIGHTNESS_PERCENT)

    val brightnessLocked: StateFlow<Boolean> = settingsDataStore.brightnessLocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadApps()
        loadContacts()
    }

    fun loadApps() {
        viewModelScope.launch(exceptionHandler) {
            _isLoadingApps.value = true
            try {
                _installedApps.value = appRepository.getInstalledApps()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading apps", e)
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    fun loadContacts() {
        viewModelScope.launch(exceptionHandler) {
            _isLoadingContacts.value = true
            try {
                _contacts.value = contactsRepository.getContacts()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading contacts", e)
            } finally {
                _isLoadingContacts.value = false
            }
        }
    }

    /**
     * Check if we have contacts permission
     */
    fun hasContactsPermission(): Boolean = contactsRepository.hasContactsPermission()

    /**
     * Check if we have call permission
     */
    fun hasCallPermission(): Boolean = contactsRepository.hasCallPermission()

    fun launchApp(packageName: String): Boolean {
        return appRepository.launchApp(packageName)
    }

    fun callContact(contact: QuickContact): Boolean {
        return contactsRepository.callContact(contact.phoneNumber)
    }

    fun dialNumber(number: String): Boolean {
        return contactsRepository.dialNumber(number)
    }

    fun callEmergency(): Boolean {
        return contactsRepository.dialNumber(emergencyNumber.value)
    }

    // Settings updates
    fun updatePassword(newPassword: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                settingsDataStore.setPassword(newPassword)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating password", e)
            }
        }
    }

    /**
     * Change password - alias for updatePassword for clarity in Settings
     */
    fun changePassword(newPassword: String) {
        updatePassword(newPassword)
    }

    /**
     * Set a home app at a specific position (0 until MAX_HOME_APPS).
     */
    fun setHomeApp(position: Int, packageName: String) {
        if (position !in 0 until MAX_HOME_APPS) return
        viewModelScope.launch(exceptionHandler) {
            try {
                val currentApps = homeApps.value.toMutableMap()
                currentApps[position] = packageName
                persistHomeApps(currentApps)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting home app", e)
            }
        }
    }

    /**
     * Add an extra app on the home screen after the default 4 tiles.
     */
    fun addHomeApp(packageName: String) {
        val next = (DEFAULT_HOME_SLOTS until MAX_HOME_APPS)
            .firstOrNull { it !in homeApps.value } ?: return
        setHomeApp(next, packageName)
    }

    /**
     * Clear a home app at a specific position
     */
    fun clearHomeApp(position: Int) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val currentApps = homeApps.value.toMutableMap()
                currentApps.remove(position)
                persistHomeApps(currentApps)
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing home app", e)
            }
        }
    }

    private suspend fun persistHomeApps(apps: Map<Int, String>) {
        val jsonSet = apps.map { (pos, pkg) ->
            JSONObject().apply {
                put("position", pos)
                put("packageName", pkg)
            }.toString()
        }.toSet()
        settingsDataStore.setVisibleApps(jsonSet)
    }

    private fun parseHomeApp(json: String): Pair<Int, String>? {
        return try {
            val obj = JSONObject(json)
            val position = obj.getInt("position")
            val packageName = obj.getString("packageName")
            position to packageName
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing home app: $json", e)
            null
        }
    }

    fun updateEmergencyNumber(number: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                settingsDataStore.setEmergencyNumber(number)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating emergency number", e)
            }
        }
    }

    fun updatePrimaryColor(color: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                settingsDataStore.setPrimaryColor(color)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating primary color", e)
            }
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                settingsDataStore.setLanguage(languageCode)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating language", e)
            }
        }
    }

    fun toggleAppVisibility(packageName: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                settingsDataStore.toggleAppVisibility(packageName)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling app visibility", e)
            }
        }
    }

    fun toggleGameApp(packageName: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                settingsDataStore.toggleGameApp(packageName)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling game app", e)
            }
        }
    }

    fun addCarouselPhoto(photoUri: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                settingsDataStore.addCarouselPhoto(photoUri)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding carousel photo", e)
            }
        }
    }

    fun setBrightnessPercent(percent: Int) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val clamped = percent.coerceIn(BrightnessHelper.MIN_PERCENT, BrightnessHelper.MAX_PERCENT)
                settingsDataStore.setBrightnessPercent(clamped)
                BrightnessHelper.apply(getApplication(), clamped)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting brightness", e)
            }
        }
    }

    fun setBrightnessLocked(locked: Boolean) {
        viewModelScope.launch(exceptionHandler) {
            try {
                settingsDataStore.setBrightnessLocked(locked)
                if (locked) {
                    BrightnessHelper.apply(getApplication(), brightnessPercent.value)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting brightness lock", e)
            }
        }
    }

    fun removeCarouselPhoto(photoUri: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                settingsDataStore.removeCarouselPhoto(photoUri)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing carousel photo", e)
            }
        }
    }

    fun addQuickContact(contact: QuickContact) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val json = serializeQuickContact(contact)
                settingsDataStore.addQuickContact(json)
                // State will update via quickContacts stateIn flow
            } catch (e: Exception) {
                Log.e(TAG, "Error adding quick contact", e)
            }
        }
    }

    fun removeQuickContact(contact: QuickContact) {
        viewModelScope.launch(exceptionHandler) {
            try {
                // Atomic read-and-remove within a single DataStore edit to prevent race conditions
                settingsDataStore.editQuickContacts { currentSet ->
                    currentSet.filterNot { json ->
                        val parsed = parseQuickContact(json)
                        parsed?.id == contact.id
                    }.toSet()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing quick contact", e)
            }
        }
    }

    /**
     * Validate password input against stored password hash.
     * Delegates to SettingsDataStore which uses SHA-256 hashing
     * and MessageDigest.isEqual for constant-time comparison to prevent timing attacks.
     */
    suspend fun validatePassword(input: String): Boolean {
        return settingsDataStore.validatePassword(input)
    }

    // JSON serialization for QuickContact (safer than pipe-delimited)
    private fun serializeQuickContact(contact: QuickContact): String {
        return JSONObject().apply {
            put("id", contact.id)
            put("name", contact.name)
            put("phoneNumber", contact.phoneNumber)
            put("photoUri", contact.photoUri ?: "")
            put("relation", contact.relation)
        }.toString()
    }

    private fun parseQuickContact(json: String): QuickContact? {
        return try {
            // Try JSON format first
            if (json.startsWith("{")) {
                val obj = JSONObject(json)
                QuickContact(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    phoneNumber = obj.getString("phoneNumber"),
                    photoUri = obj.optString("photoUri").ifBlank { null },
                    relation = obj.optString("relation").ifBlank { "" }
                )
            } else {
                // Legacy pipe-delimited format for backwards compatibility
                val parts = json.split("|")
                if (parts.size >= 3) {
                    QuickContact(
                        id = parts[0],
                        name = parts[1],
                        phoneNumber = parts[2],
                        photoUri = null,
                        relation = parts.getOrNull(3) ?: ""
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing quick contact: $json", e)
            null
        }
    }
}
