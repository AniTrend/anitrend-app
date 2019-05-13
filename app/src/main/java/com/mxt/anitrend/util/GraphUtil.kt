package com.mxt.anitrend.util

import android.text.TextUtils

import com.annimon.stream.Stream
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder
import com.mxt.anitrend.presenter.base.BasePresenter

/**
 * Created by max on 2018/03/22.
 * Graph request helper class
 */

object GraphUtil {

    /**
     * Builder provider helper method, that provides a default GraphQL Query and Variable Builder
     * @param includePaging if one of the variables should container a page limit
     */
    fun getDefaultQuery(includePaging: Boolean): QueryContainerBuilder {
        val queryContainer = QueryContainerBuilder()
        if (includePaging)
            queryContainer.putVariable(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
        return queryContainer
    }

    /**
     * Used to check if the newly applied preference key is a should trigger an application refresh
     */
    fun isKeyFilter(preferenceKey: String): Boolean {
        return !CompatUtil.equals(preferenceKey, ApplicationPref._isLightTheme) && !CompatUtil.equals(preferenceKey, ApplicationPref._updateChannel)
    }

    /**
     * Remove empty json object responses, to resolve undefined content errors
     */
    fun filterFeedList(presenter: BasePresenter, feedLists: List<FeedList>): List<FeedList> {
        val filteredList = Stream.of(feedLists)
                .filter { f -> f != null && !TextUtils.isEmpty(f.type) }
                .toList()
        if (presenter.pageInfo != null)
            presenter.pageInfo!!.perPage = filteredList.size
        return filteredList
    }

    /**
     * Remove empty json object responses, to resolve undefined content errors
     */
    fun filterNotificationList(presenter: BasePresenter, notifications: List<Notification>): List<Notification> {
        val filteredList = Stream.of(notifications)
                .filter { f -> f != null && f.activityId != 0L }
                .toList()
        if (presenter.pageInfo != null)
            presenter.pageInfo!!.perPage = filteredList.size
        return filteredList
    }
}
