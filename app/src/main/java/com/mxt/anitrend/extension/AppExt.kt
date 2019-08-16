package com.mxt.anitrend.extension

import android.content.Context
import androidx.annotation.StringRes
import org.koin.core.context.GlobalContext

val appContext by lazy {
    GlobalContext.get().koin.get<Context>()
}

fun getString(@StringRes text: Int) =
        appContext.getString(text)

fun getString(@StringRes text: Int, vararg values: String) =
        appContext.getString(text, *values)