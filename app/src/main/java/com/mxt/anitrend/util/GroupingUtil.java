package com.mxt.anitrend.util;

import android.text.TextUtils;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.model.entity.group.EntityHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by max on 2018/02/18.
 * Utils for group various types of media
 */

public class GroupingUtil {

    /**
     * Groups series relations by the relation type of the series
     */
    public static List<EntityGroup> getGroupedSeriesRelated(Media series) {
        List<Media> seriesList = new ArrayList<>();
        if(series != null) {
            if(series.getRelations() != null)
                seriesList.addAll(series.getRelations());
            if(series.getRelations_anime() != null)
                seriesList.addAll(series.getRelations_anime());
            if(series.getRelations_manga() != null)
                seriesList.addAll(series.getRelations_manga());
        }
        List<EntityGroup> entityMap = new ArrayList<>();
        if(!seriesList.isEmpty()) {
            Map<String, List<Media>> map = Stream.of(seriesList)
                    .filter(value -> !TextUtils.isEmpty(value.getRelation_type()))
                    .collect(Collectors.groupingBy(Media::getRelation_type));
            for (Map.Entry<String, List<Media>> entry: CompatUtil.getKeyFilteredMap(map)) {
                entityMap.add(new EntityHeader(entry.getKey(), entry.getValue().size()));
                entityMap.addAll(entry.getValue());
            }
        }
        return entityMap;
    }

    /**
     * Groups series relations by the relation type of the series
     */
    public static List<EntityGroup> getGroupedSeriesBaseType(List<MediaBase> seriesList) {
        List<EntityGroup> entityMap = new ArrayList<>();
        if(seriesList != null && !seriesList.isEmpty()) {
            Map<String, List<MediaBase>> map = Stream.of(seriesList)
                    .filter(value -> !TextUtils.isEmpty(value.getType()))
                    .collect(Collectors.groupingBy(MediaBase::getType));
            for (Map.Entry<String, List<MediaBase>> entry: CompatUtil.getKeyFilteredMap(map)) {
                entityMap.add(new EntityHeader(entry.getKey(), entry.getValue().size()));
                entityMap.addAll(entry.getValue());
            }
        }
        return entityMap;
    }

    /**
     * Groups series relations by the relation type of the series
     */
    public static List<EntityGroup> getGroupedSeriesType(List<Media> seriesList) {
        List<EntityGroup> entityMap = new ArrayList<>();
        if(seriesList != null && !seriesList.isEmpty()) {
            Map<String, List<Media>> map = Stream.of(seriesList)
                    .filter(value -> !TextUtils.isEmpty(value.getType()))
                    .collect(Collectors.groupingBy(Media::getType));
            for (Map.Entry<String, List<Media>> entry: CompatUtil.getKeyFilteredMap(map)) {
                entityMap.add(new EntityHeader(entry.getKey(), entry.getValue().size()));
                entityMap.addAll(entry.getValue());
            }
        }
        return entityMap;
    }

    /**
     * Groups series relations by the relation type of the role type
     */
    public static List<EntityGroup> getGroupedSeriesRoleType(List<MediaBase> seriesList) {
        List<EntityGroup> entityMap = new ArrayList<>();
        if(seriesList != null && !seriesList.isEmpty()) {
            Map<String, List<MediaBase>> map = Stream.of(seriesList)
                    .filter(value -> !TextUtils.isEmpty(value.getType()))
                    .collect(Collectors.groupingBy(MediaBase::getRole));
            for (Map.Entry<String, List<MediaBase>> entry: CompatUtil.getKeyFilteredMap(map)) {
                entityMap.add(new EntityHeader(entry.getKey(), entry.getValue().size()));
                entityMap.addAll(entry.getValue());
            }
        }
        return entityMap;
    }

    /**
     * Groups series relations by the type
     */
    public static List<EntityGroup> getGroupedSeriesListType(List<MediaList> mediaList) {
        List<EntityGroup> entityMap = new ArrayList<>();
        if(mediaList != null && !mediaList.isEmpty()) {
            Map<String, List<MediaList>> map = Stream.of(mediaList)
                    .filter(value -> !TextUtils.isEmpty(SeriesUtil.getSeriesModel(value).getType()))
                    .collect(Collectors.groupingBy(o -> SeriesUtil.getSeriesModel(o).getType()));

            for (Map.Entry<String, List<MediaList>> entry: CompatUtil.getKeyFilteredMap(map)) {
                entityMap.add(new EntityHeader(entry.getKey(), entry.getValue().size()));
                entityMap.addAll(entry.getValue());
            }
        }
        return entityMap;
    }

    /**
     * Groups characters based on roles
     */
    public static List<EntityGroup> getGroupedRoleCharacters(Media series) {
        List<EntityGroup> entityMap = new ArrayList<>();
        if(!series.getCharacters().isEmpty()) {
            Map<String, List<CharacterBase>> map = Stream.of(series.getCharacters())
                    .filter(value -> !TextUtils.isEmpty(value.getRole()))
                    .collect(Collectors.groupingBy(CharacterBase::getRole));
            for (Map.Entry<String, List<CharacterBase>> entry: CompatUtil.getKeyFilteredMap(map)) {
                entityMap.add(new EntityHeader(entry.getKey(), entry.getValue().size()));
                entityMap.addAll(entry.getValue());
            }
        }
        return entityMap;
    }
}
