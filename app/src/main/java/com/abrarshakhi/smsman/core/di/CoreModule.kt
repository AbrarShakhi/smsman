package com.abrarshakhi.smsman.core.di

import androidx.room.Room
import com.abrarshakhi.smsman.core.database.SmsmanDatabase
import com.abrarshakhi.smsman.core.permissions.SmsRoleManager
import com.abrarshakhi.smsman.core.repository.ConversationRepository
import com.abrarshakhi.smsman.core.repository.MessageMetadataRepository
import com.abrarshakhi.smsman.core.repository.MessageRepository
import com.abrarshakhi.smsman.core.notification.MessageNotifier
import com.abrarshakhi.smsman.core.settings.SettingsRepository
import com.abrarshakhi.smsman.core.repository.PinnedRepository
import com.abrarshakhi.smsman.core.repository.ThreadTitleResolver
import com.abrarshakhi.smsman.core.telephony.MessagesDataSource
import com.abrarshakhi.smsman.core.telephony.SimDataSource
import com.abrarshakhi.smsman.core.telephony.SmsSender
import com.abrarshakhi.smsman.core.telephony.ContactsDataSource
import com.abrarshakhi.smsman.core.telephony.ConversationsDataSource
import com.abrarshakhi.smsman.core.telephony.TelephonyChangeObserver
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { SmsRoleManager(androidContext()) }

    single {
        Room.databaseBuilder(androidContext(), SmsmanDatabase::class.java, SmsmanDatabase.NAME)
            .build()
    }
    single { get<SmsmanDatabase>().conversationMetaDao() }
    single { get<SmsmanDatabase>().messageMetaDao() }

    single { MessageMetadataRepository(get(), get()) }

    single { ConversationsDataSource(androidContext()) }
    single { ContactsDataSource(androidContext()) }
    single { TelephonyChangeObserver(androidContext()) }
    single { ConversationRepository(get(), get(), get(), get(), Dispatchers.IO) }

    single { MessagesDataSource(androidContext()) }
    single { SimDataSource(androidContext()) }
    single { MessageRepository(get(), get(), get(), get(), Dispatchers.IO) }
    single { ThreadTitleResolver(get(), get(), Dispatchers.IO) }
    single { SmsSender(androidContext()) }
    single { PinnedRepository(get(), get(), get(), get(), Dispatchers.IO) }
    single { MessageNotifier(androidContext()) }
    single { SettingsRepository(androidContext()) }
}
