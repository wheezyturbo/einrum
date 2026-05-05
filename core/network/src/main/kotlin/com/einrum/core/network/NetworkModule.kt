package com.einrum.core.network

import org.koin.dsl.module

interface MeetingService {
    suspend fun createMeeting(): String
    suspend fun joinMeeting(id: String): Boolean
    suspend fun getRecentContacts(): List<MeetingContact>
    suspend fun addContact(name: String, meetingId: String): Boolean
    suspend fun removeContact(meetingId: String): Boolean
}

data class MeetingContact(
    val name: String,
    val meetingId: String
)

class FakeMeetingService : MeetingService {
    private val activeMeetings = linkedSetOf("123456", "654321", "246810")
    private val contacts = mutableListOf(
        MeetingContact(name = "Design Team", meetingId = "123456"),
        MeetingContact(name = "Daily Standup", meetingId = "654321")
    )

    override suspend fun createMeeting(): String {
        var id: String
        do {
            id = (100000..999999).random().toString()
        } while (activeMeetings.contains(id))
        activeMeetings.add(id)
        return id
    }

    override suspend fun joinMeeting(id: String): Boolean = activeMeetings.contains(id)

    override suspend fun getRecentContacts(): List<MeetingContact> = contacts.toList()

    override suspend fun addContact(name: String, meetingId: String): Boolean {
        if (name.isBlank() || meetingId.length != 6 || !meetingId.all { it.isDigit() }) return false
        if (!activeMeetings.contains(meetingId)) return false
        if (contacts.any { it.meetingId == meetingId }) return true
        contacts.add(0, MeetingContact(name = name, meetingId = meetingId))
        return true
    }

    override suspend fun removeContact(meetingId: String): Boolean = contacts.removeAll { it.meetingId == meetingId }
}

val networkModule = module {
    single<MeetingService> { FakeMeetingService() }
}
