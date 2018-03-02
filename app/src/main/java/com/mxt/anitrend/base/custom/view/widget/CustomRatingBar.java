package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.v7.widget.AppCompatRatingBar;
import android.util.AttributeSet;

import com.mxt.anitrend.base.interfaces.view.CustomView;

/**
 * Created by max on 2017/10/27.
 * Custom rating bar
 */

public class CustomRatingBar extends AppCompatRatingBar implements CustomView {

    public CustomRatingBar(Context context) {
        super(context);
        onInit();
    }

    public CustomRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public CustomRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        setIsIndicator(true);
        setStepSize(0.1f);
        setNumStars(5);
        setMax(5);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    @BindingAdapter("averageScore")
    public static void setAverageScore(CustomRatingBar view, double averageScore) {
        float rating = (float)averageScore * view.getMax() / 100;
        view.setRating(rating);
    }
}
