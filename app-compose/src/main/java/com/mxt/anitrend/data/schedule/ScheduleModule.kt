package com.mxt.anitrend.data.schedule

import com.mxt.anitrend.ui.schedule.AiringViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val scheduleModule = module {
    single<AiringRepository> { MockAiringRepository() }
    viewModel { AiringViewModel(get()) }
}
