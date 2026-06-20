package com.mxt.anitrend.data.feed

import com.mxt.anitrend.ui.feed.FeedViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val feedModule = module {
    single<FeedRepository> { ApolloFeedRepository(get()) }
    viewModel { FeedViewModel(get()) }
}
