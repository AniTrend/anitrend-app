package com.mxt.anitrend.data.studio

import com.mxt.anitrend.ui.studio.StudioDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val studioModule = module {
    single<StudioRepository> { ApolloStudioRepository(get()) }
    viewModel { params -> StudioDetailViewModel(get(), params.get()) }
}
