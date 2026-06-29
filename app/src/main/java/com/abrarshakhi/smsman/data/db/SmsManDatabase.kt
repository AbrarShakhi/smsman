package com.abrarshakhi.smsman.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abrarshakhi.smsman.data.db.dao.AttachmentDao
import com.abrarshakhi.smsman.data.db.dao.BlockedNumberDao
import com.abrarshakhi.smsman.data.db.dao.ContactCacheDao
import com.abrarshakhi.smsman.data.db.dao.ConversationDao
import com.abrarshakhi.smsman.data.db.dao.MessageDao
import com.abrarshakhi.smsman.data.db.dao.ScheduledMessageDao
import com.abrarshakhi.smsman.data.db.entity.AttachmentEntity
import com.abrarshakhi.smsman.data.db.entity.BlockedNumberEntity
import com.abrarshakhi.smsman.data.db.entity.ContactCacheEntity
import com.abrarshakhi.smsman.data.db.entity.ConversationEntity
import com.abrarshakhi.smsman.data.db.entity.MessageEntity
import com.abrarshakhi.smsman.data.db.entity.ScheduledMessageEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        AttachmentEntity::class,
        ContactCacheEntity::class,
        BlockedNumberEntity::class,
        ScheduledMessageEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class SmsManDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun contactCacheDao(): ContactCacheDao
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao

    companion object {
        const val NAME = "smsman.db"
    }
}
