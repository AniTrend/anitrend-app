package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.WideImageView
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.mapper.toUserBase
import com.mxt.anitrend.databinding.FragmentProfileBinding
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.navigation.extension.navigateToComment
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.navigateToMediaList
import com.mxt.anitrend.navigation.extension.navigateToFavourites
import com.mxt.anitrend.navigation.extension.navigateToMessages
import com.mxt.anitrend.navigation.extension.navigateToNotifications
import com.mxt.anitrend.navigation.extension.navigateToProfile
import com.mxt.anitrend.navigation.extension.navigateToSettings
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import com.mxt.anitrend.viewmodel.ProfileViewModel
import com.mxt.anitrend.viewmodel.UserFeedViewModel
import com.mxt.anitrend.viewmodel.UserOverviewViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/** Single Navigation 2 destination for a user profile and its local sections. */
class ProfileFragment : Fragment() {
    private enum class Section { OVERVIEW, MEDIA_LIST, TEXT }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val settings: Settings by inject()
    private val databaseHelper: DatabaseHelper by inject()
    private val userRepository: UserRepository by inject()
    private val profileViewModel: ProfileViewModel by viewModel()
    private val overviewViewModel: UserOverviewViewModel by viewModel()
    private val feedViewModel: UserFeedViewModel by viewModel()

    private var model: UserBase? = null
    private var userId = 0L
    private var userName: String? = null
    private var section = Section.OVERVIEW
    private var activeSection: Any? = null
    private var renderedSection: Section? = null
    private var mediaActionUtil: MediaActionUtil? = null

    companion object {
        private const val KEY_SECTION = "profile_section"

        fun fromBundle(bundle: Bundle?): UserScreenParam? = resolve(
            typed = bundle?.screenParam<UserScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id, 0L) ?: 0L,
            legacyName = bundle?.getString(KeyUtil.arg_userName),
        )

