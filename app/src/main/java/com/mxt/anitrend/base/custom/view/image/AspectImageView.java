package com.mxt.anitrend.base.custom.view.image;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/09/01.
 * Custom image view that can respect a given aspect view ratio,
 * either specify the width of the image and the height will be automatically calculated
 * or set to wrap content to automatically get the view width at runtime
 */

public class AspectImageView extends androidx.appcompat.widget.AppCompatImageView implements CustomView {

    private int spanSize;
    private int defaultMargin;
    private Point deviceDimens = new Point();

    public AspectImageView(Context context) {
        super(context);
        onInit();
    }

    public AspectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public AspectImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int Width;
        if((Width = MeasureSpec.getSize(widthMeasureSpec)) == 0)
            Width = (deviceDimens.x / spanSize) - defaultMargin;

        int Height = (int) (Width * KeyUtil.AspectRatio);
        super.onMeasure(MeasureSpec.makeMeasureSpec(Width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(Height, MeasureSpec.EXACTLY));
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        defaultMargin = getResources().getDimensionPixelSize(R.dimen.md_margin);
        spanSize = getResources().getInteger(R.integer.grid_list_x2);
        CompatUtil.INSTANCE.getScreenDimens(deviceDimens, getContext());
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    @BindingAdapter({"imageUrl"})
    public static void setImage(AspectImageView view, String url) {
        Glide.with(view.getContext()).load(url)
                .transition(DrawableTransitionOptions.withCrossFade(350))
                .apply(RequestOptions.centerCropTransform())
                .into(view);
    }

    @BindingAdapter({"imageUrl"})
    public static void setImage(AspectImageView view, ImageBase imageBase) {
        if(imageBase != null) {
            if (imageBase.getExtraLarge() != null)
                setImage(view, imageBase.getExtraLarge());
            else
                setImage(view, imageBase.getLarge());
        }
    }
}
