package com.mxt.anitrend.base.custom.recycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.LinearSnapHelper;

/**
 * 控制fling速度的RecyclerView
 *
 * Created by jameson on 9/1/16.
 */
public class SpeedRecyclerView extends RecyclerView implements CustomView {

    private static final float FLING_SCALE_DOWN_FACTOR = 0.5f; // 减速因子
    private static final int FLING_MAX_VELOCITY = 8000; // 最大顺时滑动速度
    private final LinearSnapHelper mLinearSnapHelper = new LinearSnapHelper();

    public SpeedRecyclerView(Context context) {
        super(context);
        onInit();
    }

    public SpeedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public SpeedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onInit();
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        velocityX = solveVelocity(velocityX);
        velocityY = solveVelocity(velocityY);
        return super.fling(velocityX, velocityY);
    }

    private int solveVelocity(int velocity) {
        if (velocity > 0)
            return Math.min(velocity, FLING_MAX_VELOCITY);
        else
            return Math.max(velocity, -FLING_MAX_VELOCITY);
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        setOnFlingListener(null);
        mLinearSnapHelper.attachToRecyclerView(this);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        clearOnScrollListeners();
    }

    public LinearSnapHelper getSnapHelper() {
        return mLinearSnapHelper;
    }
}
