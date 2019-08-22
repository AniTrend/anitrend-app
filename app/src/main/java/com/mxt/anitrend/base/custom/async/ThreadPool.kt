package com.mxt.anitrend.base.custom.async

import java.util.concurrent.Executors

/**
 * Created by max on 2018/02/20.
 */

object ThreadPool {

    private val executorService = Executors.newCachedThreadPool()

    fun execute(task: () -> Unit) =
        executorService.execute(task)
}
