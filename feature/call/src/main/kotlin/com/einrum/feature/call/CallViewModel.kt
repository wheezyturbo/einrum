package com.einrum.feature.call

import com.einrum.core.ai.AiService
import com.einrum.core.network.webrtc.WebRtcClient
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
    private val aiService: AiService,
    private val webRtcClient: WebRtcClient
) : ViewModel() {

    private val _state = MutableStateFlow(CallState(meetingId = meetingId))
    val state: StateFlow<CallState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CallEffect>()
    val effects: SharedFlow<CallEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            webRtcClient.initialize()
        }

        viewModelScope.launch {
            webRtcClient.localVideoTrack.collect { track ->
                _state.update { it.copy(localStream = track) }
            }
        }

        viewModelScope.launch {
            webRtcClient.remoteVideoTracks.collect { tracks ->
                val newParticipants = tracks.map { (id, track) ->
                    Participant(id, "User $id", isCameraEnabled = true, isMicEnabled = true, videoStream = track)
                }
                _state.update { it.copy(participants = newParticipants) }
            }
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
        val nextState = !state.value.isMicEnabled
        _state.update { it.copy(isMicEnabled = nextState) }
        viewModelScope.launch {
            webRtcClient.toggleMic(nextState)
        }
    }

    private fun toggleCamera() {
        val nextState = !state.value.isCameraEnabled
        _state.update { it.copy(isCameraEnabled = nextState) }
        viewModelScope.launch {
            webRtcClient.toggleCamera(nextState)
        }
    }

    private fun toggleBlur() {
        _state.update { it.copy(isBlurEnabled = !it.isBlurEnabled) }
    }

    private fun leaveCall() {
        viewModelScope.launch {
            webRtcClient.disconnect()
            _effects.emit(CallEffect.NavigateBack)
        }
    }
}
