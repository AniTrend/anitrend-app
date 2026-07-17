package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetButtonStateBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.apiError
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2017/11/16.
 * widget that represents the state of an
 * external user, either following or not
 */
class FollowStateWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener,
    RetroCallback<UserBase> {
    private var model: UserBase? = null
    private lateinit var binding: WidgetButtonStateBinding
    private var presenter: WidgetPresenter<UserBase>? = null
    private val tagName = FollowStateWidget::class.java.simpleName

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding = WidgetButtonStateBinding.inflate(context.getLayoutInflater(), this, true)
        presenter = WidgetPresenter(context)
        binding.widgetFlipper.setOnClickListener(this)
    }

    fun setUserModel(model: UserBase) {
        this.model = model
        val localPresenter = presenter
        if (localPresenter?.settings?.isAuthenticated == true) {
            if (!localPresenter.isCurrentUser(model)) {
                setControlText()
            } else {
                visibility = GONE
            }
        } else {
            visibility = GONE
        }
    }

    private fun setControlText() {
        val currentModel = model ?: return
        if (currentModel.isFollowing) {
            binding.buttonStateText.setText(R.string.following)
        } else {
            binding.buttonStateText.setText(R.string.follow)
        }
        resetFlipperState()
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        visibility = VISIBLE
        presenter?.onDestroy()
        resetFlipperState()
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == WidgetPresenter.LOADING_STATE) {
            binding.widgetFlipper.displayedChild = WidgetPresenter.CONTENT_STATE
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.widget_flipper -> {
                if (binding.widgetFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext()
                    val currentModel = model ?: return
                    presenter?.params?.apply {
                        putLong(KeyUtil.arg_userId, currentModel.id)
                    }
                    presenter?.requestData(KeyUtil.MUT_TOGGLE_FOLLOW, context, this)
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

    /**
     * Invoked for a received HTTP response.
     */
    override fun onResponse(
        call: Call<UserBase>,
        response: Response<UserBase>,
    ) {
        try {
            if (response.isSuccessful) {
                model?.toggleFollow()
                model?.let { presenter?.notifyAllListeners(BaseConsumer(KeyUtil.MUT_TOGGLE_FOLLOW, it), false) }
                setControlText()
            } else {
                Timber.w(response.apiError())
                setControlText()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     */
    override fun onFailure(
        call: Call<UserBase>,
        throwable: Throwable,
    ) {
        try {
            Timber.w(throwable)
            setControlText()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
