package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import androidx.databinding.BindingAdapter;
import androidx.appcompat.widget.AppCompatRatingBar;
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
    public static void setAverageScore(CustomRatingBar view, int meanScore) {
        float rating = (float)meanScore * view.getMax() / 100;
        view.setRating(rating);
    }
}
