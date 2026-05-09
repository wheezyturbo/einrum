package com.einrum.feature.meeting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    onNavigateToCall: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LobbyEffect.NavigateToCall -> onNavigateToCall(effect.roomId)
                is LobbyEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LobbyContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LobbyContent(
    state: LobbyState,
    snackbarHostState: SnackbarHostState,
    onIntent: (LobbyIntent) -> Unit
) {
    val bg = Brush.linearGradient(
        listOf(
            Color(0xFF0B132B),
            Color(0xFF1C2541),
            Color(0xFF3A506B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Einrum Rooms",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create a room or join an existing room for audio, video, and screen sharing.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFD9E2EC)
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = state.guestName,
                                onValueChange = { onIntent(LobbyIntent.UpdateGuestName(it)) },
                                label = { Text("Display Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isJoining
                            )
                            OutlinedTextField(
                                value = state.roomId,
                                onValueChange = { onIntent(LobbyIntent.UpdateRoomId(it)) },
                                label = { Text("Room ID") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isJoining
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onIntent(LobbyIntent.JoinRoom) },
                                    enabled = !state.isJoining && state.guestName.isNotBlank() && state.roomId.length == 6,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (state.isJoining) {
                                        CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("Join Room")
                                    }
                                }
                                OutlinedButton(
                                    onClick = { onIntent(LobbyIntent.CreateRoom) },
                                    enabled = !state.isJoining,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Create Room")
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Next: wire LiveKit/Daily/Agora SDK to the RTC service for real calls.",
                        color = Color(0xFFD9E2EC),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
