package com.abrarshakhi.smsman.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abrarshakhi.smsman.data.db.entity.ContactCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactCacheDao {

    @Query("SELECT * FROM contacts_cache WHERE phoneNumberE164 = :phoneE164")
    suspend fun byPhone(phoneE164: String): ContactCacheEntity?

    @Query("SELECT * FROM contacts_cache WHERE phoneNumberE164 = :phoneE164")
    fun observeByPhone(phoneE164: String): Flow<ContactCacheEntity?>

    @Query("SELECT * FROM contacts_cache WHERE phoneNumberE164 IN (:phones)")
    suspend fun byPhones(phones: List<String>): List<ContactCacheEntity>

    @Upsert
    suspend fun upsert(entity: ContactCacheEntity)

    @Upsert
    suspend fun upsertAll(entities: List<ContactCacheEntity>)

    @Query("DELETE FROM contacts_cache WHERE phoneNumberE164 = :phoneE164")
    suspend fun delete(phoneE164: String)

    @Query("DELETE FROM contacts_cache WHERE lastSyncedAt < :olderThan")
    suspend fun deleteStale(olderThan: Long)
}
