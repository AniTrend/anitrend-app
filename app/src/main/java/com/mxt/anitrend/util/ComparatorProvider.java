package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.model.entity.general.SeriesList;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Maxwell on 2/12/2017.
 * Comparator Provider returns comparators of various types
 */
public final class ComparatorProvider {

    public static Comparator<Series> getSeriesComparator(ApplicationPref prefs, String languageTitle) {
        if(prefs.getOrder().equals(KeyUtils.OrderTypes[KeyUtils.DESC]))
            return (s1, s2) -> {
                switch (prefs.getSort()){
                    case KeyUtils.key_sort_title:
                        switch (languageTitle) {
                            case KeyUtils.key_language_romaji:
                                return s1.getTitle_romaji().compareTo(s2.getTitle_romaji());
                            case KeyUtils.key_language_english:
                                return s1.getTitle_english().compareTo(s2.getTitle_english());
                            case KeyUtils.key_language_japanese:
                                return s1.getTitle_japanese().compareTo(s2.getTitle_japanese());
                        }
                        return s1.getTitle_english().compareTo(s2.getTitle_english());
                    case KeyUtils.key_sort_score:
                        return s1.getAverage_score() > s2.getAverage_score()? 1:-1;
                    case KeyUtils.key_sort_popularity:
                        return s1.getPopularity() > s2.getPopularity()? 1:-1;
                    case KeyUtils.key_sort_start_date:
                        return s1.getStart_date_fuzzy() > s2.getStart_date_fuzzy()?1:-1;
                    case KeyUtils.key_sort_end_date:
                        return s1.getEnd_date_fuzzy() > s2.getEnd_date_fuzzy()?1:-1;
                }
                return 0;
            };

        return (s2, s1) -> {
            switch (prefs.getSort()){
                case KeyUtils.key_sort_title:
                    switch (languageTitle) {
                        case KeyUtils.key_language_romaji:
                            return s1.getTitle_romaji().compareTo(s2.getTitle_romaji());
                        case KeyUtils.key_language_english:
                            return s1.getTitle_english().compareTo(s2.getTitle_english());
                        case KeyUtils.key_language_japanese:
                            return s1.getTitle_japanese().compareTo(s2.getTitle_japanese());
                    }
                    return s1.getTitle_english().compareTo(s2.getTitle_english());
                case KeyUtils.key_sort_score:
                    return s1.getAverage_score() > s2.getAverage_score()? 1:-1;
                case KeyUtils.key_sort_popularity:
                    return s1.getPopularity() > s2.getPopularity()? 1:-1;
                case KeyUtils.key_sort_start_date:
                    return s1.getStart_date_fuzzy() > s2.getStart_date_fuzzy()?1:-1;
                case KeyUtils.key_sort_end_date:
                    return s1.getEnd_date_fuzzy() > s2.getEnd_date_fuzzy()?1:-1;
            }
            return 0;
        };
    }

    public static Comparator<SeriesList> getAnimeComparator(ApplicationPref prefs, String languageTitle) {
        if(prefs.getOrder().equals(KeyUtils.OrderTypes[KeyUtils.DESC]))
            return (s1, s2) -> {
                switch (prefs.getSort()){
                    case KeyUtils.key_sort_title:
                        switch (languageTitle) {
                            case KeyUtils.key_language_romaji:
                                return s1.getAnime().getTitle_romaji().compareTo(s2.getAnime().getTitle_romaji());
                            case KeyUtils.key_language_english:
                                return s1.getAnime().getTitle_english().compareTo(s2.getAnime().getTitle_english());
                            case KeyUtils.key_language_japanese:
                                return s1.getAnime().getTitle_japanese().compareTo(s2.getAnime().getTitle_japanese());
                        }
                        return s1.getAnime().getTitle_english().compareTo(s2.getAnime().getTitle_english());
                    case KeyUtils.key_sort_score:
                        return s1.getScore_raw() < s2.getScore_raw()? 1:-1;
                    case KeyUtils.key_sort_popularity:
                        return s1.getAnime().getPopularity() > s2.getAnime().getPopularity()? 1:-1;
                    case KeyUtils.key_sort_start_date:
                        return s1.getAnime().getStart_date_fuzzy_formatted().compareTo(s2.getAnime().getStart_date_fuzzy_formatted());
                    case KeyUtils.key_sort_end_date:
                        return s1.getAnime().getEnd_date_fuzzy_formatted().compareTo(s2.getAnime().getEnd_date_fuzzy_formatted());
                }
                return 0;
            };

        return (s2, s1) -> {
            switch (prefs.getSort()){
                case KeyUtils.key_sort_title:
                    switch (languageTitle) {
                        case KeyUtils.key_language_romaji:
                            return s1.getAnime().getTitle_romaji().compareTo(s2.getAnime().getTitle_romaji());
                        case KeyUtils.key_language_english:
                            return s1.getAnime().getTitle_english().compareTo(s2.getAnime().getTitle_english());
                        case KeyUtils.key_language_japanese:
                            return s1.getAnime().getTitle_japanese().compareTo(s2.getAnime().getTitle_japanese());
                    }
                    return s1.getAnime().getTitle_english().compareTo(s2.getAnime().getTitle_english());
                case KeyUtils.key_sort_score:
                    return s1.getScore_raw() < s2.getScore_raw()? 1:-1;
                case KeyUtils.key_sort_popularity:
                    return s1.getAnime().getPopularity() > s2.getAnime().getPopularity()? 1:-1;
                case KeyUtils.key_sort_start_date:
                    return s1.getAnime().getStart_date_fuzzy_formatted().compareTo(s2.getAnime().getStart_date_fuzzy_formatted());
                case KeyUtils.key_sort_end_date:
                    return s1.getAnime().getEnd_date_fuzzy_formatted().compareTo(s2.getAnime().getEnd_date_fuzzy_formatted());
            }
            return 0;
        };
    }

