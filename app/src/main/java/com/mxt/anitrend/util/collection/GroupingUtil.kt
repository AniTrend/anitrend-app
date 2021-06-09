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
            it.format
        }

        for ((key, value) in CompatUtil.getKeyFilteredMap(map)) {
            val recyclerHeaderItem = RecyclerHeaderItem(key, value.size)
            if (!entityMap.contains(recyclerHeaderItem))
                entityMap.add(recyclerHeaderItem)
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
            it.language
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
                if (!edge.characterRole.isNullOrBlank())
                    edge.node.subGroupTitle = edge.characterRole
                edge.node.contentType = KeyUtil.RECYCLER_TYPE_HEADER
                entityMap.add(edge.node)
            }
            if (!CompatUtil.isEmpty(edge.voiceActors))
                entityMap.addAll(edge.voiceActors)
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
        for (edge in edges) {
            val recyclerHeaderItem = RecyclerHeaderItem(edge.relationType)
            if (!entityMap.contains(recyclerHeaderItem)) {
                val totalItems = Stream.of(edges).map<String> { it.relationType }
                        .filter { role ->
                            CompatUtil.equals(
                                role,
                                edge.relationType
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
            val recyclerHeaderItem = RecyclerHeaderItem(edge.role)
            if (!entityMap.contains(recyclerHeaderItem)) {
                val totalItems = Stream.of(edges).map<String>{ it.role }
                        .filter { role -> CompatUtil.equals(role, edge.role) }
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
            it.node.startDate.year.let { year ->
                when (year) {
                    0 -> "TBA"
                    else -> year.toString()
                }
            }
        }.distinct().sorted()

        for (year in years.reversed()) {
            val recyclerHeaderItem = RecyclerHeaderItem(year, 0, false)
            if (!entityMap.contains(recyclerHeaderItem))
                entityMap.add(recyclerHeaderItem)

            val characters = edges.filter {
                when (it.node.startDate.year) {
                    0 -> "TBA" == year
                    else -> it.node.startDate.year.toString() == year
                }
            }.flatMap { mediaEdge ->
                mediaEdge.characters.map { character ->
                    CharacterStaffBase(character, mediaEdge.node)
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
            val recyclerHeaderItem = RecyclerHeaderItem(edge.role)
            if (!entityMap.contains(recyclerHeaderItem)) {
                val totalItems = Stream.of(edges).map<String> { it.role }
                        .filter { role -> CompatUtil.equals(role, edge.role) }
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
            val recyclerHeaderItem = RecyclerHeaderItem(edge.staffRole)
            if (!entityMap.contains(recyclerHeaderItem)) {
                val totalItems = Stream.of(edges).map<String> { it.staffRole }
                        .filter { role ->
                            CompatUtil.equals(
                                role,
                                edge.staffRole
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
        return if (!CompatUtil.isEmpty(model)) recyclerItems.subList(model!!.size, recyclerItems.size) else recyclerItems
    }

    fun <T : RecyclerItem> wrapInGroup(data: List<T>): List<RecyclerItem> {
        return ArrayList<RecyclerItem>(data)
    }
}
