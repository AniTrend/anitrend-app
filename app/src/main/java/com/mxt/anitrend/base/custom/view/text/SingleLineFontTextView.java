package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.content.res.AssetManager;
import android.databinding.BindingAdapter;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by max on 2017/12/24.
 * custom font single line text view
 */

public class SingleLineFontTextView extends SingleLineTextView {

    public SingleLineFontTextView(Context context) {
        super(context);
    }

    public SingleLineFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SingleLineFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        super.onInit();
        AssetManager assertManager = getContext().getAssets();
        setTypeface(Typeface.createFromAsset(assertManager, "fonts/Lobster-Regular.ttf"));
    }

    @BindingAdapter({"fontName"})
    public static void setCustomFontType(SingleLineTextView singleLineTextView, String fontName) {
        String fontPath = String.format("fonts/%s", fontName);
        AssetManager assertManager = singleLineTextView.getContext().getAssets();
        singleLineTextView.setTypeface(Typeface.createFromAsset(assertManager, fontPath));
    }
}
