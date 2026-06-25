package com.mxt.anitrend.util.collection

import com.annimon.stream.Stream
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.base.CharacterStaffBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.group.CharacterActorsFragment
import java.util.*

/**
 * Created by max on 2018/02/18.
 * TextUtil for group various types of media
 */

object GroupingUtil {

    /**
     * Groups media by the media format, assuming that the media has be sorted by format
     * @see KeyUtil.MediaFormat
     * <br></br>
     *
     * Only to be used when the sort type is @{@link KeyUtil.MediaSort.FORMAT}
     * which is the default sort type for the request @{@link KeyUtil.STAFF_MEDIA_REQ}
     * <br></br>
     *
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    fun groupMediaByFormat(edges: List<MediaBase>, model: List<RecyclerItem>?): List<RecyclerItem> {
        val entityMap = model.orEmpty().toMutableList()

        val map = edges.filter {
            !it.format.isNullOrBlank()
        }.groupBy {
            it.format.orEmpty()
        }

        for ((key, value) in CompatUtil.getKeyFilteredMap(map)) {
            val recyclerHeaderItem = RecyclerHeaderItem(key, value.size)
            if (!entityMap.contains(recyclerHeaderItem)) {
                entityMap.add(recyclerHeaderItem)
            }
            entityMap.addAll(value)
        }
        return getDifference(model, entityMap)
    }

    /**
     * Groups media by the media format, assuming that the media has be sorted by language
     * @see KeyUtil.StaffSort
     * <br></br>
     *
     * Only to be used when the sort type is @{@link KeyUtil.StaffSort.LANGUAGE}
     * which is the default sort type for the request @{@link KeyUtil.STAFF_MEDIA_REQ}
     * <br></br>
     *
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    fun groupStaffByLanguage(edges: List<StaffBase>, model: List<RecyclerItem>?): List<RecyclerItem> {
        val entityMap = model.orEmpty().toMutableList()

        val map = edges.filter {
            !it.language.isNullOrBlank()
        }.groupBy {
            it.language.orEmpty()
        }

        for ((key, value) in CompatUtil.getKeyFilteredMap(map)) {
            val recyclerHeaderItem = RecyclerHeaderItem(key, value.size)
            if (!entityMap.contains(recyclerHeaderItem)) {
                entityMap.add(recyclerHeaderItem)
            }
            entityMap.addAll(value)
        }
        return getDifference(model, entityMap)
    }

    /**
     * Groups edge container items their media/node and the character role
     * N.B. In this use case the main model is not used to check for existence
     * of a given role because the voiceActors and characterRoles are grouped by media
     * <br></br>
     *
     * @see CharacterActorsFragment restricted and should only be used by this
     *
     * @see EdgeContainer
     * <br></br>
     *
     *
     * @param edges The potential external model response which needs to be grouped
     */
    fun groupActorMediaEdge(edges: List<MediaEdge>): List<RecyclerItem> {
        val entityMap = ArrayList<RecyclerItem>()
        for (edge in edges) {
            if (edge.node != null) {
                if (!edge.characterRole.isNullOrBlank()) {
                    edge.node.subGroupTitle = edge.characterRole
                }
                edge.node.contentType = KeyUtil.RECYCLER_TYPE_HEADER
                entityMap.add(edge.node)
            }
            edge.voiceActors?.let { voiceActors ->
                if (!CompatUtil.isEmpty(voiceActors)) {
                    entityMap.addAll(voiceActors)
                }
            }
        }
        return entityMap
    }

    /**
     * Groups edge container items their media/node and the media relation type
     * @see MediaEdge
     * <br></br>
     *
     *
     * @param edges The potential external model response which needs to be grouped
     */
    fun groupMediaByRelationType(edges: List<MediaEdge>): List<RecyclerItem> {
        val entityMap = ArrayList<RecyclerItem>()
        for (edge in edges.sortedBy { it.relationType.orEmpty() }) {
            val relationType = edge.relationType.orEmpty()
            val recyclerHeaderItem = RecyclerHeaderItem(relationType)
            if (!entityMap.contains(recyclerHeaderItem)) {
                val totalItems = Stream.of(edges).map<String> { it.relationType.orEmpty() }
                    .filter { role ->
                        CompatUtil.equals(
                            role,
                            relationType,
                        )
                    }
                    .count()
                recyclerHeaderItem.size = totalItems.toInt()
                entityMap.add(recyclerHeaderItem)
            }
            entityMap.add(edge.node)
        }
        return entityMap
    }

