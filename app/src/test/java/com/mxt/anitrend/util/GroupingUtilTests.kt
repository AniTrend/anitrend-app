package com.mxt.anitrend.util

import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.collection.GroupingUtil
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class GroupingUtilTests {

    private val relationTypes = arrayOf(
        KeyUtil.ADAPTATION,
        KeyUtil.PREQUEL,
        KeyUtil.SEQUEL,
        KeyUtil.PARENT,
        KeyUtil.SIDE_STORY,
        KeyUtil.CHARACTER,
        KeyUtil.SUMMARY,
        KeyUtil.ALTERNATIVE,
        KeyUtil.SPIN_OFF
    )
    private val characterRoles = arrayOf(KeyUtil.MAIN, KeyUtil.SUPPORTING, KeyUtil.BACKGROUND)
    private val staffRoles = arrayOf(
        "Character Dseign",
        "Director",
        "Music",
        "Series Compostion"
    )
    private val languages = arrayOf("ENGLISH", "JAPANESE")

    private val mediaOfAllFormats: List<MediaBase> = KeyUtil.MediaFormat
        .filterNotNull()
        .map { format ->
            mock(MediaBase::class.java).apply {
                `when`(this.format).thenReturn(format)
            }
        }
        .sortedBy { it.format }

    private val mediaFormatMap: Map<String, List<MediaBase>> = mediaOfAllFormats
        .groupBy { it.format }

    private val staffOfAllLanguages: List<StaffBase> = languages
        .sorted()
        .map { language ->
            mock(StaffBase::class.java).apply {
                `when`(this.language).thenReturn(language)
            }
        }

    private val staffLanguageMap: Map<String, List<StaffBase>> = staffOfAllLanguages
        .groupBy { it.language }

    private val mediaOfAllRelations: List<MediaEdge> = relationTypes
        .sorted()
        .flatMap { relation ->
            val edges = listOf(
                mock(MediaEdge::class.java),
                mock(MediaEdge::class.java)
            )
            edges.forEach { edge ->
                `when`(edge.relationType).thenReturn(relation)
                val media = mock(MediaBase::class.java)
                `when`(edge.node).thenReturn(media)
            }
            edges
        }

    private val mediaRelationMap: Map<String, List<MediaBase>> = mediaOfAllRelations
        .groupBy { it.relationType }
        .mapValues { entry ->
            entry.value.map { it.node }
        }

    private val charactersOfAllRoles: List<CharacterEdge> = characterRoles
        .sorted()
        .map { role ->
            mock(CharacterEdge::class.java).apply {
                `when`(this.role).thenReturn(role)
                val character = mock(CharacterBase::class.java)
                `when`(this.node).thenReturn(character)
            }
        }

    private val characterRoleMap: Map<String, List<CharacterBase>> = charactersOfAllRoles
        .groupBy { it.role }
        .mapValues { entry ->
            entry.value.map { it.node }
        }

    private val staffOfAllRoles: List<StaffEdge> = staffRoles
        .sorted()
        .map { role ->
            mock(StaffEdge::class.java).apply {
                `when`(this.role).thenReturn(role)
                val staff = mock(StaffBase::class.java)
                `when`(this.node).thenReturn(staff)
            }
        }

    private val staffRoleMap: Map<String, List<StaffBase>> = staffOfAllRoles
        .groupBy { it.role }
        .mapValues { entry ->
            entry.value.map { it.node }
        }

    private val mediaOfAllStaffRoles: List<MediaEdge> = staffRoles
        .sorted()
        .map { role ->
            mock(MediaEdge::class.java).apply {
                `when`(this.staffRole).thenReturn(role)
                val media = mock(MediaBase::class.java)
                `when`(this.node).thenReturn(media)
            }
        }

    private val mediaStaffRoleMap: Map<String, List<MediaBase>> = mediaOfAllStaffRoles
        .groupBy { it.staffRole }
        .mapValues { entry ->
            entry.value.map { it.node }
        }

    @Test
    fun groupMediaByFormat_ifTheMediaListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupMediaByFormat(emptyList(), null), empty())
    }

    @Test
    fun groupMediaByFormat_ifTheExistingListIsNull_shouldReturnAllItems() {
        val required = mediaFormatMap.keys
            .sorted()
            .flatMap(getRecyclerItemsMapperForMap(mediaFormatMap))

        val results = GroupingUtil.groupMediaByFormat(mediaOfAllFormats, null)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupMediaByFormat_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        val existingFormats = listOf(KeyUtil.MANGA, KeyUtil.OVA, KeyUtil.ONE_SHOT)

        val existingItems = existingFormats.flatMap { format ->
            val items = mutableListOf<RecyclerItem>()
            items.add(RecyclerHeaderItem(format, 1))
            val media = mock(MediaBase::class.java)
            `when`(media.format).thenReturn(format)
            items.add(media)
            items
        }

        val required = mediaFormatMap.keys
            .sorted()
            .filter { format -> !existingFormats.contains(format) }
            .flatMap(getRecyclerItemsMapperForMap(mediaFormatMap))

        val results = GroupingUtil.groupMediaByFormat(mediaOfAllFormats, existingItems)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupStaffByLanguage_ifTheMediaListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupStaffByLanguage(emptyList(), null), empty())
    }

    @Test
    fun groupStaffByLanguage_ifTheExistingListIsNull_shouldReturnAllItems() {
        val required = staffLanguageMap.keys
            .sorted()
            .flatMap(getRecyclerItemsMapperForMap(staffLanguageMap))

        val results = GroupingUtil.groupStaffByLanguage(staffOfAllLanguages, null)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupStaffByLanguage_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        val existingLanguages = listOf("ENGLISH")

        val existingItems = existingLanguages.flatMap { language ->
            val items = mutableListOf<RecyclerItem>()
            items.add(RecyclerHeaderItem(language, 1))
            val staff = mock(StaffBase::class.java)
            `when`(staff.language).thenReturn(language)
            items.add(staff)
            items
        }

        val required = staffLanguageMap.keys
            .sorted()
            .filter { language -> !existingLanguages.contains(language) }
            .flatMap(getRecyclerItemsMapperForMap(staffLanguageMap))

        val results = GroupingUtil.groupStaffByLanguage(staffOfAllLanguages, existingItems)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupActorMediaEdge_ifEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupActorMediaEdge(emptyList()), empty())
    }

    @Test
    fun groupActorMediaEdge_shouldReturnTheMediaAsAHeaderFollowedByVoiceActors() {
        val mediaEdges = characterRoles.map { role ->
            val edge = mock(MediaEdge::class.java)
            `when`(edge.characterRole).thenReturn(role)

            val va1 = mock(StaffBase::class.java)
            val va2 = mock(StaffBase::class.java)
            `when`(edge.voiceActors).thenReturn(listOf(va1, va2))

            val media = mock(MediaBase::class.java)
            `when`(edge.node).thenReturn(media)

            edge
        }

        val required = mediaEdges.flatMap { edge ->
            val items = mutableListOf<RecyclerItem>()
            items.add(edge.node)
            items.addAll(edge.voiceActors)
            items
        }

        val result = GroupingUtil.groupActorMediaEdge(mediaEdges)

        assertThat(result, hasSize(required.size))
        assertThat(result, containsItemsOf(required))

        val media = result.filterIsInstance<MediaBase>()

        media.forEachIndexed { index, item ->
            verify(item).setSubGroupTitle(characterRoles[index])
            verify(item).setContentType(KeyUtil.RECYCLER_TYPE_HEADER)
        }
    }

    @Test
    fun groupMediaByRelationType_ifEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupMediaByRelationType(emptyList()), empty())
    }

    @Test
    fun groupMediaByRelationType_shouldReturnAHeaderForEachRelationFollowedByMedia() {
        val required = mediaRelationMap.keys
            .sorted()
            .flatMap(getRecyclerItemsMapperForMap(mediaRelationMap))

        val results = GroupingUtil.groupMediaByRelationType(mediaOfAllRelations)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupCharactersByRole_ifTheCharacterEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupCharactersByRole(emptyList(), null), empty())
    }

    @Test
    fun groupCharactersByRole_ifTheExistingListIsNull_shouldReturnAllItems() {
        val required = characterRoleMap.keys
            .sorted()
            .flatMap(getRecyclerItemsMapperForMap(characterRoleMap))

        val results = GroupingUtil.groupCharactersByRole(charactersOfAllRoles, null)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupCharactersByRole_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        val existingRoles = listOf(KeyUtil.MAIN, KeyUtil.BACKGROUND)

        val existingItems = existingRoles.flatMap { role ->
            val items = mutableListOf<RecyclerItem>()
            items.add(RecyclerHeaderItem(role, 1))
            items.add(mock(CharacterBase::class.java))
            items
        }

        val required = characterRoleMap.keys
            .sorted()
            .filter { role -> !existingRoles.contains(role) }
            .flatMap(getRecyclerItemsMapperForMap(characterRoleMap))

        val results = GroupingUtil.groupCharactersByRole(charactersOfAllRoles, existingItems)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupStaffByRole_ifTheStaffEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupStaffByRole(emptyList(), null), empty())
    }

    @Test
    fun groupStaffByRole_ifTheExistingListIsNull_shouldReturnAllItems() {
        val required = staffRoleMap.keys
            .sorted()
            .flatMap(getRecyclerItemsMapperForMap(staffRoleMap))

        val results = GroupingUtil.groupStaffByRole(staffOfAllRoles, null)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupStaffByRole_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        val existingRoles = listOf("Director", "Character Design")

        val existingItems = existingRoles.flatMap { role ->
            val items = mutableListOf<RecyclerItem>()
            items.add(RecyclerHeaderItem(role, 1))
            items.add(mock(StaffBase::class.java))
            items
        }

        val required = staffRoleMap.keys
            .sorted()
            .filter { role -> !existingRoles.contains(role) }
            .flatMap(getRecyclerItemsMapperForMap(staffRoleMap))

        val results = GroupingUtil.groupStaffByRole(staffOfAllRoles, existingItems)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupMediaByStaffRole_ifTheMediaEdgeListIsEmpty_shouldReturnAnEmptyList() {
        assertThat(GroupingUtil.groupMediaByStaffRole(emptyList(), null), empty())
    }

    @Test
    fun groupMediaByStaffRole_ifTheExistingListIsNull_shouldReturnAllItems() {
        val required = mediaStaffRoleMap.keys
            .sorted()
            .flatMap(getRecyclerItemsMapperForMap(mediaStaffRoleMap))

        val results = GroupingUtil.groupMediaByStaffRole(mediaOfAllStaffRoles, null)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    @Test
    fun groupMediaByStaffRole_ifTheExistingListIsNotEmpty_shouldNotReturnExistingHeaders() {
        val existingRoles = listOf("Director", "Character Design")

        val existingItems = existingRoles.flatMap { role ->
            val items = mutableListOf<RecyclerItem>()
            items.add(RecyclerHeaderItem(role, 1))
            items.add(mock(MediaBase::class.java))
            items
        }

        val required = mediaStaffRoleMap.keys
            .sorted()
            .filter { role -> !existingRoles.contains(role) }
            .flatMap(getRecyclerItemsMapperForMap(mediaStaffRoleMap))

        val results = GroupingUtil.groupMediaByStaffRole(mediaOfAllStaffRoles, existingItems)

        assertThat(results, hasSize(required.size))
        assertThat(results, containsItemsOf(required))
    }

    private fun <T : RecyclerItem> getRecyclerItemsMapperForMap(
        map: Map<String, List<T>>
    ): (String) -> List<RecyclerItem> = getRecyclerItemsMapperForMap(map) { true }

    private fun <T : RecyclerItem> getRecyclerItemsMapperForMap(
        map: Map<String, List<T>>,
        includeHeader: (String) -> Boolean
    ): (String) -> List<RecyclerItem> = { key ->
        val items = mutableListOf<RecyclerItem>()
        val itemsForKey = map[key].orEmpty()
        if (includeHeader(key)) {
            items.add(RecyclerHeaderItem(key, itemsForKey.size))
        }
        items.addAll(itemsForKey)
        items
    }

    private fun <T> containsItemsOf(collection: Collection<T>): Matcher<Iterable<out T>> {
        val matchers = collection.map { Matchers.equalTo(it) }.toTypedArray()
        return contains(*matchers)
    }
}
