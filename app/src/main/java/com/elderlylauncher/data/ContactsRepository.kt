package com.elderlylauncher.data

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ContactsRepository"

/**
 * Repository for contacts.
 * Uses applicationContext to prevent memory leaks.
 */
class ContactsRepository(context: Context) {

    // Use applicationContext to prevent Activity/Fragment context leaks
    private val appContext: Context = context.applicationContext

    /**
     * Check if we have contacts permission
     */
    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if we have call permission
     */
    fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get all contacts with phone numbers
     */
    suspend fun getContacts(): List<QuickContact> = withContext(Dispatchers.IO) {
        // Check permission first
        if (!hasContactsPermission()) {
            Log.w(TAG, "READ_CONTACTS permission not granted")
            return@withContext emptyList()
        }

        val contacts = mutableListOf<QuickContact>()

        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            )

            val cursor = appContext.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                // Validate column indices
                if (idIndex < 0 || nameIndex < 0 || numberIndex < 0) {
                    Log.e(TAG, "Required columns not found in cursor")
                    return@withContext emptyList()
                }

                while (it.moveToNext()) {
                    val id = it.getString(idIndex) ?: continue
                    val name = it.getString(nameIndex) ?: continue
                    val number = it.getString(numberIndex) ?: continue
                    val photo = if (photoIndex >= 0) it.getString(photoIndex) else null

                    // Avoid duplicates
                    if (contacts.none { c -> c.id == id }) {
                        contacts.add(
                            QuickContact(
                                id = id,
                                name = name,
                                phoneNumber = number,
                                photoUri = photo
                            )
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException querying contacts", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts", e)
        }

        contacts
    }

    /**
     * Search contacts by name
     */
    suspend fun searchContacts(query: String): List<QuickContact> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        // Check permission first
        if (!hasContactsPermission()) {
            Log.w(TAG, "READ_CONTACTS permission not granted")
            return@withContext emptyList()
        }

        val contacts = mutableListOf<QuickContact>()

        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            )

            val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%$query%")

            val cursor = appContext.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                // Validate column indices
                if (idIndex < 0 || nameIndex < 0 || numberIndex < 0) {
                    Log.e(TAG, "Required columns not found in cursor")
                    return@withContext emptyList()
                }

                while (it.moveToNext()) {
                    val id = it.getString(idIndex) ?: continue
                    val name = it.getString(nameIndex) ?: continue
                    val number = it.getString(numberIndex) ?: continue
                    val photo = if (photoIndex >= 0) it.getString(photoIndex) else null

                    if (contacts.none { c -> c.id == id }) {
                        contacts.add(
                            QuickContact(
                                id = id,
                                name = name,
                                phoneNumber = number,
                                photoUri = photo
                            )
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException searching contacts", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching contacts", e)
        }

        contacts
    }

    /**
     * Call a contact directly (requires CALL_PHONE permission)
     * @return true if call was initiated, false otherwise
     */
    fun callContact(phoneNumber: String): Boolean {
        if (!hasCallPermission()) {
            Log.w(TAG, "CALL_PHONE permission not granted, falling back to dialer")
            return dialNumber(phoneNumber)
        }

        return try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No phone app found", e)
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException making call, falling back to dialer", e)
            dialNumber(phoneNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error making call", e)
            false
        }
    }

    /**
     * Open dialer with number (no permission required)
     * @return true if dialer was opened, false otherwise
     */
    fun dialNumber(phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No dialer app found", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error opening dialer", e)
            false
        }
    }
}
