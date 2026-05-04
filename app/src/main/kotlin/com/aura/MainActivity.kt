package com.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aura.feature.meeting.LobbyScreen
import com.aura.feature.meeting.LobbyViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val lobbyViewModel: LobbyViewModel = koinViewModel()
                    LobbyScreen(
                        viewModel = lobbyViewModel,
                        onNavigateToCall = { meetingId ->
                            // TODO: Handle navigation to call screen
                        }
                    )
                }
            }
        }
    }
}
