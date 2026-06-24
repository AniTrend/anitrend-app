package com.mxt.anitrend.base.custom.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.annimon.stream.IntPair
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.custom.view.editor.ComposerWidget
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.databinding.FragmentCommentBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.widget.ProgressLayout
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by max on 2017/12/02.
 * Comment fragment base class style
 */
abstract class FragmentBaseComment :
    FragmentBase<FeedReply, WidgetPresenter<FeedList>, FeedList>(),
    RecyclerLoadListener,
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
    protected lateinit var swipeRefreshLayout: CustomSwipeRefreshLayout
    protected lateinit var recyclerView: StatefulRecyclerView
    protected lateinit var originRecycler: StatefulRecyclerView
    protected lateinit var stateLayout: ProgressLayout
    protected lateinit var composerWidget: ComposerWidget

    private var binding: FragmentCommentBinding? = null

    protected var userActivityId: Long = 0
    protected var feedList: FeedList? = null

    protected var query: String? = null
    protected var isLimit: Boolean = false

    protected lateinit var mAdapter: RecyclerViewAdapter<FeedReply>
    private lateinit var mLayoutManager: StaggeredGridLayoutManager

    private val stateLayoutOnClick =
        View.OnClickListener {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false)
            }
            showLoading()
            onRefresh()
        }

    private val snackBarOnClick =
        View.OnClickListener {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false)
            }
            if (snackbar?.isShown == true) {
                snackbar?.dismiss()
            }
            swipeRefreshLayout.setLoading(true)
            makeRequest()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCommentBinding.inflate(inflater, container, false)
        val root = requireNotNull(binding).root
        swipeRefreshLayout = requireNotNull(binding).refreshLayout
        recyclerView = requireNotNull(binding).recyclerView
        originRecycler = requireNotNull(binding).commentOrigin
        stateLayout = requireNotNull(binding).stateLayout
        composerWidget = requireNotNull(binding).composerWidget
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false
        mLayoutManager =
            StaggeredGridLayoutManager(
                resources.getInteger(mColumnSize),
                StaggeredGridLayoutManager.VERTICAL,
            )
        recyclerView.layoutManager = mLayoutManager
        swipeRefreshLayout.setOnRefreshAndLoadListener(this)
        swipeRefreshLayout.setPermitLoad(false)
        originRecycler.layoutManager = LinearLayoutManager(context)
        originRecycler.setHasFixedSize(true)
        originRecycler.isNestedScrollingEnabled = false
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onStart() {
        super.onStart()
        showLoading()
        if (mAdapter.itemCount < 1) {
            onRefresh()
        } else {
            updateUI()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KeyUtil.key_pagination, isPager)
        outState.putInt(KeyUtil.key_columns, mColumnSize)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { state ->
            isPager = state.getBoolean(KeyUtil.key_pagination)
            mColumnSize = state.getInt(KeyUtil.key_columns)
        }
    }

    protected fun addScrollLoadTrigger() {
        if (isPager) {
            if (!recyclerView.hasOnScrollListener()) {
                presenter.initListener(mLayoutManager, this)
                recyclerView.addOnScrollListener(presenter)
            }
        }
    }

    protected fun removeScrollLoadTrigger() {
        if (isPager) {
            recyclerView.clearOnScrollListeners()
        }
    }

    override fun onPause() {
        super.onPause()
        removeScrollLoadTrigger()
    }

    override fun onResume() {
        super.onResume()
        addScrollLoadTrigger()
    }

    override fun showError(error: String) {
        super.showError(error)
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false)
        }
        if (swipeRefreshLayout.isLoading()) {
            swipeRefreshLayout.setLoading(false)
        }
        if (presenter.currentPage > 1 && isPager) {
            if (stateLayout.isLoading) {
                stateLayout.showContent()
            }
            snackbar =
                NotifyUtil
                    .make(stateLayout, R.string.text_unable_to_load_next_page, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.try_again, snackBarOnClick)
            snackbar?.show()
        } else {
            val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_cry)
            stateLayout.showError(
                drawable,
                error,
                getString(R.string.try_again),
                stateLayoutOnClick,
            )
        }
    }

    override fun showEmpty(message: String) {
        super.showEmpty(message)
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false)
        }
        if (swipeRefreshLayout.isLoading()) {
            swipeRefreshLayout.setLoading(false)
        }
        if (presenter.currentPage > 1 && isPager) {
            if (stateLayout.isLoading) {
                stateLayout.showContent()
            }
            snackbar =
                NotifyUtil
                    .make(stateLayout, R.string.text_unable_to_load_next_page, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.try_again, snackBarOnClick)
            snackbar?.show()
        } else {
            val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_sweat)
            stateLayout.showError(
                drawable,
                message,
                getString(R.string.try_again),
                stateLayoutOnClick,
            )
        }
    }

    fun showContent() {
        stateLayout.showContent()
    }

    fun showLoading() {
        stateLayout.showLoading()
    }

    fun setLimitReached() {
        if (presenter.currentPage != 0) {
            swipeRefreshLayout.setRefreshing(false)
            isLimit = true
        }
    }

    override fun onRefresh() {
        isLimit = false
        presenter.onRefreshPage()
        makeRequest()
    }

    override fun onLoad() = Unit

    override fun onLoadMore() {
        swipeRefreshLayout.setLoading(true)
        makeRequest()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSearch(query: String) {
        val filter = mAdapter.filter
        if (filter != null && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            this.query = query
            filter.filter(query)
        }
    }

    protected fun injectAdapter() {
        if (mAdapter.itemCount > 0) {
            mAdapter.setClickListener(this)
            if (recyclerView.adapter == null) {
                actionMode?.let { mAdapter.setActionModeCallback(it) }
                recyclerView.adapter = mAdapter
            } else {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false)
                } else if (swipeRefreshLayout.isLoading()) {
                    swipeRefreshLayout.setLoading(false)
                }
                if (!TextUtils.isEmpty(query)) {
                    mAdapter.filter?.filter(query)
                }
            }
            showContent()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    override fun onChanged(content: FeedList?) {
        val replies = content?.replies
        if (!CompatUtil.isEmpty(replies)) {
            val items = replies ?: emptyList()
            if (isPager && !swipeRefreshLayout.isRefreshing()) {
                if (mAdapter.itemCount < 1) {
                    mAdapter.onItemsInserted(items)
                } else {
                    mAdapter.onItemRangeInserted(items)
                }
            } else {
                mAdapter.onItemsInserted(items)
            }
            updateUI()
        } else {
            if (isPager) {
                setLimitReached()
            }
            if (mAdapter.itemCount < 1) {
                showEmpty(getString(R.string.layout_empty_response))
            }
        }
    }

    abstract override fun onItemClick(
        target: View,
        data: IntPair<FeedReply>,
    )

    abstract override fun onItemLongClick(
        target: View,
        data: IntPair<FeedReply>,
    )
}
