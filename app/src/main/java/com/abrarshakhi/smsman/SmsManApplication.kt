package com.abrarshakhi.smsman

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.abrarshakhi.smsman.data.telephony.TelephonySyncCoordinator
import com.abrarshakhi.smsman.framework.notification.MessageNotificationCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SmsManApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var telephonySyncCoordinator: TelephonySyncCoordinator
    @Inject lateinit var notificationCoordinator: MessageNotificationCoordinator

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationCoordinator.ensureChannels()
        telephonySyncCoordinator.start()
    }
}
