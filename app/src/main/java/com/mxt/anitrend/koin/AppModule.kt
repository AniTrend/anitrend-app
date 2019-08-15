package com.mxt.anitrend.koin

import com.mxt.anitrend.model.entity.MyObjectBox
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.ApplicationPref
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.dsl.module

object AppModule : KoinComponent {
    val appModule = module {
        single {
            MyObjectBox.builder()
                .androidContext(androidContext())
                .build()
        }
        factory {
            ApplicationPref(androidContext())
        }
        factory {
            BasePresenter(androidContext())
        }
        factory {
            MediaPresenter(androidContext())
        }
    }

    @JvmStatic
    fun <T : Any> get(`class`: Class<T>): T = getKoin().get(
            `class`::class,
            null,
            null
    )
}