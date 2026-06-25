package com.mxt.anitrend.base.custom.sheet

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.custom.viewmodel.ViewModelBase
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.base.interfaces.event.ResponseCallback
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.model.entity.giphy.GiphyContainer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.widget.ProgressLayout

/**
 * Created by max on 2017/12/09.
 * giphy loading list bottom sheet
 */
abstract class BottomSheetGiphyList :
    BottomSheetBase<GiphyContainer>(),
    ItemClickListener<Giphy>,
    androidx.lifecycle.Observer<GiphyContainer?>,
    ResponseCallback,
    RecyclerLoadListener,
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    protected var container: GiphyContainer? = null

    protected var stateLayout: ProgressLayout? = null
    protected var recyclerView: StatefulRecyclerView? = null

    protected lateinit var mAdapter: RecyclerViewAdapter<Giphy>
    protected lateinit var mLayoutManager: StaggeredGridLayoutManager

    protected var mColumnSize: Int = 0
    protected var isPager: Boolean = false
    protected var isLimit: Boolean = false

    private val stateLayoutOnClick =
        View.OnClickListener {
            stateLayout?.showLoading()
            onRefresh()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(true)
    }

    override fun onStart() {
        super.onStart()
        stateLayout?.showLoading()
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
        outState.putInt(KeyUtil.arg_page, presenter.currentPage)
        outState.putInt(KeyUtil.arg_page_offset, presenter.currentOffset)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { state ->
            isPager = state.getBoolean(KeyUtil.key_pagination)
            mColumnSize = state.getInt(KeyUtil.key_columns)
            presenter.currentPage = state.getInt(KeyUtil.arg_page)
            presenter.currentOffset = state.getInt(KeyUtil.arg_page_offset)
        }
    }

    protected fun addScrollLoadTrigger() {
        if (isPager) {
            val recycler = recyclerView ?: return
            if (!recycler.hasOnScrollListener()) {
                presenter.initListener(mLayoutManager, this)
                recycler.addOnScrollListener(presenter)
            }
        }
    }

    protected fun removeScrollLoadTrigger() {
        if (isPager) {
            recyclerView?.clearOnScrollListeners()
        }
    }

    protected fun bindListViews(rootView: View) {
        stateLayout = rootView.findViewById(R.id.stateLayout)
        recyclerView = rootView.findViewById(R.id.recyclerView)
    }

    override fun onPause() {
        removeScrollLoadTrigger()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        addScrollLoadTrigger()
    }

    protected fun injectAdapter() {
        mAdapter.setClickListener(this)
        val recycler = recyclerView ?: return
        if (recycler.adapter == null) {
            recycler.setHasFixedSize(true)
            recycler.isNestedScrollingEnabled = true
            recycler.layoutManager = mLayoutManager
            recycler.adapter = mAdapter
        }
        if (mAdapter.itemCount < 1) {
            val drawable =
                context?.getCompatDrawable(
                    R.drawable.ic_new_releases_white_24dp,
                    R.color.colorStateBlue,
                ) ?: return
            stateLayout?.showEmpty(drawable, getString(R.string.layout_empty_response))
        } else {
            stateLayout?.showContent()
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun setViewModel(stateSupported: Boolean) {
        if (viewModel == null) {
            viewModel = ViewModelProvider(this).get(ViewModelBase::class.java) as ViewModelBase<GiphyContainer>
            viewModel?.setContext(requireContext())
            if (viewModel?.model?.hasActiveObservers() == false) {
                viewModel?.model?.observe(this, this)
            }
            if (stateSupported) {
                viewModel?.state = this
            }
        }
    }

    fun setLimitReached() {
        if (presenter.currentOffset != 0) {
            isLimit = true
        }
    }

    override fun onRefresh() {
        mAdapter.clearDataSet()
        if (isPager) {
            presenter.onRefreshPage()
        }
        makeRequest()
    }

    override fun onLoad() = Unit

    override fun onLoadMore() {
        makeRequest()
    }

    protected abstract fun updateUI()

    abstract fun makeRequest()

    override fun onChanged(content: GiphyContainer?) {
        if (content != null && !content.data.isNullOrEmpty()) {
            if (isPager) {
                if (mAdapter.itemCount < 1) {
                    mAdapter.onItemsInserted(content.data)
                } else {
                    mAdapter.onItemRangeInserted(content.data)
                }
            } else {
                mAdapter.onItemsInserted(content.data)
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

    override fun showError(error: String) {
        super.showError(error)
        val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_cry) ?: return
        stateLayout?.showError(drawable, error, getString(R.string.try_again), stateLayoutOnClick)
    }

    override fun showEmpty(message: String) {
        super.showEmpty(message)
        val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_sweat) ?: return
        stateLayout?.showError(drawable, message, getString(R.string.try_again), stateLayoutOnClick)
    }
}
