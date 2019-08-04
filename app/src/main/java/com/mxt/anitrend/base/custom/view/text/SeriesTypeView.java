package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import androidx.databinding.BindingAdapter;
import android.util.AttributeSet;

import com.mxt.anitrend.R;

public class SeriesTypeView extends SingleLineTextView {


    public SeriesTypeView(Context context) {
        super(context);
    }

    public SeriesTypeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeriesTypeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @BindingAdapter("seriesType")
    public static void setSeriesType(SeriesTypeView view, String seriesType) {
        String attribute = view.getContext().getString(R.string.title_series_type);
        view.setText(String.format("%s %s", attribute, seriesType));
    }
}
