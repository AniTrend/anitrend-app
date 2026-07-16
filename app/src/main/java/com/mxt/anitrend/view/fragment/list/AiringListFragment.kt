package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import com.mxt.anitrend.adapter.recycler.index.MediaListAdapter
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.media.MediaListUtil

/**
 * Created by max on 2017/11/03.
 */
class AiringListFragment : MediaListFragment() {
    companion object {
        @JvmStatic
        fun newInstance(): AiringListFragment = AiringListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.database.currentUser?.let { userBase ->
            userId = userBase.id
            userName = userBase.name
        }
        mediaType = KeyUtil.ANIME
        (mAdapter as? MediaListAdapter)?.setCurrentUser(userName)
        statusIn = KeyUtil.CURRENT
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun onChanged(value: PageContainer<MediaListCollection>?) {
        if (value != null) {
            if (value.hasPageInfo()) {
                presenter.setPageInfo(value.pageInfo)
            }
            if (!value.isEmpty) {
                val mediaListCollection = value.pageData.firstOrNull()
                if (mediaListCollection != null) {
                    val mediaList =
                        mediaListCollection.entries
                            .orEmpty()
                            .filter { entry ->
                                CompatUtil.equals(entry.media.status, KeyUtil.RELEASING)
                            }

                    val mediaListSort = presenter.settings.mediaListSort ?: KeyUtil.PROGRESS
                    if (MediaListUtil.isTitleSort(mediaListSort)) {
                        sortMediaListByTitle(mediaList)
                    } else {
                        onPostProcessed(mediaList)
                    }
                    mediaListCollectionBase = mediaListCollection
                } else {
                    onPostProcessed(emptyList())
                }
            } else {
                onPostProcessed(emptyList())
            }
        } else {
            onPostProcessed(emptyList())
        }

        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }
}
