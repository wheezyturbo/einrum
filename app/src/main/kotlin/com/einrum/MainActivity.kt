package com.einrum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.einrum.feature.meeting.LobbyScreen
import com.einrum.feature.meeting.LobbyViewModel
import com.einrum.feature.call.CallScreen
import com.einrum.feature.call.CallViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                var currentMeetingId by remember { mutableStateOf<String?>(null) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = currentMeetingId,
                        label = "ScreenTransition"
                    ) { meetingId ->
                        if (meetingId == null) {
                            val lobbyViewModel: LobbyViewModel = koinViewModel()
                            LobbyScreen(
                                viewModel = lobbyViewModel,
                                onNavigateToCall = { id -> currentMeetingId = id }
                            )
                        } else {
                            val callViewModel: CallViewModel = koinViewModel { parametersOf(meetingId) }
                            CallScreen(
                                viewModel = callViewModel,
                                onNavigateBack = { currentMeetingId = null }
                            )
                        }
                    }
                }
            }
        }
    }
}
