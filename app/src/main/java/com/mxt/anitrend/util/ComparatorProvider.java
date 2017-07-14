package com.mxt.anitrend.util;

import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.ListItem;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Maxwell on 2/12/2017.
 * Comparator Provider returns comparators of various types
 */
public class ComparatorProvider {

    public static Comparator<Series> getSeriesComparator(final ApiPreferences apiPrefs) {
        if(apiPrefs.getOrder().equals(KeyUtils.OrderTypes[KeyUtils.DESC]))
            return new Comparator<Series>() {
                @Override
                public int compare(Series s1, Series s2) {
                    switch (apiPrefs.getSort()){
                        case "title_romaji":
                            switch (apiPrefs.getTitleLanguage()) {
                                case "romaji":
                                    return s1.getTitle_romaji().compareTo(s2.getTitle_romaji());
                                case "english":
                                    return s1.getTitle_english().compareTo(s2.getTitle_english());
                                case "japanese":
                                    return s1.getTitle_japanese().compareTo(s2.getTitle_japanese());
                            }
                            return s1.getTitle_english().compareTo(s2.getTitle_english());
                        case "score":
                            return s1.getAverage_score() > s2.getAverage_score()? 1:-1;
                        case "popularity":
                            return s1.getPopularity() > s2.getPopularity()? 1:-1;
                        case "start_date":
                            return s1.getStart_date_fuzzy() > s2.getStart_date_fuzzy()?1:-1;
                        case "end_date":
                            return s1.getEnd_date_fuzzy() > s2.getEnd_date_fuzzy()?1:-1;
                    }
                    return 0;
                }
            };

        return new Comparator<Series>() {
            @Override
            public int compare(Series s2, Series s1) {
                switch (apiPrefs.getSort()){
                    case "title_romaji":
                        switch (apiPrefs.getTitleLanguage()) {
                            case "romaji":
                                return s1.getTitle_romaji().compareTo(s2.getTitle_romaji());
                            case "english":
                                return s1.getTitle_english().compareTo(s2.getTitle_english());
                            case "japanese":
                                return s1.getTitle_japanese().compareTo(s2.getTitle_japanese());
                        }
                        return s1.getTitle_english().compareTo(s2.getTitle_english());
                    case "score":
                        return s1.getAverage_score() > s2.getAverage_score()? 1:-1;
                    case "popularity":
                        return s1.getPopularity() > s2.getPopularity()? 1:-1;
                    case "start_date":
                        return s1.getStart_date_fuzzy() > s2.getStart_date_fuzzy()?1:-1;
                    case "end_date":
                        return s1.getEnd_date_fuzzy() > s2.getEnd_date_fuzzy()?1:-1;
                }
                return 0;
            }
        };
    }

    public static Comparator<ListItem> getAnimeComparator(final ApiPreferences apiPrefs) {
        if(apiPrefs.getOrder().equals(KeyUtils.OrderTypes[KeyUtils.DESC]))
            return new Comparator<ListItem>() {
                @Override
                public int compare(ListItem s1, ListItem s2) {
                    switch (apiPrefs.getSort()){
                        case "title_romaji":
                            switch (apiPrefs.getTitleLanguage()) {
                                case "romaji":
                                    return s1.getAnime().getTitle_romaji().compareTo(s2.getAnime().getTitle_romaji());
                                case "english":
                                    return s1.getAnime().getTitle_english().compareTo(s2.getAnime().getTitle_english());
                                case "japanese":
                                    return s1.getAnime().getTitle_japanese().compareTo(s2.getAnime().getTitle_japanese());
                            }
                            return s1.getAnime().getTitle_english().compareTo(s2.getAnime().getTitle_english());
                        case "score":
                            return s1.getScore_raw() < s2.getScore_raw()? 1:-1;
                        case "popularity":
                            return s1.getAnime().getPopularity() > s2.getAnime().getPopularity()? 1:-1;
                        case "start_date":
                            return s1.getAnime().getStart_date_fuzzy().compareTo(s2.getAnime().getStart_date_fuzzy());
                        case "end_date":
                            return s1.getAnime().getEnd_date_fuzzy().compareTo(s2.getAnime().getEnd_date_fuzzy());
                    }
                    return 0;
                }
            };

        return new Comparator<ListItem>() {
            @Override
            public int compare(ListItem s2, ListItem s1) {
                switch (apiPrefs.getSort()){
                    case "title_romaji":
                        switch (apiPrefs.getTitleLanguage()) {
                            case "romaji":
                                return s1.getAnime().getTitle_romaji().compareTo(s2.getAnime().getTitle_romaji());
                            case "english":
                                return s1.getAnime().getTitle_english().compareTo(s2.getAnime().getTitle_english());
                            case "japanese":
                                return s1.getAnime().getTitle_japanese().compareTo(s2.getAnime().getTitle_japanese());
                        }
                        return s1.getAnime().getTitle_english().compareTo(s2.getAnime().getTitle_english());
                    case "score":
                        return s1.getScore_raw() < s2.getScore_raw()? 1:-1;
                    case "popularity":
                        return s1.getAnime().getPopularity() > s2.getAnime().getPopularity()? 1:-1;
                    case "start_date":
                        return s1.getAnime().getStart_date_fuzzy().compareTo(s2.getAnime().getStart_date_fuzzy());
                    case "end_date":
                        return s1.getAnime().getEnd_date_fuzzy().compareTo(s2.getAnime().getEnd_date_fuzzy());
                }
                return 0;
            }
        };
    }