        @VisibleForTesting
        internal fun resolve(
            typed: UserScreenParam?,
            legacyId: Long,
            legacyName: String?,
        ): UserScreenParam? = typed ?: UserScreenParam(legacyId, legacyName).takeIf {
            legacyId > 0L || !legacyName.isNullOrBlank()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        fromBundle(arguments)?.let { param ->
            userId = param.userId
            userName = param.initialName
        } ?: run {
            userId = arguments?.getLong(KeyUtil.arg_id, 0L) ?: 0L
            userName = arguments?.getString(KeyUtil.arg_userName)
        }
        section = savedInstanceState?.getString(KEY_SECTION)?.let { value ->
            runCatching { Section.valueOf(value) }.getOrDefault(Section.OVERVIEW)
        } ?: Section.OVERVIEW
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.profileStatsWidget.onMediaListRequested = { mediaType ->
            model?.let { user ->
                findNavControllerOrNull()?.navigateToMediaList(
                    UserScreenParam(user.id, user.name),
                    mediaType,
                )
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileIdentityTier.isVisible = false
        binding.profileBanner.setOnClickListener {
            model?.let { user ->
                CompatUtil.imagePreview(view, user.bannerImage, R.string.image_preview_error_profile_banner)
            }
        }
        binding.profileErrorRetry.setOnClickListener { profileViewModel.load(userId, userName) }
        setupSections()
        observeProfile()
        if (userId <= 0L && userName.isNullOrBlank()) {
            showErrorState(getString(R.string.profile_error_no_user), retryEnabled = false)
        } else {
            profileViewModel.load(userId, userName)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_SECTION, section.name)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
        val current = model
        val currentUser = current?.let { isCurrentUser(it.id) } == true
        menu.findItem(R.id.action_notification).isVisible = currentUser
        menu.findItem(R.id.action_message).isVisible = settings.isAuthenticated
        menu.findItem(R.id.action_settings).isVisible = currentUser
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_notification -> {
            findNavControllerOrNull()?.navigateToNotifications()
            true
        }
        R.id.action_message -> {
            if (!settings.isAuthenticated) return true
            val current = model
            when {
                current == null -> NotifyUtil.makeText(requireContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                isCurrentUser(current.id) -> findNavControllerOrNull()?.navigateToMessages()
                else -> showMessageComposer(current)
            }
            true
        }
        R.id.action_share -> {
            model?.name?.let { name ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(), "https://anilist.co/user/%s", name))
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
            }
            true
        }
        R.id.action_settings -> {
            findNavControllerOrNull()?.navigateToSettings()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        mediaActionUtil?.onDestroy()
        clearActiveSection()
        binding.profileStatsWidget.onMediaListRequested = null
        _binding = null
        super.onDestroyView()
    }

    private fun setupSections() {
        val tabs = binding.profileSections.smartTab
        tabs.removeAllTabs()
        listOf(
            R.string.profile_section_overview,
            R.string.profile_section_media,
            R.string.profile_section_text,
        ).forEach { tabs.addTab(tabs.newTab().setText(it)) }
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                section = Section.entries.getOrElse(tab.position) { Section.OVERVIEW }
                renderSection()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
        tabs.getTabAt(section.ordinal)?.select()
    }

    private fun observeProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.state.collect { state ->
                    when (state) {
                        ProfileViewModel.UiState.Loading -> showLoadingState()
                        is ProfileViewModel.UiState.Success -> {
                            model = state.user
                            bindHeader(state.user)
                            showContentState()
                            renderSection()
                        }
                        is ProfileViewModel.UiState.Error -> showErrorState(state.message)
                    }
                }
            }
        }
    }

    private fun bindHeader(user: UserBase) {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.loadStats(userId, userName).onSuccess(binding.profileStatsWidget::setStats)
        }
        WideImageView.setImage(binding.profileBanner, user.bannerImage)
        binding.profileDisplayName.text = user.name
        binding.profileHandle.text = user.name?.let { "@$it" }
        Glide.with(this)
            .load(user.avatar?.large)
            .apply(RequestOptions.circleCropTransform())
            .apply(RequestOptions.placeholderOf(R.drawable.avatar_placeholder))
            .transition(DrawableTransitionOptions.withCrossFade(150))
            .into(binding.profileAvatar)
        binding.profileIdentityTier.isVisible = true
        requireActivity().invalidateOptionsMenu()
    }

    private fun renderSection() {
        if (model == null || _binding == null) return
        if (!shouldRebuildSection(renderedSection?.name, section.name)) return
        clearActiveSection()
        val container = binding.profileSectionContainer
        activeSection = when (section) {
            Section.OVERVIEW -> ProfileOverviewSection(
                activity = requireActivity(),
                settings = settings,
                userRepository = userRepository,
                viewModel = overviewViewModel,
                userId = userId,
                userName = userName,
                onOpenFavourites = { id -> findNavControllerOrNull()?.navigateToFavourites(UserScreenParam(id)) },
                onOpenUser = { param -> findNavControllerOrNull()?.navigateToProfile(param) },
            ).also { controller ->
                container.addView(controller.inflate(layoutInflater, container))
                controller.start(viewLifecycleOwner)
            }
            Section.MEDIA_LIST, Section.TEXT -> ProfileFeedSection(
                settings = settings,
                databaseHelper = databaseHelper,
                userRepository = userRepository,
                viewModel = feedViewModel,
                userId = userId,
                userName = userName,
                type = if (section == Section.MEDIA_LIST) KeyUtil.MEDIA_LIST else KeyUtil.TEXT,
                onOpenMedia = { target, feedId ->
                    val item = feedViewModel.state.value.let { state ->
                        (state as? UserFeedViewModel.UiState.Success)?.items?.firstOrNull { it.id == feedId }
                    }
                    item?.mediaId?.let { id -> navigateToMedia(com.mxt.anitrend.navigation.model.MediaScreenParam(id, item.mediaType)) }
                },
                onOpenComments = { id -> findNavControllerOrNull()?.navigateToComment(CommentScreenParam(id)) },
                onEditFeed = ::editFeed,
                onShowLikes = { _, likes -> showLikes(likes) },
                onOpenProfile = { _, id -> navigateToProfile(UserScreenParam(id)) },
                onLongPressMedia = ::onLongPressMedia,
                onToggleLike = feedViewModel::toggleLike,
                onDeleteFeed = feedViewModel::deleteFeed,
            ).also { controller ->
                container.addView(controller.inflate(layoutInflater, container))
                controller.start(viewLifecycleOwner)
            }
        }
        renderedSection = section
    }

    private fun clearActiveSection() {
        when (val current = activeSection) {
            is ProfileOverviewSection -> current.clear()
            is ProfileFeedSection -> current.clear()
        }
        bindingOrNull()?.profileSectionContainer?.removeAllViews()
        activeSection = null
        renderedSection = null
    }

    private fun editFeed(feedId: Long) {
        val item = (feedViewModel.state.value as? UserFeedViewModel.UiState.Success)?.items?.firstOrNull { it.id == feedId } ?: return
        BottomSheetComposer.Builder()
            .setUserActivity(item)
            .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
            .setTitle(R.string.edit_status_title)
            .build()
            .show(parentFragmentManager, null)
    }

    private fun showLikes(likes: List<UserSummaryRecord>) {
        val users = likes.map { it.toUserBase() }
        if (users.isNotEmpty()) {
            BottomSheetUsers.Builder()
                .setModel(users)
                .setOnUserClick(::navigateToProfile)
                .setTitle(R.string.title_bottom_sheet_likes)
                .build()
                .show(parentFragmentManager, null)
        }
    }

    private fun onLongPressMedia(target: View, feedId: Long): Boolean {
        if (!settings.isAuthenticated) return false
        val item = (feedViewModel.state.value as? UserFeedViewModel.UiState.Success)?.items?.firstOrNull { it.id == feedId }
        val mediaId = item?.mediaId ?: return false
        mediaActionUtil = MediaActionUtil.Builder().setId(mediaId).build(requireActivity())
        mediaActionUtil?.startSeriesAction()
        return true
    }

    private fun showMessageComposer(user: UserBase) {
        BottomSheetComposer.Builder()
            .setUserModel(user)
            .setRequestMode(KeyUtil.MUT_SAVE_MESSAGE_FEED)
            .setTitle(R.string.text_message_to)
            .build()
            .show(parentFragmentManager, null)
    }

    private fun isCurrentUser(id: Long): Boolean = settings.isAuthenticated && profileViewModel.isCurrentUser(id, userName)

    private fun showLoadingState() {
        binding.profileStateOverlay.isVisible = true
        binding.profileLoadingState.isVisible = true
        binding.profileErrorState.isVisible = false
    }

    private fun showContentState() {
        binding.profileStateOverlay.isVisible = false
    }

    private fun showErrorState(message: String, retryEnabled: Boolean = true) {
        binding.profileStateOverlay.isVisible = true
        binding.profileLoadingState.isVisible = false
        binding.profileErrorState.isVisible = true
        binding.profileErrorText.text = message
        binding.profileErrorRetry.isVisible = retryEnabled
    }

    private fun bindingOrNull() = _binding

    private fun findNavControllerOrNull() = runCatching { findNavController() }.getOrNull()

    @VisibleForTesting
    internal fun shouldRebuildSection(activeSection: String?, requestedSection: String): Boolean = activeSection != requestedSection
}
