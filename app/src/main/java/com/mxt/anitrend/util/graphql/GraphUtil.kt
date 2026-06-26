package com.mxt.anitrend.util.graphql

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
            feedLists
                .filter { f -> !f?.type.isNullOrBlank() }
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
            notifications
                .filter { f -> !f?.type.isNullOrBlank() }
        if (presenter.getPageInfo() != null) {
            presenter.getPageInfo()?.perPage = filteredList.size
        }
        return filteredList
    }
}
