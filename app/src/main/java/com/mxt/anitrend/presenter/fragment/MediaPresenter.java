package com.mxt.anitrend.presenter.fragment;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import com.mxt.anitrend.R;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.meta.ScoreDistribution;
import com.mxt.anitrend.model.entity.anilist.meta.StatusDistribution;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.MediaUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 2018/01/01.
 */

public class MediaPresenter extends BasePresenter {

    public MediaPresenter(Context context) {
        super(context);
    }

    public Spanned getHashTag(Media media) {
        if(media != null && !TextUtils.isEmpty(media.getHashTag()))
            return Html.fromHtml(String.format("<a href=\"https://twitter.com/search?q=%%23%s&src=typd\">%s</a>",
                    media.getHashTag().replace("#", ""), media.getHashTag()));
        return Html.fromHtml(getContext().getString(R.string.TBA));
    }

    public String getMainStudio(Media media) {
        if(media != null && media.getStudios() != null && !media.getStudios().isEmpty()) {
           ConnectionContainer<List<StudioBase>> studioContainer = media.getStudios();
           Optional<StudioBase> result = Stream.of(studioContainer.getConnection())
                   .findFirst();
           if(result.isPresent())
               return result.get().getName();
        }
        return getContext().getString(R.string.TBA);
    }

    public StudioBase getMainStudioObject(Media media) {
        if(media != null && media.getStudios() != null && !media.getStudios().isEmpty()) {
           ConnectionContainer<List<StudioBase>> studioContainer = media.getStudios();
           Optional<StudioBase> result = Stream.of(studioContainer.getConnection())
                   .findFirst();
           if(result.isPresent())
               return result.get();
        }
        return null;
    }

    public List<PieEntry> getMediaStats(List<StatusDistribution> statusDistribution) {
        int highestStatus = Stream.of(statusDistribution)
                .max((o1, o2) -> o1.getAmount() > o2.getAmount() ? 1 : -1)
                .get().getAmount();

        return Stream.of(statusDistribution)
                .map(st -> new PieEntry(
                        (((st.getAmount()) / (highestStatus)) * 100),
                        CompatUtil.capitalizeWords(st.getStatus())))
                .toList();
    }

    public List<BarEntry> getMediaScoreDistribution(List<ScoreDistribution> scoreDistribution) {
        return Stream.of(scoreDistribution)
                .mapIndexed(0, 1, (index, sc) ->
                        new BarEntry(index, sc.getScore()))
                .toList();
    }

    public String getEpisodeDuration(Media media) {
        if(media != null && media.getDuration() > 0)
            getContext().getString(R.string.text_anime_length, media.getDuration());
        return getContext().getString(R.string.TBA);
    }

    public String getMediaSeason(Media media) {
        if(media != null && media.getStartDate() != null)
            return DateUtil.getMediaSeason(media.getStartDate());
        return getContext().getString(R.string.TBA);
    }

    public String getMediaSource(Media media) {
        if(media != null && !TextUtils.isEmpty(media.getSource()))
            return CompatUtil.capitalizeWords(media.getSource());
        return getContext().getString(R.string.TBA);
    }

    public String getMediaStatus(Media media) {
        if(media != null && (!TextUtils.isEmpty(media.getStatus())))
            return CompatUtil.capitalizeWords(media.getStatus());
        return getContext().getString(R.string.TBA);
    }

    public String getEpisodeCount(Media media) {
        if(media != null && media.getEpisodes() > 0)
            return getContext().getString(R.string.text_anime_episodes, media.getEpisodes());
        return getContext().getString(R.string.TBA);
    }

    public String getVolumeCount(Media media) {
        if(media != null && media.getVolumes() > 0)
            return getContext().getString(R.string.text_manga_volumes, media.getVolumes());
        return getContext().getString(R.string.TBA);
    }

    public String getChapterCount(Media media) {
        if(media != null && media.getChapters() > 0)
            return getContext().getString(R.string.text_manga_chapters, media.getChapters());
        return getContext().getString(R.string.TBA);
    }

    public List<Genre> buildGenres(Media media) {
        List<Genre> genres = new ArrayList<>();
        if(media != null && media.getGenres() != null) {
            for (String genre: media.getGenres()) {
                if(!TextUtils.isEmpty(genre))
                    genres.add(new Genre(genre));
                else
                    break;
            }
        }
        return genres;
    }

    public String getMediaFormat(MediaBase media) {
        if(media != null && !TextUtils.isEmpty(media.getFormat()))
            return CompatUtil.capitalizeWords(media.getFormat());
        return getContext().getString(R.string.tba_placeholder);
    }

    public String getMediaScore(Media media) {
        if(media != null)
            return getContext().getString(R.string.text_anime_score, media.getMeanScore());
        return getContext().getString(R.string.tba_placeholder);
    }

    public int isAnime(Media media) {
        if(MediaUtil.isAnimeType(media))
            return View.VISIBLE;
        return View.GONE;
    }

    public int isManga(Media media) {
        if(MediaUtil.isMangaType(media))
            return View.VISIBLE;
        return View.GONE;
    }
}
