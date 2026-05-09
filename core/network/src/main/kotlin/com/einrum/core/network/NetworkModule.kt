package com.einrum.core.network

import org.koin.dsl.module

interface RoomService {
    suspend fun createRoom(): String
    suspend fun joinRoom(id: String): Boolean
}

interface RtcService {
    suspend fun connectToRoom(roomId: String, displayName: String): Boolean
    suspend fun leaveRoom()
    suspend fun toggleMic(enabled: Boolean)
    suspend fun toggleCamera(enabled: Boolean)
    suspend fun toggleScreenShare(enabled: Boolean)
}

class FakeRoomService : RoomService {
    private val activeRooms = linkedSetOf("123456", "654321", "246810")

    override suspend fun createRoom(): String {
        var id: String
        do {
            id = (100000..999999).random().toString()
        } while (activeRooms.contains(id))
        activeRooms.add(id)
        return id
    }

    override suspend fun joinRoom(id: String): Boolean = activeRooms.contains(id)
}

class FakeRtcService : RtcService {
    override suspend fun connectToRoom(roomId: String, displayName: String): Boolean = true
    override suspend fun leaveRoom() = Unit
    override suspend fun toggleMic(enabled: Boolean) = Unit
    override suspend fun toggleCamera(enabled: Boolean) = Unit
    override suspend fun toggleScreenShare(enabled: Boolean) = Unit
}

val networkModule = module {
    single<RoomService> { FakeRoomService() }
    single<RtcService> { FakeRtcService() }
}
