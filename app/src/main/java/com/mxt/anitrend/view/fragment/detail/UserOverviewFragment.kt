package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.FragmentUserAboutBinding
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.StatsRing
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.viewmodel.UserOverviewViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class UserOverviewFragment : Fragment() {

    private var _binding: FragmentUserAboutBinding? = null
    private val binding: FragmentUserAboutBinding
        get() = requireNotNull(_binding)

    private var model: User? = null

    private var userId: Long = 0
    private var userName: String = ""

    private val settings: Settings by inject()
    private val userRepository: UserRepository by inject()

    private val userOverviewViewModel: UserOverviewViewModel by viewModel()

    companion object {
        fun newInstance(args: Bundle): UserOverviewFragment {
            val fragment = UserOverviewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            userId = args.getLong(KeyUtil.arg_id, 0L)
            userName = args.getString(KeyUtil.arg_userName, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUserAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userAvatar.setOnClickListener { onImageClick() }
        binding.userStatsContainer.setOnClickListener { onStatsClick() }
        binding.stateLayout.showLoading()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userOverviewViewModel.state.collect { state ->
                    when (state) {
                        is UserOverviewViewModel.UiState.Loading -> {
                            binding.stateLayout.showLoading()
                        }
                        is UserOverviewViewModel.UiState.Success -> {
                            model = state.user
                            bindUser(state.user)
                        }
                        is UserOverviewViewModel.UiState.Error -> {
                            binding.stateLayout.showError(
                                requireContext().getCompatDrawable(R.drawable.ic_emoji_sweat),
                                state.message,
                                getString(R.string.try_again),
                            ) { loadUser() }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userOverviewViewModel.isFollowing.collect { committedFollowState ->
                    renderFollowCta(committedFollowState)
                }
            }
        }

        loadUser()
    }

    private fun loadUser() {
        binding.stateLayout.showLoading()
        userOverviewViewModel.load(userId = userId, userName = userName)
    }

    private fun bindUser(user: User) {
        binding.stateLayout.showContent()
        binding.userAvatar.setImage(user.avatar)
        binding.userNameText.text = user.name
        binding.widgetStatusText.richMarkDown(user.about)
        binding.widgetStatus.visibility = View.GONE

        binding.userFollowStateWidget.setListener { userId ->
            userOverviewViewModel.toggleFollow(userId)
        }
        binding.userFollowStateWidget.setCurrentUser(userOverviewViewModel.currentUserSnapshot)
        binding.userFollowStateWidget.setUserModel(user)
        // Reconcile the CTA with the currently committed follow state so a pre-existing
        // store record is never lost when the independent state and follow-state
        // collectors race: the follow-state collector may have consumed and rendered
        // its emission before [model] was available to render it.
        renderFollowCta(userOverviewViewModel.isFollowing.value)
        binding.userAboutPanelWidget.setFragmentActivity(activity)
        binding.userAboutPanelWidget.setUserId(user.id, lifecycle)
        loadPanelStats(user.id)
        showRingStats()
    }

    /**
     * Re-renders only the follow CTA from the ViewModel's committed follow state.
     * Null means no committed record exists yet, so the server-loaded follow value
     * on the loaded [model] stays the fallback. The loaded [User] is never mutated;
     * a lightweight render-only model is pushed into the widget instead.
     */
    private fun renderFollowCta(committedFollowState: Boolean?) {
        val user = model ?: return
        val isFollowing = committedFollowState ?: user.isFollowing
        binding.userFollowStateWidget.setUserModel(
            UserBase(name = user.name, isFollowing = isFollowing).apply { id = user.id },
        )
    }

    private fun loadPanelStats(userId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val followersDef = async { userRepository.getFollowers(id = userId, perPage = 1) }
            val followingDef = async { userRepository.getFollowing(id = userId, perPage = 1) }
            val favsDef = async { userRepository.getFavouritesCount(id = userId, perPage = 1) }

            val followersTotal = followersDef.await().getOrNull()?.pageInfo?.total
            val followingTotal = followingDef.await().getOrNull()?.pageInfo?.total
            val favConnection = favsDef.await().getOrNull()?.connection
            val favouritesTotal = if (favConnection != null) {
                listOfNotNull(
                    favConnection.anime?.pageInfo?.total,
                    favConnection.manga?.pageInfo?.total,
                    favConnection.characters?.pageInfo?.total,
                    favConnection.staff?.pageInfo?.total,
                    favConnection.studios?.pageInfo?.total,
                ).sum()
            } else {
                null
            }

            binding.userAboutPanelWidget.setStats(followersTotal, followingTotal, favouritesTotal)
        }
    }

    private fun showRingStats() {
        context?.let { ctx ->
            val ringList = generateStatsData()
            if (ringList.size > 1) {
                binding.userStats.setDrawBg(
                    CompatUtil.isLightTheme(settings),
                    ctx.getCompatColorAttr(R.attr.subtitleColor),
                )
                binding.userStats.setData(ringList, 500)
            }
        }
    }

    private fun generateStatsData(): List<StatsRing> {
        var userGenreStats: List<StatsRing> = ArrayList()
        val statistics = model?.statistics
        val genres = statistics?.anime?.genres
        if (statistics != null && genres != null && genres.isNotEmpty()) {
            val highestValue = genres.maxByOrNull { it.count }?.count ?: 0
            userGenreStats = genres
                .sortedByDescending { it.count }.map { genreStats ->
                    val percentage = genreStats.count.toFloat() / highestValue.toFloat() * 100f
                    StatsRing(percentage.toInt(), genreStats.genre, genreStats.count.toString())
                }.take(5)
        }
        return userGenreStats
    }

    private fun onImageClick() {
        CompatUtil.imagePreview(
            requireView(),
            model?.avatar?.large,
            R.string.image_preview_error_user_avatar,
        )
    }

    private fun onStatsClick() {
        val ringList = generateStatsData()
        if (ringList.size > 1) {
            context?.let { ctx ->
                binding.userStats.setDrawBg(
                    CompatUtil.isLightTheme(settings),
                    ctx.getCompatColorAttr(R.attr.subtitleColor),
                )
                binding.userStats.setData(ringList, 500)
            }
        } else {
            activity?.let {
                NotifyUtil.makeText(it, R.string.text_error_request, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        binding.userFollowStateWidget.setListener(null)
        binding.userFollowStateWidget.setCurrentUser(null)
        binding.userFollowStateWidget.onViewRecycled()
        binding.userAboutPanelWidget.onViewRecycled()
        super.onDestroyView()
        _binding = null
    }
}
