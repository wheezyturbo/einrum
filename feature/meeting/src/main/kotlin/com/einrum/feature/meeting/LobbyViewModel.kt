package com.einrum.feature.meeting

import com.einrum.core.network.RoomService
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

class LobbyViewModel(
    private val roomService: RoomService
) : ViewModel() {

    private val _state = MutableStateFlow(LobbyState())
    val state: StateFlow<LobbyState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LobbyEffect>()
    val effects: SharedFlow<LobbyEffect> = _effects.asSharedFlow()

    fun onIntent(intent: LobbyIntent) {
        when (intent) {
            is LobbyIntent.UpdateRoomId -> updateRoomId(intent.id)
            is LobbyIntent.UpdateGuestName -> updateGuestName(intent.name)
            LobbyIntent.JoinRoom -> joinRoom()
            LobbyIntent.CreateRoom -> createRoom()
        }
    }

    private fun updateRoomId(id: String) {
        val sanitizedId = id.filter { it.isDigit() }.take(6)
        _state.update { it.copy(roomId = sanitizedId, error = null) }
    }

    private fun updateGuestName(name: String) {
        val sanitizedName = name.filter { it.isLetterOrDigit() || it.isWhitespace() }.take(20)
        _state.update { it.copy(guestName = sanitizedName, error = null) }
    }

    private fun joinRoom() {
        val currentId = _state.value.roomId
        val guestName = _state.value.guestName

        if (guestName.isBlank()) {
            viewModelScope.launch {
                _effects.emit(LobbyEffect.ShowError("Please enter a display name"))
            }
            return
        }
        
        if (currentId.isBlank()) {
            viewModelScope.launch {
                _effects.emit(LobbyEffect.ShowError("Room ID cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isJoining = true) }
            val success = roomService.joinRoom(currentId)
            _state.update { it.copy(isJoining = false) }
            
            if (success) {
                _effects.emit(LobbyEffect.NavigateToCall(currentId))
            } else {
                _effects.emit(LobbyEffect.ShowError("Room not found. Create one first."))
            }
        }
    }

    private fun createRoom() {
        viewModelScope.launch {
            _state.update { it.copy(isJoining = true) }
            val newId = roomService.createRoom()
            _state.update { it.copy(isJoining = false, roomId = newId) }
            _effects.emit(LobbyEffect.NavigateToCall(newId))
        }
    }
}
