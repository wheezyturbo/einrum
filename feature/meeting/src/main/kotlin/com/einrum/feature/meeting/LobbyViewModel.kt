package com.einrum.feature.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LobbyViewModel : ViewModel() {

    private val _state = MutableStateFlow(LobbyState())
    val state: StateFlow<LobbyState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LobbyEffect>()
    val effects: SharedFlow<LobbyEffect> = _effects.asSharedFlow()

    fun onIntent(intent: LobbyIntent) {
        when (intent) {
            is LobbyIntent.UpdateMeetingId -> updateMeetingId(intent.id)
            LobbyIntent.JoinMeeting -> joinMeeting()
            LobbyIntent.CreateMeeting -> createMeeting()
        }
    }

    private fun updateMeetingId(id: String) {
        _state.update { it.copy(meetingId = id, error = null) }
    }

    private fun joinMeeting() {
        val currentId = _state.value.meetingId
        if (currentId.isBlank()) {
            viewModelScope.launch {
                _effects.emit(LobbyEffect.ShowError("Meeting ID cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isJoining = true) }
            val success = meetingService.joinMeeting(currentId)
            _state.update { it.copy(isJoining = false) }
            
            if (success) {
                _effects.emit(LobbyEffect.NavigateToCall(currentId))
            } else {
                _effects.emit(LobbyEffect.ShowError("Failed to join meeting"))
            }
        }
    }

    private fun createMeeting() {
        viewModelScope.launch {
            _state.update { it.copy(isJoining = true) }
            val newId = meetingService.createMeeting()
            _state.update { it.copy(isJoining = false, meetingId = newId) }
            _effects.emit(LobbyEffect.NavigateToCall(newId))
        }
    }
}
.NavigateToCall(newId))
        }
    }
}
