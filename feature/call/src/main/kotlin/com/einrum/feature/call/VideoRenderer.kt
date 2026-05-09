package com.einrum.feature.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.koin.compose.koinInject
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Composable
fun VideoRenderer(
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    scalingType: RendererCommon.ScalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL,
    mirror: Boolean = true
) {
    val context = LocalContext.current
    val eglBase: EglBase = koinInject()
    
    val view = remember {
        SurfaceViewRenderer(context).apply {
            init(eglBase.eglBaseContext, null)
            setScalingType(scalingType)
            setEnableHardwareScaler(true)
            setMirror(mirror)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { view },
        update = { v ->
            videoTrack?.addSink(v)
        }
    )

    DisposableEffect(videoTrack) {
        onDispose {
            videoTrack?.removeSink(view)
        }
    }
}
