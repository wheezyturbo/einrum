package com.einrum.core.network.webrtc

import kotlinx.coroutines.flow.StateFlow
import org.webrtc.VideoTrack

interface WebRtcClient {
    val localVideoTrack: StateFlow<VideoTrack?>
    val remoteVideoTracks: StateFlow<Map<String, VideoTrack>>

    suspend fun initialize(meetingId: String)
    suspend fun toggleMic(enabled: Boolean)
    suspend fun toggleCamera(enabled: Boolean)
    suspend fun disconnect()
}
