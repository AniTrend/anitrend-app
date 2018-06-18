package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem;
import com.mxt.anitrend.model.entity.group.RecyclerItem;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupingUtilTests {


    // a sorted list of MediaBase objects of all media formats
    private List<MediaBase> allMedia =
            Stream.of(KeyUtil.MediaFormat)
                    .filter(Objects::nonNull)
                    .map(format -> {
                        MediaBase media = mock(MediaBase.class);
                        when(media.getFormat()).thenReturn(format);
                        return media;
                    })
                    .sorted(Comparator.comparing(MediaBase::getFormat))
                    .collect(Collectors.toList());

    private Map<String, List<MediaBase>> mediaFormatMap =
            allMedia.stream().collect(Collectors.groupingBy(MediaBase::getFormat));

    // a sorted list of StaffBase objects of different languages
    private List<StaffBase> allStaff =
            Stream.of("ENGLISH", "JAPANESE")
                    .filter(Objects::nonNull)
                    .sorted()
                    .map(language -> {
                        StaffBase staff = mock(StaffBase.class);
                        when(staff.getLanguage()).thenReturn(language);
                        return staff;
                    }).collect(Collectors.toList());

    private Map<String, List<StaffBase>> staffLanguageMap =
            allStaff.stream().collect(Collectors.groupingBy(StaffBase::getLanguage));


    //region groupMediaByFormat
    @Test
    public void groupMediaByFormat_ifTheMediaListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupMediaByFormat(Collections.emptyList(), null), empty());
    }

    @Test
    public void groupMediaByFormat_ifTheExistingListIsNull_shouldReturnAllItems() {


        /*
        the required result, a list containing a RecyclerHeaderItem per media format,
        followed by all media of that format
         */
        List<RecyclerItem> required =
                mediaFormatMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(mediaFormatMap))
                        .collect(Collectors.toList());


        List<RecyclerItem> results =
                GroupingUtil.groupMediaByFormat(allMedia, null);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }

    // TODO: 18/06/18 confirm whether exisiting media objects should be included in the results
    @Test
    public void groupMediaByFormat_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        List<String> existingFormats = Arrays.asList(KeyUtil.MANGA, KeyUtil.OVA, KeyUtil.ONE_SHOT);

        List<RecyclerItem> existingItems =
                existingFormats.stream()
                        .flatMap(format -> {
                            List<RecyclerItem> items = new ArrayList<>();
                            items.add(new RecyclerHeaderItem(format, 1));
                            MediaBase media = mock(MediaBase.class);
                            when(media.getFormat()).thenReturn(format);
                            items.add(media);
                            return items.stream();
                        })
                        .collect(Collectors.toList());

        /*
        The required result, a list containing a RecyclerHeaderItem per non-existing media format,
        followed by all media of that format
         */
        List<RecyclerItem> required =
                mediaFormatMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(mediaFormatMap,
                                format -> !existingFormats.contains(format)))
                        .collect(Collectors.toList());

        List<RecyclerItem> results =
                GroupingUtil.groupMediaByFormat(allMedia, existingItems);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }
    //endregion

    //region groupStaffByLanguage
    @Test
    public void groupStaffByLanguage_ifTheMediaListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupStaffByLanguage(Collections.emptyList(), null), empty());
    }

    @Test
    public void groupStaffByLanguage_ifTheExistingListIsNull_shouldReturnAllItems() {


        /*
        the required result, a list containing a RecyclerHeaderItem per language,
        followed by all staff of that language
         */
        List<RecyclerItem> required =
                staffLanguageMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(staffLanguageMap))
                        .collect(Collectors.toList());


        List<RecyclerItem> results =
                GroupingUtil.groupStaffByLanguage(allStaff, null);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }

    // TODO: 18/06/18 confirm whether existing staff objects should be included in the results
    @Test
    public void groupStaffByLanguage_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        List<String> existingLanguages = Collections.singletonList("ENGLISH");

        List<RecyclerItem> existingItems =
                existingLanguages.stream()
                        .flatMap(language -> {
                            List<RecyclerItem> items = new ArrayList<>();
                            items.add(new RecyclerHeaderItem(language, 1));
                            StaffBase staff = mock(StaffBase.class);
                            when(staff.getLanguage()).thenReturn(language);
                            items.add(staff);
                            return items.stream();
                        })
                        .collect(Collectors.toList());

        /*
        The required result, a list containing a RecyclerHeaderItem per non-existing language,
        followed by all staff of that language
         */
        List<RecyclerItem> required =
                staffLanguageMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(staffLanguageMap,
                                language -> !existingLanguages.contains(language)))
                        .collect(Collectors.toList());

        List<RecyclerItem> results =
                GroupingUtil.groupStaffByLanguage(allStaff, existingItems);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }
    //endregion

    //region groupActorMediaEdge
    @Test
    public void groupActorMediaEdge_ifEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupActorMediaEdge(Collections.emptyList()), empty());
    }



    //endregion

    @Test
    public void groupMediaByRelationType() {
    }

    @Test
    public void groupCharactersByRole() {
    }

    @Test
    public void groupStaffByRole() {
    }

    @Test
    public void groupMediaByStaffRole() {
    }

    /*
    wrapInGroup
     */

    @Test
    public void wrapInGroup() {
    }

    //region test utils
    private static <T extends RecyclerItem> Function<String, Stream<RecyclerItem>> getRecyclerItemsMapperForMap(Map<String, List<T>> map) {
        return getRecyclerItemsMapperForMap(map, format -> true);
    }

    private static <T extends RecyclerItem> Function<String, Stream<RecyclerItem>> getRecyclerItemsMapperForMap(Map<String, List<T>> map,
                                                                                                                Function<String, Boolean> includeHeader) {
        return format -> {
            List<RecyclerItem> formatItems = new ArrayList<>();
            if (includeHeader.apply(format)) {
                formatItems.add(new RecyclerHeaderItem(format, map.get(format).size()));
            }
            formatItems.addAll(map.get(format));
            return formatItems.stream();
        };
    }

    private static <T> Matcher<Iterable<? extends T>> containsItemsOf(Collection<T> collection) {
        return contains(collection.stream()
                .map(Matchers::equalTo)
                .collect(Collectors.toList()));
    }
    //endregion
}