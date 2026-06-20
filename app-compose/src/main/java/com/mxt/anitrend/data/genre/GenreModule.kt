package com.mxt.anitrend.data.genre

import com.mxt.anitrend.ui.genre.GenreListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val genreModule = module {
    single<GenreRepository> { MockGenreRepository() }
    viewModel { GenreListViewModel(get()) }
}
