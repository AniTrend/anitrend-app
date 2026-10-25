package com.mxt.anitrend.base.custom.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.ui.paging.ManualPageState
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.widget.ProgressLayout

/**
 * Created by max on 2017/09/12.
 * Abstract fragment list base class
 */
abstract class FragmentBaseList<M, C> :
    FragmentBase<M, C>(),
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    protected var isLimit: Boolean = false
    protected lateinit var swipeRefreshLayout: CustomSwipeRefreshLayout
    protected lateinit var stateLayout: ProgressLayout

    /** Page bookkeeping only; Compose hosts own viewport detection and request gating. */
    protected val pageState = ManualPageState()

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
        val binding = FragmentListBinding.inflate(inflater, container, false)
        swipeRefreshLayout = binding.refreshLayout
        stateLayout = binding.stateLayout
        swipeRefreshLayout.setOnRefreshAndLoadListener(this)
        activity?.let { CompatUtil.configureSwipeRefreshLayout(swipeRefreshLayout, it) }
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KeyUtil.key_pagination, isPager)
        outState.putInt(KeyUtil.arg_page, pageState.currentPage)
        outState.putInt(KeyUtil.arg_page_offset, pageState.currentOffset)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { state ->
            isPager = state.getBoolean(KeyUtil.key_pagination)
            pageState.currentPage = state.getInt(KeyUtil.arg_page)
            pageState.currentOffset = state.getInt(KeyUtil.arg_page_offset)
        }
    }

    override fun showError(error: String) {
        super.showError(error)
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false)
        }
        if (swipeRefreshLayout.isLoading()) {
            swipeRefreshLayout.setLoading(false)
        }
        if (pageState.currentPage > 1 && isPager) {
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
        if (pageState.currentPage > 1 && isPager) {
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
        if (pageState.currentPage != 0) {
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
            onRefresh()
        }
    }

    override fun onRefresh() {
        isLimit = false
        pageState.onRefreshPage()
        makeRequest()
    }

    override fun onLoad() = Unit

    protected fun setSwipeRefreshLayoutEnabled(state: Boolean) {
        swipeRefreshLayout.setPermitRefresh(state)
    }

    abstract override fun onChanged(value: C?)
}
