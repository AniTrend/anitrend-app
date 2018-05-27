package com.mxt.anitrend.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge;
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge;
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.body.EdgeContainer;
import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem;
import com.mxt.anitrend.view.fragment.group.CharacterActorsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by max on 2018/02/18.
 * Utils for group various types of media
 */

public class GroupingUtil {

    /**
     * Groups media by the media format, assuming that the media has be sorted by format
     * @see KeyUtil.MediaFormat
     * <br/>
     *
     * Only to be used when the sort type is @{@link KeyUtil.MediaSort#FORMAT}
     * which is the default sort type for the request @{@link KeyUtil#STAFF_MEDIA_REQ}
     * <br/>
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    public static List<RecyclerItem> groupMediaByFormat(@NonNull List<MediaBase> edges, @Nullable List<RecyclerItem> model) {
        List<RecyclerItem> entityMap = !CompatUtil.isEmpty(model) ? new ArrayList<>(model) : new ArrayList<>();

        Map<String, List<MediaBase>> map = Stream.of(edges)
                .filter(value -> !TextUtils.isEmpty(value.getFormat()))
                .collect(Collectors.groupingBy(MediaBase::getFormat));

        for (Map.Entry<String, List<MediaBase>> entry: CompatUtil.getKeyFilteredMap(map)) {
            RecyclerHeaderItem recyclerHeaderItem = new RecyclerHeaderItem(entry.getKey(), entry.getValue().size());
            if(!entityMap.contains(recyclerHeaderItem))
                entityMap.add(recyclerHeaderItem);
            entityMap.addAll(entry.getValue());
        }
        return getDifference(model, entityMap);
    }

    /**
     * Groups media by the media format, assuming that the media has be sorted by language
     * @see KeyUtil.StaffSort
     * <br/>
     *
     * Only to be used when the sort type is @{@link KeyUtil.StaffSort#LANGUAGE}
     * which is the default sort type for the request @{@link KeyUtil#STAFF_MEDIA_REQ}
     * <br/>
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    public static List<RecyclerItem> groupStaffByLanguage(@NonNull List<StaffBase> edges, @Nullable List<RecyclerItem> model) {
        List<RecyclerItem> entityMap = !CompatUtil.isEmpty(model) ? new ArrayList<>(model) : new ArrayList<>();

        Map<String, List<StaffBase>> map = Stream.of(edges)
                .filter(value -> !TextUtils.isEmpty(value.getLanguage()))
                .collect(Collectors.groupingBy(StaffBase::getLanguage));

        for (Map.Entry<String, List<StaffBase>> entry: CompatUtil.getKeyFilteredMap(map)) {
            RecyclerHeaderItem recyclerHeaderItem = new RecyclerHeaderItem(entry.getKey(), entry.getValue().size());
            if(!entityMap.contains(recyclerHeaderItem)) {
                entityMap.add(recyclerHeaderItem);
            }
            entityMap.addAll(entry.getValue());
        }
        return getDifference(model, entityMap);
    }

    /**
     * Groups edge container items their media/node and the character role
     * N.B. In this use case the main model is not used to check for existence
     * of a given role because the voiceActors and characterRoles are grouped by media
     * <br/>
     *
     * @see CharacterActorsFragment restricted and should only be used by this
     * @see EdgeContainer
     * <br/>
     *
     * @param edges The potential external model response which needs to be grouped
     */
    public static List<RecyclerItem> groupActorMediaEdge(List<MediaEdge> edges) {
        List<RecyclerItem> entityMap = new ArrayList<>();
        for (MediaEdge edge : edges) {
            if(edge.getNode() != null) {
                if(!TextUtils.isEmpty(edge.getCharacterRole()))
                    edge.getNode().setSubGroupTitle(edge.getCharacterRole());
                edge.getNode().setContentType(KeyUtil.RECYCLER_TYPE_HEADER);
                entityMap.add(edge.getNode());
            }
            if(!CompatUtil.isEmpty(edge.getVoiceActors()))
                entityMap.addAll(edge.getVoiceActors());
        }
        return entityMap;
    }

