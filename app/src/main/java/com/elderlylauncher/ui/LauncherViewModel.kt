package com.elderlylauncher.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elderlylauncher.data.*
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

    // Quick contacts
    private val _quickContacts = MutableStateFlow<List<QuickContact>>(emptyList())
    val quickContacts: StateFlow<List<QuickContact>> = _quickContacts.asStateFlow()

    // Settings
    val password: StateFlow<String> = settingsDataStore.password
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_PASSWORD)

    val emergencyNumber: StateFlow<String> = settingsDataStore.emergencyNumber
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_EMERGENCY_NUMBER)

    val primaryColor: StateFlow<String> = settingsDataStore.primaryColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_PRIMARY_COLOR)

    init {
        loadApps()
        loadContacts()
        loadQuickContacts()
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

    private fun loadQuickContacts() {
        viewModelScope.launch(exceptionHandler) {
            settingsDataStore.quickContacts.collect { jsonSet ->
                _quickContacts.value = jsonSet.mapNotNull { json ->
                    parseQuickContact(json)
                }
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

    fun addQuickContact(contact: QuickContact) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val json = serializeQuickContact(contact)
                settingsDataStore.addQuickContact(json)
                // State will update via loadQuickContacts() collection
            } catch (e: Exception) {
                Log.e(TAG, "Error adding quick contact", e)
            }
        }
    }

    fun removeQuickContact(contact: QuickContact) {
        viewModelScope.launch(exceptionHandler) {
            try {
                // Find and remove the contact by ID from stored set
                settingsDataStore.quickContacts.first().forEach { json ->
                    val parsed = parseQuickContact(json)
                    if (parsed?.id == contact.id) {
                        settingsDataStore.removeQuickContact(json)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing quick contact", e)
            }
        }
    }

    /**
     * Validate password input against stored password.
     * Uses constant-time comparison to prevent timing attacks.
     */
    fun validatePassword(input: String): Boolean {
        val stored = password.value
        if (input.length != stored.length) return false
        var result = 0
        for (i in input.indices) {
            result = result or (input[i].code xor stored[i].code)
        }
        return result == 0
    }

    // JSON serialization for QuickContact (safer than pipe-delimited)
    private fun serializeQuickContact(contact: QuickContact): String {
        return JSONObject().apply {
            put("id", contact.id)
            put("name", contact.name)
            put("phoneNumber", contact.phoneNumber)
            put("photoUri", contact.photoUri ?: "")
            put("relation", contact.relation ?: "")
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
                    relation = obj.optString("relation").ifBlank { null }
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
                        relation = parts.getOrNull(3)
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing quick contact: $json", e)
            null
        }
    }
}
