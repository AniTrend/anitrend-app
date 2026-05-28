package com.mxt.anitrend.base.custom.view.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.model.entity.base.StatsRing
import java.util.ArrayList

/**
 * Created by max on 2017/12/01.
 * Originally created by ldoublem
 * https://github.com/ldoublem/RingProgress
 */
class RingProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CustomView {

    private val paint = Paint()
    private var bitmapBg: Bitmap? = null
    private val paintText = Paint()
    private var sweepAngle = 180
    private var padding = 0
    private var widthSize = 0

    private var ringWidth = 0

    private var rotateAngle = 270

    private var bgShadowColor = Color.argb(100, 0, 0, 0)
    private var bgColor = Color.rgb(141, 141, 141)

    private val colorSetsStart = intArrayOf(
        0x6fc1ea, 0x48c76d, 0xf7464a, 0x46bfbd,
        0xfba640, 0x615ae8, 0xec89cb, 0x87837e, 0x8BC34A, 0x46529a
    )

    private val colorSetsEnd = intArrayOf(
        0xf06fc1ea.toInt(), 0xf048c76d.toInt(), 0xf0f7464a.toInt(), 0xf046bfbd.toInt(),
        0xf0fba640.toInt(), 0xf0615ae8.toInt(), 0xf0ec89cb.toInt(), 0xf087837e.toInt(),
        0xf08BC34A.toInt(), 0xf046529a.toInt()
    )

    private var listStatsRing: MutableList<StatsRing> = ArrayList()
    private val rectFBg = RectF()
    private var isCorner = true
    private var isDrawBg = true
    private var isDrawBgShadow = true
    private var ringWidthScale = 0f
    private var bgChange = false

    private var valueAnimator: ValueAnimator? = null
    private var animatedValue = 1f

