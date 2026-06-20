package com.mxt.anitrend.data.api

import org.koin.dsl.module

val apiModule = module {
    single { ApiClient.apolloClient }
}
