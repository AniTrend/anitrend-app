package com.mxt.anitrend.service

import android.app.IntentService
import android.content.Intent
import android.util.Log

import com.annimon.stream.Stream
import com.google.firebase.analytics.FirebaseAnalytics
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.ErrorUtil
import com.mxt.anitrend.util.GraphUtil
import com.mxt.anitrend.util.KeyUtil

import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2017/10/24.
 * Genre & Tags Service
 */

class TagGenreService : IntentService(ServiceName) {

    private fun fetchAllMediaTags() {
        val widgetPresenter = WidgetPresenter<List<MediaTag>>(applicationContext)
        if (widgetPresenter.database.getBoxStore(MediaTag::class.java).count() < 1) {
            widgetPresenter.params.putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(false))
            widgetPresenter.requestData(KeyUtil.MEDIA_TAG_REQ, applicationContext, object : RetroCallback<List<MediaTag>> {
                override fun onResponse(call: Call<List<MediaTag>>, response: Response<List<MediaTag>>) {
                    val responseBody: List<MediaTag>? = response.body()
                    if (response.isSuccessful && responseBody != null)
                        if (!CompatUtil.isEmpty(responseBody))
                            widgetPresenter.database.mediaTags = responseBody
                        else
                            Timber.tag(ServiceName).e(ErrorUtil.getError(response))
                }

                override fun onFailure(call: Call<List<MediaTag>>, throwable: Throwable) {
                    Timber.tag("fetchAllMediaTags").e(throwable)
                    throwable.printStackTrace()
                }
            })
        }
    }

    private fun fetchAllMediaGenres() {
        val widgetPresenter = WidgetPresenter<List<String>>(applicationContext)
        if (widgetPresenter.database.getBoxStore(Genre::class.java).count() < 1) {
            widgetPresenter.params.putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(false))
            widgetPresenter.requestData(KeyUtil.GENRE_COLLECTION_REQ, applicationContext, object : RetroCallback<List<String>> {
                override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                    val responseBody: List<String>? = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        if (!CompatUtil.isEmpty(responseBody)) {
                            val genreList = Stream.of(responseBody)
                                    .map { Genre(it) }
                                    .toList()
                            widgetPresenter.database.genreCollection = genreList
                        }
                    } else
                        Timber.tag(ServiceName).e(ErrorUtil.getError(response))
                }

                override fun onFailure(call: Call<List<String>>, throwable: Throwable) {
                    Timber.tag("fetchAllMediaGenres").e(throwable)
                    throwable.printStackTrace()
                }
            })
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        fetchAllMediaGenres()
        fetchAllMediaTags()
    }

    companion object {
        private const val ServiceName = "TagGenreService"
    }
}
