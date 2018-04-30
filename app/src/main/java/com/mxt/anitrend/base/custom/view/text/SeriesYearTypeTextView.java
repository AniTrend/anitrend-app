package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.Locale;

public class SeriesYearTypeTextView extends SingleLineTextView {

    public SeriesYearTypeTextView(Context context) {
        super(context);
    }

    public SeriesYearTypeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeriesYearTypeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onViewRecycled() {
        super.onViewRecycled();
    }

    @Override
    public void onInit() {
        super.onInit();
    }

    @BindingAdapter("media")
    public static void htmlText(SeriesYearTypeTextView seriesYearTypeTextView, MediaBase mediaBase) {
        Context context = seriesYearTypeTextView.getContext();
        FuzzyDate startDate = mediaBase.getStartDate();
        String year = startDate.isValidDate() ? String.format(Locale.getDefault(), "%d", startDate.getYear()) : context.getString(R.string.tba_placeholder);
        switch (mediaBase.getType()) {
            case KeyUtil.ANIME:
                if(CompatUtil.equals(mediaBase.getFormat(), KeyUtil.MOVIE))
                    seriesYearTypeTextView.setText(String.format(Locale.getDefault(), "%s - %s", year, CompatUtil.capitalizeWords(mediaBase.getFormat())));
                else {
                    if(mediaBase.getEpisodes() > 0)
                        seriesYearTypeTextView.setText(String.format(Locale.getDefault(), "%s - %s", year, context.getString(R.string.text_anime_episodes, mediaBase.getEpisodes())));
                    else
                        seriesYearTypeTextView.setText(String.format(Locale.getDefault(), "%s - %s", year, CompatUtil.capitalizeWords(mediaBase.getFormat())));
                }
                break;
            case KeyUtil.MANGA:
                if(mediaBase.getChapters() > 0)
                    seriesYearTypeTextView.setText(String.format(Locale.getDefault(), "%s - %s", year, context.getString(R.string.text_manga_chapters, mediaBase.getChapters())));
                else
                    seriesYearTypeTextView.setText(String.format(Locale.getDefault(), "%s - %s", year, CompatUtil.capitalizeWords(mediaBase.getFormat())));
                break;
        }
    }
}
