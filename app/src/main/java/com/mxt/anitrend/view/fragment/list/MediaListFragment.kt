package com.mxt.anitrend.view.fragment.list

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaListAdapter
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.model.entity.base.MediaListCollectionBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.util.media.MediaUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.view.activity.detail.MediaActivity
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by max on 2017/12/18.
 * media list fragment
 */
open class MediaListFragment :
    FragmentBaseList<MediaList, PageContainer<MediaListCollection>, MediaPresenter>(),
    BaseConsumer.onRequestModelChange<MediaList> {
    protected var userId: Long = 0
    protected var userName: String? = null

    @KeyUtil.MediaType
    protected var mediaType: String? = null
    protected var mediaListOptions: MediaListOptions? = null
    protected var statusIn: String? = null

    protected var mediaListCollectionBase: MediaListCollectionBase? = null

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): MediaListFragment {
            val args = Bundle(params)
            return MediaListFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            userId = args.getLong(KeyUtil.arg_id)
            userName = args.getString(KeyUtil.arg_userName)
            statusIn = args.getString(KeyUtil.arg_statusIn)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }

        isFilterableEnabled = true
        isPager = false
        hasSubscriber = true
        val ctx = requireContext()
        mAdapter =
            MediaListAdapter(ctx).apply {
                setCurrentUser(userName)
            }
        setPresenter(MediaPresenter(ctx))
        setViewModel(true)

        mColumnSize =
            if (presenter.settings.mediaListStyle == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1) {
                R.integer.single_list_x1
            } else {
                R.integer.grid_list_x2
            }
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_genre).isVisible = false
        menu.findItem(R.id.action_tag).isVisible = false
        menu.findItem(R.id.action_type).isVisible = false
        menu.findItem(R.id.action_year).isVisible = false
        menu.findItem(R.id.action_status).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ctx = context ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_sort -> {
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_sort,
                    CompatUtil.getIndexOf(KeyUtil.MediaListSortType, presenter.settings.mediaListSort),
                    CompatUtil.capitalizeWords(KeyUtil.MediaListSortType),
                ) { dialog, _ ->
                    presenter.settings.mediaListSort = KeyUtil.MediaListSortType[dialog.selectedIndex]
                }
                return true
            }
            R.id.action_order -> {
                val sortOrders = arrayOf(KeyUtil.ASC, KeyUtil.DESC)
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_order,
                    CompatUtil.getIndexOf(sortOrders, presenter.settings.sortOrder),
                    CompatUtil.getStringList(ctx, R.array.order_by_types),
                ) { dialog, _ ->
                    presenter.settings.saveSortOrder(
                        sortOrders.getOrNull(dialog.selectedIndex) ?: presenter.settings.sortOrder,
                    )
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val user = presenter.database.currentUser
        if (user != null) {
            mediaListOptions = user.mediaListOptions
        }
        val mediaListSort = presenter.settings.mediaListSort ?: KeyUtil.PROGRESS
        val params = viewModel?.params ?: return
        params.apply {
            putString(KeyUtil.arg_mediaType, mediaType)
            putBoolean(KeyUtil.arg_forceSingleCompletedList, true)
            statusIn?.let {
                putString(KeyUtil.arg_statusIn, it)
            } ?: remove(KeyUtil.arg_statusIn)
            if (userId != 0L) {
                putLong(KeyUtil.arg_userId, userId)
                remove(KeyUtil.arg_userName)
            } else {
                putString(KeyUtil.arg_userName, userName)
                remove(KeyUtil.arg_userId)
            }
            mediaListOptions?.let { options ->
                putString(KeyUtil.arg_scoreFormat, options.scoreFormat)
            }
            putString(
                KeyUtil.arg_sort,
                if (!MediaListUtil.isTitleSort(mediaListSort)) {
                    mediaListSort + presenter.settings.sortOrder
                } else {
                    KeyUtil.MEDIA_ID + presenter.settings.sortOrder
                },
            )
        }
        context?.let { ctx ->
            viewModel?.requestData(KeyUtil.MEDIA_LIST_COLLECTION_REQ, ctx)
        }
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?,
    ) {
        if (key != null && isFilterableEnabled && GraphUtil.isKeyFilter(key)) {
            val mediaListSort = presenter.settings.mediaListSort ?: KeyUtil.PROGRESS
            if (CompatUtil.equals(key, Settings._mediaListSort) && MediaListUtil.isTitleSort(mediaListSort)) {
                swipeRefreshLayout.setRefreshing(true)
                sortMediaListByTitle(mAdapter.data)
            } else {
                super.onSharedPreferenceChanged(sharedPreferences, key)
            }
        }
    }

    @SuppressLint("SwitchIntDef")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    override fun onModelChanged(consumer: BaseConsumer<MediaList>) {
        if (consumer.requestMode == KeyUtil.MUT_SAVE_MEDIA_LIST || consumer.requestMode == KeyUtil.MUT_DELETE_MEDIA_LIST) {
            if (presenter.isCurrentUser(userId, userName)) {
                val changeModel = consumer.changeModel
                if (changeModel == null) {
                    onRefresh()
                    return
                }
                val pair = CompatUtil.findIndexOf(mAdapter.data, changeModel)
                if (pair != null) {
                    val pairIndex = pair.index
                    when (consumer.requestMode) {
                        KeyUtil.MUT_SAVE_MEDIA_LIST -> {
                            if (mediaListCollectionBase == null ||
                                CompatUtil.equals(
                                    mediaListCollectionBase?.status,
                                    changeModel.status.orEmpty(),
                                )
                            ) {
                                mAdapter.onItemChanged(changeModel, pairIndex)
                            } else {
                                mAdapter.onItemRemoved(pairIndex)
                            }
                        }
                        KeyUtil.MUT_DELETE_MEDIA_LIST -> {
                            mAdapter.onItemRemoved(pairIndex)
                        }
                    }
                } else if (mediaListCollectionBase == null ||
                    CompatUtil.equals(mediaListCollectionBase?.status, changeModel.status.orEmpty())
                ) {
                    onRefresh()
                }
            }
        }
    }

    override fun onChanged(content: PageContainer<MediaListCollection>?) {
        if (content != null) {
            if (content.hasPageInfo()) {
                presenter.setPageInfo(content.pageInfo)
            }
            if (!content.isEmpty) {
                val mediaListCollection = content.pageData.firstOrNull()
                if (mediaListCollection != null) {
                    val entries = mediaListCollection.entries.orEmpty()
                    val mediaListSort = presenter.settings.mediaListSort ?: KeyUtil.PROGRESS
                    if (MediaListUtil.isTitleSort(mediaListSort)) {
                        sortMediaListByTitle(entries)
                    } else {
                        onPostProcessed(entries)
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

    override fun onItemClick(
        target: View,
        data: IndexedValue<MediaList>,
    ) {
        when (target.id) {
            R.id.container,
            R.id.series_image,
            -> {
                val host = activity ?: return
                val mediaBase = data.value.media
                val intent =
                    Intent(host, MediaActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, data.value.mediaId)
                        putExtra(KeyUtil.arg_mediaType, mediaBase.type)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<MediaList>,
    ) {
        when (target.id) {
            R.id.container,
            R.id.series_image,
            -> {
                if (presenter.settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(data.value.mediaId)
                            .build(host)
                    mediaActionUtil.startSeriesAction()
                } else {
                    context?.let {
                        NotifyUtil
                            .makeText(
                                it,
                                R.string.info_login_req,
                                R.drawable.ic_group_add_grey_600_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
            }
        }
    }

    protected fun sortMediaListByTitle(mediaLists: List<MediaList>) {
        val sortOrder = presenter.settings.sortOrder
        val sorted =
            mediaLists.sortedWith { first, second ->
                val firstTitle = MediaUtil.getMediaTitle(first.media)
                val secondTitle = MediaUtil.getMediaTitle(second.media)
                if (CompatUtil.equals(sortOrder, KeyUtil.ASC)) {
                    firstTitle.compareTo(secondTitle)
                } else {
                    secondTitle.compareTo(firstTitle)
                }
            }
        mAdapter.onItemsInserted(sorted)
        updateUI()
    }
}
