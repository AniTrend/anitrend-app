package com.mxt.anitrend.presenter.fragment;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.presenter.CommonPresenter;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.MediaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by max on 2018/01/01.
 */

public class SeriesPresenter extends CommonPresenter {

    public SeriesPresenter(Context context) {
        super(context);
    }

    public Spanned getHashTag(Media series) {
        if(series != null && !TextUtils.isEmpty(series.getHashtag()))
            return Html.fromHtml(String.format("<a href=\"https://twitter.com/search?q=%%23%s&src=typd\">%s</a>",
                    series.getHashtag().replace("#", ""), series.getHashtag()));
        return Html.fromHtml(getContext().getString(R.string.TBA));
    }

    public String getMainStudio(Media series) {
        if(series != null) {
           Optional<StudioBase> result = Stream.of(series.getStudio())
                    .filter(model -> model.getMain_studio() == 1)
                    .findFirst();
           if(result.isPresent())
               return result.get().getName();
        }
        return getContext().getString(R.string.TBA);
    }

    public List<PieEntry> getSeriesStats(List<Map.Entry<String, Float>> model) {
        List<PieEntry> entries = new ArrayList<>(model.size());
        Float maximum = getMaximumOf(model);

        List<Map.Entry<String, Float>> mapEntry = Stream.of(model).toList();

        for (Map.Entry<String, Float> entry: mapEntry) {
            float percentage = ((entry.getValue()) / ((float)maximum)) * 100f;
            String key = entry.getKey().replace("_", " ");
            entries.add(new PieEntry(percentage, CompatUtil.capitalizeWords(key)));
        }
        return entries;
    }

    public List<BarEntry> getSeriesScoreDistribution(List<Map.Entry<Integer, Float>> model) {
        List<BarEntry> results = new ArrayList<>();
        if(model != null) {
            float index = 0f;
            for (Map.Entry<Integer, Float> entry: model) {
                results.add(new BarEntry(index, entry.getValue()));
                index++;
            }
        }
        return results;
    }

    public List<List<Entry>> getSeriesAiringCorrelation(List<Map.Entry<String, Map<String, Float>>> model) {
        List<List<Entry>> results = new ArrayList<>();
        if(model != null) {
            List<Entry> score = new ArrayList<>();
            List<Entry> watching = new ArrayList<>();
            float index = 0f;
            for (Map.Entry<String, Map<String, Float>> entry : model) {
                score.add(new Entry(index, entry.getValue().get("score")));
                watching.add(new Entry(index, entry.getValue().get("watching")));
                index += 0.5f;
            }
            results.add(score);
            results.add(watching);
        }
        return results;
    }

    private Float getMaximumOf(List<Map.Entry<String, Float>> map) {
        return Stream.of(map)
                .max((o1, o2) -> o1.getValue() > o2.getValue() ? 1:-1)
                .get().getValue();
    }

    public String getEpisodeDuration(Media series) {
        if(series != null && series.getDuration() > 0)
            getContext().getString(R.string.text_anime_length, series.getDuration());
        return getContext().getString(R.string.TBA);
    }

    public String getSeriesSeason(Media series) {
        if(series != null && series.getStart_date_fuzzy() > 0)
            return DateUtil.getSeriesSeason(series.getStart_date_fuzzy());
        return getContext().getString(R.string.TBA);
    }

    public String getSeriesStatus(Media series) {
        if(series != null && (!TextUtils.isEmpty(series.getAiring_status()) || !TextUtils.isEmpty(series.getPublishing_status()))) {
            if (series.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME])) {
                return CompatUtil.capitalizeWords(series.getAiring_status());
            }
            return CompatUtil.capitalizeWords(series.getPublishing_status());
        }
        return getContext().getString(R.string.TBA);
    }

    public String getEpisodeCount(Media series) {
        if(series != null && series.getTotal_episodes() > 0)
            return getContext().getString(R.string.text_anime_episodes, series.getTotal_episodes());
        return getContext().getString(R.string.TBA);
    }

    public String getVolumeCount(Media series) {
        if(series != null && series.getTotal_volumes() > 0)
            return getContext().getString(R.string.text_manga_volumes, series.getTotal_volumes());
        return getContext().getString(R.string.TBA);
    }

    public String getChapterCount(Media series) {
        if(series != null && series.getTotal_chapters() > 0)
            return getContext().getString(R.string.text_manga_chapters, series.getTotal_chapters());
        return getContext().getString(R.string.TBA);
    }

    public List<Genre> buildGenres(Media series) {
        List<Genre> genres = new ArrayList<>();
        if(series != null && series.getGenres() != null) {
            for (String genre: series.getGenres()) {
                if(!TextUtils.isEmpty(genre))
                    genres.add(new Genre(genre));
                else
                    break;
            }
        }
        return genres;
    }

    public int isAnime(Media series) {
        if(MediaUtil.isAnimeType(series))
            return View.VISIBLE;
        return View.GONE;
    }

    public int isManga(Media series) {
        if(MediaUtil.isMangaType(series))
            return View.VISIBLE;
        return View.GONE;
    }
}
