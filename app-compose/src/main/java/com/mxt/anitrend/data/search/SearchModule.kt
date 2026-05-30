package com.mxt.anitrend.data.search

import com.mxt.anitrend.ui.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    single<SearchRepository> { ApolloSearchRepository(get()) }
    viewModel { SearchViewModel(get()) }
}
