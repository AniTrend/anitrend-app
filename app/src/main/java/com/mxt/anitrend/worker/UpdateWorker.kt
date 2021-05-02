package com.mxt.anitrend.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.base.VersionBase
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil
import retrofit2.HttpException
import timber.log.Timber

class UpdateWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val presenter: WidgetPresenter<VersionBase>
) : CoroutineWorker(context, workerParams) {

    private val endpoint by lazy(LazyThreadSafetyMode.NONE) {
        WebFactory.createRepositoryService()
    }

    private fun shouldCheckForUpdate(): Boolean {
        val versionBase = presenter.database.remoteVersion
        // How frequent the application checks for updates on startup
        return versionBase == null || DateUtil.timeDifferenceSatisfied(
            KeyUtil.TIME_UNIT_MINUTES,
            versionBase.lastChecked,
            15
        )
    }

    private fun requestUpdateInformation(): VersionBase? {
        return if (shouldCheckForUpdate()) {
            val response = endpoint.checkVersion(
                presenter.settings.updateChannel
            ).execute()

            val data = response.body()

            if (response.isSuccessful)
                data
            else
                throw HttpException(response)
        } else null
    }

    /**
     * A suspending method to do your work.  This function runs on the coroutine context specified
     * by [coroutineContext].
     * <p>
     * A CoroutineWorker is given a maximum of ten minutes to finish its execution and return a
     * [ListenableWorker.Result].  After this time has expired, the worker will be signalled to
     * stop.
     *
     * @return The [ListenableWorker.Result] of the result of the background work; note that
     * dependent work will not execute if you return [ListenableWorker.Result.failure]
     */
    override suspend fun doWork(): Result {
        val result = runCatching {
            requestUpdateInformation()
        }.onSuccess { versionBase ->
            if (versionBase != null)
                presenter.database.remoteVersion = versionBase
        }.onFailure {
            Timber.e(it)
        }

        return if (result.isSuccess)
            Result.success()
        else Result.failure()
    }
}