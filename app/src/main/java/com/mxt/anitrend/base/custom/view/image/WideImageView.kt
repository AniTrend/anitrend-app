package com.mxt.anitrend.base.custom.view.image

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.util.KeyUtil
import timber.log.Timber

/**
 * Created by max on 2017/10/30.
 * 16 x 10 Aspect image view
 */
class WideImageView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr),
    CustomView {
    private var defaultMargin = 0
    private var defaultDimens = 0

    init {
        onInit()
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        var height =
            ((MeasureSpec.getSize(heightMeasureSpec) - defaultMargin) * KeyUtil.WideAspectRatio).toInt()

        if (heightMeasureSpec == 0) {
            height = ((defaultDimens - defaultMargin) * KeyUtil.WideAspectRatio).toInt()
        }

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
    }

    override fun onInit() {
        defaultMargin = resources.getDimensionPixelSize(R.dimen.sm_margin)
        defaultDimens = resources.getDimensionPixelSize(R.dimen.app_bar_height)
    }

    override fun onViewRecycled() = Unit

    companion object {
        @JvmStatic
        fun setImage(
            view: WideImageView,
            url: String?,
        ) {
            try {
                if (url != null) {
                    Glide
                        .with(view.context)
                        .load(url)
                        .transition(DrawableTransitionOptions.withCrossFade(350))
                        .apply(RequestOptions.centerCropTransform())
                        .into(view)
                } else {
                    Glide
                        .with(view.context)
                        .load(R.drawable.reg_bg)
                        .transition(DrawableTransitionOptions.withCrossFade(350))
                        .apply(RequestOptions.centerCropTransform())
                        .into(view)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}
