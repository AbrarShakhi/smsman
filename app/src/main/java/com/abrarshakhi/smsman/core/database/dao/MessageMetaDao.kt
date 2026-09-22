package com.abrarshakhi.smsman.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abrarshakhi.smsman.core.database.entity.MessageMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageMetaDao {

    /** Backs the Pinned tab: newest pin first, across every conversation. */
    @Query("SELECT * FROM message_meta ORDER BY pinnedAt DESC")
    fun observeAllPinned(): Flow<List<MessageMetaEntity>>

    @Query("SELECT messageId FROM message_meta WHERE threadId = :threadId")
    fun observePinnedIdsInThread(threadId: Long): Flow<List<Long>>

    @Upsert
    suspend fun upsert(meta: MessageMetaEntity)

    @Query("DELETE FROM message_meta WHERE messageId = :messageId")
    suspend fun unpin(messageId: Long)

    /** Drops pins whose provider rows no longer exist; see repository for when this runs. */
    @Query("DELETE FROM message_meta WHERE messageId IN (:messageIds)")
    suspend fun prune(messageIds: List<Long>)
}
