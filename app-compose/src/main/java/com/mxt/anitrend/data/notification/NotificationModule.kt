package com.mxt.anitrend.data.notification

import com.mxt.anitrend.ui.notification.NotificationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val notificationModule = module {
    single<NotificationRepository> { ApolloNotificationRepository(get()) }
    viewModel { NotificationViewModel(get()) }
}
