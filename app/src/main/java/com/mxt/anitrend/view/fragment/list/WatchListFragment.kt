package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.EpisodeAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentChannelBase
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.repository.CrunchyrollRepository
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.EpisodeUtil
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
    private val mediaRepository: MediaRepository by inject()
    private val crunchyrollRepository: CrunchyrollRepository by inject()

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
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        if (externalLinks != null) {
            val feed = targetLink != null && targetLink?.startsWith(BuildConfig.FEEDS_LINK) == true
            val link = targetLink
            if (link == null) {
                showEmpty(getString(R.string.waring_missing_episode_links))
                return
            }
            viewLifecycleOwner.lifecycleScope.launch {
                val result = when {
                    feed && isPopular -> crunchyrollRepository.getPopularFeed()
                    feed -> crunchyrollRepository.getLatestFeed()
                    else -> crunchyrollRepository.getRss(link)
                }
                result
                    .onSuccess { rss ->
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            onChanged(rss)
                        }
                    }
                    .onFailure { throwable ->
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            Timber.e(throwable)
                            showEmpty(throwable.message ?: getString(R.string.layout_empty_response))
                        }
                    }
            }
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                val isAdult = if (presenter.settings.displayAdultContent) null else false
                val mediaType = mediaType?.let { MediaType.valueOf(it) }
                mediaRepository.getMediaEpisodes(id = mediaId, type = mediaType, isAdult = isAdult)
                    .onSuccess { connectionContainer ->
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            if (!connectionContainer.isEmpty) {
                                externalLinks = connectionContainer.connection
                                val links = externalLinks
                                if ((mAdapter?.itemCount ?: 0) < 1 && links != null) {
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
}
