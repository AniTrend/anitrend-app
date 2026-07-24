package com.mxt.anitrend.util.media

import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.event.LifecycleListener
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.apiError
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2018/01/05.
 * Media list action helper class is responsible for showing the correct dialog
 * for a given media
 */
class MediaActionUtil private constructor(
    private val context: FragmentActivity,
) : RetroCallback<MediaBase>,
    LifecycleListener,
KoinComponent {
    private var progressDialog: AlertDialog? = null
    private val presenter by inject<WidgetPresenter<MediaBase>>()
    private val lifecycle: Lifecycle = context.lifecycle
    private var mediaId: Long = 0

    private fun setMediaId(mediaId: Long) {
        this.mediaId = mediaId
    }

    private fun actionPicker() {
        val currentUser =
            presenter.database.currentUser ?: run {
                dismissProgress()
                NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show()
                return
            }
        val mediaListOptions: MediaListOptions = currentUser.mediaListOptions

        presenter.params.apply {
            putLong(KeyUtil.arg_id, mediaId)
            putString(KeyUtil.arg_scoreFormat, mediaListOptions.scoreFormat)
        }
        presenter.requestData(KeyUtil.MEDIA_WITH_LIST_REQ, context, this)
    }

    private fun dismissProgress() {
        progressDialog?.dismiss()
    }

    fun startSeriesAction() {
        progressDialog = NotifyUtil.createProgressDialog(context, R.string.text_checking_collection)
        progressDialog?.show()
        actionPicker()
    }

    private fun showActionDialog(mediaBase: MediaBase) {
        try {
            MediaDialogUtil.createSeriesManage(context, mediaBase)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResponse(
        call: Call<MediaBase>,
        response: Response<MediaBase>,
    ) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            val mediaBase = response.body()
            if (response.isSuccessful && mediaBase != null) {
                showActionDialog(mediaBase)
            } else {
                Timber.w(response.apiError())
                NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show()
            }
            dismissProgress()
        }
    }

    override fun onFailure(
        call: Call<MediaBase>,
        throwable: Throwable,
    ) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            dismissProgress()
            Timber.e(throwable)
            throwable.printStackTrace()
            NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause(changeListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        presenter.onPause(changeListener)
    }

    override fun onResume(changeListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        presenter.onResume(changeListener)
    }

    override fun onDestroy() {
        progressDialog?.dismiss()
        presenter.onDestroy()
    }

    class Builder {
        private var mediaId: Long = 0

        fun setId(mediaId: Long) = apply {
            this.mediaId = mediaId
        }

        fun build(context: FragmentActivity): MediaActionUtil {
            val mediaActionUtil = MediaActionUtil(context)
            mediaActionUtil.setMediaId(mediaId)
            return mediaActionUtil
        }
    }
}
