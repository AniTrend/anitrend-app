package com.mxt.anitrend.service

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotificationUtil
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

/**
 * Created by Maxwell on 1/22/2017.
 */
class JobDispatcherService(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    private val presenter by inject<BasePresenter>()

    private val notificationUtil by inject<NotificationUtil>()

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
                        requestNotifications(this)
                    }
                }
                return Result.success()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
                e.printStackTrace()
            }
            return Result.retry()
        }
        return Result.failure()
    }

    private fun requestUser(): User? {
        val userGraphContainer = userEndpoint.getCurrentUser(
                GraphUtil.getDefaultQuery(false)
        ).execute().body() as User?

        return (userGraphContainer).let {
            it?.also { user ->
                presenter.database.currentUser = user
            }
            it
        }
    }

    private fun requestNotifications(user: User) {
        if (user.unreadNotificationCount > 0)
            notificationUtil.createNotification(user)
    }

    companion object {
        private val TAG = JobDispatcherService::class.java.simpleName
    }
}
