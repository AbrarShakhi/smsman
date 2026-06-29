package com.abrarshakhi.smsman.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.abrarshakhi.smsman.data.db.entity.ScheduledMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMessageDao {

    @Query("SELECT * FROM scheduled_messages WHERE status = 0 ORDER BY scheduledAt ASC")
    fun observePending(): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE id = :id")
    suspend fun byId(id: Long): ScheduledMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScheduledMessageEntity): Long

    @Upsert
    suspend fun upsert(entity: ScheduledMessageEntity)

    @Query("UPDATE scheduled_messages SET status = :status, lastErrorMessage = :error WHERE id = :id")
    suspend fun updateStatus(id: Long, status: Int, error: String?)

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun delete(id: Long)
}
