package com.mxt.anitrend.base.custom.view.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetAutoIncrementerBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.graphql.apiError
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.util.media.MediaUtil
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2018/02/22.
 * auto increment widget for changing series progress with just a tap
 */
class AutoIncrementWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener,
    RetroCallback<MediaList> {
    @KeyUtil.RequestType
    private var requestType: Int = KeyUtil.MUT_SAVE_MEDIA_LIST
    private lateinit var presenter: WidgetPresenter<MediaList>
    private lateinit var binding: WidgetAutoIncrementerBinding

    @KeyUtil.MediaListStatus
    private var status: String? = null
    private var model: MediaList? = null

    private var currentUser: String? = null
    private val tagName = AutoIncrementWidget::class.java.simpleName

    init {
        onInit()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : this(context, attrs, defStyleAttr)

    override fun onInit() {
        presenter = WidgetPresenter(context)
        binding = WidgetAutoIncrementerBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val currentModel = model ?: return
        if (presenter.isCurrentUser(currentUser) && MediaUtil.isAllowedStatus(currentModel)) {
            if (!MediaUtil.isIncrementLimitReached(currentModel)) {
                if (view.id == R.id.widget_flipper) {
                    if (binding.widgetFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
                        binding.widgetFlipper.showNext()
                        updateModelState()
                    } else {
                        NotifyUtil
                            .makeText(
                                context,
                                R.string.busy_please_wait,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
            } else {
                NotifyUtil
                    .makeText(
                        context,
                        if (MediaUtil.isAnimeType(currentModel.media)) {
                            R.string.text_unable_to_increment_episodes
                        } else {
                            R.string.text_unable_to_increment_chapters
                        },
                        R.drawable.ic_warning_white_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    fun setModel(
        model: MediaList,
        currentUser: String?,
    ) {
        this.model = model
        this.currentUser = currentUser
        status = model.status
        binding.seriesProgressIncrement.setSeriesModel(model, presenter.isCurrentUser(currentUser))
    }

    override fun onViewRecycled() {
        resetFlipperState()
        presenter.onDestroy()
        model = null
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == WidgetPresenter.LOADING_STATE) {
            binding.widgetFlipper.displayedChild = WidgetPresenter.CONTENT_STATE
        }
    }

    override fun onResponse(
        call: Call<MediaList>,
        response: Response<MediaList>,
    ) {
        try {
            val responseModel = response.body()
            val modelClone = model?.clone()
            if (response.isSuccessful && responseModel != null && modelClone != null) {
                val isModelCategoryChanged = responseModel.status != status
                responseModel.media = modelClone.media
                val updatedModel = responseModel.clone()
                model = updatedModel
                binding.seriesProgressIncrement.setSeriesModel(
                    updatedModel,
                    presenter.isCurrentUser(currentUser),
                )
                if (isModelCategoryChanged || MediaListUtil.isProgressUpdatable(modelClone)) {
                    if (isModelCategoryChanged) {
                        NotifyUtil
                            .makeText(
                                context,
                                R.string.text_changes_saved,
                                R.drawable.ic_check_circle_white_24dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                    presenter.notifyAllListeners(BaseConsumer(requestType, updatedModel), false)
                } else {
                    resetFlipperState()
                }
            } else {
                resetFlipperState()
                Timber.tag(tagName).w(response.apiError())
                NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Timber.w(e)
        }
    }

    override fun onFailure(
        call: Call<MediaList>,
        throwable: Throwable,
    ) {
        try {
            Timber.w(throwable)
            resetFlipperState()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun updateModelState() {
        val currentModel = model ?: return
        if (currentModel.progress < 1 &&
            (
                CompatUtil.equals(currentModel.status, KeyUtil.PLANNING) ||
                    CompatUtil.equals(currentModel.status, KeyUtil.CURRENT)
                )
        ) {
            currentModel.status = KeyUtil.CURRENT
            currentModel.startedAt = DateUtil.currentDate
        }
        currentModel.progress = currentModel.progress + 1
        if (MediaUtil.isIncrementLimitReached(currentModel)) {
            currentModel.status = KeyUtil.COMPLETED
            currentModel.completedAt = DateUtil.currentDate
        }
        val user = presenter.database.currentUser ?: return
        presenter.params =
            MediaListUtil.getMediaListParams(
                currentModel,
                user.mediaListOptions.scoreFormat,
            )
        presenter.requestData(requestType, context, this)
    }
}
