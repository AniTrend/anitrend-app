package com.mxt.anitrend.service

import android.content.Context
import android.util.Log

import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.container.body.GraphContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.GraphUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotificationUtil

import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import retrofit2.Call
import retrofit2.Response

/**
 * Created by Maxwell on 1/22/2017.
 */
class JobDispatcherService(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val presenter by lazy {
        BasePresenter(context)
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
    override fun doWork(): ListenableWorker.Result {
        if (presenter.applicationPref.isAuthenticated) {
            val userModel = WebFactory.createService(UserModel::class.java, applicationContext)
            val request = userModel.getCurrentUser(GraphUtil.getDefaultQuery(false))
            try {
                val response = request.execute()
                val userGraphContainer: Any? = response.body()
                if (response.isSuccessful && userGraphContainer != null) {
                    val currentUser = userGraphContainer as User?
                    val previousUserData = presenter.database.currentUser
                    presenter.database.saveCurrentUser(currentUser)
                    if (previousUserData.unreadNotificationCount != currentUser!!.unreadNotificationCount) {
                        if (currentUser.unreadNotificationCount > 0) {
                            presenter.notifyAllListeners(BaseConsumer(KeyUtil.USER_CURRENT_REQ, currentUser), false)
                            NotificationUtil.createNotification(applicationContext, currentUser)
                        }
                    }
                    return ListenableWorker.Result.success()
                }
            } catch (e: Exception) {
                e.message?.apply {
                    Log.e(toString(), this)
                }
                e.printStackTrace()
            }

        }
        return ListenableWorker.Result.retry()
    }
}
