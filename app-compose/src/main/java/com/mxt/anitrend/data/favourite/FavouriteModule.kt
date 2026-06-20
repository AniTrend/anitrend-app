package com.mxt.anitrend.data.favourite

import com.mxt.anitrend.ui.favourite.FavouritesViewModel
import com.mxt.anitrend.ui.favourite.UserFavouritesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val favouriteModule = module {
    single<FavouriteRepository> { ApolloFavouriteRepository(get()) }
    viewModel { FavouritesViewModel(get()) }
    viewModel { UserFavouritesViewModel(get()) }
}
