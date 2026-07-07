package com.abrarshakhi.smsman.di

import com.abrarshakhi.smsman.data.datastore.SettingsRepositoryImpl
import com.abrarshakhi.smsman.data.repository.BackupRepositoryImpl
import com.abrarshakhi.smsman.data.repository.BlockedNumberRepositoryImpl
import com.abrarshakhi.smsman.data.repository.ContactsRepositoryImpl
import com.abrarshakhi.smsman.data.repository.ConversationRepositoryImpl
import com.abrarshakhi.smsman.data.repository.MessageRepositoryImpl
import com.abrarshakhi.smsman.data.repository.ScheduledMessageRepositoryImpl
import com.abrarshakhi.smsman.data.repository.SearchRepositoryImpl
import com.abrarshakhi.smsman.domain.repository.BackupRepository
import com.abrarshakhi.smsman.domain.repository.BlockedNumberRepository
import com.abrarshakhi.smsman.domain.repository.ContactsRepository
import com.abrarshakhi.smsman.domain.repository.ConversationRepository
import com.abrarshakhi.smsman.domain.repository.MessageRepository
import com.abrarshakhi.smsman.domain.repository.ScheduledMessageRepository
import com.abrarshakhi.smsman.domain.repository.SearchRepository
import com.abrarshakhi.smsman.domain.repository.SettingsRepository
import com.abrarshakhi.smsman.domain.repository.SimRepository
import com.abrarshakhi.smsman.framework.SimRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConversationRepository(impl: ConversationRepositoryImpl): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindContactsRepository(impl: ContactsRepositoryImpl): ContactsRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindBlockedNumberRepository(impl: BlockedNumberRepositoryImpl): BlockedNumberRepository

    @Binds
    @Singleton
    abstract fun bindScheduledMessageRepository(impl: ScheduledMessageRepositoryImpl): ScheduledMessageRepository

    @Binds
    @Singleton
    abstract fun bindSimRepository(impl: SimRepositoryImpl): SimRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
