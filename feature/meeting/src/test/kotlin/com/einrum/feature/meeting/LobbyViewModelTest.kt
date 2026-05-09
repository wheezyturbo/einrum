package com.einrum.feature.meeting

import com.einrum.core.network.RoomService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val roomService: RoomService = mock()
    private lateinit var viewModel: LobbyViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LobbyViewModel(roomService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when updating room id, state is updated`() = runTest {
        viewModel.onIntent(LobbyIntent.UpdateRoomId("123456"))
        assertEquals("123456", viewModel.state.value.roomId)
    }

    @Test
    fun `when joining room succeeds, loading state clears`() = runTest {
        whenever(roomService.joinRoom("123456")).thenReturn(true)
        viewModel.onIntent(LobbyIntent.UpdateGuestName("Alex"))
        viewModel.onIntent(LobbyIntent.UpdateRoomId("123456"))
        viewModel.onIntent(LobbyIntent.JoinRoom)
        
        advanceUntilIdle()
        
        // Effects are collected in the UI, but we can test state or use a Turbine-like approach
        // For simplicity, checking state
        assertEquals(false, viewModel.state.value.isJoining)
    }
}
