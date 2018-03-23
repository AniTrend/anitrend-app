package com.mxt.anitrend.base.custom.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.base.StatsRing;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 2017/12/01.
 * Originally created by ldoublem
 * https://github.com/ldoublem/RingProgress
 */

public class RingProgress extends View implements CustomView {

    private Paint mPaint;
    private Bitmap mBitmapBg;
    private Paint mPaintText;
    private int SweepAngle = 180;
    private int mPadding;
    private int mWidth;

    private int ringWidth = 0;

    private int rotateAngle = 270;

    private int bgShadowColor = Color.argb(100, 0, 0, 0);
    private int bgColor = Color.rgb(141, 141, 141);

    private int[] colorSetsStart = new int[] { 0x6fc1ea, 0x48c76d, 0xf7464a, 0x46bfbd,
            0xfba640, 0x615ae8, 0xec89cb, 0x87837e, 0x8BC34A, 0x46529a
    };

    private int[] colorSetsEnd = new int[] { 0xf06fc1ea, 0xf048c76d, 0xf0f7464a, 0xf046bfbd,
            0xf0fba640, 0xf0615ae8, 0xf0ec89cb, 0xf087837e, 0xf08BC34A, 0xf046529a
    };

    private List<StatsRing> mListStatsRing = new ArrayList<>();
    private RectF rectFBg = new RectF();
    private boolean isCorner = true;
    private boolean isDrawBg = true;
    private boolean isDrawBgShadow = true;
    private float ringWidthScale = 0f;
    private boolean bgChange = false;

    public RingProgress(Context context) {
        super(context);
        onInit();
    }

