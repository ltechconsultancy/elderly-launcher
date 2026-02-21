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
