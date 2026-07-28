package com.mxt.anitrend.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mxt.anitrend.model.api.retro.base.RepositoryService
import com.mxt.anitrend.model.entity.base.VersionBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil
import retrofit2.HttpException
import timber.log.Timber

class UpdateWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val presenter: BasePresenter,
    private val repositoryService: RepositoryService,
) : CoroutineWorker(context, workerParams) {

    private fun shouldCheckForUpdate(): Boolean {
        val versionBase = presenter.database.remoteVersion
        // How frequent the application checks for updates on startup
        return versionBase == null ||
            DateUtil.timeDifferenceSatisfied(
                KeyUtil.TIME_UNIT_MINUTES,
                versionBase.lastChecked,
                15,
            )
    }

    private fun requestUpdateInformation(): VersionBase? = if (shouldCheckForUpdate()) {
        val response =
            repositoryService
                .checkVersion(
                    presenter.settings.updateChannel,
                ).execute()

        val data = response.body()

        if (response.isSuccessful) {
            data
        } else {
            throw HttpException(response)
        }
    } else {
        null
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
        val result =
            runCatching {
                requestUpdateInformation()
            }.onSuccess { versionBase ->
                if (versionBase != null) {
                    presenter.database.remoteVersion = versionBase
                }
            }.onFailure {
                Timber.e(it)
            }

        val silent = inputData.getBoolean(KeyUtil.WorkUpdaterSilentId, false)
        val output = workDataOf(KeyUtil.WorkUpdaterSilentId to silent)

        return if (result.isSuccess) {
            Result.success(output)
        } else {
            Result.failure(output)
        }
    }
}
