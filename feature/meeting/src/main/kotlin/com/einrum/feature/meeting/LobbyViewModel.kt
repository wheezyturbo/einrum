package com.einrum.feature.meeting

import com.einrum.core.network.MeetingService
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
    private val meetingService: MeetingService
) : ViewModel() {

    private val _state = MutableStateFlow(LobbyState())
    val state: StateFlow<LobbyState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LobbyEffect>()
    val effects: SharedFlow<LobbyEffect> = _effects.asSharedFlow()

    init {
        refreshContacts()
    }

    fun onIntent(intent: LobbyIntent) {
        when (intent) {
            is LobbyIntent.UpdateMeetingId -> updateMeetingId(intent.id)
            is LobbyIntent.UpdateGuestName -> updateGuestName(intent.name)
            is LobbyIntent.UpdateContactName -> updateContactName(intent.name)
            LobbyIntent.JoinMeeting -> joinMeeting()
            LobbyIntent.CreateMeeting -> createMeeting()
            LobbyIntent.SaveContact -> saveContact()
            is LobbyIntent.RemoveContact -> removeContact(intent.meetingId)
            is LobbyIntent.JoinFromContact -> joinFromContact(intent.meetingId)
        }
    }

    private fun updateMeetingId(id: String) {
        val sanitizedId = id.filter { it.isLetterOrDigit() }.take(6)
        _state.update { it.copy(meetingId = sanitizedId, error = null) }
    }

    private fun updateGuestName(name: String) {
        val sanitizedName = name.filter { it.isLetterOrDigit() || it.isWhitespace() }.take(20)
        _state.update { it.copy(guestName = sanitizedName, error = null) }
    }

    private fun updateContactName(name: String) {
        val sanitizedName = name.filter { it.isLetterOrDigit() || it.isWhitespace() }.take(24)
        _state.update { it.copy(contactName = sanitizedName, error = null) }
    }

    private fun joinMeeting() {
        val currentId = _state.value.meetingId
        val guestName = _state.value.guestName

        if (guestName.isBlank()) {
            viewModelScope.launch {
                _effects.emit(LobbyEffect.ShowError("Please enter a display name"))
            }
            return
        }
        
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
                _effects.emit(LobbyEffect.ShowError("Meeting not found. Create one first or use a saved contact."))
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

    private fun saveContact() {
        viewModelScope.launch {
            val current = _state.value
            val name = current.contactName.ifBlank { current.guestName.ifBlank { "Contact" } }
            val success = meetingService.addContact(name = name, meetingId = current.meetingId)
            if (success) {
                _state.update { it.copy(contactName = "") }
                refreshContacts()
                _effects.emit(LobbyEffect.ShowError("Contact saved"))
            } else {
                _effects.emit(LobbyEffect.ShowError("Enter a valid 6-digit active meeting code to save contact"))
            }
        }
    }

    private fun removeContact(meetingId: String) {
        viewModelScope.launch {
            meetingService.removeContact(meetingId)
            refreshContacts()
        }
    }

    private fun joinFromContact(meetingId: String) {
        _state.update { it.copy(meetingId = meetingId) }
        joinMeeting()
    }

    private fun refreshContacts() {
        viewModelScope.launch {
            val contacts = meetingService.getRecentContacts().map { contact ->
                SavedContact(
                    name = contact.name,
                    meetingId = contact.meetingId
                )
            }
            _state.update { it.copy(contacts = contacts) }
        }
    }
}
