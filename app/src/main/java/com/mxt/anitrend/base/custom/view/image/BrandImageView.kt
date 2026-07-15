package com.mxt.anitrend.base.custom.view.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import androidx.appcompat.widget.AppCompatImageView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.util.CompatUtil

/**
 * Created by max on 2017/12/10.
 * always 4:3 aspect ratio images
 * borrowed functionality from plaid BadgedFourThreeImage
 */
class BrandImageView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr),
    CustomView {
    private var badge: Drawable = GifBadge(context)
    private var badgeBoundsSet = true
    private var badgeGravity = Gravity.END or Gravity.TOP
    private var badgePadding = 0

    private var spanSize = 0
    private val deviceDimens = Point()

    init {
        onInit()
    }

    override fun onInit() {
        badge = GifBadge(context)
        badgeGravity = Gravity.END or Gravity.TOP
        CompatUtil.getScreenDimens(deviceDimens, context)
        spanSize = resources.getInteger(R.integer.grid_giphy_x3)
        badgePadding = resources.getDimensionPixelSize(R.dimen.lg_margin)
        badge.setColorFilter(
            context.getCompatColorAttr(R.attr.colorOnSurface),
            PorterDuff.Mode.SRC_IN,
        )
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        if (width == 0) {
            width = (deviceDimens.x / spanSize) - badgePadding
        }

        val height = (width * (3.3f / 4f)).toInt()
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (!badgeBoundsSet) {
            layoutBadge()
        }
        badge.draw(canvas)
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutBadge()
    }

    private fun layoutBadge() {
        val badgeBounds = badge.bounds
        Gravity.apply(
            badgeGravity,
            badge.intrinsicWidth,
            badge.intrinsicHeight,
            Rect(0, 0, width, height),
            badgePadding,
            badgePadding,
            badgeBounds,
        )
        badge.bounds = badgeBounds
        badgeBoundsSet = true
    }

    override fun onViewRecycled() = Unit

    private class GifBadge(
        context: Context,
    ) : Drawable() {
        private val paint: Paint = Paint()

        override fun getIntrinsicWidth(): Int = width

        override fun getIntrinsicHeight(): Int = height

        override fun draw(canvas: Canvas) {
            bitmap?.let { image ->
                canvas.drawBitmap(image, bounds.left.toFloat(), bounds.top.toFloat(), paint)
            }
        }

        override fun setAlpha(alpha: Int) = Unit

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

        companion object {
            private const val GIF = "GIPHY"
            private const val TEXT_SIZE = 8 // sp
            private const val PADDING = 4 // dp
            private const val CORNER_RADIUS = 2 // dp
            private const val BACKGROUND_COLOR = Color.WHITE
            private const val TYPEFACE = "sans-serif-black"
            private const val TYPEFACE_STYLE = Typeface.NORMAL

            private var bitmap: Bitmap? = null
            private var width: Int
            private var height: Int

            init {
                width = 0
                height = 0
            }
        }

        init {
            if (bitmap == null) {
                val dm: DisplayMetrics = context.resources.displayMetrics
                val density = dm.density
                val scaledDensity = dm.scaledDensity
                val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
                textPaint.typeface = Typeface.create(TYPEFACE, TYPEFACE_STYLE)
                textPaint.textSize = TEXT_SIZE * scaledDensity

                val padding = PADDING * density
                val cornerRadius = CORNER_RADIUS * density
                val textBounds = Rect()
                textPaint.getTextBounds(GIF, 0, GIF.length, textBounds)
                height = (padding + textBounds.height() + padding).toInt()
                width = (padding + textBounds.width() + padding).toInt()
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap?.setHasAlpha(true)
                bitmap?.let { safeBitmap ->
                    val canvas = Canvas(safeBitmap)
                    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    backgroundPaint.color = BACKGROUND_COLOR
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, backgroundPaint)
                    } else {
                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
                    }
                    textPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    canvas.drawText(GIF, padding, height - padding, textPaint)
                }
            }
        }
    }
}
