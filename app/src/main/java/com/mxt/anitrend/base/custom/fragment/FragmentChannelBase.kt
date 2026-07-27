package com.mxt.anitrend.base.custom.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.parcelableArrayList
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.crunchy.Channel
import com.mxt.anitrend.model.entity.crunchy.Episode
import com.mxt.anitrend.model.entity.crunchy.Rss
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.collection.EpisodeUtil
import com.mxt.anitrend.widget.ProgressLayout
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Created by max on 2017/11/04.
 */
abstract class FragmentChannelBase :
    FragmentBase<Channel, Rss>(),
    RecyclerLoadListener,
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
    protected lateinit var swipeRefreshLayout: CustomSwipeRefreshLayout
    protected lateinit var recyclerView: StatefulRecyclerView
    protected lateinit var stateLayout: ProgressLayout

    protected val presenter by inject<BasePresenter>()

    private var binding: FragmentListBinding? = null

    protected var query: String? = null
    protected var isLimit: Boolean = false
    protected var isPopular: Boolean = false
    protected var targetLink: String? = null
    protected var copyright: String? = null

    protected var externalLinks: List<ExternalLink>? = null
    protected var mAdapter: RecyclerViewAdapter<Episode>? = null
    private lateinit var mLayoutManager: StaggeredGridLayoutManager

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            isPopular = args.getBoolean(KeyUtil.arg_popular)
            externalLinks = args.parcelableArrayList(KeyUtil.arg_list_model)
            externalLinks?.let { links ->
                targetLink = EpisodeUtil.episodeSupport(links)
            }
        }
        mColumnSize = R.integer.single_list_x1
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
        if (mAdapter?.itemCount ?: 0 < 1) {
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
            swipeRefreshLayout.setLoading(false)
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

    protected fun injectAdapter() {
        val adapter = mAdapter
        if (adapter != null && adapter.itemCount > 0) {
            if (recyclerView.adapter == null) {
                recyclerView.adapter = adapter
            } else {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false)
                } else if (swipeRefreshLayout.isLoading()) {
                    swipeRefreshLayout.setLoading(false)
                }
                if (!query.isNullOrEmpty()) {
                    adapter.filter?.filter(query)
                }
            }
            showContent()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    override fun onChanged(value: Rss?) {
        try {
            val channel = value?.channel
            val episodes = channel?.episode
            if (episodes != null) {
                copyright = channel.copyright
                mAdapter?.onItemsInserted(episodes)
                updateUI()
            } else {
                showEmpty(getString(R.string.layout_empty_response))
            }
        } catch (exception: Exception) {
            Timber.tag("onChanged(Rss content)").e(exception)
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    protected val clickListener =
        object : ItemClickListener<Episode> {
            override fun onItemClick(
                target: View,
                data: IndexedValue<Episode>,
            ) {
                if (target.id == R.id.series_image) {
                    val host = activity ?: return
                    val episode = data.value
                    DialogUtil.createMessage(
                        host,
                        episode.title,
                        episode.description + "<br/><br/>" + (copyright ?: ""),
                        R.string.Watch,
                        R.string.Dismiss,
                        R.string.action_search,
                    ) { _, _ ->
                        if (episode.link != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(episode.link))
                            startActivity(intent)
                        } else {
                            NotifyUtil.makeText(host, R.string.text_premium_show, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onItemLongClick(
                target: View,
                data: IndexedValue<Episode>,
            ) = Unit
        }
}
