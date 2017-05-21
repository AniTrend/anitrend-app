package com.mxt.anitrend.custom;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.mxt.anitrend.R;
import com.mxt.anitrend.utils.ApplicationPrefs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TacticalTwerking on 16/6/23.
 * GitHub   https://github.com/TacticalTwerking
 */
public class PolygonView  extends View {

    private static final long ANIMATION_DURATION = 800L;
    private static final long ANIMATION_DELAY = 50L;
    private int mSize = -1;
    private int mCirclePadding = -1;
    private int mViewCenter = -1;
    private int mCircleStrokeWidth = 4;
    private int mSides = 5;
    private int mActuallyRadius = -1;
    private int mHighLightPadding = 5;
    private int mRotateOffset = 90;
    private float mPieces = 0;
    private float mMinimalValuesPercentage = .5f;
    private float[] mProgressValues;
    private float[] mAnimationProgress;
    private String[] mLabels = new String[]{};
    private Paint mPaintCircle;
    private Paint mPaintNodes;
    private Paint mPaintArcs;
    private Paint mPaintInnerLines;
    private Paint mPaintLabels;
    private boolean mAnimationRunning = false;
    private ApplicationPrefs prefs;
    private float maxValue;

    public PolygonView(Context context) {
        super(context);
        prefs = new ApplicationPrefs(context);
        init();
    }

