/*
 * Copyright (C) 2020 AniTrend
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.IdRes
import androidx.fragment.app.*
import androidx.lifecycle.ViewModel
import com.mxt.anitrend.extension.fragmentManager
import com.mxt.anitrend.ui.model.FragmentItem
import org.koin.androidx.fragment.android.KoinFragmentFactory
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.component.KoinScopeComponent
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/**
 * Get given dependency, from either local scope or global scope
 *
 * @param qualifier - bean qualifier / optional
 * @param parameters - injection parameters
 */
inline fun <reified T : Any> KoinScopeComponent.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = runCatching {
    scope.get<T>(qualifier, parameters)
}.getOrElse {
    getKoin().get(qualifier, parameters)
}

/**
 * Inject lazily, by both local and global scope
 *
 * @param qualifier - bean qualifier / optional
 * @param parameters - injection parameters
 */
inline fun <reified T : Any> KoinScopeComponent.inject(
    qualifier: Qualifier? = null,
    lazyMode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    noinline parameters: ParametersDefinition? = null,
) = lazy(lazyMode) { get<T>(qualifier, parameters) }

/**
 * Checks for existing fragment in [FragmentManager], if one exists that is used otherwise
 * a new instance is created.
 *
 * @param contentFrame Resource Id of the view to inflate the fragment into
 * @param context Any lifecycle driver context
 * @param fragmentTransaction Optional transaction to run while committing fragment
 *
 * @return [String] tag of the fragment
 *
 * @see androidx.fragment.app.commit
 *
 * @throws NotImplementedError if [context] cannot resolve [FragmentManager]
 */
inline fun FragmentItem<*>.commit(
    @IdRes contentFrame: Int,
    context: Context,
    fragmentTransaction: FragmentTransaction.() -> Unit = {},
): String {
    val fragmentTag = tag()
    val fragmentManager = context.fragmentManager()
    val backStack = fragmentManager.findFragmentByTag(fragmentTag)

    fragmentManager.commit {
        fragmentTransaction()
        backStack?.let {
            replace(contentFrame, it, fragmentTag)
        } ?: replace(contentFrame, fragment, parameter, fragmentTag)
    }
    return fragmentTag
}

/**
 * Checks for existing fragment in [FragmentManager], if one exists that is used otherwise
 * a new instance is created.
 *
 * @return tag of the fragment
 *
 * @see androidx.fragment.app.commit
 */
inline fun <T : Fragment> FragmentItem<T>.commit(
    contentFrame: View,
    context: Context,
    action: FragmentTransaction.() -> Unit = {},
) = commit(contentFrame.id, context, action)

/**
 * Uses fragment factory to instantiate a fragment class definition
 *
 * @param classDefinition [Class] with an out variance of type [Fragment]
 */
inline fun <reified T : Fragment> FragmentActivity.createFragment(classDefinition: Class<out T>): T {
    val qualifier = classDefinition.name
    val factory = supportFragmentManager.fragmentFactory
    require(factory is KoinFragmentFactory) {
        """Fragment factory for $this is $factory instead of `KoinFragmentFactory`.
            |Did you forget to call setupKoinFragmentFactory before onCreate?
        """.trimMargin()
    }
    return factory.instantiate(classLoader, qualifier) as T
}

/**
 * Retrieves or creates a new fragment using the given [FragmentItem]
 *
 * @param activity Calling activity
 *
 * @see runIfActivityContext
 */
inline fun <reified T : Fragment> FragmentItem<T>.fragmentByTagOrNew(activity: FragmentActivity): T {
    val fragmentTag = tag()
    val fragmentManager = activity.supportFragmentManager
    val fragment =
        fragmentManager.findFragmentByTag(fragmentTag) as? T
            ?: activity.createFragment(fragment)
    fragment.arguments = parameter
    return fragment
}

/**
 * Retrieves or creates a new fragment using the given [tag]
 *
 * @param classDefinition A [Class] with an out variance of type [Fragment]
 * @param tag Tag to identity the fragment
 * @param lazyMode [LazyThreadSafetyMode] to use
 */
inline fun <reified T : Fragment> FragmentActivity.fragmentByTagOrNew(
    fragmentItem: FragmentItem<T>,
    lazyMode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
) = lazy(lazyMode) {
    fragmentItem.fragmentByTagOrNew(this)
}

/**
 * Resolve view models from within views
 */
inline fun <reified T : ViewModel> ViewGroup.viewModel(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    when (val component = context) {
        is ComponentActivity -> {
            component.getViewModel(
                qualifier = qualifier,
                extrasProducer = null,
                parameters = parameters,
            )
        }
        else -> throw NotImplementedError("Not sure how to handle view model retrieval for $this")
    }
}

/**
 * Resolve view models from within action providers, by default this looks view models
 * using the parent activity as the owner. You should assure that any fragment also use
 * `sharedViewModel` to avoid getting a mismatch between viewModels
 *
 * @param qualifier Custom qualifier to lookup
 * @param owner ViewModel owner, defaults to parent activity
 * @param parameters
 */
inline fun <reified T : ViewModel> AbstractActionProvider.sharedViewModel(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    when (val component = context) {
        is ComponentActivity -> {
            component.getViewModel(
                qualifier = qualifier,
                extrasProducer = null,
                parameters = parameters,
            )
        }
        else -> throw NotImplementedError("Not sure how to handle view model retrieval for $this")
    }
}
