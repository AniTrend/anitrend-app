package com.mxt.anitrend.base.custom.view.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetAutoIncrementerBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.util.media.MediaUtil
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
    View.OnClickListener {

    @Suppress("LongParameterList")
    interface Listener {
        fun onSaveMediaListEntry(
            id: Int?,
            mediaId: Long?,
            status: MediaListStatus?,
            score: Double?,
            progress: Int?,
            progressVolumes: Int?,
            repeat: Int?,
            priority: Int?,
            private: Boolean,
            hiddenFromStatusLists: Boolean,
            customLists: List<String?>?,
            advancedScores: List<Double?>?,
            notes: String?,
            startedAt: FuzzyDateInput?,
            completedAt: FuzzyDateInput?,
            onResult: (Result<MediaList>) -> Unit,
        )
    }

    private lateinit var binding: WidgetAutoIncrementerBinding

    @KeyUtil.MediaListStatus
    private var status: String? = null
    private var model: MediaList? = null

    private var currentUser: String? = null
    private var currentUserFull: UserBase? = null
    private val tagName = AutoIncrementWidget::class.java.simpleName
    private var listener: Listener? = null
    private var recycled = false

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun setCurrentUser(user: UserBase?) {
        this.currentUserFull = user
    }

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
        binding = WidgetAutoIncrementerBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val currentModel = model ?: return
        if (currentUserFull?.name == currentUser && MediaUtil.isAllowedStatus(currentModel)) {
            if (!MediaUtil.isIncrementLimitReached(currentModel)) {
                if (view.id == R.id.widget_flipper) {
                    if (binding.widgetFlipper.displayedChild == CONTENT_STATE) {
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
        binding.seriesProgressIncrement.setSeriesModel(model, currentUserFull?.name == currentUser)
    }

    override fun onViewRecycled() {
        recycled = true
        listener = null
        resetFlipperState()
        model = null
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
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
        listener?.onSaveMediaListEntry(
            id = currentModel.id.takeIf { it > 0 }?.toInt(),
            mediaId = currentModel.mediaId,
            status = currentModel.status?.let { runCatching { MediaListStatus.valueOf(it) }.getOrNull() },
            score = currentModel.score.toDouble(),
            progress = currentModel.progress,
            progressVolumes = currentModel.progressVolumes,
            repeat = currentModel.repeat,
            priority = currentModel.priority,
            private = currentModel.isHidden,
            hiddenFromStatusLists = currentModel.isHiddenFromStatusLists,
            customLists = currentModel.customLists?.filter { it.isEnabled }?.mapNotNull { it.name?.takeIf { name -> name.isNotEmpty() } },
            advancedScores = currentModel.advancedScores?.values?.map { it.toDouble() },
            notes = currentModel.notes,
            startedAt = currentModel.startedAt?.let { FuzzyDateInput(day = it.day, month = it.month, year = it.year) },
            completedAt = currentModel.completedAt?.let { FuzzyDateInput(day = it.day, month = it.month, year = it.year) },
        ) { result ->
            if (recycled || !isAttachedToWindow) return@onSaveMediaListEntry
            result.onSuccess { savedResult ->
                val isModelCategoryChanged = savedResult.status != status
                savedResult.media = currentModel.media
                model = savedResult
                binding.seriesProgressIncrement.setSeriesModel(savedResult, currentUserFull?.name == currentUser)
                if (isModelCategoryChanged || MediaListUtil.isProgressUpdatable(savedResult)) {
                    if (isModelCategoryChanged) {
                        NotifyUtil.makeText(context, R.string.text_changes_saved, R.drawable.ic_check_circle_white_24dp, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    resetFlipperState()
                }
            }.onFailure { throwable ->
                resetFlipperState()
                Timber.w(throwable)
            }
        }
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1
    }
}
