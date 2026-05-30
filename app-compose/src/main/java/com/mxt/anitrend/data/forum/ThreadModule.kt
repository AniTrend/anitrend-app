package com.mxt.anitrend.data.forum

import com.mxt.anitrend.ui.forum.ThreadsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val threadModule = module {
    single<ThreadRepository> { ApolloThreadRepository(get()) }
    viewModel { ThreadsViewModel(get()) }
}
