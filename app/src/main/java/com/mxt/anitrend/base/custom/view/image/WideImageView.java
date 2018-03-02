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
import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2017/10/30.
 * 16 x 10 Aspect image view
 */

public class WideImageView extends AppCompatImageView implements CustomView {

    private int defaultMargin, defaultDimens;

    // private Point deviceDimens = new Point();

    public WideImageView(Context context) {
        super(context);
        onInit();
    }

    public WideImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public WideImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int Width = MeasureSpec.getSize(widthMeasureSpec),
                Height = (int) ((MeasureSpec.getSize(heightMeasureSpec) - defaultMargin) * KeyUtils.WideAspectRatio);

        if(heightMeasureSpec == 0)
            Height = (int) ((defaultDimens - defaultMargin) * KeyUtils.WideAspectRatio);

        super.onMeasure(MeasureSpec.makeMeasureSpec(Width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(Height, MeasureSpec.EXACTLY));
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        defaultMargin = getResources().getDimensionPixelSize(R.dimen.sm_margin);
        defaultDimens = getResources().getDimensionPixelSize(R.dimen.app_bar_height);
        // CompatUtil.getScreenDimens(deviceDimens, getContext());
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    @BindingAdapter({"imageUrl"})
    public static void setImage(WideImageView view, String url) {
        try {
            if(url != null)
                Glide.with(view.getContext()).load(url)
                        .transition(DrawableTransitionOptions.withCrossFade(350))
                        .apply(RequestOptions.centerCropTransform())
                        .into(view);
            else
                Glide.with(view.getContext()).load(R.drawable.reg_bg)
                        .transition(DrawableTransitionOptions.withCrossFade(350))
                        .apply(RequestOptions.centerCropTransform())
                        .into(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
