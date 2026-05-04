package com.aura.feature.meeting

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val meetingModule = module {
    viewModelOf(::LobbyViewModel)
}
