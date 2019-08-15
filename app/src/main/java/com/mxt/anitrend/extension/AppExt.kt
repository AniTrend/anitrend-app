package com.mxt.anitrend.extension

import android.content.Context
import org.koin.core.context.GlobalContext

val appContext by lazy {
    GlobalContext.get().koin.get<Context>()
}