package com.einrum.feature.meeting

import androidx.compose.runtime.Immutable

@Immutable
data class LobbyState(
    val meetingId: String = "",
    val guestName: String = "",
    val contactName: String = "",
    val contacts: List<SavedContact> = emptyList(),
    val isJoining: Boolean = false,
    val error: String? = null
)

@Immutable
data class SavedContact(
    val name: String,
    val meetingId: String
)

sealed interface LobbyIntent {
    data class UpdateMeetingId(val id: String) : LobbyIntent
    data class UpdateGuestName(val name: String) : LobbyIntent
    data class UpdateContactName(val name: String) : LobbyIntent
    data object JoinMeeting : LobbyIntent
    data object CreateMeeting : LobbyIntent
    data object SaveContact : LobbyIntent
    data class RemoveContact(val meetingId: String) : LobbyIntent
    data class JoinFromContact(val meetingId: String) : LobbyIntent
}

sealed interface LobbyEffect {
    data class NavigateToCall(val meetingId: String) : LobbyEffect
    data class ShowError(val message: String) : LobbyEffect
}