    public PolygonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        prefs = new ApplicationPrefs(context);
        init();
    }

    public PolygonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        prefs = new ApplicationPrefs(context);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PolygonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        prefs = new ApplicationPrefs(context);
        init();
    }

    public void initial(int side,float []values){
        if (mAnimationRunning){
            return;
        }
        mSides = side;
        mProgressValues = values;
        mPieces = mSides > 0 ? 360 / mSides : 0;
    }

    public void initial(int side,float []values,String []labels){
        if (mAnimationRunning){
            return;
        }
        mSides = side;
        mProgressValues = values;
        mLabels = labels;
        mPieces = mSides > 0 ? 360 / mSides : 0;
    }

    public void animateProgress() {

        if (mAnimationRunning) {
            return;
        }

        mAnimationProgress = new float[mSides];
        for (int i = 0; i < mSides; i++) {
            mAnimationRunning = true;
            final int finalI = i;
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(ANIMATION_DURATION);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mAnimationProgress[finalI] = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator.setStartDelay(ANIMATION_DELAY * i);
            if (finalI== mSides -1){
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimationRunning = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mAnimationRunning = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
            }
            animator.start();
        }

    }

    private void init() {

        mPaintCircle = new Paint();
        mPaintCircle.setStrokeWidth(mCircleStrokeWidth);
        mPaintCircle.setColor(Color.parseColor("#263238"));
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setStrokeCap(Paint.Cap.ROUND);
        mPaintCircle.setShader(null);
        mPaintCircle.setStyle(Paint.Style.STROKE);

        mPaintNodes = new Paint(mPaintCircle);
        mPaintNodes.setColor(Color.parseColor("#7fB1EDDE"));
        mPaintNodes.setStrokeWidth(2);
        mPaintNodes.setStyle(Paint.Style.FILL);

        mPaintArcs = new Paint(mPaintCircle);
        mPaintArcs.setColor(Color.parseColor("#00ff26"));
        mPaintArcs.setStrokeCap(Paint.Cap.SQUARE);
        mPaintArcs.setStrokeWidth(5);

        mPaintInnerLines = new Paint(mPaintCircle);
        mPaintInnerLines.setStrokeWidth(1);
        mPaintInnerLines.setColor(Color.parseColor("#009688"));


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initProperties();
        drawCircle(canvas);

        for (int i = 0; i < mSides; i++) {
            double angle = ((Math.PI * 2 / mSides) * i) - (Math.toRadians(mRotateOffset));

            drawInnerLines(canvas,angle);
            drawArcs(canvas,i);
            //DrawNode belong here
            drawLabels(canvas,i,angle);
        }
        drawNodes(canvas);

        drawAvatar(canvas);
    }

    private Bitmap mBitmapAvatar;

    private void drawAvatar(Canvas canvas) {
        Paint pAvtBg = new Paint();
        pAvtBg.setAntiAlias(true);
        pAvtBg.setColor(Color.WHITE);
        pAvtBg.setStyle(Paint.Style.FILL);

        canvas.drawCircle(mViewCenter, mViewCenter, mActuallyRadius * mMinimalValuesPercentage * .6f, pAvtBg);
        canvas.drawBitmap(mBitmapAvatar, mViewCenter - (mBitmapAvatar.getWidth() / 2), mViewCenter - (mBitmapAvatar.getHeight() / 2), mPaintArcs);


    }


    private void drawArcs(Canvas canvas,int i) {

        List<Integer> maxValues = getMaxValues(mProgressValues);
        RectF rectF = new RectF(mCirclePadding, mCirclePadding, (mActuallyRadius * 2) + mCirclePadding, (mActuallyRadius * 2) + mCirclePadding);
        if (maxValues.size()>i){
            int sweepAngle = (int) (mPieces * mAnimationProgress[maxValues.get(i)]);
            int startAngle = (int) ((maxValues.get(i) * mPieces) - (sweepAngle / 2) - mRotateOffset);
            if (mSides == 0) {
                mHighLightPadding = 0;
            }

            if (sweepAngle != 0) {
                canvas.drawArc(rectF, startAngle + mHighLightPadding, sweepAngle - mHighLightPadding, false, mPaintArcs);
            }
        }
    }

    private void drawLabels(Canvas canvas,int i,double angle) {
        if (null == mLabels || mLabels.length != mSides) {
            return;
        }

        int maxLength = mActuallyRadius;

        String strNumericValues = new DecimalFormat("##").format(mProgressValues[i] * mAnimationProgress[i] * 100);

        Rect textBoundLabel = new Rect();
        Rect textBoundNumeric = new Rect();

        mPaintLabels.getTextBounds(mLabels[i], 0, mLabels[i].length(), textBoundLabel);
        mPaintLabels.getTextBounds(strNumericValues, 0, strNumericValues.length(), textBoundNumeric);

        float actuallyValues = maxLength + textBoundLabel.width();

        float x = (int) (Math.cos(angle) * actuallyValues + mViewCenter);
        float y = (int) (Math.sin(angle) * actuallyValues + mViewCenter);

        canvas.drawText(mLabels[i], x - (textBoundLabel.width() / 2), y + (textBoundLabel.height() / 2), mPaintLabels);

        canvas.drawText(strNumericValues, x - (textBoundNumeric.width() / 2), y - (textBoundLabel.height() / 2), mPaintLabels);

    }

    private void drawInnerLines(Canvas canvas,double angle) {
        float actuallyValues = mActuallyRadius;
        float x = (int) (Math.cos(angle) * actuallyValues + mViewCenter);
        float y = (int) (Math.sin(angle) * actuallyValues + mViewCenter);
        canvas.drawLine( mViewCenter, mViewCenter,x,y, mPaintInnerLines);
    }


    private void drawNodes(Canvas canvas) {

        Path path = new Path();
        float minimalValues = 0;
        float startX = 0;
        float startY = 0;
        float upper_max = 0;
        float temp;
        int maxHill = mActuallyRadius - mCircleStrokeWidth * 2;

        if (mMinimalValuesPercentage > 0) {
            minimalValues = maxHill * mMinimalValuesPercentage;
        }
        for (int i = 0; i < mSides; i++) {
            if(mProgressValues[i] > upper_max)
                upper_max = mProgressValues[i];

            temp = mProgressValues[i] / upper_max;
            float actuallyValues = temp * maxHill * mAnimationProgress[i];
            if (minimalValues > 0) {
                actuallyValues = minimalValues + (maxHill - minimalValues) * temp * mAnimationProgress[i];
            }

            double angle = ((Math.PI * 2 / mSides) * i) - (Math.toRadians(mRotateOffset));

            float x = (int) (Math.cos(angle) * actuallyValues + mViewCenter);
            float y = (int) (Math.sin(angle) * actuallyValues + mViewCenter);

            if (i == 0) {
                startX = x;
                startY = y;
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.lineTo(startX, startY);
        canvas.drawPath(path, mPaintNodes);
    }

    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(mViewCenter, mViewCenter, mActuallyRadius, mPaintCircle);
        canvas.drawCircle(mViewCenter, mViewCenter, mActuallyRadius / 2, mPaintInnerLines);
    }

    private void initProperties() {
        if (mSize == -1) {
            mSize = Math.min(getHeight(), getWidth());
            mCirclePadding = mSize / 6;
            mViewCenter = mSize / 2;
            mActuallyRadius = mViewCenter - mCirclePadding;
            mProgressValues = new float[mSides];
            mAnimationProgress = new float[mSides];


            mPaintLabels = new Paint(mPaintInnerLines);
            mPaintLabels.setTextSize(mSize / 35);
            mPaintLabels.setColor(prefs.isLightTheme()?Color.BLACK:Color.WHITE);
            mPaintLabels.setStyle(Paint.Style.FILL);
            mPaintLabels.setAntiAlias(true);
            mPieces = mSides > 0 ? 360 / mSides : 0;
            mBitmapAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        }
    }

    private List<Integer> getMaxValues(float[] source) {
        List<Integer> maxArrayIndex = new ArrayList<>();
        this.maxValue = source[0];
        int maxValuesIndex = 0;
        boolean notUnique = false;
        for (int i = 1; i < source.length; i++) {
            if (this.maxValue < source[i]) {
                this.maxValue = source[i];
                maxValuesIndex = i;
            } else if (maxValue == source[i]) {
                notUnique = true;
            }
        }
        if (notUnique) {
            for (int i = 0; i < source.length; i++) {
                if (this.maxValue == source[i]) {
                    maxArrayIndex.add(i);
                }
            }
        } else {
            maxArrayIndex.add(maxValuesIndex);
        }
        return maxArrayIndex;
    }
}
