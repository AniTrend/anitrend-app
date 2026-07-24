package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.EpisodeAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentChannelBase
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.EpisodeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Created by max on 2017/11/03.
 * WatchListFragment for anime types
 */
class WatchListFragment :
    FragmentChannelBase(),
    KoinComponent {
    private val fragmentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mediaRepository: MediaRepository by inject()

    private var mediaId: Long = 0

    @KeyUtil.MediaType
    private var mediaType: String? = null

    companion object {
        @JvmStatic
        fun newInstance(
            params: Bundle,
            popular: Boolean,
        ): FragmentChannelBase {
            val args =
                Bundle(params).apply {
                    putBoolean(KeyUtil.arg_popular, popular)
                }
            return WatchListFragment().apply {
                arguments = args
            }
        }

        @JvmStatic
        fun newInstance(
            externalLinks: List<ExternalLink>,
            popular: Boolean,
        ): FragmentChannelBase {
            val args =
                Bundle().apply {
                    putParcelableArrayList(
                        KeyUtil.arg_list_model,
                        ArrayList<Parcelable>(externalLinks),
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
            fragmentScope.launch {
                val isAdult = if (presenter.settings.displayAdultContent) null else false
                val mediaType = mediaType?.let { MediaType.valueOf(it) }
                mediaRepository.getMediaEpisodes(id = mediaId, type = mediaType, isAdult = isAdult)
                    .onSuccess { connectionContainer ->
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            if (!connectionContainer.isEmpty) {
                                externalLinks = connectionContainer.connection
                                val links = externalLinks
                                if (mAdapter?.itemCount ?: 0 < 1 && links != null) {
                                    targetLink = EpisodeUtil.episodeSupport(links)
                                }
                                if (targetLink == null) {
                                    showEmpty(getString(R.string.waring_missing_episode_links))
                                } else {
                                    makeRequest()
                                }
                            }
                        }
                    }
                    .onFailure { throwable ->
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            Timber.w(throwable)
                        }
                    }
            }
        }
    }

    @KeyUtil.RequestType
    private fun getRequestMode(feed: Boolean): Int = if (feed) {
        if (isPopular) {
            KeyUtil.EPISODE_POPULAR_REQ
        } else {
            KeyUtil.EPISODE_LATEST_REQ
        }
    } else {
        KeyUtil.EPISODE_FEED_REQ
    }
}
