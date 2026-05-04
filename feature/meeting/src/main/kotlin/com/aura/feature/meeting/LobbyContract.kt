package com.aura.feature.meeting

import androidx.compose.runtime.Immutable

@Immutable
data class LobbyState(
    val meetingId: String = "",
    val isJoining: Boolean = false,
    val error: String? = null
)

sealed interface LobbyIntent {
    data class UpdateMeetingId(val id: String) : LobbyIntent
    data object JoinMeeting : LobbyIntent
    data object CreateMeeting : LobbyIntent
}

sealed interface LobbyEffect {
    data class NavigateToCall(val meetingId: String) : LobbyEffect
    data class ShowError(val message: String) : LobbyEffect
}
