package com.mxt.anitrend.base.custom.view.drawable

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import androidx.annotation.IntDef
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import java.util.ArrayList
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

/**
 * Material progress drawable.
 *
 * A widget for [CustomSwipeRefreshLayout].
 */
class MaterialProgressDrawable(
    context: Context,
    private val parent: View,
) : Drawable(),
    Animatable {
    companion object {
        private val LINEAR_INTERPOLATOR: Interpolator = LinearInterpolator()
        private val MATERIAL_INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()

        private const val FULL_ROTATION = 1080.0f

        // Maps to ProgressBar.Large style
        const val LARGE = 0

        // Maps to ProgressBar default style
        const val DEFAULT = 1

        // Maps to ProgressBar default style
        private const val CIRCLE_DIAMETER = 40
        private const val CENTER_RADIUS = 8.75f
        private const val STROKE_WIDTH = 2.5f

        // Maps to ProgressBar.Large style
        private const val CIRCLE_DIAMETER_LARGE = 56
        private const val CENTER_RADIUS_LARGE = 12.5f
        private const val STROKE_WIDTH_LARGE = 3f

        private const val COLOR_START_DELAY_OFFSET = 0.75f
        private const val END_TRIM_START_DELAY_OFFSET = 0.5f
        private const val START_TRIM_DURATION_OFFSET = 0.5f

        private const val ANIMATION_DURATION = 1332

        private const val NUM_POINTS = 5f

        private const val ARROW_WIDTH = 10
        private const val ARROW_HEIGHT = 5
        private const val ARROW_OFFSET_ANGLE = 5f

        private const val ARROW_WIDTH_LARGE = 12
        private const val ARROW_HEIGHT_LARGE = 6
        private const val MAX_PROGRESS_ARC = 0.8f
    }

    @Retention(AnnotationRetention.BINARY)
    @IntDef(LARGE, DEFAULT)
    annotation class ProgressDrawableSize

    private val colors = intArrayOf(Color.BLACK)

    private val callback: Callback =
        object : Callback {
            override fun invalidateDrawable(d: Drawable) {
                invalidateSelf()
            }

            override fun scheduleDrawable(
                d: Drawable,
                what: Runnable,
                `when`: Long,
            ) {
                scheduleSelf(what, `when`)
            }

            override fun unscheduleDrawable(
                d: Drawable,
                what: Runnable,
            ) {
                unscheduleSelf(what)
            }
        }

    private val animators = ArrayList<Animation>()

    private val ring: Ring = Ring(callback)

    private var rotation = 0f

    private val resources: Resources = context.resources
    private val animation: Animation
    private var rotationCount = 0f
    private var width = 0.0
    private var height = 0.0
    var finishing = false

    init {
        ring.setColors(colors)

        updateSizes(DEFAULT)
        animation = setupAnimators()
    }

    private fun setSizeParameters(
        progressCircleWidth: Double,
        progressCircleHeight: Double,
        centerRadius: Double,
        strokeWidth: Double,
        arrowWidth: Float,
        arrowHeight: Float,
    ) {
        val metrics: DisplayMetrics = resources.displayMetrics
        val screenDensity = metrics.density

        width = progressCircleWidth * screenDensity
        height = progressCircleHeight * screenDensity
        ring.setStrokeWidth((strokeWidth * screenDensity).toFloat())
        ring.setCenterRadius(centerRadius * screenDensity)
        ring.setColorIndex(0)
        ring.setArrowDimensions(arrowWidth * screenDensity, arrowHeight * screenDensity)
        ring.setInsets(width.toInt(), height.toInt())
    }

    fun updateSizes(
        @ProgressDrawableSize size: Int,
    ) {
        if (size == LARGE) {
            setSizeParameters(
                CIRCLE_DIAMETER_LARGE.toDouble(),
                CIRCLE_DIAMETER_LARGE.toDouble(),
                CENTER_RADIUS_LARGE.toDouble(),
                STROKE_WIDTH_LARGE.toDouble(),
                ARROW_WIDTH_LARGE.toFloat(),
                ARROW_HEIGHT_LARGE.toFloat(),
            )
        } else {
            setSizeParameters(
                CIRCLE_DIAMETER.toDouble(),
                CIRCLE_DIAMETER.toDouble(),
                CENTER_RADIUS.toDouble(),
                STROKE_WIDTH.toDouble(),
                ARROW_WIDTH.toFloat(),
                ARROW_HEIGHT.toFloat(),
            )
        }
    }

    fun showArrow(show: Boolean) {
        ring.setShowArrow(show)
    }

    fun setArrowScale(scale: Float) {
        ring.setArrowScale(scale)
    }

    fun setStartEndTrim(
        startAngle: Float,
        endAngle: Float,
    ) {
        ring.setStartTrim(startAngle)
        ring.setEndTrim(endAngle)
    }

    fun setProgressRotation(rotation: Float) {
        ring.setRotation(rotation)
    }

    fun setBackgroundColor(color: Int) {
        ring.setBackgroundColor(color)
    }

    fun setColorSchemeColors(vararg colors: Int) {
        ring.setColors(colors)
        ring.setColorIndex(0)
    }

    override fun getIntrinsicHeight(): Int = height.toInt()

    override fun getIntrinsicWidth(): Int = width.toInt()

    override fun draw(c: Canvas) {
        val bounds = bounds
        val saveCount = c.save()
        c.rotate(rotation, bounds.exactCenterX(), bounds.exactCenterY())
        ring.draw(c, bounds)
        c.restoreToCount(saveCount)
    }

    override fun setAlpha(alpha: Int) {
        ring.setAlpha(alpha)
    }

    override fun getAlpha(): Int = ring.getAlpha()

    override fun setColorFilter(colorFilter: ColorFilter?) {
        ring.setColorFilter(colorFilter)
    }

    private fun setRotation(rotation: Float) {
        this.rotation = rotation
        invalidateSelf()
    }

    private fun getRotation(): Float = rotation

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun isRunning(): Boolean {
        val animators = animators
        val count = animators.size
        for (i in 0 until count) {
            val animator = animators[i]
            if (animator.hasStarted() && !animator.hasEnded()) {
                return true
            }
        }
        return false
    }

    override fun start() {
        animation.reset()
        ring.storeOriginals()
        if (ring.getEndTrim() != ring.getStartTrim()) {
            finishing = true
            animation.duration = (ANIMATION_DURATION / 2).toLong()
            parent.startAnimation(animation)
        } else {
            ring.setColorIndex(0)
            ring.resetOriginals()
            animation.duration = ANIMATION_DURATION.toLong()
            parent.startAnimation(animation)
        }
    }

    override fun stop() {
        parent.clearAnimation()
        setRotation(0f)
        ring.setShowArrow(false)
        ring.setColorIndex(0)
        ring.resetOriginals()
    }

    private fun getMinProgressArc(ring: Ring): Float = Math
        .toRadians(ring.getStrokeWidth().toDouble() / (2 * Math.PI * ring.getCenterRadius()))
        .toFloat()

    private fun evaluateColorChange(
        fraction: Float,
        startValue: Int,
        endValue: Int,
    ): Int {
        val startA = startValue shr 24 and 0xff
        val startR = startValue shr 16 and 0xff
        val startG = startValue shr 8 and 0xff
        val startB = startValue and 0xff

        val endA = endValue shr 24 and 0xff
        val endR = endValue shr 16 and 0xff
        val endG = endValue shr 8 and 0xff
        val endB = endValue and 0xff

        return (startA + (fraction * (endA - startA)).toInt() shl 24) or
            (startR + (fraction * (endR - startR)).toInt() shl 16) or
            (startG + (fraction * (endG - startG)).toInt() shl 8) or
            (startB + (fraction * (endB - startB)).toInt())
    }

    private fun updateRingColor(
        interpolatedTime: Float,
        ring: Ring,
    ) {
        if (interpolatedTime > COLOR_START_DELAY_OFFSET) {
            ring.setColor(
                evaluateColorChange(
                    (interpolatedTime - COLOR_START_DELAY_OFFSET) / (1.0f - COLOR_START_DELAY_OFFSET),
                    ring.getStartingColor(),
                    ring.getNextColor(),
                ),
            )
        }
    }

    private fun applyFinishTranslation(
        interpolatedTime: Float,
        ring: Ring,
    ) {
        updateRingColor(interpolatedTime, ring)
        val targetRotation = (floor(ring.getStartingRotation() / MAX_PROGRESS_ARC) + 1.0).toFloat()
        val minProgressArc = getMinProgressArc(ring)
        val startTrim =
            ring.getStartingStartTrim() +
                (ring.getStartingEndTrim() - minProgressArc - ring.getStartingStartTrim()) * interpolatedTime
        ring.setStartTrim(startTrim)
        ring.setEndTrim(ring.getStartingEndTrim())
        val rotation =
            ring.getStartingRotation() +
                ((targetRotation - ring.getStartingRotation()) * interpolatedTime)
        ring.setRotation(rotation)
    }

    private fun setupAnimators(): Animation {
        val ring = ring
        val animation =
            object : Animation() {
                override fun applyTransformation(
                    interpolatedTime: Float,
                    t: Transformation,
                ) {
                    if (finishing) {
                        applyFinishTranslation(interpolatedTime, ring)
                    } else {
                        val minProgressArc = getMinProgressArc(ring)
                        val startingEndTrim = ring.getStartingEndTrim()
                        val startingTrim = ring.getStartingStartTrim()
                        val startingRotation = ring.getStartingRotation()

                        updateRingColor(interpolatedTime, ring)

                        if (interpolatedTime <= START_TRIM_DURATION_OFFSET) {
                            val scaledTime = interpolatedTime / (1.0f - START_TRIM_DURATION_OFFSET)
                            val startTrim =
                                startingTrim +
                                    ((MAX_PROGRESS_ARC - minProgressArc) * MATERIAL_INTERPOLATOR.getInterpolation(scaledTime))
                            ring.setStartTrim(startTrim)
                        }

                        if (interpolatedTime > END_TRIM_START_DELAY_OFFSET) {
                            val minArc = MAX_PROGRESS_ARC - minProgressArc
                            val scaledTime =
                                (interpolatedTime - START_TRIM_DURATION_OFFSET) / (1.0f - START_TRIM_DURATION_OFFSET)
                            val endTrim =
                                startingEndTrim +
                                    (minArc * MATERIAL_INTERPOLATOR.getInterpolation(scaledTime))
                            ring.setEndTrim(endTrim)
                        }

                        val rotation = startingRotation + (0.25f * interpolatedTime)
                        ring.setRotation(rotation)

                        val groupRotation =
                            ((FULL_ROTATION / NUM_POINTS) * interpolatedTime) +
                                (FULL_ROTATION * (rotationCount / NUM_POINTS))
                        setRotation(groupRotation)
                    }
                }
            }
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.RESTART
        animation.interpolator = LINEAR_INTERPOLATOR
        animation.setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    rotationCount = 0f
                }

                override fun onAnimationEnd(animation: Animation) = Unit

                override fun onAnimationRepeat(animation: Animation) {
                    ring.storeOriginals()
                    ring.goToNextColor()
                    ring.setStartTrim(ring.getEndTrim())
                    if (finishing) {
                        finishing = false
                        animation.duration = ANIMATION_DURATION.toLong()
                        ring.setShowArrow(false)
                    } else {
                        rotationCount = (rotationCount + 1) % NUM_POINTS
                    }
                }
            },
        )
        return animation
    }

    private inner class Ring(
        private val callback: Callback,
    ) {
        private val tempBounds = RectF()
        private val paint = Paint()
        private val arrowPaint = Paint()

        private var startTrim = 0.0f
        private var endTrim = 0.0f
        private var rotation = 0.0f
        private var strokeWidth = 5.0f
        private var strokeInset = 2.5f

        private var colors: IntArray = intArrayOf()
        private var colorIndex = 0
        private var startingStartTrim = 0f
        private var startingEndTrim = 0f
        private var startingRotation = 0f
        private var showArrow = false
        private var arrow: Path? = null
        private var arrowScale = 0f
        private var ringCenterRadius = 0.0
        private var arrowWidth = 0
        private var arrowHeight = 0
        private var alpha = 0
        private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var backgroundColor = 0
        private var currentColor = 0

        init {
            paint.strokeCap = Paint.Cap.SQUARE
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE

            arrowPaint.style = Paint.Style.FILL
            arrowPaint.isAntiAlias = true
        }

        fun setBackgroundColor(color: Int) {
            backgroundColor = color
        }

        fun setArrowDimensions(
            width: Float,
            height: Float,
        ) {
            arrowWidth = width.toInt()
            arrowHeight = height.toInt()
        }

        fun draw(
            c: Canvas,
            bounds: Rect,
        ) {
            val arcBounds = tempBounds
            arcBounds.set(bounds)
            arcBounds.inset(strokeInset, strokeInset)

            val startAngle = (startTrim + rotation) * 360
            val endAngle = (endTrim + rotation) * 360
            val sweepAngle = endAngle - startAngle

            paint.color = currentColor
            c.drawArc(arcBounds, startAngle, sweepAngle, false, paint)

            drawTriangle(c, startAngle, sweepAngle, bounds)

            if (alpha < 255) {
                circlePaint.color = backgroundColor
                circlePaint.alpha = 255 - alpha
                c.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), bounds.width() / 2f, circlePaint)
            }
        }

        private fun drawTriangle(
            c: Canvas,
            startAngle: Float,
            sweepAngle: Float,
            bounds: Rect,
        ) {
            if (showArrow) {
                if (arrow == null) {
                    arrow = Path().apply { fillType = Path.FillType.EVEN_ODD }
                } else {
                    arrow?.reset()
                }

                val inset = (strokeInset / 2f) * arrowScale
                val x = (ringCenterRadius * kotlin.math.cos(0.0) + bounds.exactCenterX()).toFloat()
                val y = (ringCenterRadius * kotlin.math.sin(0.0) + bounds.exactCenterY()).toFloat()

                arrow?.moveTo(0f, 0f)
                arrow?.lineTo(arrowWidth * arrowScale, 0f)
                arrow?.lineTo((arrowWidth * arrowScale / 2), (arrowHeight * arrowScale))
                arrow?.offset(x - inset, y)
                arrow?.close()

                arrowPaint.color = currentColor
                c.rotate(startAngle + sweepAngle - ARROW_OFFSET_ANGLE, bounds.exactCenterX(), bounds.exactCenterY())
                arrow?.let { c.drawPath(it, arrowPaint) }
            }
        }

        fun setColors(colors: IntArray) {
            this.colors = colors
            setColorIndex(0)
        }

        fun setColor(color: Int) {
            currentColor = color
        }

        fun setColorIndex(index: Int) {
            colorIndex = index
            currentColor = colors[colorIndex]
        }

        fun getNextColor(): Int = colors[getNextColorIndex()]

        private fun getNextColorIndex(): Int = (colorIndex + 1) % colors.size

        fun goToNextColor() {
            setColorIndex(getNextColorIndex())
        }

        fun setColorFilter(filter: ColorFilter?) {
            paint.colorFilter = filter
            invalidateSelf()
        }

        fun setAlpha(alpha: Int) {
            this.alpha = alpha
        }

        fun getAlpha(): Int = alpha

        fun setStrokeWidth(strokeWidth: Float) {
            this.strokeWidth = strokeWidth
            paint.strokeWidth = strokeWidth
            invalidateSelf()
        }

        fun getStrokeWidth(): Float = strokeWidth

        fun setStartTrim(startTrim: Float) {
            this.startTrim = startTrim
            invalidateSelf()
        }

        fun getStartTrim(): Float = startTrim

        fun getStartingStartTrim(): Float = startingStartTrim

        fun getStartingEndTrim(): Float = startingEndTrim

        fun getStartingColor(): Int = colors[colorIndex]

        fun setEndTrim(endTrim: Float) {
            this.endTrim = endTrim
            invalidateSelf()
        }

        fun getEndTrim(): Float = endTrim

        fun setRotation(rotation: Float) {
            this.rotation = rotation
            invalidateSelf()
        }

        fun getRotation(): Float = rotation

        fun setInsets(
            width: Int,
            height: Int,
        ) {
            val minEdge = min(width, height).toFloat()
            val insets =
                if (ringCenterRadius <= 0 || minEdge < 0) {
                    ceil(strokeWidth / 2.0f)
                } else {
                    (minEdge / 2.0f - ringCenterRadius).toFloat()
                }
            strokeInset = insets
        }

        fun getInsets(): Float = strokeInset

        fun setCenterRadius(centerRadius: Double) {
            ringCenterRadius = centerRadius
        }

        fun getCenterRadius(): Double = ringCenterRadius

        fun setShowArrow(show: Boolean) {
            if (showArrow != show) {
                showArrow = show
                invalidateSelf()
            }
        }

        fun setArrowScale(scale: Float) {
            if (scale != arrowScale) {
                arrowScale = scale
                invalidateSelf()
            }
        }

        fun getStartingRotation(): Float = startingRotation

        fun storeOriginals() {
            startingStartTrim = startTrim
            startingEndTrim = endTrim
            startingRotation = rotation
        }

        fun resetOriginals() {
            startingStartTrim = 0f
            startingEndTrim = 0f
            startingRotation = 0f
            setStartTrim(0f)
            setEndTrim(0f)
            setRotation(0f)
        }

        private fun invalidateSelf() {
            callback.invalidateDrawable(this@MaterialProgressDrawable)
        }
    }
}
