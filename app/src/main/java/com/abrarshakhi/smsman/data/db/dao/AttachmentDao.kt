package com.abrarshakhi.smsman.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.abrarshakhi.smsman.data.db.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE messageId = :messageId")
    fun observeByMessage(messageId: Long): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE messageId = :messageId")
    suspend fun byMessage(messageId: Long): List<AttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AttachmentEntity>): List<Long>

    @Query("DELETE FROM attachments WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("DELETE FROM attachments WHERE messageId = :messageId")
    suspend fun deleteByMessage(messageId: Long)
}
