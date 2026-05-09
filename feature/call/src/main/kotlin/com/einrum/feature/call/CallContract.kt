package com.einrum.feature.call

import androidx.compose.runtime.Immutable
import org.webrtc.VideoTrack

@Immutable
data class CallState(
    val meetingId: String = "",
    val isMicEnabled: Boolean = true,
    val isCameraEnabled: Boolean = true,
    val isBlurEnabled: Boolean = false,
    val participants: List<Participant> = emptyList(),
    val localStream: VideoTrack? = null
)

@Immutable
data class Participant(
    val id: String,
    val name: String,
    val isCameraEnabled: Boolean,
    val isMicEnabled: Boolean,
    val videoStream: VideoTrack? = null
)

sealed interface CallIntent {
    data object ToggleMic : CallIntent
    data object ToggleCamera : CallIntent
    data object ToggleBlur : CallIntent
    data object LeaveCall : CallIntent
}

sealed interface CallEffect {
    data object NavigateBack : CallEffect
    data class ShowMessage(val text: String) : CallEffect
}
