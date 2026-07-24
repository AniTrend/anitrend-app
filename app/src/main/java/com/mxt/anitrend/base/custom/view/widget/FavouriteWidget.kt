package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetFavouriteBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import timber.log.Timber
import java.util.Locale

/**
 * Created by max on 2017/10/29.
 * Like or favourite view which manages state independently
 */
class FavouriteWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener {

    interface Listener {
        fun onToggleLike(
            id: Long,
            type: LikeableType,
            onResult: (Result<List<UserBase>>) -> Unit,
        )
    }

    private lateinit var binding: WidgetFavouriteBinding
    private var model: MutableList<UserBase>? = null
    private var likeType: String? = null
    private var modelId: Long = 0L
    private val tagName = FavouriteWidget::class.java.simpleName
    private var listener: Listener? = null
    private var recycled = false
    private var currentUser: UserBase? = null

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun setCurrentUser(user: UserBase?) {
        this.currentUser = user
    }

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding = WidgetFavouriteBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
    }

    /**
     * Clean up any resources that won't be needed
     */
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

    fun setModel(model: List<UserBase>?) {
        this.model = model?.toMutableList()
        setIconType()
    }

    fun setRequestParams(
        @KeyUtil.LikeType likeType: String,
        modelId: Long,
    ) {
        this.likeType = likeType
        this.modelId = modelId
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.widget_flipper -> {
                if (binding.widgetFlipper.displayedChild == CONTENT_STATE) {
                    binding.widgetFlipper.showNext()
                    val type = likeType ?: return
                    val id = modelId
                    if (id == 0L) return
                    val likeableType = try {
                        LikeableType.valueOf(type)
                    } catch (e: Exception) {
                        Timber.e(e, "Invalid like type: $type")
                        resetFlipperState()
                        return
                    }
                    listener?.onToggleLike(id, likeableType) { result ->
                        if (recycled || !isAttachedToWindow) return@onToggleLike
                        result.onSuccess { newLikes ->
                            model = newLikes.toMutableList()
                            setIconType()
                        }.onFailure { throwable ->
                            Timber.e(throwable)
                            resetFlipperState()
                        }
                    }
                } else {
                    NotifyUtil
                        .makeText(
                            context,
                            R.string.busy_please_wait,
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    }

    private fun setIconType() {
        val likes = model
        val user = currentUser
        if (!likes.isNullOrEmpty() && user != null && likes.contains(user)) {
            binding.widgetLike.setCompoundDrawablesWithIntrinsicBounds(
                context.getCompatDrawable(R.drawable.ic_favorite_grey_600_18dp, R.color.colorStateRed),
                null,
                null,
                null,
            )
        } else {
            binding.widgetLike.setCompoundDrawablesWithIntrinsicBounds(
                context.getCompatDrawable(R.drawable.ic_favorite_grey_600_18dp),
                null,
                null,
                null,
            )
        }
        binding.widgetLike.text = convertToText(likes?.size ?: 0)
        resetFlipperState()
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1

        fun convertToText(count: Int): String = String.format(Locale.getDefault(), " %d ", count)
    }
}
