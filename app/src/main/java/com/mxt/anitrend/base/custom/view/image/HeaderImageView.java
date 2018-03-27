package com.mxt.anitrend.base.custom.view.image;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/10/31.
 */

public class HeaderImageView extends AppCompatImageView implements CustomView {

    private int defaultMargin, deviceDimens;

    public HeaderImageView(Context context) {
        super(context);
        onInit();
    }

    public HeaderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public HeaderImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int Width = MeasureSpec.getSize(widthMeasureSpec),
                Height = (int) ((deviceDimens - defaultMargin) * KeyUtil.WideAspectRatio);

        super.onMeasure(MeasureSpec.makeMeasureSpec(Width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(Height, MeasureSpec.EXACTLY));
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        defaultMargin = getResources().getDimensionPixelSize(R.dimen.sm_margin);
        deviceDimens = getResources().getDimensionPixelSize(R.dimen.nav_header_height);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    @BindingAdapter({"bannerUrl"})
    public static void setImage(HeaderImageView view, String url) {
        Glide.with(view.getContext()).load(url)
                .transition(DrawableTransitionOptions.withCrossFade(350))
                .apply(RequestOptions.centerCropTransform())
                .into(view);
    }
}
