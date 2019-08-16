package com.mxt.anitrend.extension

import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

fun CoroutineScope.launchCatching(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        errorHandler: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
) {
    launch(coroutineContext + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
        errorHandler?.invoke(e)
    }, block = block)
}