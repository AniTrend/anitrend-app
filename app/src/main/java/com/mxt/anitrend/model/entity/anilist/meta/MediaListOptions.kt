package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.mxt.anitrend.util.KeyUtil
import kotlinx.parcelize.Parcelize
import kotlin.jvm.JvmName

/**
 * Created by max on 2018/03/22.
 * https://anilist.github.io/ApiV2-GraphQL-Docs/medialistoptions.doc.html
 */
@Parcelize
class MediaListOptions(
    @param:KeyUtil.ScoreFormat var scoreFormat: String = KeyUtil.POINT_10_DECIMAL,
    var rowOrder: String? = null,
    @get:JvmName("isUseLegacyLists") var useLegacyLists: Boolean = false,
    var animeList: MediaListTypeOptions? = null,
    var mangaList: MediaListTypeOptions? = null,
) : Parcelable
