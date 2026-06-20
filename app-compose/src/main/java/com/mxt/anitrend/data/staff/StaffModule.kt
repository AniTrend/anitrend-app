package com.mxt.anitrend.data.staff

import com.mxt.anitrend.ui.staff.StaffDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val staffModule = module {
    single<StaffRepository> { ApolloStaffRepository(get()) }
    viewModel { params -> StaffDetailViewModel(get(), params.get()) }
}
