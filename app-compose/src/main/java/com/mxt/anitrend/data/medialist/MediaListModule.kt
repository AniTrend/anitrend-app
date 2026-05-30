package com.mxt.anitrend.data.medialist

import com.mxt.anitrend.ui.medialist.MediaListEditViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val medialistModule = module {
    single<MediaListRepository> { ApolloMediaListRepository(get()) }
    viewModel { params -> MediaListEditViewModel(params.get(), get()) }
}
