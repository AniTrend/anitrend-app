package com.mxt.anitrend.view.fragment.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget.StatState
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.FragmentUserAboutBinding
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.StatsRing
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.viewmodel.UserOverviewViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * View-only controller for the profile overview section.
 *
 * This is deliberately an ordinary UI component, not a child Fragment. The
 * parent ProfileFragment owns section selection and the ViewModel remains the
 * owner of profile loading and follow mutations.
 * The callback parameters preserve the profile destination's existing actions.
 */
@Suppress("LongParameterList")
class ProfileOverviewSection(
    private val activity: FragmentActivity,
    private val settings: Settings,
    private val userRepository: UserRepository,
    private val viewModel: UserOverviewViewModel,
    private val userId: Long,
    private val userName: String?,
    private val onOpenFavourites: (Long) -> Unit,
    private val onOpenUser: (UserScreenParam) -> Unit,
) {
    private var binding: FragmentUserAboutBinding? = null
    private var model: User? = null
    private var lifecycleOwner: LifecycleOwner? = null

    /** Inflates and initializes the profile overview view. */
    fun inflate(inflater: LayoutInflater, container: ViewGroup): View {
        val sectionBinding = FragmentUserAboutBinding.inflate(inflater, container, false)
        binding = sectionBinding
        sectionBinding.userAvatar.setOnClickListener {
            CompatUtil.imagePreview(
                sectionBinding.root,
                model?.avatar?.large,
                R.string.image_preview_error_user_avatar,
            )
        }
        sectionBinding.userStatsContainer.setOnClickListener { onStatsClick() }
        sectionBinding.stateLayout.showLoading()
        return sectionBinding.root
    }

    /** Starts collecting profile state and follow state for [owner]. */
    fun start(owner: LifecycleOwner) {
        lifecycleOwner = owner
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is UserOverviewViewModel.UiState.Loading -> binding?.stateLayout?.showLoading()
                        is UserOverviewViewModel.UiState.Success -> {
                            model = state.user
                            bindUser(state.user)
                        }
                        is UserOverviewViewModel.UiState.Error -> binding?.stateLayout?.showError(
                            activity.getCompatDrawable(R.drawable.ic_emoji_sweat),
                            state.message,
                            activity.getString(R.string.try_again),
                        ) { viewModel.load(userId, userName.orEmpty()) }
                    }
                }
            }
        }
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isFollowing.collect { renderFollowCta(it) }
            }
        }
        viewModel.load(userId, userName.orEmpty())
    }

    /** Releases profile view resources and cached rendering state. */
    fun clear() {
        binding?.userFollowStateWidget?.setListener(null)
        binding?.userFollowStateWidget?.setCurrentUser(null)
        binding?.userFollowStateWidget?.onViewRecycled()
        binding?.userAboutPanelWidget?.onViewRecycled()
        binding = null
        model = null
        lifecycleOwner = null
    }

    private fun bindUser(user: User) {
        val sectionBinding = binding ?: return
        sectionBinding.stateLayout.showContent()
        sectionBinding.userAvatar.setImage(user.avatar)
        sectionBinding.userNameText.text = user.name
        sectionBinding.widgetStatusText.richMarkDown(user.about)
        sectionBinding.widgetStatus.isVisible = false
        sectionBinding.userFollowStateWidget.setListener { id -> viewModel.toggleFollow(id) }
        sectionBinding.userFollowStateWidget.setCurrentUser(viewModel.currentUserSnapshot)
        sectionBinding.userFollowStateWidget.setUserModel(user)
        renderFollowCta(viewModel.isFollowing.value)
        sectionBinding.userAboutPanelWidget.setFragmentActivity(activity)
        sectionBinding.userAboutPanelWidget.onFavouritesRequested = onOpenFavourites
        sectionBinding.userAboutPanelWidget.onUserRequested = onOpenUser
        lifecycleOwner?.let { owner -> sectionBinding.userAboutPanelWidget.setUserId(user.id, owner.lifecycle) }
        loadPanelStats(user.id)
        showRingStats()
    }

    private fun renderFollowCta(committedFollowState: Boolean?) {
        val user = model ?: return
        binding?.userFollowStateWidget?.setUserModel(
            UserBase(name = user.name, isFollowing = committedFollowState ?: user.isFollowing).apply {
                id = user.id
            },
        )
    }

    private fun loadPanelStats(userId: Long) {
        val sectionBinding = binding ?: return
        lifecycleOwner?.lifecycleScope?.launch {
            val followers = async { userRepository.getFollowers(id = userId, perPage = 1) }
            val following = async { userRepository.getFollowing(id = userId, perPage = 1) }
            val favourites = async { userRepository.getFavouritesCount(id = userId, perPage = 1) }
            sectionBinding.userAboutPanelWidget.setStats(
                followers.await().fold(
                    onSuccess = { StatState.Loaded(it.pageInfo?.total ?: 0) },
                    onFailure = { StatState.Failed },
                ),
                following.await().fold(
                    onSuccess = { StatState.Loaded(it.pageInfo?.total ?: 0) },
                    onFailure = { StatState.Failed },
                ),
                favourites.await().fold(
                    onSuccess = { container ->
                        StatState.Loaded(
                            listOfNotNull(
                                container.connection.anime?.pageInfo?.total,
                                container.connection.manga?.pageInfo?.total,
                                container.connection.characters?.pageInfo?.total,
                                container.connection.staff?.pageInfo?.total,
                                container.connection.studios?.pageInfo?.total,
                            ).sum(),
                        )
                    },
                    onFailure = { StatState.Failed },
                ),
            )
        }
    }

    private fun showRingStats() {
        val sectionBinding = binding ?: return
        val rings = generateStatsData()
        if (rings.size > 1) {
            sectionBinding.userStats.setDrawBg(
                CompatUtil.isLightTheme(settings),
                sectionBinding.root.context.getCompatColorAttr(com.mxt.anitrend.R.attr.subtitleColor),
            )
            sectionBinding.userStats.setData(rings, 500)
        }
    }

    private fun generateStatsData(): List<StatsRing> {
        val genres = model?.statistics?.anime?.genres.orEmpty()
        val highest = genres.maxOfOrNull { it.count } ?: return emptyList()
        if (highest == 0) return emptyList()
        return genres.sortedByDescending { it.count }.take(5).map {
            StatsRing((it.count.toFloat() / highest * 100f).toInt(), it.genre, it.count.toString())
        }
    }

    private fun onStatsClick() {
        if (generateStatsData().size > 1) {
            showRingStats()
        } else {
            NotifyUtil.makeText(activity, R.string.text_error_request, Toast.LENGTH_SHORT).show()
        }
    }
}
