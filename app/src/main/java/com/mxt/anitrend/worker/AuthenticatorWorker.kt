package com.mxt.anitrend.worker

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log

import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil

import java.util.concurrent.ExecutionException

import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class AuthenticatorWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    private val presenter by inject<BasePresenter>()

    private val authenticatorUri: Uri by lazy(LazyThreadSafetyMode.NONE) {
        Uri.parse(workerParams.inputData
                .getString(KeyUtil.arg_model))
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
     * [Result.failure] or
     * [Result.failure]
     */
    override fun doWork(): Result {
        val errorDataBuilder = Data.Builder()
        try {
            val authorizationCode = authenticatorUri.getQueryParameter(BuildConfig.RESPONSE_TYPE)
            if (!TextUtils.isEmpty(authorizationCode)) {
                val isSuccess = WebTokenRequest.getToken(authorizationCode)
                presenter.settings.isAuthenticated = isSuccess
                val outputData = Data.Builder()
                        .putBoolean(KeyUtil.arg_model, isSuccess)
                        .build()
                return Result.success(outputData)
            } else
                Timber.tag(TAG).e("Authorization authenticatorUri was empty or null, cannot authenticate with the current state")
        } catch (e: ExecutionException) {
            e.printStackTrace()
            errorDataBuilder.putString(KeyUtil.arg_exception_error, e.message)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            errorDataBuilder.putString(KeyUtil.arg_exception_error, e.message)
        }

        val workerErrorOutputData = errorDataBuilder
                .putString(KeyUtil.arg_uri_error, authenticatorUri
                        .getQueryParameter(KeyUtil.arg_uri_error))
                .putString(KeyUtil.arg_uri_error_description, authenticatorUri
                        .getQueryParameter(KeyUtil.arg_uri_error_description))
                .build()
        return Result.failure(workerErrorOutputData)
    }

    companion object {
        private val TAG = AuthenticatorWorker::class.java.simpleName
    }
}
