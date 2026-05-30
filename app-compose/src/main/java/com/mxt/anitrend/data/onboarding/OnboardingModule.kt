package com.mxt.anitrend.data.onboarding

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val onboardingModule = module {
    single { OnboardingPreferences(androidApplication()) }
}
