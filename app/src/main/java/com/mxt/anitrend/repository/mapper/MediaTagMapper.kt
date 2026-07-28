package com.mxt.anitrend.repository.mapper

import com.mxt.anitrend.graphql.generated.MediaTagCollectionData
import com.mxt.anitrend.model.entity.anilist.MediaTag

fun MediaTagCollectionData.MediaTagCollection.toMediaTag(): MediaTag = MediaTag(
    name = name,
    description = description,
    category = category,
    rank = rank ?: 0,
    isGeneralSpoiler = isGeneralSpoiler ?: false,
    isAdult = isAdult ?: false,
).also { mediaTag ->
    mediaTag.id = id.toLong()
}

fun List<MediaTagCollectionData.MediaTagCollection?>.toMediaTags(): List<MediaTag> = mapNotNull { mediaTag -> mediaTag?.toMediaTag() }
