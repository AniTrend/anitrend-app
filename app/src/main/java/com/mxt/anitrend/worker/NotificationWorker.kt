package com.mxt.anitrend.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.mxt.anitrend.graphql.generated.CurrentUser
import com.mxt.anitrend.graphql.generated.UserNotifications
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.repository.UserMutation
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.NotificationUtil
import timber.log.Timber

/**
 * Created by Maxwell on 1/22/2017.
 */
class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val presenter: BasePresenter,
    private val notificationUtil: NotificationUtil,
    private val userService: UserModel,
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
                requestUser()?.apply {
                    if (unreadNotificationCount != 0) {
                        userRepository.emitMutationEvent(UserMutation.CurrentUserUpdated(this))
                        requestNotifications(this)
                    }
                }
                return Result.success()
            } catch (e: Exception) {
                Timber.e(e)
            }
            return Result.retry()
        }
        return Result.failure()
    }

    private fun requestUser(): User? {
        val response =
            userService
                .getCurrentUser(
                    CurrentUser.request(asHtml = false),
                ).execute()
        if (!response.isSuccessful) {
            return null
        }
        return unwrapBody<User>(response.body())?.let {
            presenter.database.currentUser = it
            it
        }
    }

    private fun requestNotifications(user: User) {
        val response =
            userService
                .getUserNotifications(
                    UserNotifications.request(resetNotificationCount = false),
                ).execute()
        if (!response.isSuccessful) {
            return
        }
        val notificationsContainer = unwrapBody<PageContainer<Notification>>(response.body())

        if (user.unreadNotificationCount > 0 && notificationsContainer != null) {
            notificationUtil.createNotification(user, notificationsContainer)
        }
    }
}
