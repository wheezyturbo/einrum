package com.einrum.feature.call

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val callModule = module {
    viewModelOf(::CallViewModel)
}
