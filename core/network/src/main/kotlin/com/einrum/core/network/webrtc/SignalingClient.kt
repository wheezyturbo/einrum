package com.einrum.core.network.webrtc

import kotlinx.coroutines.flow.SharedFlow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

sealed interface SignalingEvent {
    data class OfferReceived(val sdp: SessionDescription) : SignalingEvent
    data class AnswerReceived(val sdp: SessionDescription) : SignalingEvent
    data class IceCandidateReceived(val candidate: IceCandidate) : SignalingEvent
}

interface SignalingClient {
    val signalingEvents: SharedFlow<SignalingEvent>

    suspend fun joinMeeting(meetingId: String)
    suspend fun sendOffer(offer: SessionDescription)
    suspend fun sendAnswer(answer: SessionDescription)
    suspend fun sendIceCandidate(candidate: IceCandidate, isLocalUser: Boolean)
    suspend fun leaveMeeting()
}