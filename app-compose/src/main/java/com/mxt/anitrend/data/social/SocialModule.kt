package com.mxt.anitrend.data.social

import com.mxt.anitrend.ui.activitydetail.ActivityDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val socialModule = module {
    single<ActivityRepository> { ApolloActivityRepository(get()) }
    viewModel { params -> ActivityDetailViewModel(get(), params.get()) }
}
