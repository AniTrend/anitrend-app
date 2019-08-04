package com.mxt.anitrend.base.custom.view.image;

import android.content.Context;
import androidx.databinding.BindingAdapter;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.CompatUtil;

/**
 * Created by max on 2017/12/03.
 */

public class AppCompatTintImageView extends AppCompatImageView implements CustomView {

    public AppCompatTintImageView(Context context) {
        super(context);
    }

    public AppCompatTintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppCompatTintImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @BindingAdapter({"imageSrc"})
    public static void setTintDrawable(AppCompatTintImageView imageView, @DrawableRes int drawable) {
        imageView.setImageDrawable(CompatUtil.INSTANCE.getDrawableTintAttr(imageView.getContext(), drawable, R.attr.colorAccent));
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {

    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    public void setTintDrawable(@DrawableRes int drawable, @ColorRes int colorTint) {
        setImageDrawable(CompatUtil.INSTANCE.getDrawable(getContext(), drawable, colorTint));
    }

    public void setTintDrawableAttr(@DrawableRes int drawable, @AttrRes int colorAttribute) {
        setImageDrawable(CompatUtil.INSTANCE.getDrawableTintAttr(getContext(), drawable, colorAttribute));
    }
}
