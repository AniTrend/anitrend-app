package com.mxt.anitrend.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.annimon.stream.Stream
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.NotificationHistory
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ClearNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    private val presenter by inject<BasePresenter>()

    private val userEndpoint by lazy(LazyThreadSafetyMode.NONE) {
        WebFactory.createService(UserModel::class.java, applicationContext)
    }

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
    override fun doWork(): Result {
        if (presenter.settings.isAuthenticated) {
            try {
                requestUser()?.apply {
                    if (unreadNotificationCount != 0) {
                        presenter.notifyAllListeners(
                            BaseConsumer(KeyUtil.USER_CURRENT_REQ, this),
                            false
                        )
                        return when(clearNotifications()) {
                            true -> Result.success()
                            else -> Result.failure()
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
            return Result.retry()
        }
        return Result.failure()
    }

    private fun requestUser(): User? {
        val result = userEndpoint.getCurrentUser(
            GraphUtil.getDefaultQuery(false)
        ).execute()
        if (!result.isSuccessful) {
            return null
        }
        val user = unwrapBody<User>(result.body())
        user?.also {
            presenter.database.currentUser = it
        }
        return user
    }

    private fun clearNotifications(): Boolean {
        val result = userEndpoint.getUserNotifications(
            GraphUtil
                .getDefaultQuery(false)
                .putVariable(KeyUtil.arg_resetNotificationCount, true)
        ).execute()

        if (result.isSuccessful) {
            val notifications = unwrapBody<PageContainer<Notification>>(result.body())
            if (notifications != null) {
                val notificationHistories = Stream.of(notifications.pageData)
                    .map { notification -> NotificationHistory(notification.id) }
                    .toList()

                presenter.database.getBoxStore(NotificationHistory::class.java)
                    .put(notificationHistories)
            }
            return true
        }

        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> unwrapBody(body: Any?): T? {
        return when (body) {
            null -> null
            is AniListContainer<*> -> body.data?.result as? T
            else -> body as? T
        }
    }

    companion object {
        private val TAG = ClearNotificationWorker::class.java.simpleName
    }
}
