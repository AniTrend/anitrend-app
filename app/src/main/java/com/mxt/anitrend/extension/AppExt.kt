package com.mxt.anitrend.extension

import android.content.Context
import androidx.annotation.StringRes
import com.mxt.anitrend.App
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import org.koin.core.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.definition.DefinitionFactory
import org.koin.core.definition.Options
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

val appContext by lazy {
    GlobalContext.get().koin.get<Context>()
}

fun getString(@StringRes text: Int) =
        appContext.getString(text)

fun getString(@StringRes text: Int, vararg values: String) =
        appContext.getString(text, *values)