package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge;
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge;
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge;
import com.mxt.anitrend.model.entity.base.CharacterBase;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GroupingUtilTests {

    // all media relation types
    private static final String[] relationTypes = {
            KeyUtil.ADAPTATION, KeyUtil.PREQUEL, KeyUtil.SEQUEL, KeyUtil.PARENT,
            KeyUtil.SIDE_STORY, KeyUtil.CHARACTER, KeyUtil.SUMMARY, KeyUtil.ALTERNATIVE,
            KeyUtil.SPIN_OFF
    };
    private static final String[] characterRoles = {
            KeyUtil.MAIN, KeyUtil.SUPPORTING, KeyUtil.BACKGROUND
    };
    private static final String[] staffRoles = {
            "Character Dseign",
            "Director",
            "Music",
            "Series Compostion"
    };
    private static final String[] languages = {"ENGLISH", "JAPANESE"};

    // a sorted list of MediaBase objects of all media formats
    private List<MediaBase> mediaOfAllFormats =
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
            mediaOfAllFormats.stream().collect(Collectors.groupingBy(MediaBase::getFormat));

    // a sorted list of StaffBase objects of different languages
    private List<StaffBase> staffOfAllLanguages =
            Stream.of(languages)
                    .filter(Objects::nonNull)
                    .sorted()
                    .map(language -> {
                        StaffBase staff = mock(StaffBase.class);
                        when(staff.getLanguage()).thenReturn(language);
                        return staff;
                    }).collect(Collectors.toList());

    private Map<String, List<StaffBase>> staffLanguageMap =
            staffOfAllLanguages.stream().collect(Collectors.groupingBy(StaffBase::getLanguage));

    // a sorted list of MediaEdge objects of all media relations
    private List<MediaEdge> mediaOfAllRelations =
            Stream.of(relationTypes)
                    .sorted()
                    .flatMap(relation -> {
                        List<MediaEdge> edges = Arrays.asList(
                                mock(MediaEdge.class),
                                mock(MediaEdge.class));
                        edges.forEach(edge -> {
                            when(edge.getRelationType()).thenReturn(relation);
                            MediaBase media = mock(MediaBase.class);
                            when(edge.getNode()).thenReturn(media);
                        });
                        return edges.stream();
                    }).collect(Collectors.toList());

    private Map<String, List<MediaBase>> mediaRelationMap =
            mediaOfAllRelations.stream()
                    .collect(Collectors.groupingBy(MediaEdge::getRelationType))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue()
                                    .stream()
                                    .map(MediaEdge::getNode)
                                    .collect(Collectors.toList())));

    // a sorted list of CharacterEdge objects of all character roles
    private List<CharacterEdge> charactersOfAllRoles =
            Stream.of(characterRoles)
                    .sorted()
                    .map(role -> {
                        CharacterEdge edge = mock(CharacterEdge.class);
                        when(edge.getRole()).thenReturn(role);
                        CharacterBase character = mock(CharacterBase.class);
                        when(edge.getNode()).thenReturn(character);
                        return edge;
                    })
                    .collect(Collectors.toList());

    private Map<String, List<CharacterBase>> characterRoleMap =
            charactersOfAllRoles.stream()
                    .collect(Collectors.groupingBy(CharacterEdge::getRole))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue()
                                    .stream()
                                    .map(CharacterEdge::getNode)
                                    .collect(Collectors.toList())));

    // a sorted list of StaffEdge objects of all staff roles
    private List<StaffEdge> staffOfAllRoles =
            Stream.of(staffRoles)
                    .sorted()
                    .map(role -> {
                        StaffEdge edge = mock(StaffEdge.class);
                        when(edge.getRole()).thenReturn(role);
                        StaffBase staff = mock(StaffBase.class);
                        when(edge.getNode()).thenReturn(staff);
                        return edge;
                    })
                    .collect(Collectors.toList());

    private Map<String, List<StaffBase>> staffRoleMap =
            staffOfAllRoles.stream()
                    .collect(Collectors.groupingBy(StaffEdge::getRole))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue()
                                    .stream()
                                    .map(StaffEdge::getNode)
                                    .collect(Collectors.toList())));

    // a sorted list of MediaEdge objects of all staff roles
    private List<MediaEdge> mediaOfAllStaffRoles =
            Stream.of(staffRoles)
                    .sorted()
                    .map(role -> {
                        MediaEdge edge = mock(MediaEdge.class);
                        when(edge.getStaffRole()).thenReturn(role);
                        MediaBase media = mock(MediaBase.class);
                        when(edge.getNode()).thenReturn(media);
                        return edge;
                    })
                    .collect(Collectors.toList());

    private Map<String, List<MediaBase>> mediaStaffRoleMap =
            mediaOfAllStaffRoles.stream()
                    .collect(Collectors.groupingBy(MediaEdge::getStaffRole))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue()
                                    .stream()
                                    .map(MediaEdge::getNode)
                                    .collect(Collectors.toList())));

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
                GroupingUtil.groupMediaByFormat(mediaOfAllFormats, null);

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
                GroupingUtil.groupMediaByFormat(mediaOfAllFormats, existingItems);

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
                GroupingUtil.groupStaffByLanguage(staffOfAllLanguages, null);

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
                GroupingUtil.groupStaffByLanguage(staffOfAllLanguages, existingItems);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }
    //endregion

    //region groupActorMediaEdge
    @Test
    public void groupActorMediaEdge_ifEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupActorMediaEdge(Collections.emptyList()), empty());
    }

    @Test
    public void groupActorMediaEdge_shouldReturnTheMediaAsAHeaderFollowedByVoiceActors() {
        List<MediaEdge> mediaEdges =
                Stream.of(characterRoles)
                        .map(role -> {
                            MediaEdge edge = mock(MediaEdge.class);
                            when(edge.getCharacterRole()).thenReturn(role);

                            StaffBase va1 = mock(StaffBase.class);
                            StaffBase va2 = mock(StaffBase.class);
                            when(edge.getVoiceActors()).thenReturn(Arrays.asList(va1, va2));

                            MediaBase media = mock(MediaBase.class);
                            when(edge.getNode()).thenReturn(media);

                            return edge;
                        }).collect(Collectors.toList());

        List<RecyclerItem> required =
                mediaEdges.stream()
                        .flatMap(edge -> {
                            List<RecyclerItem> items = new ArrayList<>();
                            items.add(edge.getNode());
                            items.addAll(edge.getVoiceActors());
                            return items.stream();
                        }).collect(Collectors.toList());

        List<RecyclerItem> result = GroupingUtil.groupActorMediaEdge(mediaEdges);

        assertThat(result, hasSize(required.size()));
        assertThat(result, containsItemsOf(required));

        List<MediaBase> media = result.stream()
                .filter(item -> item instanceof MediaBase)
                .map(item -> (MediaBase) item)
                .collect(Collectors.toList());
        // verify that the content type is set as header for media items

        // and the subgroup title is set to the character role
        for (int i = 0; i < media.size(); i++) {
            verify(media.get(i)).setSubGroupTitle(characterRoles[i]);
            verify(media.get(i)).setContentType(KeyUtil.RECYCLER_TYPE_HEADER);
        }

    }
    //endregion

    //region groupMediaByRelationType
    @Test
    public void groupMediaByRelationType_ifEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupMediaByRelationType(Collections.emptyList()), empty());
    }

    @Test
    public void groupMediaByRelationType_shouldReturnAHeaderForEachRelationFollowedByMedia() {
        List<RecyclerItem> required =
                mediaRelationMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(mediaRelationMap))
                        .collect(Collectors.toList());

        List<RecyclerItem> results =
                GroupingUtil.groupMediaByRelationType(mediaOfAllRelations);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }
    //endregion

    //region groupCharactersByRole

    @Test
    public void groupCharactersByRole_ifTheCharacterEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupCharactersByRole(Collections.emptyList(), null), empty());
    }

    @Test
    public void groupCharactersByRole_ifTheExistingListIsNull_shouldReturnAllItems() {


        /*
        the required result, a list containing a RecyclerHeaderItem per role,
        followed by all characters of that role
         */
        List<RecyclerItem> required =
                characterRoleMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(characterRoleMap))
                        .collect(Collectors.toList());


        List<RecyclerItem> results =
                GroupingUtil.groupCharactersByRole(charactersOfAllRoles, null);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }

    // TODO: 18/06/18 confirm whether exisiting character objects should be included in the results
    @Test
    public void groupCharactersByRole_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        List<String> existingRoles = Arrays.asList(KeyUtil.MAIN, KeyUtil.BACKGROUND);

        List<RecyclerItem> existingItems =
                existingRoles.stream()
                        .flatMap(role -> {
                            List<RecyclerItem> items = new ArrayList<>();
                            items.add(new RecyclerHeaderItem(role, 1));
                            items.add(mock(CharacterBase.class));
                            return items.stream();
                        })
                        .collect(Collectors.toList());

        /*
        The required result, a list containing a RecyclerHeaderItem per non-existing character role,
        followed by all characters of that role
         */
        List<RecyclerItem> required =
                characterRoleMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(characterRoleMap,
                                role -> !existingRoles.contains(role)))
                        .collect(Collectors.toList());

        List<RecyclerItem> results =
                GroupingUtil.groupCharactersByRole(charactersOfAllRoles, existingItems);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }
    //endregion

    //region groupStaffByRole
    @Test
    public void groupStaffByRole_ifTheStaffEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupStaffByRole(Collections.emptyList(), null), empty());
    }

    @Test
    public void groupStaffByRole_ifTheExistingListIsNull_shouldReturnAllItems() {


        /*
        the required result, a list containing a RecyclerHeaderItem per role,
        followed by all staff of that role
         */
        List<RecyclerItem> required =
                staffRoleMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(staffRoleMap))
                        .collect(Collectors.toList());


        List<RecyclerItem> results =
                GroupingUtil.groupStaffByRole(staffOfAllRoles, null);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }

    // TODO: 18/06/18 confirm whether exisiting staff objects should be included in the results
    @Test
    public void groupStaffByRole_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        List<String> existingRoles = Arrays.asList("Director", "Character Design");

        List<RecyclerItem> existingItems =
                existingRoles.stream()
                        .flatMap(role -> {
                            List<RecyclerItem> items = new ArrayList<>();
                            items.add(new RecyclerHeaderItem(role, 1));
                            items.add(mock(StaffBase.class));
                            return items.stream();
                        })
                        .collect(Collectors.toList());

        /*
        The required result, a list containing a RecyclerHeaderItem per non-existing staff role,
        followed by all staff of that role
         */
        List<RecyclerItem> required =
                staffRoleMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(staffRoleMap,
                                role -> !existingRoles.contains(role)))
                        .collect(Collectors.toList());

        List<RecyclerItem> results =
                GroupingUtil.groupStaffByRole(staffOfAllRoles, existingItems);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }
    //endregion

    //region groupMediaByStaffRole
    @Test
    public void groupMediaByStaffRole_ifTheMediaEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupMediaByStaffRole(Collections.emptyList(), null), empty());
    }

    @Test
    public void groupMediaByStaffRole_ifTheExistingListIsNull_shouldReturnAllItems() {


        /*
        the required result, a list containing a RecyclerHeaderItem per role,
        followed by all media of that role
         */
        List<RecyclerItem> required =
                mediaStaffRoleMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(mediaStaffRoleMap))
                        .collect(Collectors.toList());


        List<RecyclerItem> results =
                GroupingUtil.groupMediaByStaffRole(mediaOfAllStaffRoles, null);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }

    // TODO: 18/06/18 confirm whether exisiting media objects should be included in the results
    @Test
    public void groupMediaByStaffRole_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        List<String> existingRoles = Arrays.asList("Director", "Character Design");

        List<RecyclerItem> existingItems =
                existingRoles.stream()
                        .flatMap(role -> {
                            List<RecyclerItem> items = new ArrayList<>();
                            items.add(new RecyclerHeaderItem(role, 1));
                            items.add(mock(MediaBase.class));
                            return items.stream();
                        })
                        .collect(Collectors.toList());

        /*
        The required result, a list containing a RecyclerHeaderItem per non-existing staff role,
        followed by all media of that role
         */
        List<RecyclerItem> required =
                mediaStaffRoleMap.keySet().stream()
                        .sorted()
                        .flatMap(getRecyclerItemsMapperForMap(mediaStaffRoleMap,
                                role -> !existingRoles.contains(role)))
                        .collect(Collectors.toList());

        List<RecyclerItem> results =
                GroupingUtil.groupMediaByStaffRole(mediaOfAllStaffRoles, existingItems);

        assertThat(results, hasSize(required.size()));
        assertThat(results, containsItemsOf(required));
    }
    //endregion

    //region test utils
    private static <T extends RecyclerItem> Function<String, Stream<RecyclerItem>> getRecyclerItemsMapperForMap(Map<String, List<T>> map) {
        return getRecyclerItemsMapperForMap(map, format -> true);
    }

    /**
     * Make a mapper that transforms keys from a given {@link java.util.Map} to a stream of recycler items
     * this stream conditionally contains a {@link RecyclerHeaderItem} for each key followed by the associated list of items
     * from the map
     *
     * @param map           specifies the items associated with each key
     * @param includeHeader decides if a header is included in the stream for a given key
     */
    private static <T extends RecyclerItem> Function<String, Stream<RecyclerItem>> getRecyclerItemsMapperForMap(Map<String, List<T>> map,
                                                                                                                Predicate<String> includeHeader) {
        return key -> {
            List<RecyclerItem> items = new ArrayList<>();
            if (includeHeader.test(key)) {
                items.add(new RecyclerHeaderItem(key, map.get(key).size()));
            }
            items.addAll(map.get(key));
            return items.stream();
        };
    }

    private static <T> Matcher<Iterable<? extends T>> containsItemsOf(Collection<T> collection) {
        return contains(collection.stream()
                .map(Matchers::equalTo)
                .collect(Collectors.toList()));
    }
    //endregion
}