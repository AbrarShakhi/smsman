package com.abrarshakhi.smsman.common

import android.app.Application
import com.abrarshakhi.smsman.common.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SmsmanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@SmsmanApplication)
            modules(appModules)
        }
    }
}
