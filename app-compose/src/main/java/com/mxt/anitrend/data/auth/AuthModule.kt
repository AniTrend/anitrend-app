package com.mxt.anitrend.data.auth

import org.koin.dsl.module

val authModule = module {
    single { AuthRepository(get()) }
}
