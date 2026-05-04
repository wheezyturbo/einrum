package com.einrum.feature.call

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val callModule = module {
    viewModel { (meetingId: String) -> CallViewModel(meetingId, get()) }
}