    public RingProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        onInit();
    }

    public RingProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RingProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        mPaint = new Paint();

        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setColor(Color.WHITE);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RingProgress);
        if (typedArray != null) {
            isCorner = typedArray.getBoolean(R.styleable.RingProgress_showRingCorner, false);
            isDrawBg = typedArray.getBoolean(R.styleable.RingProgress_showBackground, false);
            isDrawBgShadow = typedArray.getBoolean(R.styleable.RingProgress_showBackgroundShadow, false);
            rotateAngle = typedArray.getInt(R.styleable.RingProgress_rotate, 270);
            ringWidthScale = typedArray.getFloat(R.styleable.RingProgress_ringWidthScale, 0.5f);
            bgShadowColor = typedArray.getColor(R.styleable.RingProgress_bgShadowColor, bgShadowColor);
            bgColor = typedArray.getColor(R.styleable.RingProgress_bgColor, bgColor);
            SweepAngle = typedArray.getInt(R.styleable.RingProgress_ringSweepAngle, 180);
            typedArray.recycle();
        }
    }

    public int getSweepAngle() {
        return SweepAngle;
    }

    public void setSweepAngle(int sweepAngle) {

        if (sweepAngle < 0)
            sweepAngle = 0;
        else if (sweepAngle > 360)
            sweepAngle = 360;
        SweepAngle = sweepAngle;
        bgChange = true;
        invalidate();
    }

    public int getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(int rotateAngle) {

        if (rotateAngle < 0)
            rotateAngle = 0;
        else if (rotateAngle > 360)
            rotateAngle = 360;


        this.rotateAngle = rotateAngle;
        invalidate();
    }

    public boolean isCorner() {
        return isCorner;
    }

    public void setCorner(boolean corner) {
        isCorner = corner;
        bgChange = true;
        invalidate();
    }

    public boolean isDrawBgShadow() {
        return isDrawBgShadow;
    }

    public void setDrawBgShadow(boolean drawBgShadow) {
        isDrawBgShadow = drawBgShadow;
        bgChange = true;
        invalidate();
    }

    public void setDrawBgShadow(boolean drawBgShadow, int color) {
        isDrawBgShadow = drawBgShadow;
        this.bgShadowColor = color;
        bgChange = true;
        invalidate();
    }


    public boolean isDrawBg() {
        return isDrawBg;
    }

    public void setDrawBg(boolean drawBg) {
        isDrawBg = drawBg;
        bgChange = true;
        invalidate();
    }

    public void setDrawBg(boolean drawBg, int color) {
        isDrawBg = drawBg;
        this.bgColor = color;
        bgChange = true;
        invalidate();
    }

    public List<StatsRing> getmListStatsRing() {
        return mListStatsRing;
    }

    public void setmListStatsRing(List<StatsRing> mListStatsRing) {
        this.mListStatsRing = mListStatsRing;
    }


    public float getRingWidthScale() {
        return ringWidthScale;
    }

    public void setRingWidthScale(float ringWidthScale) {
        this.ringWidthScale = ringWidthScale;
        bgChange = true;
        invalidate();
    }


    public void setData(List<StatsRing> mListStatsRing, int time) {
        this.mListStatsRing.clear();
        for (int i = 0; i < mListStatsRing.size(); i++) {
            RectF r = new RectF();
            r.top = rectFBg.top + ringWidth * i;
            r.bottom = rectFBg.bottom - ringWidth * i;
            r.left = rectFBg.left + ringWidth * i;
            r.right = rectFBg.right - ringWidth * i;
            mListStatsRing.get(i).setRectFRing(r);
        }
        this.mListStatsRing.addAll(mListStatsRing);
        if (time > 0)
            startAnim(time);
        else
            invalidate();
    }

    private void setmBitmapBg(Paint paint, Bitmap mBitmapBg) {
        Canvas canvas = null;
        canvas = new Canvas(mBitmapBg);

        for (int i = 0; i < mListStatsRing.size(); i++) {
            paint.reset();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(ringWidth);
            paint.setStyle(Paint.Style.STROKE);
            if (isCorner) {
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
            }

            int red = (bgColor & 0xff0000) >> 16;
            int green = (bgColor & 0x00ff00) >> 8;
            int blue = (bgColor & 0x0000ff);
            int colorvaluer = red + (255 - red) / mListStatsRing.size() * i;
            int colorvalueg = green + (255 - green) / mListStatsRing.size() * i;
            int colorvalueb = blue + (255 - blue) / mListStatsRing.size() * i;

            paint.setColor(Color.rgb(colorvaluer, colorvalueg, colorvalueb));


            Path pathBg = new Path();
            RectF r = new RectF();
            r.top = rectFBg.top + ringWidth * i;
            r.bottom = rectFBg.bottom - ringWidth * i;
            r.left = rectFBg.left + ringWidth * i;
            r.right = rectFBg.right - ringWidth * i;
            mListStatsRing.get(i).setRectFRing(r);

            pathBg.addArc(r, 0, SweepAngle);

            if (i == 0 && isDrawBgShadow) {
                paint.setShadowLayer(ringWidth / 3,
                        0 - ringWidth / 4,
                        0, bgShadowColor);
            }
            if (isDrawBg)
                canvas.drawPath(pathBg, paint);

        }
        bgChange = false;

    }


    private Bitmap getmBitmapBg(Paint paint) {

        if (mBitmapBg == null) {
            mBitmapBg = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            setmBitmapBg(paint, mBitmapBg);

        }

        if (bgChange) {
            mBitmapBg = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            setmBitmapBg(paint, mBitmapBg);
        }


        return mBitmapBg;

    }


    private void drawBg(Canvas canvas, Paint paint) {
        paint.setAntiAlias(true);
        canvas.drawBitmap(getmBitmapBg(paint), 0
                , 0, paint);
    }


    private void drawProgress(Canvas canvas, Paint paint) {
        for (int i = 0; i < mListStatsRing.size(); i++) {
            paint.reset();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(ringWidth);
            paint.setStyle(Paint.Style.STROKE);
            Path pathProgress = new Path();
            pathProgress.addArc(mListStatsRing.get(i).getRectFRing(), 0,

                    (int) (SweepAngle / 100f * mListStatsRing.get(i).getProgress() * mAnimatedValue)

            );

            Shader mShader = new LinearGradient(mListStatsRing.get(i).getRectFRing().left, mListStatsRing.get(i).getRectFRing().top,
                    mListStatsRing.get(i).getRectFRing().left, mListStatsRing.get(i).getRectFRing().bottom,
                    new int[]{ colorSetsStart[i], colorSetsEnd[i] }, new float[]{0f, 1f}, Shader.TileMode.CLAMP);

            paint.setShader(mShader);
            if (isCorner) {
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
            }
            canvas.drawPath(pathProgress, paint);
            paint.setShader(null);


            mPaintText.setTextSize(mPaint.getStrokeWidth() / 2);


            String textvalue = String.valueOf(mListStatsRing.get(i).getValue());

            float arc_length = (float) (Math.PI * mListStatsRing.get(i).getRectFRing().width()
                    * (mListStatsRing.get(i).getProgress() / 100f))
                    * (SweepAngle / 360f);

            float textvalue_length = getFontlength(mPaintText, textvalue);

            if (mAnimatedValue == 1) {


                if (arc_length - textvalue_length * 1.5f <= 0) {


                    float textvalue_length_one = textvalue_length * 1.0f / textvalue.length();


                    int textvalue_size = (int) (arc_length / textvalue_length_one);

                    if (textvalue_size >= textvalue.length())

                    {
                        canvas.drawTextOnPath(textvalue
                                ,
                                pathProgress,
                                10,
                                getFontHeight(mPaintText) / 3,
                                mPaintText);
                    } else {
                        String text = textvalue.substring(0, 1);
                        for (int j = 0; j < textvalue_size; j++) {
                            text = text + ".";
                        }

                        canvas.drawTextOnPath(text
                                ,
                                pathProgress,
                                10,
                                getFontHeight(mPaintText) / 3,
                                mPaintText);


                    }
                } else {

                    canvas.drawTextOnPath(textvalue
                            ,
                            pathProgress,
                            (float) (arc_length
                                    - textvalue_length * 1.5f),
                            getFontHeight(mPaintText) / 3,
                            mPaintText

                    );
                }
            }


            String text = String.valueOf(mListStatsRing.get(i).getName());


            float textlength = getFontlength(mPaintText, text);

            float textlength_one = textlength * 1.0f / text.length();

            float showtextlength = (float) (arc_length
                    - textvalue_length * 1.8f);
            if (showtextlength < 0)
                showtextlength = 0;

            float textsize = showtextlength / textlength_one;

            if (textsize > text.length()) {
                textsize = text.length();
            } else if (textsize < 1) {
                textsize = 0;
            }

            canvas.drawTextOnPath(text.substring(0, (int) textsize)
                    ,
                    pathProgress,

                    10, getFontHeight(mPaintText) / 3,
                    mPaintText

            );
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.rotate(rotateAngle, getMeasuredWidth() / 2f, getMeasuredHeight() / 2f);
        canvas.save();

        if (mListStatsRing.size() > 0)
            ringWidth = (int) (
                    mWidth / 2f / (mListStatsRing.size() + 0.5f) * (1 - ringWidthScale));
        mPadding = ringWidth;
        rectFBg.set(getMeasuredWidth() / 2 - mWidth / 2 + mPadding
                , getMeasuredHeight() / 2 - mWidth / 2 + mPadding
                , getMeasuredWidth() / 2 + mWidth / 2 - mPadding
                , getMeasuredHeight() / 2 + mWidth / 2 - mPadding);
        drawBg(canvas, mPaint);
        drawProgress(canvas, mPaint);
        canvas.restore();

    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dip2px(30), dip2px(30));
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize);
        }
        if (getMeasuredWidth() > getHeight())
            mWidth = getMeasuredHeight();
        else
            mWidth = getMeasuredWidth();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w >h)
            mWidth = h;
        else
            mWidth = w;


    }


    public float getFontlength(Paint paint, String str) {
        return paint.measureText(str);
    }

    public float getFontHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public void startAnim(int time) {
        stopAnim();
        startViewAnim(0f, 1f, time);
    }

    private ValueAnimator valueAnimator;
    private float mAnimatedValue = 1f;

    public void stopAnim() {
        if (valueAnimator != null) {
            clearAnimation();
            valueAnimator.setRepeatCount(0);
            valueAnimator.cancel();
            mAnimatedValue = 0f;
            postInvalidate();
        }
    }


    private ValueAnimator startViewAnim(float startF, final float endF, long time) {
        valueAnimator = ValueAnimator.ofFloat(startF, endF);
        valueAnimator.setDuration(time);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(0);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                mAnimatedValue = (float) valueAnimator.getAnimatedValue();
                invalidate();


            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();

        }

        return valueAnimator;
    }
}
