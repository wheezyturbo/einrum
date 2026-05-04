package com.aura.core.network

import org.koin.dsl.module

interface MeetingService {
    suspend fun createMeeting(): String
    suspend fun joinMeeting(id: String): Boolean
}

class FakeMeetingService : MeetingService {
    override suspend fun createMeeting(): String = (100000..999999).random().toString()
    override suspend fun joinMeeting(id: String): Boolean = id.length == 6
}

val networkModule = module {
    single<MeetingService> { FakeMeetingService() }
}
