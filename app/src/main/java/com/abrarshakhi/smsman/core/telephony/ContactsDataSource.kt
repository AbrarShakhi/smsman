package com.abrarshakhi.smsman.core.telephony

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.abrarshakhi.smsman.core.model.ContactInfo
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ContactsDataSource"

class ContactsDataSource(private val context: Context) {

    /** Memoised because the same addresses repeat across every thread on every refresh. */
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

    private fun query(address: String): ContactInfo? {
        // PhoneLookup normalises internally - verified that "+8801586365917" matches a contact
        // stored as "+880 1586-365917" - so the number must NOT be pre-normalised here.
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
