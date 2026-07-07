package com.abrarshakhi.smsman.di

import android.content.Context
import androidx.room.Room
import com.abrarshakhi.smsman.data.db.SmsManDatabase
import com.abrarshakhi.smsman.data.db.dao.AttachmentDao
import com.abrarshakhi.smsman.data.db.dao.BlockedNumberDao
import com.abrarshakhi.smsman.data.db.dao.ContactCacheDao
import com.abrarshakhi.smsman.data.db.dao.ConversationDao
import com.abrarshakhi.smsman.data.db.dao.MessageDao
import com.abrarshakhi.smsman.data.db.dao.ScheduledMessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmsManDatabase =
        Room.databaseBuilder(context, SmsManDatabase::class.java, SmsManDatabase.NAME)
            // Until v1.0 release, schema changes wipe local cache (Telephony provider is authoritative
            // so this only loses our pin/archive/blocked metadata, not the messages themselves).
            // Replace with real Migration() steps before public release.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()

    @Provides fun provideConversationDao(db: SmsManDatabase): ConversationDao = db.conversationDao()
    @Provides fun provideMessageDao(db: SmsManDatabase): MessageDao = db.messageDao()
    @Provides fun provideAttachmentDao(db: SmsManDatabase): AttachmentDao = db.attachmentDao()
    @Provides fun provideContactCacheDao(db: SmsManDatabase): ContactCacheDao = db.contactCacheDao()
    @Provides fun provideBlockedNumberDao(db: SmsManDatabase): BlockedNumberDao = db.blockedNumberDao()
    @Provides fun provideScheduledMessageDao(db: SmsManDatabase): ScheduledMessageDao = db.scheduledMessageDao()
}
