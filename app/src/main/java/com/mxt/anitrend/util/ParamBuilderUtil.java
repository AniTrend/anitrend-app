package com.mxt.anitrend.util;

import android.os.Bundle;
import android.text.TextUtils;

/**
 * Created by max on 2018/02/03.
 */

public class ParamBuilderUtil {

    private String series_type;
    private int year;
    private String season;
    private String series_show_type;
    private String series_status;
    private String series_sort_by;
    private String tag;
    private String tag_exclude;
    private String genre;

    private Bundle bundle;

    public static ParamBuilderUtil Builder() {
        return new ParamBuilderUtil();
    }

    private ParamBuilderUtil() {
        bundle = new Bundle();
    }

    public ParamBuilderUtil setSeries_type(String series_type) {
        this.series_type = series_type;
        return this;
    }

    public ParamBuilderUtil setYear(int year) {
        this.year = year;
        return this;
    }

    public ParamBuilderUtil setSeason(String season) {
        this.season = season;
        return this;
    }


    public ParamBuilderUtil setSortBy(String series_sort_by) {
        this.series_sort_by = series_sort_by;
        return this;
    }

    public ParamBuilderUtil setSeries_show_type(String series_show_type) {
        this.series_show_type = series_show_type;
        return this;
    }

    public ParamBuilderUtil setSeries_status(String series_status) {
        this.series_status = series_status;
        return this;
    }

    public ParamBuilderUtil addTag(String tag) {
        if(TextUtils.isEmpty(this.tag))
            this.tag = tag;
        else
            this.tag += ","+tag;
        return this;
    }

    public ParamBuilderUtil addTag_exclude(String tag_exclude) {
        if(TextUtils.isEmpty(this.tag_exclude))
            this.tag_exclude = tag_exclude;
        else
            this.tag_exclude += ","+tag_exclude;
        return this;
    }

    public ParamBuilderUtil addGenre(String genre) {
        if(TextUtils.isEmpty(this.genre))
            this.genre = genre;
        else
            this.genre += ","+genre;
        return this;
    }

    public Bundle build() {
        if(!TextUtils.isEmpty(series_type))
            bundle.putString(KeyUtils.arg_media_type, series_type);
        if(year != 0)
            bundle.putInt(KeyUtils.arg_series_year, year);
        if(!TextUtils.isEmpty(season))
            bundle.putString(KeyUtils.arg_series_season, season);
        if(!TextUtils.isEmpty(series_sort_by))
            bundle.putString(KeyUtils.arg_series_sort_by, series_sort_by);
        if(!TextUtils.isEmpty(series_show_type))
            bundle.putString(KeyUtils.arg_series_show_type, series_show_type);
        if(!TextUtils.isEmpty(series_status))
            bundle.putString(KeyUtils.arg_series_status, series_status);
        if(!TextUtils.isEmpty(tag))
            bundle.putString(KeyUtils.arg_series_tag, tag);
        if(!TextUtils.isEmpty(tag_exclude))
            bundle.putString(KeyUtils.arg_series_tag_exclude, tag_exclude);
        if(!TextUtils.isEmpty(genre))
            bundle.putString(KeyUtils.arg_series_genres, genre);
        return bundle;
    }
}
