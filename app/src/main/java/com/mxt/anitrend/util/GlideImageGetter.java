package com.mxt.anitrend.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mxt.anitrend.R;

/**
 * Uses glide to load images into an AppCompatTextView
 */
public class GlideImageGetter implements Html.ImageGetter {

    private Context context;
    private AppCompatTextView appCompatTextView;

    public static GlideImageGetter create(Context context, AppCompatTextView appCompatTextView) {
        return new GlideImageGetter(context, appCompatTextView);
    }

    private GlideImageGetter(Context context, AppCompatTextView appCompatTextView) {
        this.context = context;
        this.appCompatTextView = appCompatTextView;
    }

    @Override
    public Drawable getDrawable(String source) {
        final DrawablePlaceHolder drawablePlaceHolder = new DrawablePlaceHolder();
        Glide.with(context).load(source).apply(RequestOptions.centerCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        drawablePlaceHolder.setDrawable(resource);
                    }
                });
        return drawablePlaceHolder;
    }

    @SuppressWarnings("deprecation")
    private class DrawablePlaceHolder extends BitmapDrawable {

        protected Drawable drawable;

        public DrawablePlaceHolder() {
            setDrawable(CompatUtil.getDrawable(context, R.drawable.empty_icon));
        }

        @Override
        public void draw(Canvas canvas) {
            if (drawable != null)
                drawable.draw(canvas);
        }

        public void setDrawable(Drawable drawable) {
            // TODO: 2018/04/28 Check image bounds and make sure the maximum widgth is not more than device screen width
            this.drawable = drawable;
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            drawable.setBounds(0, 0, width, height);
            setBounds(0, 0, width, height);
            if (appCompatTextView != null)
                appCompatTextView.setText(appCompatTextView.getText());
        }
    }
}
