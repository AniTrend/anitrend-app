package com.mxt.anitrend.extension

import com.mxt.anitrend.presenter.widget.WidgetPresenter
import org.koin.core.KoinComponent
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.definition.DefinitionFactory
import org.koin.core.definition.Options
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope


/**
 * Declare a Factory definition for StateMachine generic type
 * @param override
 * @param definition - definition function
 */
inline fun <reified S> Module.widgetPresenterFactory(
        override: Boolean = false,
        noinline definition: Definition<WidgetPresenter<S>>
): BeanDefinition<WidgetPresenter<S>> {
    val beanDefinition = DefinitionFactory.createFactory(
            named<S>(), definition = definition
    )
    declareDefinition(beanDefinition, Options(override = override))
    return beanDefinition
}

/**
 * Gets instance of WidgetPresenter for matching generic type
 */
inline fun <reified T> Scope.getWdigetPresenter(
        noinline parameters: ParametersDefinition? = null
): WidgetPresenter<T> {
    return getKoin().get(WidgetPresenter::class, named<T>(), parameters)
            ?: error("$this is not registered - Koin is null")
}

object KoinExt : KoinComponent {

    /**
     * Helper to retrieve dependencies by class definition
     *
     * @param `class` registered class in koin modules
     */
    @JvmStatic
    fun <T : Any> get(`class`: Class<T>): T {
        return getKoin().get(`class`.kotlin, null, null)
    }
}