package com.mxt.anitrend.api.structure;

import java.io.Serializable;

/**
 * Created by Maxwell on 10/10/2016.
 *\
 *
/* "anime",
 2016,
 queString*//*"summer"*//*,
        null*//*e.g movie*//*,
        KeyUtils.AnimeStatusTypes[KeyUtils.AnimeStatusType.FINISHED_AIRING.ordinal()],
        null *//*genre*//*,
        null, *//*genre exclude*//*
        "popularity-desc", *//*sort*//*
        true, *//*airing data*//*
        true, *//*full page*//*
        null); *//*page*/


public class Search implements Serializable {

    private String series_type;
    private Integer year;
    private String season;
    private String item_type;
    private String item_status;
    private String genre;
    private String genre_exclude;
    private String sort_by;
    private String order_by;
    private Boolean airing_data;
    private Boolean full_page;
    private Integer page;

    public String getSeries_type() {
        return series_type;
    }

    public void setSeries_type(String series_type) {
        this.series_type = series_type;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String item_type) {
        this.item_type = item_type;
    }

    public String getItem_status() {
        return item_status;
    }

    public void setItem_status(String item_status) {
        this.item_status = item_status;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getGenre_exclude() {
        return genre_exclude;
    }

    public void setGenre_exclude(String genre_exclude) {
        this.genre_exclude = genre_exclude;
    }

    public String getSort_by() {
        if ((order_by != null &&order_by.equals("desc")) && !sort_by.contains("desc"))
            return String.format("%s-%s", sort_by, order_by);
        return sort_by;

    }

    public void setOrder_by(String order_by) {
        this.order_by = order_by;
    }

    public void setSort_by(String sort_by) {
        this.sort_by = sort_by;
    }

    public Boolean getAiring_data() {
        return airing_data;
    }

    public void setAiring_data(Boolean airing_data) {
        this.airing_data = airing_data;
    }

    public Boolean getFull_page() {
        return full_page;
    }

    public void setFull_page(Boolean full_page) {
        this.full_page = full_page;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Search(){

    }

    public Search(String series_type, Integer year, String season, String item_type, String item_status, String genre, String genre_exclude, String sort_by, String order_by, Boolean airing_data, Boolean full_page, Integer page) {
        this.series_type = series_type;
        this.year = year;
        this.season = season;
        this.item_type = item_type;
        this.item_status = item_status;
        this.genre = genre;
        this.genre_exclude = genre_exclude;
        this.sort_by = sort_by;
        this.order_by = order_by;
        this.airing_data = airing_data;
        this.full_page = full_page;
        this.page = page;
    }

    private String query;

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery(){
        return query;
    }
}
