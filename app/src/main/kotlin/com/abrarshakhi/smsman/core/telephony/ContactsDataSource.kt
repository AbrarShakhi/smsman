package com.abrarshakhi.smsman.core.telephony

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.abrarshakhi.smsman.core.model.ContactInfo
import com.abrarshakhi.smsman.core.model.ContactSuggestion
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ContactsDataSource"

class ContactsDataSource(private val context: Context) {

    private val cache = ConcurrentHashMap<String, Optional>()

    private class Optional(val value: ContactInfo?)

    fun lookup(address: String): ContactInfo? {
        if (address.isBlank()) return null
        cache[address]?.let { return it.value }

        val resolved = query(address)
        cache[address] = Optional(resolved)
        return resolved
    }

    fun invalidate() = cache.clear()

    fun search(query: String, limit: Int = 20): List<ContactSuggestion> {
        if (query.isBlank()) return emptyList()
        val uri = Uri.withAppendedPath(
            ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
            Uri.encode(query),
        )
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
        )
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                buildList {
                    while (cursor.moveToNext() && size < limit) {
                        val number = cursor.stringOrNull(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ) ?: continue
                        add(
                            ContactSuggestion(
                                name = cursor.stringOrNull(
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ).orEmpty(),
                                number = number,
                                photoUri = cursor.stringOrNull(
                                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                                ),
                            ),
                        )
                    }
                }
            }.orEmpty()
        } catch (e: SecurityException) {
            Log.w(TAG, "READ_CONTACTS not granted", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Contact search failed", e)
            emptyList()
        }
    }

    private fun query(address: String): ContactInfo? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(address),
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup.CONTACT_ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
        )
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val name = cursor.stringOrNull(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (name.isNullOrBlank()) return@use null
                ContactInfo(
                    displayName = name,
                    photoUri = cursor.stringOrNull(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI),
                    contactId = cursor.longOr(ContactsContract.PhoneLookup.CONTACT_ID, -1L)
                        .takeIf { it >= 0 },
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "READ_CONTACTS not granted", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Contact lookup failed for a message address", e)
            null
        }
    }
}
