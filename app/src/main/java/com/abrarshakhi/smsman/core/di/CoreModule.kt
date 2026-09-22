package com.abrarshakhi.smsman.core.di

import com.abrarshakhi.smsman.core.permissions.SmsRoleManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { SmsRoleManager(androidContext()) }
}
