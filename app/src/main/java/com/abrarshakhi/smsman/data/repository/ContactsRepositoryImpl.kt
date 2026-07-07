package com.abrarshakhi.smsman.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.abrarshakhi.smsman.data.db.dao.ContactCacheDao
import com.abrarshakhi.smsman.data.db.entity.ContactCacheEntity
import com.abrarshakhi.smsman.di.IoDispatcher
import com.abrarshakhi.smsman.domain.model.Contact
import com.abrarshakhi.smsman.domain.repository.ContactsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves phone numbers to contacts via [ContactsContract.PhoneLookup], cached in Room.
 *
 * Cache is keyed by the raw phone number as received from Telephony (no full E.164
 * normalization yet — PhoneLookup handles loose matching on its own). Entries older than
 * [CACHE_STALE_MS] are re-resolved from ContactsContract.
 *
 * If READ_CONTACTS isn't granted, returns null gracefully — UI falls back to the phone number.
 */
@Singleton
class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cacheDao: ContactCacheDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ContactsRepository {

    override suspend fun resolve(phoneNumber: String): Contact? = withContext(io) {
        val cached = cacheDao.byPhone(phoneNumber)
        if (cached != null && !isStale(cached)) return@withContext cached.toContact()

        val fresh = lookupSystem(phoneNumber)
        if (fresh != null) {
            cacheDao.upsert(fresh.toCacheEntity())
            fresh
        } else {
            // Negative-cache so we don't hammer ContactsContract on every render.
            cacheDao.upsert(
                ContactCacheEntity(
                    phoneNumberE164 = phoneNumber,
                    displayName = null,
                    photoUri = null,
                    lookupKey = null,
                    lastSyncedAt = System.currentTimeMillis(),
                ),
            )
            null
        }
    }

    override fun observe(phoneNumber: String): Flow<Contact?> = flow {
        // First emit any cached value (incl. negative cache as null), then trigger fresh fetch.
        emit(cacheDao.byPhone(phoneNumber)?.toContact())
        val fresh = resolve(phoneNumber)
        emit(fresh)
    }

    override suspend fun resolveAll(phoneNumbers: Collection<String>): Map<String, Contact> =
        withContext(io) {
            if (phoneNumbers.isEmpty()) return@withContext emptyMap()
            val distinct = phoneNumbers.toSet()
            val cached = cacheDao.byPhones(distinct.toList()).associateBy { it.phoneNumberE164 }
            val now = System.currentTimeMillis()

            val staleOrMissing = distinct.filter {
                val c = cached[it]
                c == null || (now - c.lastSyncedAt) > CACHE_STALE_MS
            }

            // Resolve missing/stale entries serially — typical conversation lists have
            // a few dozen distinct phones, not enough to justify parallel queries.
            for (phone in staleOrMissing) {
                resolve(phone)
            }

            // Re-read cache for the final, complete picture.
            val final = cacheDao.byPhones(distinct.toList())
            buildMap(final.size) {
                final.forEach { entity ->
                    entity.toContact()?.let { put(entity.phoneNumberE164, it) }
                }
            }
        }

    private fun lookupSystem(phoneNumber: String): Contact? {
        if (!hasReadContacts()) return null
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber),
        )
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup.PHOTO_URI,
                    ContactsContract.PhoneLookup.LOOKUP_KEY,
                ),
                null,
                null,
                null,
            )?.use { c ->
                if (c.moveToFirst()) {
                    Contact(
                        phoneNumberE164 = phoneNumber,
                        displayName = c.getString(0),
                        photoUri = c.getString(1),
                        lookupKey = c.getString(2),
                    )
                } else null
            }
        }.getOrNull()
    }

    override suspend fun search(query: String): List<Contact> = withContext(io) {
        if (query.length < 2 || !hasReadContacts()) return@withContext emptyList()
        val results = mutableListOf<Contact>()
        val uri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
            android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            android.provider.ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            android.provider.ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
        )
        val selection = "${android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        val selectionArgs = arrayOf("%$query%", "%$query%")
        runCatching {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC",
            )?.use { c ->
                while (c.moveToNext()) {
                    val number = c.getString(0) ?: continue
                    results.add(
                        Contact(
                            phoneNumberE164 = number,
                            displayName = c.getString(1),
                            photoUri = c.getString(2),
                            lookupKey = c.getString(3),
                        ),
                    )
                }
            }
        }
        results.distinctBy { it.lookupKey ?: it.phoneNumberE164 }.take(30)
    }

    private fun hasReadContacts(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    private fun isStale(entity: ContactCacheEntity): Boolean =
        System.currentTimeMillis() - entity.lastSyncedAt > CACHE_STALE_MS

    private fun ContactCacheEntity.toContact(): Contact? = displayName?.let {
        Contact(
            phoneNumberE164 = phoneNumberE164,
            displayName = it,
            photoUri = photoUri,
            lookupKey = lookupKey,
        )
    }

    private fun Contact.toCacheEntity(): ContactCacheEntity = ContactCacheEntity(
        phoneNumberE164 = phoneNumberE164,
        displayName = displayName,
        photoUri = photoUri,
        lookupKey = lookupKey,
        lastSyncedAt = System.currentTimeMillis(),
    )

    private companion object {
        const val CACHE_STALE_MS = 6 * 60 * 60 * 1000L  // 6 hours
    }
}
