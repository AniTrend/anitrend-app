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

/**
 * Created by max on 2017/10/31.
 */
class HeaderImageView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr),
    CustomView {
    private var defaultMargin = 0
    private var deviceDimens = 0

    init {
        onInit()
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = ((deviceDimens - defaultMargin) * KeyUtil.WideAspectRatio).toInt()

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
    }

    override fun onInit() {
        defaultMargin = resources.getDimensionPixelSize(R.dimen.sm_margin)
        deviceDimens = resources.getDimensionPixelSize(R.dimen.nav_header_height)
    }

    override fun onViewRecycled() = Unit

    fun setImage(url: String) {
        Glide
            .with(context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade(350))
            .apply(RequestOptions.centerCropTransform())
            .into(this)
    }
}
