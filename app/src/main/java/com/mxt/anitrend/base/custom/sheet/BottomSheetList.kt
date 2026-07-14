package com.mxt.anitrend.base.custom.sheet

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.custom.viewmodel.acquireTypedViewModelBase
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.widget.ProgressLayout
import timber.log.Timber

abstract class BottomSheetList<T : android.os.Parcelable> :
    BottomSheetBase<List<T>>(),
    ItemClickListener<T>,
    androidx.lifecycle.Observer<List<T>?>,
    RecyclerLoadListener,
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    protected var stateLayout: ProgressLayout? = null
    protected var recyclerView: StatefulRecyclerView? = null

    protected lateinit var mAdapter: RecyclerViewAdapter<T>
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

    protected abstract fun updateUI()

    protected fun setViewModel(stateSupported: Boolean) {
        if (viewModel == null) {
            viewModel = acquireTypedViewModelBase(this, stateSupported, this)
        }
    }

    fun setLimitReached() {
        if (presenter.currentPage != 0) {
            isLimit = true
        }
    }

    override fun onRefresh() {
        if (isPager) {
            presenter.onRefreshPage()
        }
        makeRequest()
    }

    override fun onLoad() = Unit

    override fun onLoadMore() {
        makeRequest()
    }

    abstract fun makeRequest()

    override fun onChanged(data: List<T>?) {
        Timber.tag(TAG ?: javaClass.simpleName).d("onChanged(@Nullable List<T> data) invoked")
    }

    override fun showError(error: String) {
        super.showError(error)
        val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_cry)
        stateLayout?.showError(drawable, error, getString(R.string.try_again), stateLayoutOnClick)
    }

    override fun showEmpty(message: String) {
        super.showEmpty(message)
        val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_sweat)
        stateLayout?.showError(drawable, message, getString(R.string.try_again), stateLayoutOnClick)
    }
}
