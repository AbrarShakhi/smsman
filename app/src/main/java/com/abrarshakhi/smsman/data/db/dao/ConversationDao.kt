package com.abrarshakhi.smsman.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.abrarshakhi.smsman.data.db.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query(
        """
        SELECT * FROM conversations
        WHERE archived = 0 AND blocked = 0
        ORDER BY pinned DESC, pinnedAt DESC, lastMessageAt DESC
        """
    )
    fun observeActive(): Flow<List<ConversationEntity>>

    @Query(
        """
        SELECT * FROM conversations
        WHERE archived = 0 AND blocked = 0 AND unreadCount > 0
        ORDER BY lastMessageAt DESC
        """
    )
    fun observeUnread(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE archived = 1 ORDER BY lastMessageAt DESC")
    fun observeArchived(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE blocked = 1 ORDER BY lastMessageAt DESC")
    fun observeBlocked(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations")
    suspend fun getAll(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE threadId = :threadId")
    suspend fun byId(threadId: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE threadId = :threadId")
    fun observeById(threadId: Long): Flow<ConversationEntity?>

    @Upsert
    suspend fun upsert(entity: ConversationEntity)

    @Upsert
    suspend fun upsertAll(entities: List<ConversationEntity>)

    @Query("DELETE FROM conversations WHERE threadId IN (:threadIds)")
    suspend fun delete(threadIds: List<Long>)

    @Query("UPDATE conversations SET archived = :archived WHERE threadId = :threadId")
    suspend fun setArchived(threadId: Long, archived: Boolean)

    @Query("UPDATE conversations SET pinned = :pinned, pinnedAt = :pinnedAt WHERE threadId = :threadId")
    suspend fun setPinned(threadId: Long, pinned: Boolean, pinnedAt: Long?)

    @Query("UPDATE conversations SET blocked = :blocked WHERE threadId = :threadId")
    suspend fun setBlocked(threadId: Long, blocked: Boolean)

    @Query("UPDATE conversations SET muted = :muted WHERE threadId = :threadId")
    suspend fun setMuted(threadId: Long, muted: Boolean)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE threadId = :threadId")
    suspend fun markRead(threadId: Long)

    @Query("UPDATE conversations SET draft = :draft WHERE threadId = :threadId")
    suspend fun updateDraft(threadId: Long, draft: String?)
}
