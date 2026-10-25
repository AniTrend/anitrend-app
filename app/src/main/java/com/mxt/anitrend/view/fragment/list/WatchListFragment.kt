package com.mxt.anitrend.view.fragment.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBase
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.parcelableArrayList
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.crunchy.Episode
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.episode.EpisodeList
import com.mxt.anitrend.ui.episode.EpisodeRowUiState
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.viewmodel.WatchListViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/11/03.
 * WatchListFragment for anime types
 */
class WatchListFragment :
    FragmentBase<Episode, WatchListViewModel.UiState>(),
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private val watchListViewModel: WatchListViewModel by viewModel()

    private var viewBinding: FragmentListBinding? = null
    private var composeView: ComposeView? = null
    private val episodes = mutableStateOf(emptyList<EpisodeRowUiState>())
    private val episodeListState = LazyGridState()

    private val stateLayoutOnClick =
        View.OnClickListener {
            val binding = viewBinding ?: return@OnClickListener
            if (binding.refreshLayout.isRefreshing()) {
                binding.refreshLayout.setRefreshing(false)
            }
            if (binding.refreshLayout.isLoading()) {
                binding.refreshLayout.setLoading(false)
            }
            binding.stateLayout.showLoading()
            onRefresh()
        }

    companion object {
        const val ARG_FEED_ROUTE = "navigation_watch_feed"

        @JvmStatic
        fun newInstance(
            params: Bundle,
            popular: Boolean,
        ): WatchListFragment {
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
        ): WatchListFragment {
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

        /**
         * The airing feed route has no media parameter and resolves to id 0 /
         * null type. Media-specific producers retain arg_id / arg_mediaType for
         * this compatibility reader.
         */
        fun fromBundle(bundle: Bundle?): MediaScreenParam? = resolve(
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
            legacyType = bundle?.getString(KeyUtil.arg_mediaType),
        )

        @VisibleForTesting
        internal fun resolve(legacyId: Long, legacyType: String?): MediaScreenParam = MediaScreenParam(mediaId = legacyId, mediaType = legacyType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        val mediaIdentity = fromBundle(args)
        watchListViewModel.configure(
            mediaId = mediaIdentity?.mediaId ?: 0L,
            mediaType = mediaIdentity?.mediaType,
            isPopular = args?.getBoolean(KeyUtil.arg_popular) ?: false,
            isFeedRoute = args?.getBoolean(ARG_FEED_ROUTE) == true,
            externalLinks = args?.parcelableArrayList(KeyUtil.arg_list_model),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentListBinding.inflate(inflater, container, false)
        viewBinding = binding
        binding.refreshLayout.setOnRefreshAndLoadListener(this)
        // RSS episodes have no paginated endpoint; only pull-to-refresh is supported.
        binding.refreshLayout.setPermitLoad(false)
        activity?.let { CompatUtil.configureSwipeRefreshLayout(binding.refreshLayout, it) }
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val host =
            ComposeView(requireContext()).apply {
                // Preserve the legacy series-image action target for the row dialog.
                id = R.id.series_image
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        EpisodeList(
                            items = episodes.value,
                            listState = episodeListState,
                            onEpisodeClick = ::dispatchEpisodeClick,
                            onEpisodeLongClick = { _ ->
                                // RSS episodes do not have a long-press action.
                            },
                        )
                    }
                }
            }
        composeView = host
        val refreshLayout = requireNotNull(viewBinding).refreshLayout
        refreshLayout.scrollBoundary = { direction ->
            if (direction < 0) episodeListState.canScrollBackward else episodeListState.canScrollForward
        }
        refreshLayout.addView(
            host,
            0,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                watchListViewModel.state.collect { state -> onChanged(state) }
            }
        }
    }

    override fun onDestroyView() {
        viewBinding?.refreshLayout?.scrollBoundary = null
        super.onDestroyView()
        composeView = null
        viewBinding = null
    }

    private fun dispatchEpisodeClick(index: Int) {
        if (composeView?.id != R.id.series_image) return
        val episode = watchListViewModel.episodeActionAt(index) ?: return
        val host = activity ?: return
        DialogUtil.createMessage(
            host,
            episode.title,
            episode.description + "<br/><br/>" + (episode.copyright ?: ""),
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

    override fun onChanged(value: WatchListViewModel.UiState?) {
        when (value) {
            null, is WatchListViewModel.UiState.Loading -> {
                if (!watchListViewModel.hasContent()) {
                    episodes.value = emptyList()
                }
                showLoading()
            }
            is WatchListViewModel.UiState.Content -> {
                episodes.value = value.rows
                clearProgressIndicators()
                if (value.sourceCount > 0) {
                    viewBinding?.stateLayout?.showContent()
                } else {
                    showEmpty(getString(R.string.layout_empty_response))
                }
            }
            is WatchListViewModel.UiState.Empty -> {
                val message =
                    when (value.reason) {
                        WatchListViewModel.EmptyReason.NO_EPISODES -> getString(R.string.layout_empty_response)
                        WatchListViewModel.EmptyReason.MISSING_EPISODE_LINKS -> getString(R.string.waring_missing_episode_links)
                    }
                showEmpty(message)
            }
            is WatchListViewModel.UiState.Error -> showError(value.message ?: getString(R.string.layout_empty_response))
        }
    }

    /** The ViewModel's source RSS snapshot, not its filtered projection, owns content-on-return. */
    private fun hasContent(): Boolean = watchListViewModel.hasContent()

    /** Called by the toolbar filter bridge; query state and row projection live in the ViewModel. */
    fun applySearchQuery(searchQuery: String?) {
        watchListViewModel.onSearchQueryChanged(searchQuery)
    }

    override fun onStart() {
        super.onStart()
        if (hasContent()) {
            watchListViewModel.restoreContent()
            updateUI()
        } else {
            showLoading()
            makeRequest()
        }
    }

    override fun updateUI() {
        onChanged(watchListViewModel.state.value)
    }

    override fun makeRequest() {
        watchListViewModel.refresh()
    }

    override fun onRefresh() {
        watchListViewModel.refresh()
    }

    // The custom container requires this callback, but bottom-loading is disabled for RSS.
    override fun onLoad() = Unit

    override fun showError(error: String) {
        super.showError(error)
        clearProgressIndicators()
        viewBinding?.stateLayout?.showError(
            context?.getCompatDrawable(R.drawable.ic_emoji_cry),
            error,
            getString(R.string.try_again),
            stateLayoutOnClick,
        )
    }

    override fun showEmpty(message: String) {
        super.showEmpty(message)
        clearProgressIndicators()
        viewBinding?.stateLayout?.showError(
            context?.getCompatDrawable(R.drawable.ic_emoji_sweat),
            message,
            getString(R.string.try_again),
            stateLayoutOnClick,
        )
    }

    private fun showLoading() {
        viewBinding?.stateLayout?.showLoading()
    }

    private fun clearProgressIndicators() {
        val refreshLayout = viewBinding?.refreshLayout ?: return
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false)
        } else if (refreshLayout.isLoading()) {
            refreshLayout.setLoading(false)
        }
    }
}
