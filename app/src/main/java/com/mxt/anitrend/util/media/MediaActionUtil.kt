package com.mxt.anitrend.util.media

import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.base.interfaces.event.LifecycleListener
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.NotifyUtil
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Created by max on 2018/01/05.
 * Media list action helper class is responsible for showing the correct dialog
 * for a given media
 */
class MediaActionUtil private constructor(
    private val context: FragmentActivity,
) : LifecycleListener,
    KoinComponent {
    private var progressDialog: AlertDialog? = null
    private val browseRepository by inject<BrowseRepository>()
    private val databaseHelper by inject<BoxQuery>()
    private val lifecycle: Lifecycle = context.lifecycle
    private var mediaId: Long = 0

    private fun setMediaId(mediaId: Long) {
        this.mediaId = mediaId
    }

    private fun actionPicker() {
        val currentUser =
            databaseHelper.currentUser ?: run {
                dismissProgress()
                NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show()
                return
            }
        val scoreFormat =
            runCatching { ScoreFormat.valueOf(currentUser.mediaListOptions.scoreFormat) }
                .getOrNull() ?: ScoreFormat.POINT_100

        context.lifecycleScope.launch {
            browseRepository
                .getMediaWithList(id = mediaId, scoreFormat = scoreFormat)
                .onSuccess(::handleMediaWithList)
                .onFailure(::handleMediaWithListFailure)
        }
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

    private fun handleMediaWithList(mediaBase: MediaBase) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            showActionDialog(mediaBase)
            dismissProgress()
        }
    }

    private fun handleMediaWithListFailure(throwable: Throwable) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            dismissProgress()
            Timber.e(throwable)
            throwable.printStackTrace()
            NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause(changeListener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

    override fun onResume(changeListener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

    override fun onDestroy() {
        progressDialog?.dismiss()
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
