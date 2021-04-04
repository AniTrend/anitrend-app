package com.mxt.anitrend.util.graphql

import com.annimon.stream.Stream
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import io.github.wax911.library.model.request.QueryContainerBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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

        val settings = koinOf<Settings>()
        if (!settings.displayAdultContent)
            queryContainer.putVariable(KeyUtil.arg_isAdult, false)

        queryContainer.putVariable(KeyUtil.arg_asHtml, false)

        return queryContainer
    }

    /**
     * Used to check if the newly applied preference key is a should trigger an application refresh
     */
    fun isKeyFilter(preferenceKey: String): Boolean {
        return !CompatUtil.equals(preferenceKey, Settings._appTheme) &&
                !CompatUtil.equals(preferenceKey, Settings._updateChannel)
    }

    /**
     * Remove empty json object responses, to resolve undefined content errors
     */
    fun filterFeedList(presenter: BasePresenter, feedLists: List<FeedList>): List<FeedList> {
        val filteredList = Stream.of(feedLists)
                .filter { f -> !f?.type.isNullOrBlank() }
                .toList()
        presenter.pageInfo?.perPage = filteredList.size
        return filteredList
    }

    /**
     * Remove empty json object responses, to resolve undefined content errors
     */
    @Deprecated("Will be deprecated once AL sorts out their notification issues")
    fun filterNotificationList(presenter: BasePresenter, notifications: List<Notification>): List<Notification> {
        val filteredList = Stream.of(notifications)
                .filter { f -> !f?.type.isNullOrBlank() }
                .toList()
        if (presenter.pageInfo != null)
            presenter.pageInfo?.perPage = filteredList.size
        return filteredList
    }
}