    init {
        if (attrs != null) {
            init(attrs)
        }
        onInit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : this(context, attrs, defStyleAttr)

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        paintText.isAntiAlias = true
        paintText.style = Paint.Style.FILL
        paintText.color = Color.WHITE
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() = Unit

    private fun init(attrs: AttributeSet) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgress)
        isCorner = typedArray.getBoolean(R.styleable.RingProgress_showRingCorner, false)
        isDrawBg = typedArray.getBoolean(R.styleable.RingProgress_showBackground, false)
        isDrawBgShadow = typedArray.getBoolean(R.styleable.RingProgress_showBackgroundShadow, false)
        rotateAngle = typedArray.getInt(R.styleable.RingProgress_rotate, 270)
        ringWidthScale = typedArray.getFloat(R.styleable.RingProgress_ringWidthScale, 0.5f)
        bgShadowColor = typedArray.getColor(R.styleable.RingProgress_bgShadowColor, bgShadowColor)
        bgColor = typedArray.getColor(R.styleable.RingProgress_bgColor, bgColor)
        sweepAngle = typedArray.getInt(R.styleable.RingProgress_ringSweepAngle, 180)
        typedArray.recycle()
    }

    fun getSweepAngle(): Int = sweepAngle

    fun setSweepAngle(sweepAngle: Int) {
        var angle = sweepAngle
        if (angle < 0)
            angle = 0
        else if (angle > 360)
            angle = 360
        this.sweepAngle = angle
        bgChange = true
        invalidate()
    }

    fun getRotateAngle(): Int = rotateAngle

    fun setRotateAngle(rotateAngle: Int) {
        var angle = rotateAngle
        if (angle < 0)
            angle = 0
        else if (angle > 360)
            angle = 360
        this.rotateAngle = angle
        invalidate()
    }

    fun isCorner(): Boolean = isCorner

    fun setCorner(corner: Boolean) {
        isCorner = corner
        bgChange = true
        invalidate()
    }

    fun isDrawBgShadow(): Boolean = isDrawBgShadow

    fun setDrawBgShadow(drawBgShadow: Boolean) {
        isDrawBgShadow = drawBgShadow
        bgChange = true
        invalidate()
    }

    fun setDrawBgShadow(drawBgShadow: Boolean, color: Int) {
        isDrawBgShadow = drawBgShadow
        bgShadowColor = color
        bgChange = true
        invalidate()
    }

    fun isDrawBg(): Boolean = isDrawBg

    fun setDrawBg(drawBg: Boolean) {
        isDrawBg = drawBg
        bgChange = true
        invalidate()
    }

    fun setDrawBg(drawBg: Boolean, color: Int) {
        isDrawBg = drawBg
        bgColor = color
        bgChange = true
        invalidate()
    }

    fun getmListStatsRing(): List<StatsRing> = listStatsRing

    fun setmListStatsRing(list: List<StatsRing>) {
        listStatsRing = list.toMutableList()
    }

    fun getRingWidthScale(): Float = ringWidthScale

    fun setRingWidthScale(ringWidthScale: Float) {
        this.ringWidthScale = ringWidthScale
        bgChange = true
        invalidate()
    }

    fun setData(list: List<StatsRing>, time: Int) {
        listStatsRing.clear()
        for (i in list.indices) {
            val r = RectF()
            r.top = rectFBg.top + ringWidth * i
            r.bottom = rectFBg.bottom - ringWidth * i
            r.left = rectFBg.left + ringWidth * i
            r.right = rectFBg.right - ringWidth * i
            list[i].rectFRing = r
        }
        listStatsRing.addAll(list)
        if (time > 0)
            startAnim(time)
        else
            invalidate()
    }

    private fun setBitmapBg(paint: Paint, bitmapBg: Bitmap) {
        val canvas = Canvas(bitmapBg)
        for (i in listStatsRing.indices) {
            paint.reset()
            paint.isAntiAlias = true
            paint.strokeWidth = ringWidth.toFloat()
            paint.style = Paint.Style.STROKE
            if (isCorner) {
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeJoin = Paint.Join.ROUND
            }

            val red = (bgColor and 0xff0000) shr 16
            val green = (bgColor and 0x00ff00) shr 8
            val blue = (bgColor and 0x0000ff)
            val colorvaluer = red + (255 - red) / listStatsRing.size * i
            val colorvalueg = green + (255 - green) / listStatsRing.size * i
            val colorvalueb = blue + (255 - blue) / listStatsRing.size * i

            paint.color = Color.rgb(colorvaluer, colorvalueg, colorvalueb)

            val pathBg = Path()
            val r = RectF()
            r.top = rectFBg.top + ringWidth * i
            r.bottom = rectFBg.bottom - ringWidth * i
            r.left = rectFBg.left + ringWidth * i
            r.right = rectFBg.right - ringWidth * i
            listStatsRing[i].rectFRing = r

            pathBg.addArc(r, 0f, sweepAngle.toFloat())

            if (i == 0 && isDrawBgShadow) {
                paint.setShadowLayer(
                    ringWidth / 3f,
                    0 - ringWidth / 4f,
                    0f,
                    bgShadowColor
                )
            }
            if (isDrawBg)
                canvas.drawPath(pathBg, paint)
        }
        bgChange = false
    }

    private fun getBitmapBg(paint: Paint): Bitmap {
        if (bitmapBg == null || bgChange) {
            val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            bitmapBg = bitmap
            setBitmapBg(paint, bitmap)
        }
        return requireNotNull(bitmapBg)
    }

    private fun drawBg(canvas: Canvas, paint: Paint) {
        paint.isAntiAlias = true
        canvas.drawBitmap(getBitmapBg(paint), 0f, 0f, paint)
    }

    private fun drawProgress(canvas: Canvas, paint: Paint) {
        for (i in listStatsRing.indices) {
            paint.reset()
            paint.isAntiAlias = true
            paint.strokeWidth = ringWidth.toFloat()
            paint.style = Paint.Style.STROKE
            val pathProgress = Path()
            pathProgress.addArc(
                listStatsRing[i].rectFRing,
                0f,
                (sweepAngle / 100f * listStatsRing[i].progress * animatedValue)
            )

            val shader: Shader = LinearGradient(
                listStatsRing[i].rectFRing.left,
                listStatsRing[i].rectFRing.top,
                listStatsRing[i].rectFRing.left,
                listStatsRing[i].rectFRing.bottom,
                intArrayOf(colorSetsStart[i], colorSetsEnd[i]),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )

            paint.shader = shader
            if (isCorner) {
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeJoin = Paint.Join.ROUND
            }
            canvas.drawPath(pathProgress, paint)
            paint.shader = null

            paintText.textSize = paint.strokeWidth / 2

            val textValue = listStatsRing[i].value.toString()

            val arcLength = (Math.PI * listStatsRing[i].rectFRing.width()
                    * (listStatsRing[i].progress / 100f)).toFloat() * (sweepAngle / 360f)

            val textValueLength = getFontlength(paintText, textValue)

            if (animatedValue == 1f) {
                if (arcLength - textValueLength * 1.5f <= 0) {
                    val textValueLengthOne = textValueLength * 1.0f / textValue.length
                    val textValueSize = (arcLength / textValueLengthOne).toInt()

                    if (textValueSize >= textValue.length) {
                        canvas.drawTextOnPath(
                            textValue,
                            pathProgress,
                            10f,
                            getFontHeight(paintText) / 3,
                            paintText
                        )
                    } else {
                        var text = textValue.substring(0, 1)
                        for (j in 0 until textValueSize) {
                            text += "."
                        }

                        canvas.drawTextOnPath(
                            text,
                            pathProgress,
                            10f,
                            getFontHeight(paintText) / 3,
                            paintText
                        )
                    }
                } else {
                    canvas.drawTextOnPath(
                        textValue,
                        pathProgress,
                        arcLength - textValueLength * 1.5f,
                        getFontHeight(paintText) / 3,
                        paintText
                    )
                }
            }

            val text = listStatsRing[i].name.toString()
            val textLength = getFontlength(paintText, text)
            val textLengthOne = textLength * 1.0f / text.length
            var showTextLength = arcLength - textValueLength * 1.8f
            if (showTextLength < 0)
                showTextLength = 0f

            var textSize = showTextLength / textLengthOne

            if (textSize > text.length) {
                textSize = text.length.toFloat()
            } else if (textSize < 1) {
                textSize = 0f
            }

            canvas.drawTextOnPath(
                text.substring(0, textSize.toInt()),
                pathProgress,
                10f,
                getFontHeight(paintText) / 3,
                paintText
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.rotate(rotateAngle.toFloat(), measuredWidth / 2f, measuredHeight / 2f)
        canvas.save()

        if (listStatsRing.isNotEmpty())
            ringWidth = (widthSize / 2f / (listStatsRing.size + 0.5f) * (1 - ringWidthScale)).toInt()
        padding = ringWidth
        rectFBg.set(
            measuredWidth / 2f - widthSize / 2f + padding,
            measuredHeight / 2f - widthSize / 2f + padding,
            measuredWidth / 2f + widthSize / 2f - padding,
            measuredHeight / 2f + widthSize / 2f - padding
        )
        drawBg(canvas, paint)
        drawProgress(canvas, paint)
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dip2px(30f), dip2px(30f))
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize)
        }
        widthSize = if (measuredWidth > height) measuredHeight else measuredWidth
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthSize = if (w > h) h else w
    }

    fun getFontlength(paint: Paint, str: String): Float = paint.measureText(str)

    fun getFontHeight(paint: Paint): Float {
        val fm = paint.fontMetrics
        return fm.descent - fm.ascent
    }

    fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun startAnim(time: Int) {
        stopAnim()
        startViewAnim(0f, 1f, time.toLong())
    }

    fun stopAnim() {
        valueAnimator?.let { animator ->
            clearAnimation()
            animator.repeatCount = 0
            animator.cancel()
            animatedValue = 0f
            postInvalidate()
        }
    }

    private fun startViewAnim(startF: Float, endF: Float, time: Long): ValueAnimator {
        valueAnimator = ValueAnimator.ofFloat(startF, endF).apply {
            duration = time
            interpolator = LinearInterpolator()
            repeatCount = 0
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animator ->
                this@RingProgress.animatedValue = animator.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                }

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                }

                override fun onAnimationRepeat(animation: Animator) {
                    super.onAnimationRepeat(animation)
                }
            })
            if (!isRunning) {
                start()
            }
        }
        return valueAnimator as ValueAnimator
    }
}
