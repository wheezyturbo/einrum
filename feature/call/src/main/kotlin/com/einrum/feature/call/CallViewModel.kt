package com.einrum.feature.call

import com.einrum.core.ai.AiService
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

class CallViewModel(
    private val meetingId: String,
    private val aiService: AiService
) : ViewModel() {

    private val _state = MutableStateFlow(CallState(meetingId = meetingId))
    val state: StateFlow<CallState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CallEffect>()
    val effects: SharedFlow<CallEffect> = _effects.asSharedFlow()

    init {
        // Initialize call and streams
        _state.update { it.copy(localStream = VideoStream.Local) }
        
        // Mock participants
        _state.update {
            it.copy(
                participants = listOf(
                    Participant("1", "Alex", true, true, VideoStream.Remote("1")),
                    Participant("2", "Jordan", false, true, null)
                )
            )
        }
    }

    fun onIntent(intent: CallIntent) {
        when (intent) {
            CallIntent.ToggleMic -> toggleMic()
            CallIntent.ToggleCamera -> toggleCamera()
            CallIntent.ToggleBlur -> toggleBlur()
            CallIntent.LeaveCall -> leaveCall()
        }
    }

    private fun toggleMic() {
        _state.update { it.copy(isMicEnabled = !it.isMicEnabled) }
    }

    private fun toggleCamera() {
        _state.update { it.copy(isCameraEnabled = !it.isCameraEnabled) }
    }

    private fun toggleBlur() {
        _state.update { it.copy(isBlurEnabled = !it.isBlurEnabled) }
    }

    private fun leaveCall() {
        viewModelScope.launch {
            _effects.emit(CallEffect.NavigateBack)
        }
    }
}
