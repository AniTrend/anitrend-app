package com.mxt.anitrend.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.repository.UserRepository
import timber.log.Timber

class ClearNotificationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val presenter: BasePresenter,
    private val userRepository: UserRepository,
) : CoroutineWorker(context, workerParams) {

    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to **synchronously** do your work and return the
     * [Result] from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
     * you need to do your work asynchronously on a thread of your own choice, see
     * [ListenableWorker].
     *
     *
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * [Result].  After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The [Result] of the computation; note that
     * dependent work will not execute if you use
     * [Result.failure]
     */
    override suspend fun doWork(): Result {
        if (presenter.settings.isAuthenticated) {
            try {
                val user = userRepository.getCurrentUser(asHtml = false).getOrThrow()
                userRepository.saveCurrentUser(user)

                if (user.unreadNotificationCount != 0) {
                    val notifications = userRepository
                        .getUserNotifications(resetNotificationCount = true)
                        .getOrThrow()
                    userRepository.saveNotificationHistory(notifications)
                    return Result.success()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
            return Result.retry()
        }
        return Result.failure()
    }
}
