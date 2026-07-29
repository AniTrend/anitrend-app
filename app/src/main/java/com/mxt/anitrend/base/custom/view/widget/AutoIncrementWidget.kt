package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetAutoIncrementerBinding
import com.mxt.anitrend.domain.model.IncrementMediaProgressCommand
import com.mxt.anitrend.domain.model.buildIncrementMediaProgressCommand
import com.mxt.anitrend.domain.model.resolveIncrementResultModel
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.NotifyUtil
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
            command: IncrementMediaProgressCommand,
            onResult: (Result<MediaList>) -> Unit,
        ) {
            onSaveMediaListEntry(
                id = command.id,
                mediaId = command.mediaId,
                status = command.status,
                score = command.score,
                scoreRaw = command.scoreRaw,
                progress = command.requestedProgress,
                progressVolumes = command.progressVolumes,
                repeat = command.repeat,
                priority = command.priority,
                private = command.isPrivate,
                hiddenFromStatusLists = command.hiddenFromStatusLists,
                customLists = command.customLists,
                advancedScores = command.advancedScores,
                notes = command.notes,
                startedAt = command.startedAt,
                completedAt = command.completedAt,
                onResult = onResult,
            )
        }

        @Deprecated("Implement the immutable command overload")
        fun onSaveMediaListEntry(
            id: Int?,
            mediaId: Long?,
            status: MediaListStatus?,
            score: Double?,
            scoreRaw: Int? = null,
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

    private var model: MediaList? = null

    private var currentUser: String? = null
    private var currentUserFull: UserBase? = null
    private var listener: Listener? = null
    private var recycled = false
    private var isSaving = false

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun setCurrentUser(user: UserBase?) {
        this.currentUserFull = user
    }

    init {
        onInit()
    }

    override fun onInit() {
        binding = WidgetAutoIncrementerBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val currentModel = model ?: return
        if (currentUserFull?.name == currentUser && MediaUtil.isAllowedStatus(currentModel)) {
            if (!MediaUtil.isIncrementLimitReached(currentModel)) {
                if (view.id == R.id.widget_flipper) {
                    if (!isSaving && binding.widgetFlipper.displayedChild == CONTENT_STATE) {
                        isSaving = true
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
        recycled = false
        isSaving = false
        this.model = model
        this.currentUser = currentUser
        resetFlipperState()
        binding.seriesProgressIncrement.setSeriesModel(model, currentUserFull?.name == currentUser)
    }

    override fun onViewRecycled() {
        recycled = true
        isSaving = false
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
        val saveListener = listener
        if (saveListener == null) {
            isSaving = false
            resetFlipperState()
            return
        }

        val command = buildIncrementMediaProgressCommand(currentModel)
        saveListener.onSaveMediaListEntry(command) { result ->
            isSaving = false
            if (recycled || !isAttachedToWindow) {
                resetFlipperState()
                return@onSaveMediaListEntry
            }

            val renderModel = resolveIncrementResultModel(currentModel, result)
            result.onSuccess { savedResult ->
                val isModelCategoryChanged = savedResult.status != currentModel.status
                model = renderModel
                binding.seriesProgressIncrement.setSeriesModel(renderModel, currentUserFull?.name == currentUser)
                if (isModelCategoryChanged) {
                    NotifyUtil.makeText(context, R.string.text_changes_saved, R.drawable.ic_check_circle_white_24dp, Toast.LENGTH_SHORT).show()
                }
                resetFlipperState()
            }.onFailure { throwable ->
                binding.seriesProgressIncrement.setSeriesModel(renderModel, currentUserFull?.name == currentUser)
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
