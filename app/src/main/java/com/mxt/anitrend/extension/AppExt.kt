package com.mxt.anitrend.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import org.koin.core.context.GlobalContext
import timber.log.Timber

val appContext by lazy {
    GlobalContext.get().koin.get<Context>()
}

fun getString(@StringRes text: Int) =
        appContext.getString(text)

fun getString(@StringRes text: Int, vararg values: String) =
        appContext.getString(text, *values)


/**
 * Request to hide the soft input window from the context of the window
 * that is currently accepting input. This should be called as a result
 * of the user doing some actually than fairly explicitly requests to
 * have the input window hidden.
 */
fun FragmentActivity?.hideKeyboard() = this?.apply {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(window.decorView.windowToken, 0)
}

/**
 * Starts a shared transition of activities connected by views
 *
 * @param target The view from the calling activity with transition name
 * @param data Intent with bundle and or activity to start
 */
fun FragmentActivity.startSharedTransitionActivity(target : View, data : Intent) {
    val participants = Pair(target, ViewCompat.getTransitionName(target))
    val transitionActivityOptions = ActivityOptionsCompat
            .makeSceneTransitionAnimation(this, participants)
    ActivityCompat.startActivity(this, data, transitionActivityOptions.toBundle())
}


/**
 * Compares if this State is greater or equal to the given [Lifecycle.State].
 *
 * @param state State to compare with
 * @return true if this State is greater or equal to the given [Lifecycle.State]
 */
fun LifecycleOwner.isStateAtLeast(state: Lifecycle.State) =
        lifecycle.currentState.isAtLeast(state)

/**
 * Lazy intent parameters for fragment activities
 *
 * @param key lookup key for the embedded item in the [FragmentActivity.getIntent]
 * @param default default value to use when key does not exist
 *
 * @return [Lazy] of the target type
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> FragmentActivity.extras(key: String, default: T) = lazy(LAZY_MODE_PUBLICATION) {
    try {
        if (intent?.extras?.containsKey(key) == true)
            intent?.extras?.get(key) as T
        else
            default
    } catch (e: Exception) {
        Timber.tag("AppExt.extras").e(e)
        error(e)
    }
}

/**
 * Lazy intent parameters for fragments
 *
 * @param key lookup key for the embedded item in the [Fragment.getArguments]
 * @param default default value to use when key does not exist
 *
 * @return [Lazy] of the target type
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> Fragment.extras(key: String, default: T) = lazy(LAZY_MODE_PUBLICATION) {
    try {
        if (arguments?.containsKey(key) == true)
            arguments?.get(key) as T
        else
            default
    } catch (e: Exception) {
        Timber.tag("AppExt.extras").e(e)
        error(e)
    }
}