package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetProfileStatsBinding
import com.mxt.anitrend.extension.getCompatColor
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.apiError
import com.mxt.anitrend.view.activity.detail.MediaListActivity
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber
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
    View.OnClickListener,
    RetroCallback<ConnectionContainer<UserStatisticTypes>> {
    private lateinit var binding: WidgetProfileStatsBinding
    private lateinit var presenter: WidgetPresenter<ConnectionContainer<UserStatisticTypes>>

    private var model: UserStatisticTypes? = null

    private var bundle: Bundle? = null
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
        presenter = WidgetPresenter(context)
        binding = WidgetProfileStatsBinding.inflate(LayoutInflater.from(context), this, true)
        // loading place holder data
        binding.userAnimeTime.text = placeHolder
        binding.userMangaChaps.text = placeHolder
        binding.userAnimeTotal.text = placeHolder
        binding.userMangaTotal.text = placeHolder

        val textColor = context.getCompatColor(R.color.white)
        binding.userAnimeTime.setTextColor(textColor)
        binding.userMangaChaps.setTextColor(textColor)
        binding.userAnimeTotal.setTextColor(textColor)
        binding.userMangaTotal.setTextColor(textColor)

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

    fun setParams(bundle: Bundle) {
        this.bundle = bundle
        presenter.params.apply {
            if (bundle.containsKey(KeyUtil.arg_id)) {
                putLong(KeyUtil.arg_id, bundle.getLong(KeyUtil.arg_id))
                remove(KeyUtil.arg_userName)
            } else {
                putString(KeyUtil.arg_userName, bundle.getString(KeyUtil.arg_userName))
                remove(KeyUtil.arg_id)
            }
        }
        presenter.requestData(KeyUtil.USER_STATS_REQ, context, this)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        presenter.onDestroy()
        model = null
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
                val intent =
                    Intent(context, MediaListActivity::class.java).apply {
                        putExtras(bundle ?: Bundle())
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    }
                context.startActivity(intent)
            }
            R.id.user_manga_total_container -> {
                val intent =
                    Intent(context, MediaListActivity::class.java).apply {
                        putExtras(bundle ?: Bundle())
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                    }
                context.startActivity(intent)
            }
        }
    }

    override fun onResponse(
        call: Call<ConnectionContainer<UserStatisticTypes>>,
        response: Response<ConnectionContainer<UserStatisticTypes>>,
    ) {
        try {
            val connectionContainer = response.body()
            if (response.isSuccessful && connectionContainer != null) {
                if (!connectionContainer.isEmpty) {
                    model = connectionContainer.connection
                    updateUI()
                }
            } else {
                Timber.tag(tagName).w(response.apiError())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onFailure(
        call: Call<ConnectionContainer<UserStatisticTypes>>,
        throwable: Throwable,
    ) {
        try {
            Timber.tag(tagName).w(throwable)
        } catch (e: Exception) {
            Timber.tag(tagName).e(e)
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
