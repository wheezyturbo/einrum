package com.einrum.feature.meeting

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val meetingModule = module {
    viewModelOf(::LobbyViewModel)
}
