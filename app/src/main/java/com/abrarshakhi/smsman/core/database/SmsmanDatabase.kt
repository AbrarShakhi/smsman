package com.abrarshakhi.smsman.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abrarshakhi.smsman.core.database.dao.ConversationMetaDao
import com.abrarshakhi.smsman.core.database.dao.MessageMetaDao
import com.abrarshakhi.smsman.core.database.entity.ConversationMetaEntity
import com.abrarshakhi.smsman.core.database.entity.MessageMetaEntity

/**
 * Holds only metadata the Telephony provider cannot represent. Messages themselves are read live
 * from the provider and deliberately not mirrored: the provider is the source of truth and is
 * written by other apps too, so a mirror would introduce staleness for no measurable gain at this
 * inbox size.
 */
@Database(
    entities = [ConversationMetaEntity::class, MessageMetaEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SmsmanDatabase : RoomDatabase() {
    abstract fun conversationMetaDao(): ConversationMetaDao
    abstract fun messageMetaDao(): MessageMetaDao

    companion object {
        const val NAME = "smsman.db"
    }
}
