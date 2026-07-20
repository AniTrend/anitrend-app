package com.mxt.anitrend.base.custom.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.presenter.CommonPresenter
import com.mxt.anitrend.base.custom.recycler.RecyclerScrollListener
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.widget.ProgressLayout
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by max on 2017/09/12.
 * Abstract fragment list base class
 */
abstract class FragmentBaseList<M, C, P : CommonPresenter> :
    FragmentBase<M, P, C>(),
    RecyclerLoadListener,
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
    protected lateinit var swipeRefreshLayout: CustomSwipeRefreshLayout
    protected lateinit var recyclerView: StatefulRecyclerView
    protected lateinit var stateLayout: ProgressLayout

    private var binding: FragmentListBinding? = null

    protected var query: String? = null

    protected var isLimit: Boolean = false

    protected lateinit var mAdapter: RecyclerViewAdapter<M>
    protected lateinit var mLayoutManager: StaggeredGridLayoutManager

    /**
     * Standalone pagination collaborator for scroll detection and page tracking.
     * Replaces the presenter-as-scroll-listener coupling.
     * Initialized at declaration to survive view recreation across back-stack transitions.
     */
    protected val mScrollListener: RecyclerScrollListener = RecyclerScrollListener()

    private val stateLayoutOnClick =
        View.OnClickListener {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false)
            }
            if (snackbar?.isShown == true) {
                snackbar?.dismiss()
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
        binding = FragmentListBinding.inflate(inflater, container, false)
        val root = requireNotNull(binding).root
        swipeRefreshLayout = requireNotNull(binding).refreshLayout
        recyclerView = requireNotNull(binding).recyclerView
        stateLayout = requireNotNull(binding).stateLayout
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = true
        mLayoutManager =
            StaggeredGridLayoutManager(
                resources.getInteger(mColumnSize),
                StaggeredGridLayoutManager.VERTICAL,
            )
        recyclerView.layoutManager = mLayoutManager
        swipeRefreshLayout.setOnRefreshAndLoadListener(this)
        activity?.let { CompatUtil.configureSwipeRefreshLayout(swipeRefreshLayout, it) }
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
        outState.putInt(KeyUtil.arg_page, mScrollListener.currentPage)
        outState.putInt(KeyUtil.arg_page_offset, mScrollListener.currentOffset)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { state ->
            isPager = state.getBoolean(KeyUtil.key_pagination)
            mColumnSize = state.getInt(KeyUtil.key_columns)
            mScrollListener.currentPage = state.getInt(KeyUtil.arg_page)
            mScrollListener.currentOffset = state.getInt(KeyUtil.arg_page_offset)
            // Compatibility shim: sync restored pagination state to presenter
            // so concrete subclasses reading presenter.currentPage still work
            presenter.currentPage = mScrollListener.currentPage
            presenter.currentOffset = mScrollListener.currentOffset
        }
    }

    protected fun addScrollLoadTrigger() {
        if (isPager) {
            if (!recyclerView.hasOnScrollListener()) {
                mScrollListener.initListener(mLayoutManager, this)
                recyclerView.addOnScrollListener(mScrollListener)
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
        if (mScrollListener.currentPage > 1 && isPager) {
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
        if (mScrollListener.currentPage > 1 && isPager) {
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

    /**
     * Migration seam: sets page info on both the standalone scroll listener and the
     * presenter. Prefer this over calling [CommonPresenter.setPageInfo] directly when
     * setting page info outside [onPostProcessed] (e.g. in [onChanged] overrides that
     * bypass the standard data pipeline). The central sync in [onPostProcessed] already
     * covers the standard path.
     *
     * @param pageInfo The page info from the API response, or null to reset.
     */
    protected fun setPageInfo(pageInfo: PageInfo?) {
        mScrollListener.setPageInfo(pageInfo)
        // Keep presenter in sync for backward compat with concrete subclasses
        presenter.setPageInfo(pageInfo)
    }

    fun showLoading() {
        stateLayout.showLoading()
    }

    fun setLimitReached() {
        if (mScrollListener.currentPage != 0) {
            swipeRefreshLayout.setLoading(false)
            isLimit = true
        }
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?,
    ) {
        if (key != null && isFilterableEnabled && GraphUtil.isKeyFilter(key)) {
            showLoading()
            mAdapter.clearDataSet()
            onRefresh()
        }
    }

    override fun onRefresh() {
        isLimit = false
        mScrollListener.onRefreshPage()
        // Compatibility shim: keep presenter pagination in sync for concrete subclasses
        // that still read presenter.currentPage in makeRequest()
        presenter.onRefreshPage()
        makeRequest()
    }

    override fun onLoad() = Unit

    override fun onLoadMore() {
        // Compatibility shim: sync pagination state from standalone scroll listener
        // to presenter so concrete subclasses reading presenter.currentPage in makeRequest()
        // get the correct page number after a scroll-triggered page advance.
        presenter.currentPage = mScrollListener.currentPage
        presenter.currentOffset = mScrollListener.currentOffset
        swipeRefreshLayout.setLoading(true)
        makeRequest()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSearch(query: String) {
        if (!isPager && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            val filter = mAdapter.filter
            if (filter != null && !CompatUtil.equals(this.query, query)) {
                this.query = query
                filter.filter(query)
            }
        }
    }

    protected fun setSwipeRefreshLayoutEnabled(state: Boolean) {
        swipeRefreshLayout.setPermitRefresh(state)
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

    protected fun onPostProcessed(content: List<M>?) {
        // Centrally sync pageInfo from presenter to standalone scroll listener.
        // Concrete subclasses call presenter.setPageInfo(...) in onChanged() before
        // reaching this method. This ensures mScrollListener knows when the last
        // page is reached for correct scroll-stop behaviour.
        mScrollListener.setPageInfo(presenter.getPageInfo())
        if (!CompatUtil.isEmpty(content)) {
            val items = content ?: emptyList()
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

    abstract override fun onChanged(value: C?)

    abstract override fun onItemClick(
        target: View,
        data: IndexedValue<M>,
    )

    abstract override fun onItemLongClick(
        target: View,
        data: IndexedValue<M>,
    )
}
