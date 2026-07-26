package com.mxt.anitrend.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.repository.BaseRepository
import timber.log.Timber

class GenreSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val presenter: BasePresenter,
    private val baseRepository: BaseRepository,
) : CoroutineWorker(context, workerParams) {

    private suspend fun requestGenres(): List<Genre> {
        val data = baseRepository.getGenres().getOrThrow()
        return if (data.isEmpty()) {
            Timber.e("GenreCollection returned empty data")
            emptyList()
        } else {
            data.map(::Genre)
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
                requestGenres()
            }.onSuccess {
                presenter.database.genreCollection = it
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
