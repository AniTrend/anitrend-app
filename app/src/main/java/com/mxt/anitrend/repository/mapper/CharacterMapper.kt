package com.mxt.anitrend.repository.mapper

import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import com.mxt.anitrend.model.entity.anilist.MediaCharacter
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase as CharacterEntity

fun CharacterBaseData.toCharacterEntity(): CharacterEntity =
    character?.toCharacterEntity() ?: throw IllegalStateException("Empty response body")

fun CharacterBaseData.Character.toCharacterEntity(): CharacterEntity =
    CharacterEntity().also { entity ->
        entity.id = id.toLong()
        entity.name = name?.toTitleBase()
        entity.image = image?.toImageBase()
        entity.isFavourite = isFavourite
        entity.siteUrl = siteUrl
    }

fun CharacterOverviewData.toMediaCharacterEntity(): MediaCharacter =
    character?.toMediaCharacterEntity() ?: throw IllegalStateException("Empty response body")

fun CharacterOverviewData.Character.toMediaCharacterEntity(): MediaCharacter =
    MediaCharacter().also { entity ->
        entity.id = id.toLong()
        entity.name = name?.toTitleBase()
        entity.image = image?.toImageBase()
        entity.isFavourite = isFavourite
        entity.siteUrl = siteUrl
        entity.applyDescription(description)
    }

private fun CharacterBaseData.CharacterName.toTitleBase(): TitleBase =
    TitleBase(
        first = first,
        last = last,
        original = native,
        alternative = alternative?.mapNotNull { value -> value },
    )

private fun CharacterOverviewData.CharacterName.toTitleBase(): TitleBase =
    TitleBase(
        first = first,
        last = last,
        original = native,
        alternative = alternative?.mapNotNull { value -> value },
    )

private fun CharacterBaseData.CharacterImage.toImageBase(): ImageBase =
    ImageBase(
        extraLarge = null,
        large = large,
        medium = medium,
    )

private fun CharacterOverviewData.CharacterImage.toImageBase(): ImageBase =
    ImageBase(
        extraLarge = null,
        large = large,
        medium = medium,
    )
