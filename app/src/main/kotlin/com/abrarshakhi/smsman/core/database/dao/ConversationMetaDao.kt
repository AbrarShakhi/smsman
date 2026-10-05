package com.abrarshakhi.smsman.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abrarshakhi.smsman.core.database.entity.ConversationMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationMetaDao {

    @Query("SELECT threadId FROM conversation_meta WHERE isFavorite = 1")
    fun observeFavoriteThreadIds(): Flow<List<Long>>

    @Query("SELECT * FROM conversation_meta WHERE threadId = :threadId LIMIT 1")
    suspend fun find(threadId: Long): ConversationMetaEntity?

    @Upsert
    suspend fun upsert(meta: ConversationMetaEntity)

    @Query("DELETE FROM conversation_meta WHERE threadId = :threadId")
    suspend fun delete(threadId: Long)
}
