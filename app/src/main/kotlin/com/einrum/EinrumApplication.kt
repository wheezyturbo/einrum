package com.einrum

import android.app.Application
import com.einrum.core.network.networkModule
import com.einrum.feature.meeting.meetingModule
import com.einrum.feature.call.callModule
import com.einrum.core.ai.aiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class EinrumApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@EinrumApplication)
            modules(networkModule, meetingModule, callModule, aiModule)
        }
    }
}
