package com.einrum.core.network.webrtc

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.webrtc.VideoTrack

class WebRtcClientImpl(
    private val context: Context
) : WebRtcClient {

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    override val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTracks = MutableStateFlow<Map<String, VideoTrack>>(emptyMap())
    override val remoteVideoTracks: StateFlow<Map<String, VideoTrack>> = _remoteVideoTracks.asStateFlow()

    private var isMicEnabled = true
    private var isCameraEnabled = true

    override suspend fun initialize() {
        // TODO: Initialize PeerConnectionFactory, capture camera/mic, and start signaling.
        // For the scope of UI and privacy architecture, we ensure permissions are checked
        // by the UI before this is called.
    }

    override suspend fun toggleMic(enabled: Boolean) {
        isMicEnabled = enabled
        // TODO: Iterate over local audio tracks and set enabled state.
    }

    override suspend fun toggleCamera(enabled: Boolean) {
        isCameraEnabled = enabled
        // TODO: Iterate over local video tracks and set enabled state, or stop camera capture.
    }

    override suspend fun disconnect() {
        // TODO: Release resources securely (close PeerConnection, stop capturers, dispose factory).
    }
}