    public static Comparator<SeriesList> getMangaComparator(ApplicationPref prefs, String languageTitle) {
        if(prefs.getOrder().equals(KeyUtils.OrderTypes[KeyUtils.DESC]))
            return (s1, s2) -> {
                switch (prefs.getSort()) {
                    case KeyUtils.key_sort_title:
                        switch (languageTitle) {
                            case KeyUtils.key_language_romaji:
                                return s1.getManga().getTitle_romaji().compareTo(s2.getManga().getTitle_romaji());
                            case KeyUtils.key_language_english:
                                return s1.getManga().getTitle_english().compareTo(s2.getManga().getTitle_english());
                            case KeyUtils.key_language_japanese:
                                return s1.getManga().getTitle_japanese().compareTo(s2.getManga().getTitle_japanese());
                        }
                        return s1.getManga().getTitle_english().compareTo(s2.getManga().getTitle_english());
                    case KeyUtils.key_sort_score:
                        return s1.getScore_raw() > s2.getScore_raw()? 1:-1;
                    case KeyUtils.key_sort_popularity:
                        return s1.getManga().getPopularity() > s2.getManga().getPopularity() ? 1 : -1;
                    case KeyUtils.key_sort_start_date:
                        return s1.getManga().getStart_date_fuzzy_formatted().compareTo(s2.getManga().getStart_date_fuzzy_formatted());
                    case KeyUtils.key_sort_end_date:
                        return s1.getManga().getEnd_date_fuzzy_formatted().compareTo(s2.getAnime().getEnd_date_fuzzy_formatted());
                }
                return 0;
            };

        return (s2, s1) -> {
            switch (prefs.getSort()) {
                case KeyUtils.key_sort_title:
                    switch (languageTitle) {
                        case KeyUtils.key_language_romaji:
                            return s1.getManga().getTitle_romaji().compareTo(s2.getManga().getTitle_romaji());
                        case KeyUtils.key_language_english:
                            return s1.getManga().getTitle_english().compareTo(s2.getManga().getTitle_english());
                        case KeyUtils.key_language_japanese:
                            return s1.getManga().getTitle_japanese().compareTo(s2.getManga().getTitle_japanese());
                    }
                    return s1.getManga().getTitle_english().compareTo(s2.getManga().getTitle_english());
                case KeyUtils.key_sort_score:
                    return s1.getScore_raw() > s2.getScore_raw()? 1:-1;
                case KeyUtils.key_sort_popularity:
                    return s1.getManga().getPopularity() > s2.getManga().getPopularity() ? 1 : -1;
                case KeyUtils.key_sort_start_date:
                    return s1.getManga().getStart_date_fuzzy_formatted().compareTo(s2.getManga().getStart_date_fuzzy_formatted());
                case KeyUtils.key_sort_end_date:
                    return s1.getManga().getEnd_date_fuzzy_formatted().compareTo(s2.getAnime().getEnd_date_fuzzy_formatted());
            }
            return 0;
        };
    }

    public static Comparator<HashMap.Entry<String, Integer>> getGenresComparator() {
        return (o1, o2) -> o2.getValue().compareTo(o1.getValue());
    }

    public static Comparator<HashMap.Entry<String, Integer>> getGenreValueComparator() {
        return (o1, o2) -> o1.getValue() > o2.getValue()? -1 : 1;
    }

    public static <T> Comparator<HashMap.Entry<String, T>> getKeyComparator() {
        return (o1, o2) -> o1.getKey().compareTo(o2.getKey());
    }

    public static Comparator<Series> getSeriesStudioComparator(ApplicationPref prefs, String languageTitle) {
        return (s1, s2) -> {
            switch (prefs.getSort()) {
                case KeyUtils.key_sort_title:
                    switch (languageTitle) {
                        case KeyUtils.key_language_romaji:
                            return s1.getTitle_romaji().compareTo(s2.getTitle_romaji());
                        case KeyUtils.key_language_english:
                            return s1.getTitle_english().compareTo(s2.getTitle_english());
                        case KeyUtils.key_language_japanese:
                            return s1.getTitle_japanese().compareTo(s2.getTitle_japanese());
                    }
                    return s1.getTitle_english().compareTo(s2.getTitle_english());
                case KeyUtils.key_sort_score:
                    return s1.getAverage_score() < s2.getAverage_score()? 1:-1;
                case KeyUtils.key_sort_popularity:
                    return s1.getPopularity() < s2.getPopularity()? 1:-1;
                case KeyUtils.key_sort_start_date:
                    return s1.getStart_date_fuzzy() < s2.getStart_date_fuzzy()?1:-1;
                case KeyUtils.key_sort_end_date:
                    return s1.getEnd_date_fuzzy() < s2.getEnd_date_fuzzy()?1:-1;
            }
            return s1.getPopularity() < s2.getPopularity()? 1:-1;
        };
    }
}