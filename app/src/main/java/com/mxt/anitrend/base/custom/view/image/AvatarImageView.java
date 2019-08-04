package com.mxt.anitrend.base.custom.view.image;

import android.content.Context;
import androidx.databinding.BindingAdapter;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase;

/**
 * Created by max on 2017/10/29.
 * Circle image view
 */

public class AvatarImageView extends AppCompatImageView implements CustomView {

    public AvatarImageView(Context context) {
        super(context);
        onInit();
    }

    public AvatarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public AvatarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
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

    @BindingAdapter({"avatarUrl"})
    public static void setImage(AvatarImageView view, String url) {
        Glide.with(view.getContext()).load(url).apply(RequestOptions.centerCropTransform())
                .apply(RequestOptions.placeholderOf(R.drawable.avatar_placeholder))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .apply(RequestOptions.circleCropTransform())
                .into(view);
    }

    @BindingAdapter({"avatarUrl"})
    public static void setImage(AvatarImageView view, ImageBase imageBase) {
        setImage(view, imageBase.getLarge());
    }
}
