package com.mxt.anitrend.data.media

import com.mxt.anitrend.ui.detail.MediaDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    single<MediaRepository> { ApolloMediaRepository(get()) }
    viewModel { params -> MediaDetailViewModel(get(), params.get()) }
}
