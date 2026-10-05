package com.abrarshakhi.smsman.di

import androidx.room.Room
import com.abrarshakhi.smsman.data.database.SmsmanDatabase
import com.abrarshakhi.smsman.data.provider.ContactsDataSource
import com.abrarshakhi.smsman.data.provider.ConversationsDataSource
import com.abrarshakhi.smsman.data.provider.MessagesDataSource
import com.abrarshakhi.smsman.data.provider.SimDataSource
import com.abrarshakhi.smsman.data.provider.TelephonyChangeObserver
import com.abrarshakhi.smsman.data.repository.ConversationRepository
import com.abrarshakhi.smsman.data.repository.MessageMetadataRepository
import com.abrarshakhi.smsman.data.repository.MessageRepository
import com.abrarshakhi.smsman.data.repository.PinnedRepository
import com.abrarshakhi.smsman.data.repository.SearchRepository
import com.abrarshakhi.smsman.data.repository.ThreadTitleResolver
import com.abrarshakhi.smsman.data.settings.SettingsRepository
import com.abrarshakhi.smsman.notification.MessageNotifier
import com.abrarshakhi.smsman.sms.SmsRoleManager
import com.abrarshakhi.smsman.sms.SmsSender
import com.abrarshakhi.smsman.ui.MainAppViewModel
import com.abrarshakhi.smsman.ui.chat.ChatViewModel
import com.abrarshakhi.smsman.ui.conversations.ConversationsViewModel
import com.abrarshakhi.smsman.ui.newmessage.NewMessageViewModel
import com.abrarshakhi.smsman.ui.pinned.PinnedViewModel
import com.abrarshakhi.smsman.ui.search.SearchViewModel
import com.abrarshakhi.smsman.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(androidContext(), SmsmanDatabase::class.java, SmsmanDatabase.NAME)
            .build()
    }
    single { get<SmsmanDatabase>().conversationMetaDao() }
    single { get<SmsmanDatabase>().messageMetaDao() }

    single { ConversationsDataSource(androidContext()) }
    single { MessagesDataSource(androidContext()) }
    single { ContactsDataSource(androidContext()) }
    single { SimDataSource(androidContext()) }
    single { TelephonyChangeObserver(androidContext()) }

    single { MessageMetadataRepository(get(), get()) }
    single { ConversationRepository(get(), get(), get(), get()) }
    single { MessageRepository(get(), get(), get(), get(), get()) }
    single { PinnedRepository(get(), get(), get(), get()) }
    single { SearchRepository(get(), get()) }
    single { ThreadTitleResolver(get(), get()) }
    single { SettingsRepository(androidContext()) }
}

val platformModule = module {
    single { SmsRoleManager(androidContext()) }
    single { SmsSender(androidContext()) }
    single { MessageNotifier(androidContext()) }
}

val viewModelModule = module {
    viewModelOf(::MainAppViewModel)
    viewModel { (favoritesOnly: Boolean) -> ConversationsViewModel(get(), favoritesOnly) }
    viewModelOf(::PinnedViewModel)
    viewModel { (threadId: Long) ->
        ChatViewModel(
            repository = get(),
            metadata = get(),
            titleResolver = get(),
            sender = get(),
            messages = get(),
            notifier = get(),
            threadId = threadId,
        )
    }
    viewModelOf(::NewMessageViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SettingsViewModel)
}

val appModules = listOf(dataModule, platformModule, viewModelModule)