    /**
     * Groups characters by role, assuming that the characters have been sorted by format
     * @see KeyUtil.CharacterRole
     * <br></br>
     *
     * Only to be used when the sort type is @{@link KeyUtil.CharacterSort.ROLE}
     * which is the default sort type for the request @{@link KeyUtil.CHARACTER_ACTORS_REQ}
     * <br></br>
     *
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    fun groupCharactersByRole(edges: List<CharacterEdge>, model: List<RecyclerItem>?): List<RecyclerItem> {
        val entityMap = model.orEmpty().toMutableList()
        for (edge in edges) {
            val role = edge.role.orEmpty()
            val recyclerHeaderItem = RecyclerHeaderItem(role)
            if (!entityMap.contains(recyclerHeaderItem)) {
                val totalItems = Stream.of(edges).map<String> { it.role.orEmpty() }
                    .filter { otherRole -> CompatUtil.equals(otherRole, role) }
                    .count()
                recyclerHeaderItem.size = totalItems.toInt()
                entityMap.add(recyclerHeaderItem)
            }
            entityMap.add(edge.node)
        }
        return getDifference(model, entityMap)
    }

    /**
     * Groups characters by year
     * <br></br>
     *
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    fun groupCharactersByYear(edges: List<MediaEdge>, model: List<RecyclerItem>?): List<RecyclerItem> {
        val entityMap = model.orEmpty().toMutableList()

        val years = edges.map {
            val year = it.node.startDate?.year ?: 0
            when (year) {
                0 -> "TBA"
                else -> year.toString()
            }
        }.distinct().sorted()

        for (year in years.reversed()) {
            val recyclerHeaderItem = RecyclerHeaderItem(year, 0, false)
            if (!entityMap.contains(recyclerHeaderItem)) {
                entityMap.add(recyclerHeaderItem)
            }

            val characters = edges.filter { mediaEdge ->
                val startYear = mediaEdge.node.startDate?.year ?: 0
                when (startYear) {
                    0 -> "TBA" == year
                    else -> startYear.toString() == year
                }
            }.flatMap { mediaEdge ->
                mediaEdge.characters.orEmpty().mapNotNull { character ->
                    if (character == null) {
                        null
                    } else {
                        CharacterStaffBase(character, mediaEdge.node)
                    }
                }
            }
            entityMap.addAll(characters)
        }

        return getDifference(model, entityMap)
    }

    /**
     * Groups media by the staff role, assuming that the staff has be sorted by role
     * <br></br>
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    fun groupStaffByRole(edges: List<StaffEdge>, model: List<RecyclerItem>?): List<RecyclerItem> {
        val entityMap = model.orEmpty().toMutableList()
        for (edge in edges) {
            val role = edge.role.orEmpty()
            val recyclerHeaderItem = RecyclerHeaderItem(role)
            if (!entityMap.contains(recyclerHeaderItem)) {
                val totalItems = Stream.of(edges).map<String> { it.role.orEmpty() }
                    .filter { otherRole -> CompatUtil.equals(otherRole, role) }
                    .count()
                recyclerHeaderItem.size = totalItems.toInt()
                entityMap.add(recyclerHeaderItem)
            }
            entityMap.add(edge.node)
        }
        return getDifference(model, entityMap)
    }

    /**
     * Groups media by the staff role, assuming that the staff items have be sorted by format
     * <br></br>
     *
     * @param edges The potential external model response which needs to be grouped
     * @param model The current model item/s containing all data minus current mediaItems
     */
    fun groupMediaByStaffRole(edges: List<MediaEdge>, model: List<RecyclerItem>?): List<RecyclerItem> {
        val entityMap = model.orEmpty().toMutableList()
        for (edge in edges) {
            val staffRole = edge.staffRole.orEmpty()
            val recyclerHeaderItem = RecyclerHeaderItem(staffRole)
            if (!entityMap.contains(recyclerHeaderItem)) {
                val totalItems = Stream.of(edges).map<String> { it.staffRole.orEmpty() }
                    .filter { role ->
                        CompatUtil.equals(
                            role,
                            staffRole,
                        )
                    }
                    .count()
                recyclerHeaderItem.size = totalItems.toInt()
                entityMap.add(recyclerHeaderItem)
            }
            entityMap.add(edge.node)
        }
        return getDifference(model, entityMap)
    }

    /**
     * Returns only new items that were not previously added to the list
     * <br></br>
     *
     * @param model Existing items thus far from a paginated result set
     * @param recyclerItems Model that holds all grouped items including previously stored results
     */
    private fun getDifference(model: List<RecyclerItem>?, recyclerItems: List<RecyclerItem>): List<RecyclerItem> {
        val currentSize = model?.size ?: 0
        return if (currentSize < recyclerItems.size) recyclerItems.subList(currentSize, recyclerItems.size) else emptyList()
    }

    fun <T : RecyclerItem> wrapInGroup(data: List<T>): List<RecyclerItem> = ArrayList<RecyclerItem>(data)
}
