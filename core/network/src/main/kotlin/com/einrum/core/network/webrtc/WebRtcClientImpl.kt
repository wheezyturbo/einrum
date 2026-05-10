package com.einrum.core.network.webrtc

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.*

class WebRtcClientImpl(
    private val context: Context,
    private val eglBaseContext: EglBase.Context,
    private val signalingClient: SignalingClient
) : WebRtcClient {

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    override val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTracks = MutableStateFlow<Map<String, VideoTrack>>(emptyMap())
    override val remoteVideoTracks: StateFlow<Map<String, VideoTrack>> = _remoteVideoTracks.asStateFlow()

    private var isMicEnabled = true
    private var isCameraEnabled = true

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var localVideoSource: VideoSource? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrackRef: VideoTrack? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun initialize(meetingId: String) {
        // 1. Initialize WebRTC Global Context
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        // 2. Build PeerConnectionFactory with Hardware Acceleration
        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .createPeerConnectionFactory()

        // 3. Initialize Audio
        localAudioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("EinrumAudio0", localAudioSource)
        localAudioTrack?.setEnabled(isMicEnabled)

        // 4. Initialize Camera (Video)
        videoCapturer = createCameraCapturer(context)
        if (videoCapturer != null && peerConnectionFactory != null) {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
            localVideoSource = peerConnectionFactory!!.createVideoSource(videoCapturer!!.isScreencast)
            
            videoCapturer!!.initialize(surfaceTextureHelper, context, localVideoSource!!.capturerObserver)
            videoCapturer!!.startCapture(1280, 720, 30) // HD capture @ 30fps

            localVideoTrackRef = peerConnectionFactory!!.createVideoTrack("EinrumVideo0", localVideoSource)
            localVideoTrackRef?.setEnabled(isCameraEnabled)
            
            _localVideoTrack.value = localVideoTrackRef
        }

        // 5. Create PeerConnection
        val rtcConfig = PeerConnection.RTCConfiguration(
            listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        )
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, peerConnectionObserver)

        localAudioTrack?.let { peerConnection?.addTrack(it, listOf("EinrumStream")) }
        localVideoTrackRef?.let { peerConnection?.addTrack(it, listOf("EinrumStream")) }

        // 6. Wire Signaling
        signalingClient.joinMeeting(meetingId)
        
        scope.launch {
            signalingClient.signalingEvents.collect { event ->
                when (event) {
                    is SignalingEvent.OfferReceived -> {
                        peerConnection?.setRemoteDescription(SimpleSdpObserver(), event.sdp)
                        peerConnection?.createAnswer(object : SimpleSdpObserver() {
                            override fun onCreateSuccess(sdp: SessionDescription) {
                                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                                scope.launch { signalingClient.sendAnswer(sdp) }
                            }
                        }, MediaConstraints())
                    }
                    is SignalingEvent.AnswerReceived -> {
                        peerConnection?.setRemoteDescription(SimpleSdpObserver(), event.sdp)
                    }
                    is SignalingEvent.IceCandidateReceived -> {
                        peerConnection?.addIceCandidate(event.candidate)
                    }
                }
            }
        }

        // As the caller, create offer
        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                scope.launch { signalingClient.sendOffer(sdp) }
            }
        }, MediaConstraints())
    }

    private val peerConnectionObserver = object : PeerConnection.Observer {
        override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
        override fun onIceConnectionReceivingChange(receiving: Boolean) {}
        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
        
        override fun onIceCandidate(candidate: IceCandidate) {
            scope.launch { signalingClient.sendIceCandidate(candidate, true) }
        }
        override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
        
        override fun onAddStream(stream: MediaStream) {
            if (stream.videoTracks.isNotEmpty()) {
                val track = stream.videoTracks.first()
                val currentTracks = _remoteVideoTracks.value.toMutableMap()
                currentTracks[stream.id] = track
                _remoteVideoTracks.value = currentTracks
            }
        }
        override fun onRemoveStream(stream: MediaStream) {
            val currentTracks = _remoteVideoTracks.value.toMutableMap()
            currentTracks.remove(stream.id)
            _remoteVideoTracks.value = currentTracks
        }
        override fun onDataChannel(channel: DataChannel?) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
    }

    private fun createCameraCapturer(context: Context): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames

        // Prefer Front Facing Camera
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        // Fallback to Back Facing
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        return null
    }

    override suspend fun toggleMic(enabled: Boolean) {
        isMicEnabled = enabled
        localAudioTrack?.setEnabled(enabled)
    }

    override suspend fun toggleCamera(enabled: Boolean) {
        isCameraEnabled = enabled
        localVideoTrackRef?.setEnabled(enabled)
    }

    override suspend fun disconnect() {
        try {
            scope.launch { signalingClient.leaveMeeting() }
            
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null
            
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            surfaceTextureHelper?.dispose()
            
            localVideoSource?.dispose()
            localAudioSource?.dispose()
            peerConnectionFactory?.dispose()
            
            _localVideoTrack.value = null
            _remoteVideoTracks.value = emptyMap()
        } catch (e: Exception) {}
    }
}

open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) {}
}