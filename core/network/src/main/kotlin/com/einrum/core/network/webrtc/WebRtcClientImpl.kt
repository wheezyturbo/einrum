package com.einrum.core.network.webrtc

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.webrtc.*

class WebRtcClientImpl(
    private val context: Context,
    private val eglBaseContext: EglBase.Context
) : WebRtcClient {

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    override val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTracks = MutableStateFlow<Map<String, VideoTrack>>(emptyMap())
    override val remoteVideoTracks: StateFlow<Map<String, VideoTrack>> = _remoteVideoTracks.asStateFlow()

    private var isMicEnabled = true
    private var isCameraEnabled = true

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var localVideoSource: VideoSource? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrackRef: VideoTrack? = null

    override suspend fun initialize() {
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
            
            // Expose track to UI
            _localVideoTrack.value = localVideoTrackRef
        }
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
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            surfaceTextureHelper?.dispose()
            
            localVideoSource?.dispose()
            localAudioSource?.dispose()
            peerConnectionFactory?.dispose()
            
            _localVideoTrack.value = null
            _remoteVideoTracks.value = emptyMap()
        } catch (e: Exception) {
            // Log silently
        }
    }
}
