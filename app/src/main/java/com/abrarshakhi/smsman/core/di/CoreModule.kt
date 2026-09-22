package com.abrarshakhi.smsman.core.di

import androidx.room.Room
import com.abrarshakhi.smsman.core.database.SmsmanDatabase
import com.abrarshakhi.smsman.core.permissions.SmsRoleManager
import com.abrarshakhi.smsman.core.repository.MessageMetadataRepository
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
}
