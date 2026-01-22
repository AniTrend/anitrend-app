package com.mxt.anitrend.base.custom.view.image

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/09/01.
 * Custom image view that can respect a given aspect view ratio,
 * either specify the width of the image and the height will be automatically calculated
 * or set to wrap content to automatically get the view width at runtime
 */
class AspectImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr), CustomView {

    private var spanSize: Int = 0
    private var defaultMargin: Int = 0
    private val deviceDimens = Point()

    init {
        onInit()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        if (width == 0) {
            width = (deviceDimens.x / spanSize) - defaultMargin
        }

        val height = (width * KeyUtil.AspectRatio).toInt()
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    override fun onInit() {
        defaultMargin = resources.getDimensionPixelSize(R.dimen.md_margin)
        spanSize = resources.getInteger(R.integer.grid_list_x2)
        CompatUtil.getScreenDimens(deviceDimens, context)
    }

    override fun onViewRecycled() = Unit

    companion object {
        @JvmStatic
        fun setImage(view: AspectImageView, url: String?) {
            if (url.isNullOrBlank()) return
            Glide.with(view.context).load(url)
                .transition(DrawableTransitionOptions.withCrossFade(350))
                .apply(RequestOptions.centerCropTransform())
                .into(view)
        }

        @JvmStatic
        fun setImage(view: AspectImageView, imageBase: ImageBase?) {
            if (imageBase != null) {
                if (imageBase.extraLarge != null)
                    setImage(view, imageBase.extraLarge)
                else
                    setImage(view, imageBase.large)
            }
        }
    }
}