    public static Comparator<ListItem> getMangaComparator(final ApiPreferences apiPrefs) {
        if(apiPrefs.getOrder().equals(KeyUtils.OrderTypes[KeyUtils.DESC]))
            return new Comparator<ListItem>() {
                @Override
                public int compare(ListItem s1, ListItem s2) {
                    switch (apiPrefs.getSort()) {
                        case "title_romaji":
                            switch (apiPrefs.getTitleLanguage()) {
                                case "romaji":
                                    return s1.getManga().getTitle_romaji().compareTo(s2.getManga().getTitle_romaji());
                                case "english":
                                    return s1.getManga().getTitle_english().compareTo(s2.getManga().getTitle_english());
                                case "japanese":
                                    return s1.getManga().getTitle_japanese().compareTo(s2.getManga().getTitle_japanese());
                            }
                            return s1.getManga().getTitle_english().compareTo(s2.getManga().getTitle_english());
                        case "score":
                            return s1.getScore_raw() > s2.getScore_raw()? 1:-1;
                        case "popularity":
                            return s1.getManga().getPopularity() > s2.getManga().getPopularity() ? 1 : -1;
                        case "start_date":
                            return s1.getManga().getStart_date_fuzzy() > s2.getManga().getStart_date_fuzzy() ? 1 : -1;
                        case "end_date":
                            return s1.getManga().getEnd_date_fuzzy() > s2.getManga().getEnd_date_fuzzy() ? 1 : -1;
                    }
                    return 0;
                }
            };

        return new Comparator<ListItem>() {
            @Override
            public int compare(ListItem s2, ListItem s1) {
                switch (apiPrefs.getSort()) {
                    case "title_romaji":
                        switch (apiPrefs.getTitleLanguage()) {
                            case "romaji":
                                return s1.getManga().getTitle_romaji().compareTo(s2.getManga().getTitle_romaji());
                            case "english":
                                return s1.getManga().getTitle_english().compareTo(s2.getManga().getTitle_english());
                            case "japanese":
                                return s1.getManga().getTitle_japanese().compareTo(s2.getManga().getTitle_japanese());
                        }
                        return s1.getManga().getTitle_english().compareTo(s2.getManga().getTitle_english());
                    case "score":
                        return s1.getScore_raw() > s2.getScore_raw()? 1:-1;
                    case "popularity":
                        return s1.getManga().getPopularity() > s2.getManga().getPopularity() ? 1 : -1;
                    case "start_date":
                        return s1.getManga().getStart_date_fuzzy() > s2.getManga().getStart_date_fuzzy() ? 1 : -1;
                    case "end_date":
                        return s1.getManga().getEnd_date_fuzzy() > s2.getManga().getEnd_date_fuzzy() ? 1 : -1;
                }
                return 0;
            }
        };
    }

    public static Comparator<HashMap.Entry<String, Integer>> getGenresComparator(final ApiPreferences apiPrefs) {
        return new Comparator<HashMap.Entry<String, Integer>>() {
            @Override
            public int compare(HashMap.Entry<String, Integer> o1, HashMap.Entry<String, Integer> o2) {
                if(apiPrefs.getOrder().equals(KeyUtils.OrderTypes[KeyUtils.DESC]))
                    return o2.getValue().compareTo(o1.getValue());
                return o1.getValue().compareTo(o2.getValue());
            }
        };
    }

    public static Comparator<HashMap.Entry<String, Integer>> getGenreKeyComparator() {
        return new Comparator<HashMap.Entry<String, Integer>>() {
            @Override
            public int compare(HashMap.Entry<String, Integer> o1, HashMap.Entry<String, Integer> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        };
    }

    public static Comparator<Series> getSeriesStudioComparator(final ApiPreferences apiPrefs) {
        return new Comparator<Series>() {
            @Override
            public int compare(Series s1, Series s2) {
                switch (apiPrefs.getSort()) {
                    case "title_romaji":
                        switch (apiPrefs.getTitleLanguage()) {
                            case "romaji":
                                return s1.getTitle_romaji().compareTo(s2.getTitle_romaji());
                            case "english":
                                return s1.getTitle_english().compareTo(s2.getTitle_english());
                            case "japanese":
                                return s1.getTitle_japanese().compareTo(s2.getTitle_japanese());
                        }
                        return s1.getTitle_english().compareTo(s2.getTitle_english());
                    case "score":
                        return s1.getAverage_score() < s2.getAverage_score()? 1:-1;
                    case "popularity":
                        return s1.getPopularity() < s2.getPopularity()? 1:-1;
                    case "start_date":
                        return s1.getStart_date_fuzzy() < s2.getStart_date_fuzzy()?1:-1;
                    case "end_date":
                        return s1.getEnd_date_fuzzy() < s2.getEnd_date_fuzzy()?1:-1;
                }
                return s1.getPopularity() < s2.getPopularity()? 1:-1;
            }
        };
    }
}
