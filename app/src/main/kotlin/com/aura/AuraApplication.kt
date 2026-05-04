package com.aura

import android.app.Application
import com.aura.core.network.networkModule
import com.aura.feature.meeting.meetingModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AuraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@AuraApplication)
            modules(networkModule, meetingModule)
        }
    }
}
