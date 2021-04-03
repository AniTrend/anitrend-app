package com.mxt.anitrend.worker.factory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.mxt.anitrend.worker.ClearNotificationWorker
import com.mxt.anitrend.worker.NotificationWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

internal class WorkManagerFactory : WorkerFactory(), KoinComponent {

    private fun resolveDependency(
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = get(
        qualifier = named(workerClassName)
    ) { parametersOf(workerParameters) }

    /**
     * Override this method to implement your custom worker-creation logic.  Use
     * [Configuration.Builder.setWorkerFactory] to use your custom class.
     *
     *
     * Throwing an [Exception] here will crash the application. If a [WorkerFactory]
     * is unable to create an instance of the [ListenableWorker], it should return `null` so it can delegate to the default [WorkerFactory].
     *
     *
     * Returns a new instance of the specified `workerClassName` given the arguments.  The
     * returned worker must be a newly-created instance and must not have been previously returned
     * or invoked by WorkManager. Otherwise, WorkManager will throw an
     * [IllegalStateException].
     *
     * @param appContext The application context
     * @param workerClassName The class name of the worker to create
     * @param workerParameters Parameters for worker initialization
     * @return A new [ListenableWorker] instance of type `workerClassName`, or
     * `null` if the worker could not be created
     */
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = when (workerClassName) {
        "com.mxt.anitrend.service.JobDispatcherService" -> resolveDependency(
            NotificationWorker::class.java.canonicalName!!,
            workerParameters
        )
        "com.mxt.anitrend.service.ClearNotificationService" -> resolveDependency(
            ClearNotificationWorker::class.java.canonicalName!!,
            workerParameters
        )
        else -> resolveDependency(workerClassName, workerParameters)
    }
}