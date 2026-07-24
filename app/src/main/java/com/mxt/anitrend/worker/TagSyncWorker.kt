package com.mxt.anitrend.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.anitrend.retrofit.graphql.model.EmptyGraphQLVariables
import co.anitrend.retrofit.graphql.model.GraphQLRequest
import com.mxt.anitrend.graphql.generated.MediaTagCollection
import com.mxt.anitrend.model.api.retro.anilist.BaseModel
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.graphql.apiError
import timber.log.Timber

class TagSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val presenter: BasePresenter,
    private val baseService: BaseModel,
) : CoroutineWorker(context, workerParams) {

    private fun requestTags(): List<MediaTag> {
        val response =
            baseService
                .getTags(
                    GraphQLRequest<EmptyGraphQLVariables>(
                        query = MediaTagCollection.document,
                        operationName = MediaTagCollection.name,
                    ),
                ).execute()

        if (!response.isSuccessful) {
            Timber.e(response.apiError())
            return emptyList()
        }

        val data: List<MediaTag>? = unwrapBody(response.body())

        return if (data.isNullOrEmpty()) {
            Timber.e("MediaTagCollection returned empty data")
            emptyList()
        } else {
            data
        }
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
                requestTags()
            }.onSuccess {
                presenter.database.mediaTags = it
            }.onFailure {
                Timber.e(it)
            }

        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}
