package com.einrum.feature.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

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
        // Privacy Fallback / Loading
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF04070E)), contentAlignment = Alignment.Center) {
            Text("Requesting Privacy Permissions...", color = Color.White)
        }
    }
}

@Composable
private fun CallContent(
    state: CallState,
    onIntent: (CallIntent) -> Unit
) {
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF04070E), Color(0xFF101726), Color(0xFF1D2B45))
    )
    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Text(
            text = "Room ${state.meetingId}",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        // Participant Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.participants) { participant ->
                ParticipantItem(participant)
            }
        }

        // Local Stream PiP
        if (state.isCameraEnabled && state.localStream != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 120.dp)
                    .size(100.dp, 150.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Color.Black,
                tonalElevation = 12.dp,
                shadowElevation = 8.dp
            ) {
                VideoRenderer(
                    videoTrack = state.localStream,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Call Controls
        CallControls(
            isMicEnabled = state.isMicEnabled,
            isCameraEnabled = state.isCameraEnabled,
            isBlurEnabled = state.isBlurEnabled,
            onIntent = onIntent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun ParticipantItem(participant: Participant) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp)),
        color = Color(0x332A3446)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (participant.isCameraEnabled && participant.videoStream != null) {
                VideoRenderer(
                    videoTrack = participant.videoStream,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (participant.isCameraEnabled) {
                // Connecting / Loading placeholder
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF3A4B68)))
            } else {
                Text(
                    text = participant.name.take(1),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
            }
            
            Text(
                text = participant.name,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
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
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onIntent(CallIntent.ToggleMic) }) {
                Icon(
                    imageVector = if (isMicEnabled) Icons.Filled.Mic else Icons.Filled.MicOff,
                    contentDescription = "Toggle Mic",
                    tint = if (isMicEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
            }

            IconButton(onClick = { onIntent(CallIntent.ToggleCamera) }) {
                Icon(
                    imageVector = if (isCameraEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                    contentDescription = "Toggle Camera",
                    tint = if (isCameraEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
            }

            IconButton(onClick = { onIntent(CallIntent.ToggleBlur) }) {
                Icon(
                    imageVector = Icons.Filled.BlurOn,
                    contentDescription = "Toggle Blur",
                    tint = if (isBlurEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = { onIntent(CallIntent.LeaveCall) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Leave Call",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}
