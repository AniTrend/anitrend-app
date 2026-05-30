package com.mxt.anitrend.data.profile

import com.mxt.anitrend.ui.profile.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> { ApolloProfileRepository(get()) }
    viewModel { ProfileViewModel(get()) }
}
