package com.abrarshakhi.smsman.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abrarshakhi.smsman.data.db.entity.BlockedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {

    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun observeAll(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE phoneNumberE164 = :phoneE164)")
    suspend fun isBlocked(phoneE164: String): Boolean

    @Upsert
    suspend fun upsert(entity: BlockedNumberEntity)

    @Query("DELETE FROM blocked_numbers WHERE phoneNumberE164 = :phoneE164")
    suspend fun unblock(phoneE164: String)
}
