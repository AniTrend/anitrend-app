package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetFavouriteBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.graphql.apiError
import io.github.wax911.library.model.request.QueryContainerBuilder
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2017/10/29.
 * Like or favourite view which manages state independently
 */
class FavouriteWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    RetroCallback<List<UserBase>>,
    View.OnClickListener {

    private var presenter: WidgetPresenter<List<UserBase>>? = null
    private lateinit var binding: WidgetFavouriteBinding
    private var model: MutableList<UserBase>? = null
    private val tagName = FavouriteWidget::class.java.simpleName

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        presenter = WidgetPresenter(context)
        binding = WidgetFavouriteBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        resetFlipperState()
        presenter?.onDestroy()
        presenter = null
        model = null
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.displayedChild = WidgetPresenter.CONTENT_STATE
    }

    fun setModel(model: List<UserBase>?) {
        this.model = model?.toMutableList()
        setIconType()
    }

    fun setRequestParams(@KeyUtil.LikeType likeType: String, modelId: Long) {
        val queryContainer: QueryContainerBuilder = GraphUtil.getDefaultQuery(false)
            .putVariable(KeyUtil.arg_id, modelId)
            .putVariable(KeyUtil.arg_type, likeType)
        presenter?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.widget_flipper -> {
                if (binding.widgetFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext()
                    presenter?.requestData(KeyUtil.MUT_TOGGLE_LIKE, context, this)
                } else {
                    NotifyUtil.makeText(
                        context,
                        R.string.busy_please_wait,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setIconType() {
        val likes = model
        val currentUser = presenter?.database?.currentUser
        if (!CompatUtil.isEmpty(likes) && currentUser != null && likes?.contains(currentUser) == true) {
            binding.widgetLike.setCompoundDrawablesWithIntrinsicBounds(
                context.getCompatDrawable(R.drawable.ic_favorite_grey_600_18dp, R.color.colorStateRed),
                null,
                null,
                null
            )
        } else {
            binding.widgetLike.setCompoundDrawablesWithIntrinsicBounds(
                context.getCompatDrawable(R.drawable.ic_favorite_grey_600_18dp),
                null,
                null,
                null
            )
        }
        binding.widgetLike.text = WidgetPresenter.convertToText(CompatUtil.sizeOf(likes))
        resetFlipperState()
    }

    /**
     * Invoked for a received HTTP response.
     */
    override fun onResponse(call: Call<List<UserBase>>, response: Response<List<UserBase>>) {
        try {
            if (response.isSuccessful) {
                val currentUser = presenter?.database?.currentUser
                if (currentUser != null) {
                    val likes = model ?: mutableListOf<UserBase>().also { model = it }
                    if (!CompatUtil.isEmpty(likes) && likes.contains(currentUser))
                        likes.remove(currentUser)
                    else
                        likes.add(currentUser)
                }
                setIconType()
            } else {
                Timber.tag(tagName).w(response.apiError())
                resetFlipperState()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     */
    override fun onFailure(call: Call<List<UserBase>>, throwable: Throwable) {
        try {
            Timber.w(throwable)
            resetFlipperState()
        } catch (e: Exception) {
            Timber.e(throwable)
        }
    }
}
