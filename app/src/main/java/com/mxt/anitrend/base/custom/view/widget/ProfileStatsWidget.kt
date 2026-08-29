package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetProfileStatsBinding
import com.mxt.anitrend.domain.user.model.UserStatisticsRecord
import com.mxt.anitrend.util.KeyUtil
import java.util.Locale

/**
 * Created by max on 2017/11/26.
 * status widget
 */
class ProfileStatsWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener {

    private lateinit var binding: WidgetProfileStatsBinding

    var onMediaListRequested: ((String) -> Unit)? = null

    private var model: UserStatisticsRecord? = null

    private val tagName = ProfileStatsWidget::class.java.simpleName

    private val placeHolder = ".."

    init {
        onInit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : this(context, attrs, defStyleAttr)

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding = WidgetProfileStatsBinding.inflate(LayoutInflater.from(context), this, true)
        // loading place holder data
        binding.userAnimeTime.text = placeHolder
        binding.userMangaChaps.text = placeHolder
        binding.userAnimeTotal.text = placeHolder
        binding.userMangaTotal.text = placeHolder

        binding.userAnimeTotalContainer.setOnClickListener(this)
        binding.userAnimeTimeContainer.setOnClickListener(this)
        binding.userMangaTotalContainer.setOnClickListener(this)
        binding.userMangaChapsContainer.setOnClickListener(this)
    }

    private fun updateUI() {
        val stats = model ?: return
        binding.userAnimeTime.text = getAnimeTime(stats.anime.minutesWatched)
        binding.userMangaChaps.text = getMangaChaptersCount(stats.manga.chaptersRead)
        binding.userAnimeTotal.text = getCount(stats.anime.count)
        binding.userMangaTotal.text = getCount(stats.manga.count)
    }

    fun setStats(stats: UserStatisticsRecord?) {
        model = stats
        updateUI()
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        model = null
        onMediaListRequested = null
    }

    override fun onClick(view: View) {
        val stats = model
        when (view.id) {
            R.id.user_anime_time_container -> {
                if (stats != null) {
                    Snackbar
                        .make(
                            this,
                            context.getString(
                                R.string.text_user_anime_time,
                                getAnimeTime(stats.anime.minutesWatched),
                            ),
                            Snackbar.LENGTH_LONG,
                        ).show()
                }
            }
            R.id.user_manga_chaps_container -> {
                if (stats != null) {
                    Snackbar
                        .make(
                            this,
                            context.getString(
                                R.string.text_user_manga_chapters,
                                getMangaChaptersCount(stats.manga.chaptersRead),
                            ),
                            Snackbar.LENGTH_LONG,
                        ).show()
                }
            }
            R.id.user_anime_total_container -> {
                onMediaListRequested?.invoke(KeyUtil.ANIME)
            }
            R.id.user_manga_total_container -> {
                onMediaListRequested?.invoke(KeyUtil.MANGA)
            }
        }
    }

    fun getAnimeTime(animeTime: Int?): String {
        if (animeTime == null || animeTime < 1) {
            return placeHolder
        }
        var itemTime = animeTime / 60f
        if (itemTime > 60) {
            itemTime /= 24
            if (itemTime > 365) {
                return context.getString(R.string.anime_time_years, itemTime / 365)
            }
            return context.getString(R.string.anime_time_days, itemTime)
        }
        return context.getString(R.string.anime_time_hours, itemTime)
    }

    fun getMangaChaptersCount(mangaChap: Int?): String {
        if (mangaChap == null || mangaChap < 1) {
            return placeHolder
        }
        if (mangaChap > 1000) {
            return String.format(Locale.getDefault(), "%.1f K", mangaChap / 1000f)
        }
        return String.format(Locale.getDefault(), "%d", mangaChap)
    }

    fun getCount(totalCount: Int): String {
        if (totalCount >= 1000) {
            return String.format(Locale.getDefault(), "%.1f K", totalCount / 1000f)
        }
        return String.format(Locale.getDefault(), "%d", totalCount)
    }
}
