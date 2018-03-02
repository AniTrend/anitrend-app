package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.CustomRatingWidgetBinding;
import com.mxt.anitrend.util.CompatUtil;

import java.util.Locale;

/**
 * Created by max on 2018/01/27.
 * Special text base rating view
 */

public class RatingTextView extends LinearLayout implements CustomView {

    private static final int MAX = 5;
    private CustomRatingWidgetBinding binding;

    public RatingTextView(Context context) {
        super(context);
        onInit();
    }

    public RatingTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public RatingTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RatingTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        binding = CustomRatingWidgetBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
    }

    public void setFavourState(boolean isFavourite) {
        @ColorRes int colorTint = isFavourite ? R.color.colorStateYellow : R.color.white;
        Drawable drawable = CompatUtil.getDrawable(getContext(), R.drawable.ic_star_grey_600_24dp, colorTint);
        binding.ratingFavourState.setImageDrawable(drawable);
    }

    private void setRating(float value) {
        binding.setRating(String.format(Locale.getDefault(),"%.2f", value));
    }

    @BindingAdapter("rating")
    public static void setAverageRating(RatingTextView view, double averageScore) {
        float rating = (float)averageScore * MAX / 100;
        view.setRating(rating);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