    /**
     * Groups edge container items their media/node and the media relation type
     * @see MediaEdge
     * <br/>
     *
     * @param edges The potential external model response which needs to be grouped
     */
    public static List<RecyclerItem> groupMediaByRelationType(List<MediaEdge> edges) {
        List<RecyclerItem> entityMap = new ArrayList<>();
        for (MediaEdge edge: edges) {
            RecyclerHeaderItem recyclerHeaderItem = new RecyclerHeaderItem(edge.getRelationType());
            if(!entityMap.contains(recyclerHeaderItem)) {
                long totalItems = Stream.of(edges).map(MediaEdge::getRelationType)
                        .filter(role -> CompatUtil.equals(role, edge.getRelationType()))
                        .count();
                recyclerHeaderItem.setSize((int) totalItems);
                entityMap.add(recyclerHeaderItem);
            }
            entityMap.add(edge.getNode());
        }
        return entityMap;
    }
    /**
     * Groups characters by role, assuming that the characters have been sorted by format
     * @see KeyUtil.CharacterRole
     * <br/>
     *
     * Only to be used when the sort type is @{@link KeyUtil.CharacterSort#ROLE}
     * which is the default sort type for the request @{@link KeyUtil#CHARACTER_ACTORS_REQ}
     * <br/>
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    public static List<RecyclerItem> groupCharactersByRole(List<CharacterEdge> edges, @Nullable List<RecyclerItem> model) {
        List<RecyclerItem> entityMap = !CompatUtil.isEmpty(model) ? new ArrayList<>(model) : new ArrayList<>();
        for (CharacterEdge edge: edges) {
            RecyclerHeaderItem recyclerHeaderItem = new RecyclerHeaderItem(edge.getRole());
            if(!entityMap.contains(recyclerHeaderItem)) {
                long totalItems = Stream.of(edges).map(CharacterEdge::getRole)
                        .filter(role -> CompatUtil.equals(role, edge.getRole()))
                        .count();
                recyclerHeaderItem.setSize((int) totalItems);
                entityMap.add(recyclerHeaderItem);
            }
            entityMap.add(edge.getNode());
        }
        return getDifference(model, entityMap);
    }

    /**
     * Groups media by the staff role, assuming that the staff has be sorted by role
     * <br/>
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    public static List<RecyclerItem> groupStaffByRole(List<StaffEdge> edges, @Nullable List<RecyclerItem> model) {
        List<RecyclerItem> entityMap = !CompatUtil.isEmpty(model) ? new ArrayList<>(model) : new ArrayList<>();
        for (StaffEdge edge: edges) {
            RecyclerHeaderItem recyclerHeaderItem = new RecyclerHeaderItem(edge.getRole());
            if(!entityMap.contains(recyclerHeaderItem)) {
                long totalItems = Stream.of(edges).map(StaffEdge::getRole)
                        .filter(role -> CompatUtil.equals(role, edge.getRole()))
                        .count();
                recyclerHeaderItem.setSize((int) totalItems);
                entityMap.add(recyclerHeaderItem);
            }
            entityMap.add(edge.getNode());
        }
        return getDifference(model, entityMap);
    }

    /**
     * Groups media by the staff role, assuming that the staff items have be sorted by format
     * <br/>
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    public static List<RecyclerItem> groupMediaByStaffRole(List<MediaEdge> edges, @Nullable List<RecyclerItem> model) {
        List<RecyclerItem> entityMap = !CompatUtil.isEmpty(model) ? new ArrayList<>(model) : new ArrayList<>();
        for (MediaEdge edge: edges) {
            RecyclerHeaderItem recyclerHeaderItem = new RecyclerHeaderItem(edge.getStaffRole());
            if(!entityMap.contains(recyclerHeaderItem)) {
                long totalItems = Stream.of(edges).map(MediaEdge::getStaffRole)
                        .filter(role -> CompatUtil.equals(role, edge.getStaffRole()))
                        .count();
                recyclerHeaderItem.setSize((int) totalItems);
                entityMap.add(recyclerHeaderItem);
            }
            entityMap.add(edge.getNode());
        }
        return getDifference(model, entityMap);
    }

    /**
     * Returns only new items that were not previously added to the list
     * <br/>
     *
     * @param model Existing items thus far from a paginated result set
     * @param recyclerItems Model that holds all grouped items including previously stored results
     */
    private static List<RecyclerItem> getDifference(@Nullable List<? extends RecyclerItem> model, List<RecyclerItem> recyclerItems) {
        if(!CompatUtil.isEmpty(model))
            return recyclerItems.subList(model.size(), recyclerItems.size());
        return recyclerItems;
    }
    
    public static <T extends RecyclerItem> List<RecyclerItem> wrapInGroup(List<T> data) {
        return new ArrayList<>(data);
    }
}
