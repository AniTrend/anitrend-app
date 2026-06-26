package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.UserAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.custom.viewmodel.acquireTypedViewModelBase
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.databinding.BottomSheetListBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.widget.ProgressLayout

class BottomSheetListUsers :
    BottomSheetBase<PageContainer<UserBase>>(),
    ItemClickListener<UserBase>,
    androidx.lifecycle.Observer<PageContainer<UserBase>?>,
    RecyclerLoadListener,
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private var stateLayout: ProgressLayout? = null
    private var recyclerView: StatefulRecyclerView? = null

    private lateinit var mAdapter: RecyclerViewAdapter<UserBase>
    private lateinit var mLayoutManager: StaggeredGridLayoutManager

    private var mColumnSize: Int = 0
    private var isPager: Boolean = false
    private var isLimit: Boolean = false

    private var count: Int = 0
    private var userId: Long = 0

    @KeyUtil.RequestType
    private var requestType: Int = 0

    private val stateLayoutOnClick =
        View.OnClickListener {
            stateLayout?.showLoading()
            onRefresh()
        }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetListUsers = BottomSheetListUsers().apply {
            arguments = bundle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            count = args.getInt(KeyUtil.arg_model)
            userId = args.getLong(KeyUtil.arg_userId)
            requestType = args.getInt(KeyUtil.arg_request_type)
        }
        mAdapter = UserAdapter(ctx)
        setViewModel(true)
        isPager = true
        presenter = BasePresenter(ctx)
        mColumnSize = resources.getInteger(R.integer.single_list_x1)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val binding = BottomSheetListBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        bindToolbarViews(binding.root)
        stateLayout = binding.stateLayout
        recyclerView = binding.recyclerView
        createBottomSheetBehavior(binding.root)
        mLayoutManager = StaggeredGridLayoutManager(mColumnSize, StaggeredGridLayoutManager.VERTICAL)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        toolbarTitle?.text = getString(mTitle, count)
        searchView?.visibility = View.GONE
        stateLayout?.showLoading()
        if (mAdapter.itemCount < 1) {
            onRefresh()
        } else {
            updateUI()
        }
    }

    private fun addScrollLoadTrigger() {
        if (isPager) {
            val recycler = recyclerView ?: return
            if (!recycler.hasOnScrollListener()) {
                presenter.initListener(mLayoutManager, this)
                recycler.addOnScrollListener(presenter)
            }
        }
    }

    private fun removeScrollLoadTrigger() {
        if (isPager) {
            recyclerView?.clearOnScrollListeners()
        }
    }

    override fun onPause() {
        removeScrollLoadTrigger()
        super.onPause()
    }

    override fun onResume() {
        addScrollLoadTrigger()
        super.onResume()
    }

    private fun injectAdapter() {
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

    private fun updateUI() {
        injectAdapter()
    }

    private fun setViewModel(stateSupported: Boolean) {
        if (viewModel == null) {
            viewModel =
                acquireTypedViewModelBase(
                    observer = this,
                    stateSupported = stateSupported,
                    state = this,
                )
        }
    }

    private fun setLimitReached() {
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

    fun makeRequest() {
        val ctx = context ?: return
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_id, userId)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
        }
        viewModel?.requestData(requestType, ctx)
    }

    private fun onPostProcessed(content: List<UserBase>?) {
        val items = content ?: emptyList()
        if (!CompatUtil.isEmpty(items)) {
            if (isPager) {
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

    override fun onChanged(content: PageContainer<UserBase>?) {
        if (content != null) {
            if (content.hasPageInfo()) {
                presenter.setPageInfo(content.pageInfo)
            }
            if (!content.isEmpty) {
                onPostProcessed(content.pageData)
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

    override fun showError(error: String) {
        super.showError(error)
        val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_cry)
        stateLayout?.showError(drawable, error, getString(R.string.button_try_again), stateLayoutOnClick)
    }

    override fun showEmpty(message: String) {
        super.showEmpty(message)
        val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_sweat)
        stateLayout?.showError(drawable, message, getString(R.string.button_try_again), stateLayoutOnClick)
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<UserBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent =
                    Intent(host, ProfileActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(KeyUtil.arg_id, data.value.id)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<UserBase>,
    ) = Unit

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> = newInstance(bundle)

        fun setUserId(userId: Long): Builder {
            bundle.putLong(KeyUtil.arg_userId, userId)
            return this
        }

        fun setModelCount(count: Int): Builder {
            bundle.putInt(KeyUtil.arg_model, count)
            return this
        }

        fun setRequestType(
            @KeyUtil.RequestType requestType: Int,
        ): Builder {
            bundle.putInt(KeyUtil.arg_request_type, requestType)
            return this
        }
    }
}
