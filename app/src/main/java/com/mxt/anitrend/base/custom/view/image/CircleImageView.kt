package com.mxt.anitrend.base.custom.view.image

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout

/**
 * Circle image view.
 *
 * A widget for [CustomSwipeRefreshLayout].
 */
class CircleImageView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {
    companion object {
        private const val KEY_SHADOW_COLOR = 0x1E000000
        private const val FILL_SHADOW_COLOR = 0x3D000000
        private const val X_OFFSET = 0f
        private const val Y_OFFSET = 1.75f
        private const val SHADOW_RADIUS = 3.5f
        private const val SHADOW_ELEVATION = 4
    }

    private var listener: Animation.AnimationListener? = null
    private var activeAnimation: Animation? = null
    private var shadowRadius = 0

    constructor(context: Context, color: Int, radius: Float) : this(context) {
        val density = resources.displayMetrics.density
        val diameter = (radius * density * 2).toInt()
        val shadowYOffset = (density * Y_OFFSET).toInt()
        val shadowXOffset = (density * X_OFFSET).toInt()

        shadowRadius = (density * SHADOW_RADIUS).toInt()

        val circle: ShapeDrawable =
            if (elevationSupported()) {
                val shape = ShapeDrawable(OvalShape())
                ViewCompat.setElevation(this, SHADOW_ELEVATION * density)
                shape
            } else {
                val oval = OvalShadow(shadowRadius, diameter)
                val shape = ShapeDrawable(oval)
                setLayerType(LAYER_TYPE_SOFTWARE, shape.paint)
                shape.paint.setShadowLayer(shadowRadius.toFloat(), shadowXOffset.toFloat(), shadowYOffset.toFloat(), KEY_SHADOW_COLOR)
                val padding = shadowRadius
                setPadding(padding, padding, padding, padding)
                shape
            }
        circle.paint.color = color
        background = circle
    }

    private fun elevationSupported(): Boolean = android.os.Build.VERSION.SDK_INT >= 21

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!elevationSupported()) {
            setMeasuredDimension(measuredWidth + shadowRadius * 2, measuredHeight + shadowRadius * 2)
        }
    }

    fun setAnimationListener(listener: Animation.AnimationListener?) {
        this.listener = listener
    }

    override fun onAnimationStart() {
        super.onAnimationStart()
        activeAnimation = animation ?: activeAnimation
        activeAnimation?.also { listener?.onAnimationStart(it) }
    }

    override fun onAnimationEnd() {
        super.onAnimationEnd()
        activeAnimation?.also { listener?.onAnimationEnd(it) }
        activeAnimation = null
    }

    /**
     * Update the background color of the circle image view.
     *
     * @param colorRes Id of a color resource.
     */
    fun setBackgroundColorRes(colorRes: Int) {
        setBackgroundColor(ContextCompat.getColor(context, colorRes))
    }

    override fun setBackgroundColor(color: Int) {
        val backgroundDrawable = background
        if (backgroundDrawable is ShapeDrawable) {
            backgroundDrawable.paint.color = color
        }
    }

    private inner class OvalShadow(
        shadowRadius: Int,
        circleDiameter: Int,
    ) : OvalShape() {
        private val radialGradient: RadialGradient
        private val shadowPaint: Paint
        private val circleDiameter: Int

        init {
            shadowPaint = Paint()
            this@CircleImageView.shadowRadius = shadowRadius
            this.circleDiameter = circleDiameter
            radialGradient =
                RadialGradient(
                    circleDiameter / 2f,
                    circleDiameter / 2f,
                    this@CircleImageView.shadowRadius.toFloat(),
                    intArrayOf(FILL_SHADOW_COLOR, Color.TRANSPARENT),
                    null,
                    Shader.TileMode.CLAMP,
                )
            shadowPaint.shader = radialGradient
        }

        override fun draw(
            canvas: Canvas,
            paint: Paint,
        ) {
            val viewWidth = this@CircleImageView.width
            val viewHeight = this@CircleImageView.height
            canvas.drawCircle(
                viewWidth / 2f,
                viewHeight / 2f,
                (circleDiameter / 2f + shadowRadius),
                shadowPaint,
            )
            canvas.drawCircle(viewWidth / 2f, viewHeight / 2f, (circleDiameter / 2f), paint)
        }
    }
}
