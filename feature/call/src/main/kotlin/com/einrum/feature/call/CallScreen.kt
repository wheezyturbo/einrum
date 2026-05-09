package com.einrum.feature.call

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    viewModel: CallViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions[Manifest.permission.CAMERA] == true &&
                         permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
        viewModel.effects.collect { effect ->
            when (effect) {
                CallEffect.NavigateBack -> onNavigateBack()
                is CallEffect.ShowMessage -> { /* Show Snackbar */ }
            }
        }
    }

    if (hasPermissions) {
        CallContent(
            state = state,
            onIntent = viewModel::onIntent
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Requesting Privacy Permissions...", color = Color.White)
        }
    }
}

@Composable
private fun CallContent(
    state: CallState,
    onIntent: (CallIntent) -> Unit
) {
    var showControls by remember { mutableStateOf(true) }

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showControls = !showControls }
    ) {
        // Main Full-Screen Video (Remote if exists, else Local)
        val mainParticipant = state.participants.firstOrNull()
        if (mainParticipant != null && mainParticipant.videoStream != null) {
            VideoRenderer(
                videoTrack = mainParticipant.videoStream,
                modifier = Modifier.fillMaxSize(),
                mirror = false
            )
        } else if (state.isCameraEnabled && state.localStream != null) {
            VideoRenderer(
                videoTrack = state.localStream,
                modifier = Modifier.fillMaxSize(),
                mirror = true
            )
        } else {
            // Placeholder when no video
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)), contentAlignment = Alignment.Center) {
                Text("Camera Off", color = Color.Gray, style = MaterialTheme.typography.titleLarge)
            }
        }

        // Room Info (Top Left)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Room: ${state.meetingId}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }

        // PiP Local Stream (Bottom Right)
        if (mainParticipant != null && state.isCameraEnabled && state.localStream != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = if (showControls) 110.dp else 16.dp)
                    .size(100.dp, 150.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Color.DarkGray,
                shadowElevation = 8.dp
            ) {
                VideoRenderer(
                    videoTrack = state.localStream,
                    modifier = Modifier.fillMaxSize(),
                    mirror = true
                )
            }
        }

        // Glassmorphic Call Controls (Bottom Center)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            CallControls(
                isMicEnabled = state.isMicEnabled,
                isCameraEnabled = state.isCameraEnabled,
                isBlurEnabled = state.isBlurEnabled,
                onIntent = onIntent
            )
        }
    }
}

@Composable
private fun CallControls(
    isMicEnabled: Boolean,
    isCameraEnabled: Boolean,
    isBlurEnabled: Boolean,
    onIntent: (CallIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color(0x66000000), // Translucent black
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mic
            ControlIconButton(
                isActive = isMicEnabled,
                activeIcon = Icons.Filled.Mic,
                inactiveIcon = Icons.Filled.MicOff,
                onClick = { onIntent(CallIntent.ToggleMic) },
                activeColor = Color.White,
                inactiveColor = Color(0xFFE53935)
            )

            // Camera
            ControlIconButton(
                isActive = isCameraEnabled,
                activeIcon = Icons.Filled.Videocam,
                inactiveIcon = Icons.Filled.VideocamOff,
                onClick = { onIntent(CallIntent.ToggleCamera) },
                activeColor = Color.White,
                inactiveColor = Color(0xFFE53935)
            )

            // Leave Call
            IconButton(
                onClick = { onIntent(CallIntent.LeaveCall) },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFE53935), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Leave Call",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ControlIconButton(
    isActive: Boolean,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    activeColor: Color,
    inactiveColor: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(if (isActive) Color(0x33FFFFFF) else Color.White, CircleShape)
    ) {
        Icon(
            imageVector = if (isActive) activeIcon else inactiveIcon,
            contentDescription = null,
            tint = if (isActive) activeColor else inactiveColor
        )
    }
}
