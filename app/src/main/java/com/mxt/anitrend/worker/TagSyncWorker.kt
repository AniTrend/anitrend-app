package com.mxt.anitrend.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.BaseModel
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.graphql.apiError
import timber.log.Timber

class TagSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val presenter: WidgetPresenter<List<MediaTag>>
) : CoroutineWorker(context, workerParams) {

    private val endpoint by lazy(LazyThreadSafetyMode.NONE) {
        WebFactory.createService(BaseModel::class.java, applicationContext)
    }

    private fun requestTags(): List<MediaTag> {
        val response = endpoint.getTags(
            GraphUtil.getDefaultQuery(false)
        ).execute()

        @Suppress("UNCHECKED_CAST")
        val data = response.body() as? List<MediaTag>

        if (response.isSuccessful && data != null) {
            return data.orEmpty()
        } else
            Timber.e(response.apiError())

        return emptyList()
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
            requestTags()
        }.onSuccess {
            presenter.database.mediaTags = it
        }.onFailure {
            Timber.e(it)
        }

        return if (result.isSuccess)
            Result.success()
        else Result.failure()
    }
}