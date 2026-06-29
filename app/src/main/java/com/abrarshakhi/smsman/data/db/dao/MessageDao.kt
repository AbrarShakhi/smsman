package com.abrarshakhi.smsman.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abrarshakhi.smsman.data.db.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY date ASC")
    fun observeByThread(threadId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY date ASC")
    suspend fun getAll(): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun byId(id: Long): MessageEntity?

    @Upsert
    suspend fun upsert(entity: MessageEntity)

    @Upsert
    suspend fun upsertAll(entities: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("UPDATE messages SET read = 1, seen = 1 WHERE threadId = :threadId AND read = 0")
    suspend fun markThreadRead(threadId: Long)

    @Query("UPDATE messages SET status = :status, errorCode = :errorCode WHERE id = :id")
    suspend fun updateStatus(id: Long, status: Int, errorCode: Int)

    @Query("UPDATE messages SET mmsDownloadState = :state WHERE id = :id")
    suspend fun updateMmsDownloadState(id: Long, state: Int)

    @Query(
        """
        SELECT * FROM messages
        WHERE body LIKE '%' || :query || '%'
        ORDER BY date DESC
        LIMIT :limit
        """
    )
    suspend fun search(query: String, limit: Int = 200): List<MessageEntity>

    @Query(
        """
        SELECT * FROM messages
        WHERE threadId = :threadId AND body LIKE '%' || :query || '%'
        ORDER BY date DESC
        """
    )
    suspend fun searchInThread(threadId: Long, query: String): List<MessageEntity>
}
