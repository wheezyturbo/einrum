package com.einrum.feature.meeting

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    onNavigateToCall: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LobbyEffect.NavigateToCall -> onNavigateToCall(effect.meetingId)
                is LobbyEffect.ShowError -> { /* Handle error via Snackbar or Toast */ }
            }
        }
    }

    LobbyContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LobbyContent(
    state: LobbyState,
    onIntent: (LobbyIntent) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Einrúm Video", style = MaterialTheme.typography.headlineMedium) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Premium video meetings. Now free for everyone.",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = state.guestName,
                onValueChange = { onIntent(LobbyIntent.UpdateGuestName(it)) },
                label = { Text("Your Display Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isJoining
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.meetingId,
                onValueChange = { onIntent(LobbyIntent.UpdateMeetingId(it)) },
                label = { Text("Enter meeting code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isJoining
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onIntent(LobbyIntent.JoinMeeting) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isJoining && state.meetingId.isNotBlank() && state.guestName.isNotBlank()
            ) {
                if (state.isJoining) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Join Meeting")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { onIntent(LobbyIntent.CreateMeeting) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isJoining
            ) {
                Text("New Meeting")
            }
        }
    }
}
