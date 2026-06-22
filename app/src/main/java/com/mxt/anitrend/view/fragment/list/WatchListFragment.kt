package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.EpisodeAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentChannelBase
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.EpisodeUtil
import com.mxt.anitrend.util.graphql.apiError
import com.mxt.anitrend.util.graphql.GraphUtil
import co.anitrend.retrofit.graphql.model.request.QueryContainerBuilder
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2017/11/03.
 * WatchListFragment for anime types
 */
class WatchListFragment : FragmentChannelBase(), RetroCallback<ConnectionContainer<List<ExternalLink>>> {

    private var mediaId: Long = 0
    @KeyUtil.MediaType
    private var mediaType: String? = null

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle, popular: Boolean): FragmentChannelBase {
            val args = Bundle(params).apply {
                putBoolean(KeyUtil.arg_popular, popular)
            }
            return WatchListFragment().apply {
                arguments = args
            }
        }

        @JvmStatic
        fun newInstance(externalLinks: List<ExternalLink>, popular: Boolean): FragmentChannelBase {
            val args = Bundle().apply {
                putParcelableArrayList(
                    KeyUtil.arg_list_model,
                    ArrayList<Parcelable>(externalLinks)
                )
                putBoolean(KeyUtil.arg_popular, popular)
            }
            return WatchListFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            mediaId = args.getLong(KeyUtil.arg_id)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
        mAdapter = EpisodeAdapter(ctx)
        mAdapter?.setClickListener(clickListener)
        setPresenter(WidgetPresenter<ConnectionContainer<List<ExternalLink>>>(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val context = context ?: return
        if (externalLinks != null) {
            val feed = targetLink != null && targetLink?.startsWith(BuildConfig.FEEDS_LINK) == true
            val bundle = viewModel?.params ?: Bundle.EMPTY
            bundle.putString(KeyUtil.arg_search, targetLink)
            bundle.putBoolean(KeyUtil.arg_feed, feed)
            viewModel?.requestData(getRequestMode(feed), context)
        } else {
            val queryContainer: QueryContainerBuilder = GraphUtil.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, mediaId)
                .putVariable(KeyUtil.arg_type, mediaType)
            presenter.params.putParcelable(KeyUtil.arg_graph_params, queryContainer)
            presenter.requestData(KeyUtil.MEDIA_EPISODES_REQ, context, this)
        }
    }

    @KeyUtil.RequestType
    private fun getRequestMode(feed: Boolean): Int {
        return if (feed)
            if (isPopular) KeyUtil.EPISODE_POPULAR_REQ else KeyUtil.EPISODE_LATEST_REQ
        else
            KeyUtil.EPISODE_FEED_REQ
    }

    override fun onResponse(
        call: Call<ConnectionContainer<List<ExternalLink>>>,
        response: Response<ConnectionContainer<List<ExternalLink>>>
    ) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            val connectionContainer = response.body()
            if (response.isSuccessful && connectionContainer != null) {
                if (!connectionContainer.isEmpty) {
                    externalLinks = connectionContainer.connection
                    val links = externalLinks
                    if (mAdapter?.itemCount ?: 0 < 1 && links != null)
                        targetLink = EpisodeUtil.episodeSupport(links)
                    if (targetLink == null)
                        showEmpty(getString(R.string.waring_missing_episode_links))
                    else
                        makeRequest()
                }
            } else {
                Timber.w(response.apiError())
            }
        }
    }

    override fun onFailure(
        call: Call<ConnectionContainer<List<ExternalLink>>>,
        throwable: Throwable
    ) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Timber.w(throwable)
        }
    }
}
