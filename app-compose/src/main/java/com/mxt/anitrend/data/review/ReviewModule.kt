package com.mxt.anitrend.data.review

import com.mxt.anitrend.ui.review.ReviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val reviewModule = module {
    single<ReviewRepository> { MockReviewRepository() }
    viewModel { ReviewViewModel(get()) }
}
