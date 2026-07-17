package com.mxt.anitrend.base.custom.view.container

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import android.widget.AbsListView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.NestedScrollingParent
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import com.mxt.anitrend.base.custom.view.drawable.MaterialProgressDrawable
import com.mxt.anitrend.base.custom.view.image.CircleImageView

/**
 * Created by max on 2017/12/05.
 * Both way swipe refresh layout.
 *
 * This is a more powerful [androidx.swiperefreshlayout.widget.SwipeRefreshLayout], it can swipe
 * to refresh and load.
 *
 * @author wangdaye MySplash
 */
class CustomSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs),
    NestedScrollingParent,
    NestedScrollingChild {

    companion object {
        const val DIRECTION_TOP = 0
        const val DIRECTION_BOTTOM = 1

        private const val MAX_ALPHA = 255
        private const val STARTING_PROGRESS_ALPHA = (0.3f * MAX_ALPHA).toInt()

        private const val CIRCLE_DIAMETER = 40

        private const val DECELERATE_INTERPOLATION_FACTOR = 2f
        private const val DRAG_RATE = 0.5f

        // Max amount of circle that can be filled by progress during swipe gesture,
        // where 1.0 is a full circle
        private const val MAX_PROGRESS_ANGLE = 0.8f

        private const val SCALE_DOWN_DURATION = 150

        private const val ALPHA_ANIMATION_DURATION = 300

        private const val ANIMATE_TO_TRIGGER_DURATION = 200

        private const val ANIMATE_TO_START_DURATION = 200

        // Default background for the progress spinner
        private const val CIRCLE_BG_LIGHT = 0xFFFAFAFA.toInt()

        // Default offset in dips from the top of the view to where the progress spinner should stop
        private const val DEFAULT_CIRCLE_TARGET = 64

        private val LAYOUT_ATTRS = intArrayOf(android.R.attr.enabled)
    }

    private var target: View? = null
    private var listener: OnRefreshAndLoadListener? = null
    private var isRefreshingFlag = false
    private var isLoadingFlag = false
    private var permitRefresh = true
    private var permitLoad = true
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private val dragTriggerDistances = floatArrayOf(-1f, -1f)

    private var totalUnconsumed = 0f
    private val nestedScrollingParentHelper: NestedScrollingParentHelper = NestedScrollingParentHelper(this)
    private val nestedScrollingChildHelper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)
    private val parentScrollConsumed = IntArray(2)
    private val parentOffsetInWindow = IntArray(2)
    private var nestedScrollInProgress = false

    private val mediumAnimationDuration: Int = resources.getInteger(android.R.integer.config_mediumAnimTime)
    private var dragOffsetDistance = 0
    private var originalOffsetCalculated = false

    private var initialDownY = 0f
    private var isBeingDragged = false
    private var scale = false
    private var returningToStart = false
    private val decelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)

    private val circleViews: Array<CircleImageView>
    private val progress: Array<MaterialProgressDrawable>

    private var from = 0
    private var startingScale = 0f

    private var scaleAnimation: Animation? = null
    private var scaleDownAnimation: Animation? = null
    private var alphaStartAnimation: Animation? = null
    private var alphaMaxAnimation: Animation? = null
    private var scaleDownToStartAnimation: Animation? = null

    private var notifyListener = false

    private val circleWidth: Int
    private val circleHeight: Int

    init {
        setWillNotDraw(false)

        val metrics: DisplayMetrics = resources.displayMetrics
        circleWidth = (CIRCLE_DIAMETER * metrics.density).toInt()
        circleHeight = (CIRCLE_DIAMETER * metrics.density).toInt()
        dragTriggerDistances[DIRECTION_TOP] = DEFAULT_CIRCLE_TARGET * metrics.density
        dragTriggerDistances[DIRECTION_BOTTOM] = DEFAULT_CIRCLE_TARGET * metrics.density

        circleViews = arrayOf(
            CircleImageView(context, CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2f),
            CircleImageView(context, CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2f),
        )
        progress = arrayOf(
            MaterialProgressDrawable(context, this),
            MaterialProgressDrawable(context, this),
        )
        for (i in 0 until 2) {
            progress[i].setBackgroundColor(CIRCLE_BG_LIGHT)
            circleViews[i].setImageDrawable(progress[i])
            circleViews[i].visibility = View.GONE
            addView(circleViews[i])
        }

        isChildrenDrawingOrderEnabled = true

        setNestedScrollingEnabled(true)

        val a: TypedArray = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS)
        isEnabled = a.getBoolean(0, true)
        a.recycle()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        reset()
    }

    private fun ensureTarget() {
        if (target == null) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child != circleViews[0] && child != circleViews[1]) {
                    target = child
                    break
                }
            }
        }
    }

    fun setDragTriggerDistance(dir: Int, distance: Int) {
        var adjustedDistance = distance
        if (dir == DIRECTION_BOTTOM) {
            adjustedDistance += circleHeight
        }
        dragTriggerDistances[dir] = adjustedDistance.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (target == null) {
            ensureTarget()
        }
        target?.measure(
            MeasureSpec.makeMeasureSpec(
                measuredWidth - paddingLeft - paddingRight,
                MeasureSpec.EXACTLY,
            ),
            MeasureSpec.makeMeasureSpec(
                measuredHeight - paddingTop - paddingBottom,
                MeasureSpec.EXACTLY,
            ),
        )

        for (i in 0 until 2) {
            circleViews[i].measure(
                MeasureSpec.makeMeasureSpec(circleWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(circleHeight, MeasureSpec.EXACTLY),
            )
        }
        if (!originalOffsetCalculated) {
            originalOffsetCalculated = true
            dragOffsetDistance = 0
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = measuredWidth
        val height = measuredHeight
        if (childCount == 0) {
            return
        }
        if (target == null) {
            ensureTarget()
        }
        target?.let { child ->
            val childLeft = paddingLeft
            val childTop = paddingTop
            val childWidth = width - paddingLeft - paddingRight
            val childHeight = height - paddingTop - paddingBottom
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
        }

        if (dragOffsetDistance == 0) {
            circleViews[0].layout(
                (width / 2 - circleWidth / 2),
                -circleHeight,
                (width / 2 + circleWidth / 2),
                0,
            )
            circleViews[1].layout(
                (width / 2 - circleWidth / 2),
                measuredHeight,
                (width / 2 + circleWidth / 2),
                measuredHeight + circleHeight,
            )
        } else if (dragOffsetDistance > 0) {
            circleViews[0].layout(
                (width / 2 - circleWidth / 2),
                dragOffsetDistance - circleHeight,
                (width / 2 + circleWidth / 2),
                dragOffsetDistance,
            )
            circleViews[1].layout(
                (width / 2 - circleWidth / 2),
                measuredHeight,
                (width / 2 + circleWidth / 2),
                measuredHeight + circleHeight,
            )
        } else if (dragOffsetDistance < 0) {
            circleViews[0].layout(
                (width / 2 - circleWidth / 2),
                -circleHeight,
                (width / 2 + circleWidth / 2),
                0,
            )
            circleViews[1].layout(
                (width / 2 - circleWidth / 2),
                measuredHeight + dragOffsetDistance,
                (width / 2 + circleWidth / 2),
                measuredHeight + circleHeight + dragOffsetDistance,
            )
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        ensureTarget()

        val action = ev.actionMasked

        if (returningToStart && action == MotionEvent.ACTION_DOWN) {
            returningToStart = false
        }

        if (!isEnabled ||
            returningToStart ||
            nestedScrollInProgress ||
            isRefreshingFlag ||
            isLoadingFlag
        ) {
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val oldOffset = dragOffsetDistance
                for (i in 0 until 2) {
                    setTargetOffsetTopAndBottom(i, -oldOffset)
                }

                isBeingDragged = false
                val initialDownY = ev.y
                if (initialDownY == -1f) {
                    return false
                }
                this.initialDownY = initialDownY
            }

            MotionEvent.ACTION_MOVE -> {
                val yDiff = ev.y - initialDownY
                if (yDiff > touchSlop && !isBeingDragged && !canChildScrollUp() && permitRefresh) {
                    isBeingDragged = true
                    progress[DIRECTION_TOP].setAlpha(STARTING_PROGRESS_ALPHA)
                } else if (yDiff < -touchSlop && !isBeingDragged && !canChildScrollDown() && permitLoad) {
                    isBeingDragged = true
                    progress[DIRECTION_BOTTOM].setAlpha(STARTING_PROGRESS_ALPHA)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> {
                isBeingDragged = false
            }
        }

        return isBeingDragged
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked

        if (returningToStart && action == MotionEvent.ACTION_DOWN) {
            returningToStart = false
        }

        if (!isEnabled ||
            returningToStart ||
            nestedScrollInProgress ||
            isRefreshingFlag ||
            isLoadingFlag
        ) {
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                isBeingDragged = false
            }

            MotionEvent.ACTION_MOVE -> {
                val y = ev.y
                val offset = (y - initialDownY) * DRAG_RATE
                if (isBeingDragged) {
                    if (offset > 0 && !canChildScrollUp()) {
                        moveSpinner(DIRECTION_TOP, offset)
                    } else if (offset < 0 && !canChildScrollDown()) {
                        moveSpinner(DIRECTION_BOTTOM, offset)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                val y = ev.y
                val offset = (y - initialDownY) * DRAG_RATE
                isBeingDragged = false
                if (offset > 0 && !canChildScrollUp()) {
                    finishSpinner(DIRECTION_TOP, offset)
                } else if (offset < 0 && !canChildScrollDown()) {
                    finishSpinner(DIRECTION_BOTTOM, offset)
                }
                return false
            }

            MotionEvent.ACTION_CANCEL -> return false
        }

        return true
    }

    private fun moveSpinner(dir: Int, dragDistance: Float) {
        progress[dir].showArrow(true)
        val originalDragPercent = kotlin.math.abs(dragDistance) / dragTriggerDistances[dir]

        val dragPercent = kotlin.math.min(1f, kotlin.math.abs(originalDragPercent))
        val adjustedPercent = (kotlin.math.max(dragPercent - 0.4, 0.0) * 5 / 3).toFloat()
        val extraOS = kotlin.math.abs(dragDistance) - dragTriggerDistances[dir]
        val tensionSlingshotPercent = kotlin.math.max(
            0f,
            kotlin.math.min(extraOS, dragTriggerDistances[dir] * 2) / dragTriggerDistances[dir],
        )
        val slingshotPercent = tensionSlingshotPercent / 4f
        val tensionPercent = (slingshotPercent - (slingshotPercent * slingshotPercent)) * 2f
        val extraMove = dragTriggerDistances[dir] * tensionPercent * 2

        val offset =
            ((dragTriggerDistances[dir] * dragPercent) + extraMove).toInt() * if (dir == DIRECTION_TOP) 1 else -1

        if (circleViews[dir].visibility != View.VISIBLE) {
            circleViews[dir].visibility = View.VISIBLE
        }

        if (!scale) {
            circleViews[dir].scaleX = 1f
            circleViews[dir].scaleY = 1f
        }

        if (scale) {
            setAnimationProgress(dir, kotlin.math.min(1f, kotlin.math.abs(dragDistance / dragTriggerDistances[dir])))
        }
        if (kotlin.math.abs(dragDistance) < dragTriggerDistances[dir]) {
            if (progress[dir].alpha > STARTING_PROGRESS_ALPHA &&
                !isAnimationRunning(alphaStartAnimation)
            ) {
                startProgressAlphaStartAnimation(dir)
            }
        } else {
            if (progress[dir].alpha < MAX_ALPHA && !isAnimationRunning(alphaMaxAnimation)) {
                startProgressAlphaMaxAnimation(dir)
            }
        }
        val strokeStart = adjustedPercent * 0.8f
        progress[dir].setStartEndTrim(0f, kotlin.math.min(MAX_PROGRESS_ANGLE, strokeStart))
        progress[dir].setArrowScale(kotlin.math.min(1f, adjustedPercent))

        val rotation = (-0.25f + 0.4f * adjustedPercent + tensionPercent * 2) * 0.5f
        progress[dir].setProgressRotation(rotation)
        setTargetOffsetTopAndBottom(dir, offset - dragOffsetDistance)
    }

    private fun finishSpinner(dir: Int, dragDistance: Float) {
        if (kotlin.math.abs(dragDistance) > dragTriggerDistances[dir]) {
            if (dir == DIRECTION_TOP) {
                setRefreshing(true, true)
            } else {
                setLoading(true, true)
            }
        } else {
            if (dir == DIRECTION_TOP) {
                isRefreshingFlag = false
            } else {
                isLoadingFlag = false
            }
            progress[dir].setStartEndTrim(0f, 0f)
            var listener: Animation.AnimationListener? = null
            if (!scale) {
                listener = object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) = Unit
                    override fun onAnimationRepeat(animation: Animation) = Unit
                    override fun onAnimationEnd(animation: Animation) {
                        if (!scale) {
                            startScaleDownAnimation(dir, null)
                        }
                    }
                }
            }
            animateOffsetToStartPosition(dir, dragOffsetDistance, listener)
            progress[dir].showArrow(false)
        }
    }

    private fun isAnimationRunning(animation: Animation?): Boolean = animation != null && animation.hasStarted() && !animation.hasEnded()

    fun canChildScrollUp(): Boolean = target?.let { it.canScrollVertically(-1) } ?: false

    fun canChildScrollDown(): Boolean = target?.let { it.canScrollVertically(1) } ?: false

    override fun requestDisallowInterceptTouchEvent(b: Boolean) {
        val targetView = target
        if (android.os.Build.VERSION.SDK_INT < 21 &&
            targetView is AbsListView ||
            (targetView != null && !ViewCompat.isNestedScrollingEnabled(targetView))
        ) {
            return
        } else {
            super.requestDisallowInterceptTouchEvent(b)
        }
    }

    fun setRefreshing(refreshing: Boolean) {
        if (refreshing && (isRefreshingFlag || isLoadingFlag)) {
            return
        }
        if (refreshing) {
            circleViews[DIRECTION_BOTTOM].visibility = GONE
            isRefreshingFlag = true
            setTargetOffsetTopAndBottom(
                DIRECTION_TOP,
                (dragTriggerDistances[DIRECTION_TOP] - dragOffsetDistance).toInt(),
            )
            notifyListener = false
            startScaleUpAnimation(DIRECTION_TOP, refreshListener)
        } else {
            setRefreshing(false, false)
        }
    }

    fun setLoading(loading: Boolean) {
        if (loading && (isRefreshingFlag || isLoadingFlag)) {
            return
        }
        if (loading) {
            circleViews[DIRECTION_TOP].visibility = GONE
            isLoadingFlag = true
            setTargetOffsetTopAndBottom(
                DIRECTION_BOTTOM,
                (-dragTriggerDistances[DIRECTION_BOTTOM] - dragOffsetDistance).toInt(),
            )
            notifyListener = false
            startScaleUpAnimation(DIRECTION_BOTTOM, loadListener)
        } else {
            setLoading(false, false)
        }
    }

    private fun setRefreshing(refreshing: Boolean, notify: Boolean) {
        if (refreshing && (isRefreshingFlag || isLoadingFlag)) {
            return
        }
        if (isRefreshingFlag != refreshing) {
            notifyListener = notify
            ensureTarget()
            isRefreshingFlag = refreshing
            if (isRefreshingFlag) {
                animateOffsetToCorrectPosition(DIRECTION_TOP, dragOffsetDistance, refreshListener)
            } else {
                startScaleDownAnimation(DIRECTION_TOP, refreshListener)
            }
        }
    }

    private fun setLoading(loading: Boolean, notify: Boolean) {
        if (loading && (isRefreshingFlag || isLoadingFlag)) {
            return
        }
        if (isLoadingFlag != loading) {
            notifyListener = notify
            ensureTarget()
            isLoadingFlag = loading
            if (isLoadingFlag) {
                animateOffsetToCorrectPosition(DIRECTION_BOTTOM, dragOffsetDistance, loadListener)
            } else {
                startScaleDownAnimation(DIRECTION_BOTTOM, loadListener)
            }
        }
    }

    fun isRefreshing(): Boolean = isRefreshingFlag

    fun isLoading(): Boolean = isLoadingFlag

    fun setPermitRefresh(permit: Boolean) {
        permitRefresh = permit
        if (!permitRefresh && !permitLoad) {
            isEnabled = false
        }
    }

    fun setPermitLoad(permit: Boolean) {
        permitLoad = permit
        if (!permitRefresh && !permitLoad) {
            isEnabled = false
        }
    }

    private fun moveToStart(dir: Int, interpolatedTime: Float) {
        val offset = (from * (1 - interpolatedTime)).toInt()
        setTargetOffsetTopAndBottom(dir, offset - dragOffsetDistance)
    }

    private fun setTargetOffsetTopAndBottom(dir: Int, offset: Int) {
        circleViews[dir].bringToFront()
        circleViews[dir].offsetTopAndBottom(offset)
        dragOffsetDistance += offset
    }

    private fun reset() {
        val oldOffset = dragOffsetDistance
        for (i in 0 until 2) {
            circleViews[i].clearAnimation()
            progress[i].stop()
            circleViews[i].visibility = View.GONE
            setColorViewAlpha(i, MAX_ALPHA)
            if (scale) {
                setAnimationProgress(i, 0f)
            } else {
                setTargetOffsetTopAndBottom(i, -oldOffset)
            }
        }
        dragOffsetDistance = 0
    }

    private fun setColorViewAlpha(dir: Int, targetAlpha: Int) {
        circleViews[dir].background?.alpha = targetAlpha
        progress[dir].setAlpha(targetAlpha)
    }

    fun setProgressBackgroundColorSchemeResource(@ColorRes colorRes: Int) {
        setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, colorRes))
    }

    fun setProgressBackgroundColorSchemeColor(@ColorInt color: Int) {
        for (i in 0 until 2) {
            circleViews[i].setBackgroundColor(color)
            progress[i].setBackgroundColor(color)
        }
    }

    fun setColorSchemeResources(@ColorRes vararg colorResIds: Int) {
        val colors = IntArray(colorResIds.size)
        for (i in colorResIds.indices) {
            colors[i] = ContextCompat.getColor(context, colorResIds[i])
        }
        setColorSchemeColors(*colors)
    }

    @SuppressLint("SupportAnnotationUsage")
    @ColorInt
    fun setColorSchemeColors(vararg colors: Int) {
        ensureTarget()
        for (i in 0 until 2) {
            progress[i].setColorSchemeColors(*colors)
        }
    }

    private fun animateOffsetToCorrectPosition(dir: Int, from: Int, listener: Animation.AnimationListener?) {
        this.from = from
        if (dir == DIRECTION_TOP) {
            animateToTopCorrectPosition.reset()
            animateToTopCorrectPosition.duration = ANIMATE_TO_TRIGGER_DURATION.toLong()
            animateToTopCorrectPosition.interpolator = decelerateInterpolator
        } else {
            animateToBottomCorrectPosition.reset()
            animateToBottomCorrectPosition.duration = ANIMATE_TO_TRIGGER_DURATION.toLong()
            animateToBottomCorrectPosition.interpolator = decelerateInterpolator
        }
        if (listener != null) {
            circleViews[dir].setAnimationListener(listener)
        }
        circleViews[dir].clearAnimation()
        circleViews[dir].startAnimation(
            if (dir == DIRECTION_TOP) animateToTopCorrectPosition else animateToBottomCorrectPosition,
        )
    }

    private val animateToTopCorrectPosition: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            setTargetOffsetTopAndBottom(
                DIRECTION_TOP,
                (from + (dragTriggerDistances[DIRECTION_TOP] - from) * interpolatedTime - dragOffsetDistance).toInt(),
            )
            progress[DIRECTION_TOP].setArrowScale(1 - interpolatedTime)
        }
    }

    private val animateToBottomCorrectPosition: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            setTargetOffsetTopAndBottom(
                DIRECTION_BOTTOM,
                (from + (-dragTriggerDistances[DIRECTION_BOTTOM] - from) * interpolatedTime - dragOffsetDistance).toInt(),
            )
            progress[DIRECTION_BOTTOM].setArrowScale(1 - interpolatedTime)
        }
    }

    private fun animateOffsetToStartPosition(dir: Int, from: Int, listener: Animation.AnimationListener?) {
        if (scale) {
            if (dir == DIRECTION_TOP) {
                startScaleDownReturnToTopStartAnimation(from, listener)
            } else {
                startScaleDownReturnToBottomStartAnimation(from, listener)
            }
        } else {
            this.from = from
            if (dir == DIRECTION_TOP) {
                animateToTopStartPosition.reset()
                animateToTopStartPosition.duration = ANIMATE_TO_START_DURATION.toLong()
                animateToTopStartPosition.interpolator = decelerateInterpolator
            } else {
                animateToBottomStartPosition.reset()
                animateToBottomStartPosition.duration = ANIMATE_TO_START_DURATION.toLong()
                animateToBottomStartPosition.interpolator = decelerateInterpolator
            }
            if (listener != null) {
                circleViews[dir].setAnimationListener(listener)
            }
            circleViews[dir].clearAnimation()
            circleViews[dir].startAnimation(
                if (dir == DIRECTION_TOP) animateToTopStartPosition else animateToBottomStartPosition,
            )
        }
    }

    private val animateToTopStartPosition: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            moveToStart(DIRECTION_TOP, interpolatedTime)
        }
    }

    private val animateToBottomStartPosition: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            moveToStart(DIRECTION_BOTTOM, interpolatedTime)
        }
    }

    private fun startScaleDownReturnToTopStartAnimation(from: Int, listener: Animation.AnimationListener?) {
        this.from = from
        startingScale = circleViews[DIRECTION_TOP].scaleX
        scaleDownToStartAnimation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val targetScale = startingScale + (-startingScale * interpolatedTime)
                setAnimationProgress(DIRECTION_TOP, targetScale)
                moveToStart(DIRECTION_TOP, interpolatedTime)
            }
        }
        scaleDownToStartAnimation?.duration = SCALE_DOWN_DURATION.toLong()
        if (listener != null) {
            circleViews[DIRECTION_TOP].setAnimationListener(listener)
        }
        circleViews[DIRECTION_TOP].clearAnimation()
        circleViews[DIRECTION_TOP].startAnimation(scaleDownToStartAnimation)
    }

    private fun startScaleDownReturnToBottomStartAnimation(from: Int, listener: Animation.AnimationListener?) {
        this.from = from
        startingScale = circleViews[DIRECTION_BOTTOM].scaleX
        scaleDownToStartAnimation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val targetScale = startingScale + (-startingScale * interpolatedTime)
                setAnimationProgress(DIRECTION_BOTTOM, targetScale)
                moveToStart(DIRECTION_BOTTOM, interpolatedTime)
            }
        }
        scaleDownToStartAnimation?.duration = SCALE_DOWN_DURATION.toLong()
        if (listener != null) {
            circleViews[DIRECTION_BOTTOM].setAnimationListener(listener)
        }
        circleViews[DIRECTION_BOTTOM].clearAnimation()
        circleViews[DIRECTION_BOTTOM].startAnimation(scaleDownToStartAnimation)
    }

    private fun startScaleUpAnimation(dir: Int, listener: Animation.AnimationListener?) {
        circleViews[dir].visibility = View.VISIBLE
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            progress[dir].setAlpha(MAX_ALPHA)
        }
        scaleAnimation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                setAnimationProgress(dir, interpolatedTime)
            }
        }
        scaleAnimation?.duration = mediumAnimationDuration.toLong()
        if (listener != null) {
            circleViews[dir].setAnimationListener(listener)
        }
        circleViews[dir].clearAnimation()
        circleViews[dir].startAnimation(scaleAnimation)
    }

    private fun setAnimationProgress(dir: Int, progressAmount: Float) {
        circleViews[dir].scaleX = progressAmount
        circleViews[dir].scaleY = progressAmount
    }

    private fun startScaleDownAnimation(dir: Int, listener: Animation.AnimationListener?) {
        scaleDownAnimation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                setAnimationProgress(dir, 1 - interpolatedTime)
            }
        }
        scaleDownAnimation?.duration = SCALE_DOWN_DURATION.toLong()
        circleViews[dir].setAnimationListener(listener)
        circleViews[dir].clearAnimation()
        circleViews[dir].startAnimation(scaleDownAnimation)
    }

    private fun startProgressAlphaStartAnimation(dir: Int) {
        alphaStartAnimation = startAlphaAnimation(dir, progress[dir].alpha, STARTING_PROGRESS_ALPHA)
    }

    private fun startProgressAlphaMaxAnimation(dir: Int) {
        alphaMaxAnimation = startAlphaAnimation(dir, progress[dir].alpha, MAX_ALPHA)
    }

    private fun startAlphaAnimation(dir: Int, startingAlpha: Int, endingAlpha: Int): Animation {
        val alpha = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                progress[dir].setAlpha(
                    (startingAlpha + ((endingAlpha - startingAlpha) * interpolatedTime)).toInt(),
                )
            }
        }
        alpha.duration = ALPHA_ANIMATION_DURATION.toLong()
        circleViews[dir].setAnimationListener(null)
        circleViews[dir].clearAnimation()
        circleViews[dir].startAnimation(alpha)
        return alpha
    }

    interface OnRefreshAndLoadListener {
        fun onRefresh()
        fun onLoad()
    }

    fun setOnRefreshAndLoadListener(listener: OnRefreshAndLoadListener?) {
        this.listener = listener
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean = isEnabled &&
        !returningToStart &&
        !isRefreshingFlag &&
        !isLoadingFlag &&
        (permitRefresh || permitLoad) &&
        (nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
        totalUnconsumed = 0f
        nestedScrollInProgress = true
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (dy > 0 && totalUnconsumed > 0) {
            if (dy.toFloat() > totalUnconsumed) {
                consumed[1] = totalUnconsumed.toInt()
                totalUnconsumed = 0f
            } else {
                totalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
            moveSpinner(DIRECTION_TOP, totalUnconsumed)
        } else if (dy < 0 && totalUnconsumed < 0) {
            if (dy.toFloat() < totalUnconsumed) {
                consumed[1] = totalUnconsumed.toInt()
                totalUnconsumed = 0f
            } else {
                totalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
            moveSpinner(DIRECTION_BOTTOM, totalUnconsumed)
        }

        val parentConsumed = parentScrollConsumed
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }
    }

    override fun getNestedScrollAxes(): Int = nestedScrollingParentHelper.nestedScrollAxes

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
    ) {
        dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            parentOffsetInWindow,
        )

        val dy = dyUnconsumed + parentOffsetInWindow[1]
        if (dy < 0 && !canChildScrollUp() && !isRefreshingFlag && permitRefresh) {
            totalUnconsumed -= dy
            moveSpinner(DIRECTION_TOP, totalUnconsumed)
        } else if (dy > 0 && !canChildScrollDown() && !isLoadingFlag && permitLoad) {
            totalUnconsumed -= dy
            moveSpinner(DIRECTION_BOTTOM, totalUnconsumed)
        }
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean = dispatchNestedPreFling(velocityX, velocityY)

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean = dispatchNestedFling(velocityX, velocityY, consumed)

    override fun onStopNestedScroll(target: View) {
        nestedScrollingParentHelper.onStopNestedScroll(target)
        nestedScrollInProgress = false
        if (totalUnconsumed > 0) {
            finishSpinner(DIRECTION_TOP, totalUnconsumed)
        } else if (totalUnconsumed < 0) {
            finishSpinner(DIRECTION_BOTTOM, totalUnconsumed)
        }
        totalUnconsumed = 0f
        stopNestedScroll()
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        nestedScrollingChildHelper.setNestedScrollingEnabled(enabled)
    }

    override fun isNestedScrollingEnabled(): Boolean = nestedScrollingChildHelper.isNestedScrollingEnabled

    override fun startNestedScroll(axes: Int): Boolean = nestedScrollingChildHelper.startNestedScroll(axes)

    override fun stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean = nestedScrollingChildHelper.hasNestedScrollingParent()

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
    ): Boolean = nestedScrollingChildHelper.dispatchNestedScroll(
        dxConsumed,
        dyConsumed,
        dxUnconsumed,
        dyUnconsumed,
        offsetInWindow,
    )

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean = nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean = nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean = nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)

    private val refreshListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) = Unit
        override fun onAnimationRepeat(animation: Animation) = Unit
        override fun onAnimationEnd(animation: Animation) {
            if (isRefreshingFlag) {
                progress[DIRECTION_TOP].setAlpha(MAX_ALPHA)
                progress[DIRECTION_TOP].start()
                if (notifyListener) {
                    listener?.onRefresh()
                }
                dragOffsetDistance = dragTriggerDistances[DIRECTION_TOP].toInt()
            } else {
                reset()
            }
        }
    }

    private val loadListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) = Unit
        override fun onAnimationRepeat(animation: Animation) = Unit
        override fun onAnimationEnd(animation: Animation) {
            if (isLoadingFlag) {
                progress[DIRECTION_BOTTOM].setAlpha(MAX_ALPHA)
                progress[DIRECTION_BOTTOM].start()
                if (notifyListener) {
                    listener?.onLoad()
                }
                dragOffsetDistance = -dragTriggerDistances[DIRECTION_BOTTOM].toInt()
            } else {
                reset()
            }
        }
    }
}
