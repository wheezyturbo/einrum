package com.einrum.feature.meeting

import androidx.compose.runtime.Immutable

@Immutable
data class LobbyState(
    val roomId: String = "",
    val guestName: String = "",
    val isJoining: Boolean = false,
    val error: String? = null
)

sealed interface LobbyIntent {
    data class UpdateRoomId(val id: String) : LobbyIntent
    data class UpdateGuestName(val name: String) : LobbyIntent
    data object JoinRoom : LobbyIntent
    data object CreateRoom : LobbyIntent
}

sealed interface LobbyEffect {
    data class NavigateToCall(val roomId: String) : LobbyEffect
    data class ShowError(val message: String) : LobbyEffect
}
