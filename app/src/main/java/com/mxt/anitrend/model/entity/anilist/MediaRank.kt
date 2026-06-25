package com.mxt.anitrend.model.entity.anilist

import android.os.Parcelable
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Created by Maxwell on 10/24/2016.
 */
@Parcelize
class MediaRank
@JvmOverloads
constructor(
    var id: Int = 0,
    var rank: Int = 0,
    @param:KeyUtil.MediaRankType var type: String = "",
    @param:KeyUtil.MediaFormat var format: String = "",
    var year: Int = 0,
    @param:KeyUtil.MediaSeason var season: String? = null,
    var isAllTime: Boolean = false,
    var context: String = "",
) : Parcelable {
    val typeHtml: String
        get() {
            val upperContext = context.uppercase(Locale.getDefault())
            val formattedFormat = CompatUtil.capitalizeWords(format)
            val upperSeason = season?.uppercase(Locale.getDefault())
            return if (year == 0) {
                if (upperSeason != null) {
                    String.format(
                        Locale.getDefault(),
                        "<b>#%d %s <small>%s<small/></b> <small>(%s)</small>",
                        rank,
                        upperContext,
                        upperSeason,
                        formattedFormat,
                    )
                } else {
                    String.format(
                        Locale.getDefault(),
                        "<b>#%d %s</b> <small>(%s)</small>",
                        rank,
                        upperContext,
                        formattedFormat,
                    )
                }
            } else {
                if (upperSeason != null) {
                    String.format(
                        Locale.getDefault(),
                        "<b>#%d %s <small>%s %d</small></b> <small>(%s)</small>",
                        rank,
                        upperContext,
                        upperSeason,
                        year,
                        formattedFormat,
                    )
                } else {
                    String.format(
                        Locale.getDefault(),
                        "<b>#%d %s <small>%d</small></b> <small>(%s)</small>",
                        rank,
                        upperContext,
                        year,
                        formattedFormat,
                    )
                }
            }
        }

    val typeHtmlPlainTitle: String
        get() {
            val upperContext = context.uppercase(Locale.getDefault())
            val formattedFormat = CompatUtil.capitalizeWords(format)
            val upperSeason = season?.uppercase(Locale.getDefault())
            return if (year == 0) {
                if (upperSeason != null) {
                    String.format(
                        Locale.getDefault(),
                        "%s <small>%s<small/> <small>(%s)</small>",
                        upperContext,
                        upperSeason,
                        formattedFormat,
                    )
                } else {
                    String.format(
                        Locale.getDefault(),
                        "%s <small>(%s)</small>",
                        upperContext,
                        formattedFormat,
                    )
                }
            } else {
                if (upperSeason != null) {
                    String.format(
                        Locale.getDefault(),
                        "%s <small>%s %d</small> <small>(%s)</small>",
                        upperContext,
                        upperSeason,
                        year,
                        formattedFormat,
                    )
                } else {
                    String.format(
                        Locale.getDefault(),
                        "%s <small>%d</small> <small>(%s)</small>",
                        upperContext,
                        year,
                        formattedFormat,
                    )
                }
            }
        }
}
