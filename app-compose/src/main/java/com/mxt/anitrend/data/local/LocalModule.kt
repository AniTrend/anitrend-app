package com.mxt.anitrend.data.local

import android.content.Context
import com.mxt.anitrend.data.local.dao.UserPreferencesDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single { AppDatabase.create(androidContext()) }
    single<UserPreferencesDao> { get<AppDatabase>().userPreferencesDao() }
}
