package com.mxt.anitrend.data.character

import com.mxt.anitrend.ui.character.CharacterDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val characterModule = module {
    single<CharacterRepository> { ApolloCharacterRepository(get()) }
    viewModel { params -> CharacterDetailViewModel(get(), params.get()) }
}
