package com.mxt.anitrend.ui.detail

import android.content.Context
import android.view.View
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.CharacterStaffBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.character.CharacterMediaHeaderUiState
import com.mxt.anitrend.ui.character.CharacterRowUiState
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCardUiState
import com.mxt.anitrend.ui.staff.StaffRowUiState

internal fun projectGroupedDetailRows(
    items: List<RecyclerItem>,
    projectContent: (RecyclerItem, String) -> DetailGridRow?,
): List<DetailGridRow> {
    if (items.isEmpty()) return emptyList()

    val occurrences = HashMap<String, Int>()
    val seenKeys = HashSet<String>(items.size)
    val result = ArrayList<DetailGridRow>(items.size)
    var groupKey = "unheaded:0"

    items.forEach { item ->
        if (item is RecyclerHeaderItem) {
            val title = item.getTitle()
            val occurrence = occurrences[title] ?: 0
            occurrences[title] = occurrence + 1
            groupKey = detailGroupKey(title, occurrence)
            val header =
                DetailGridRow.Header(
                    key = "group-header:$groupKey",
                    title = title,
                    count = item.size,
                )
            if (seenKeys.add(header.key)) result.add(header)
        } else {
            val row = projectContent(item, groupKey)
            if (row != null && seenKeys.add(row.key)) result.add(row)
        }
    }

    return result
}

internal fun projectGroupedMediaSeriesRows(
    context: Context,
    items: List<RecyclerItem>,
    onMediaClick: (View, MediaBase) -> Unit,
    onMediaLongClick: (View, MediaBase) -> Unit,
): List<DetailGridRow> = projectGroupedDetailRows(items) { item, groupKey ->
    val media = item as? MediaBase ?: return@projectGroupedDetailRows null
    mediaSeriesRow(context, media, groupKey, onMediaClick, onMediaLongClick)
}

internal fun projectGroupedCharacterRows(
    items: List<RecyclerItem>,
    onCharacterClick: (View, CharacterBase) -> Unit,
): List<DetailGridRow> = projectGroupedDetailRows(items) { item, groupKey ->
    val character = item as? CharacterBase ?: return@projectGroupedDetailRows null
    DetailGridRow.Character(
        key = scopedRowKey(groupKey, "character", character.id.toString()),
        state = CharacterRowUiState.from(character),
        onClick = { target -> onCharacterClick(target, character) },
    )
}

internal fun projectGroupedStaffRows(
    items: List<RecyclerItem>,
    onStaffClick: (View, StaffBase) -> Unit,
): List<DetailGridRow> = projectGroupedDetailRows(items) { item, groupKey ->
    val staff = item as? StaffBase ?: return@projectGroupedDetailRows null
    DetailGridRow.Staff(
        key = scopedRowKey(groupKey, "staff", staff.id.toString()),
        state = StaffRowUiState.from(staff),
        onClick = { target -> onStaffClick(target, staff) },
    )
}

internal fun projectStaffCharacterRoleRows(
    items: List<RecyclerItem>,
    onCharacterClick: (View, CharacterStaffBase) -> Unit,
): List<DetailGridRow> = projectGroupedDetailRows(items) { item, groupKey ->
    val role = item as? CharacterStaffBase ?: return@projectGroupedDetailRows null
    val character = role.character
    val media = role.media
    val state =
        StaffCharacterRoleUiState(
            characterId = character.id,
            characterName = character.name?.fullName.orEmpty(),
            mediaId = media.id,
            mediaTitle = media.title?.userPreferred.orEmpty(),
            coverImageUrl = character.image?.let { it.extraLarge ?: it.large },
            isFavourite = character.isFavourite,
        )
    DetailGridRow.StaffCharacterRole(
        key =
        scopedRowKey(
            groupKey,
            "character-role",
            "${character.id}:${media.id}:${media.type.orEmpty()}",
        ),
        state = state,
        onClick = { target -> onCharacterClick(target, role) },
    )
}

internal fun projectCharacterActorRows(
    edges: List<MediaEdge>,
    onMediaClick: (View, MediaBase) -> Unit,
    onMediaLongClick: (View, MediaBase) -> Unit,
    onStaffClick: (View, StaffBase) -> Unit,
): List<DetailGridRow> {
    if (edges.isEmpty()) return emptyList()

    val seenKeys = HashSet<String>(edges.size)
    val rows = ArrayList<DetailGridRow>(edges.size)
    edges.forEach { edge ->
        val media = edge.node
        val groupKey = actorGroupKey(media.id, media.type, edge.characterRole)
        val header =
            DetailGridRow.CharacterMediaHeader(
                key =
                scopedRowKey(
                    groupKey,
                    "media-header",
                    "${media.id}:${media.type.orEmpty()}:${edge.characterRole.orEmpty()}",
                ),
                state =
                CharacterMediaHeaderUiState.from(media).copy(
                    subGroupTitle = edge.characterRole?.takeIf(String::isNotBlank) ?: media.subGroupTitle,
                ),
                onClick = { target -> onMediaClick(target, media) },
                onLongClick = { target -> onMediaLongClick(target, media) },
            )
        if (seenKeys.add(header.key)) rows.add(header)

        edge.voiceActors.orEmpty().forEach { staff ->
            val row =
                DetailGridRow.Staff(
                    key = scopedRowKey(groupKey, "staff", staff.id.toString()),
                    state = StaffRowUiState.from(staff),
                    onClick = { target -> onStaffClick(target, staff) },
                )
            if (seenKeys.add(row.key)) rows.add(row)
        }
    }

    return rows
}

private fun mediaSeriesRow(
    context: Context,
    media: MediaBase,
    groupKey: String,
    onMediaClick: (View, MediaBase) -> Unit,
    onMediaLongClick: (View, MediaBase) -> Unit,
) = DetailGridRow.MediaSeries(
    key = scopedRowKey(groupKey, "media", "${media.id}:${media.type.orEmpty()}"),
    state = MediaSeriesCardUiState.from(context, media),
    onClick = { target -> onMediaClick(target, media) },
    onLongClick = { target -> onMediaLongClick(target, media) },
)

private fun detailGroupKey(title: String, occurrence: Int): String = "${title.length}:$title:$occurrence"

private fun actorGroupKey(mediaId: Long, mediaType: String?, role: String?): String {
    val normalizedRole = role?.takeIf(String::isNotBlank).orEmpty()
    return "actor:$mediaId:${mediaType.orEmpty()}:${normalizedRole.length}:$normalizedRole"
}

private fun scopedRowKey(
    groupKey: String,
    type: String,
    identity: String,
): String = "$groupKey/$type:$identity"
