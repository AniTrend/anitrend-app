package com.mxt.anitrend.data.watchlist

import com.mxt.anitrend.ui.watchlist.WatchListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val watchListModule = module {
    single<WatchListRepository> { ApolloWatchListRepository(get()) }
    viewModel { WatchListViewModel(get()) }
}
