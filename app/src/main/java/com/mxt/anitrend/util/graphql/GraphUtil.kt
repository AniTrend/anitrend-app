package com.mxt.anitrend.util.graphql

import com.annimon.stream.Stream
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.Settings

/**
 * Created by max on 2018/03/22.
 * Graph request helper class
 */
object GraphUtil {
    /**
     * Used to check if the newly applied preference key is a should trigger an application refresh
     */
    fun isKeyFilter(preferenceKey: String): Boolean = !CompatUtil.equals(preferenceKey, Settings._appTheme) &&
        !CompatUtil.equals(preferenceKey, Settings._updateChannel)

    /**
     * Remove empty json object responses, to resolve undefined content errors
     */
    fun filterFeedList(
        presenter: BasePresenter,
        feedLists: List<FeedList>,
    ): List<FeedList> {
        val filteredList =
            Stream
                .of(feedLists)
                .filter { f -> !f?.type.isNullOrBlank() }
                .toList()
        presenter.getPageInfo()?.perPage = filteredList.size
        return filteredList
    }

    /**
     * Remove empty json object responses, to resolve undefined content errors
     */
    @Deprecated("Will be deprecated once AL sorts out their notification issues")
    fun filterNotificationList(
        presenter: BasePresenter,
        notifications: List<Notification>,
    ): List<Notification> {
        val filteredList =
            Stream
                .of(notifications)
                .filter { f -> !f?.type.isNullOrBlank() }
                .toList()
        if (presenter.getPageInfo() != null) {
            presenter.getPageInfo()?.perPage = filteredList.size
        }
        return filteredList
    }
}
