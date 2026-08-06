package com.mxt.anitrend.view.activity.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.detail.ProfilePageAdapter
import com.mxt.anitrend.base.custom.view.image.WideImageView
import com.mxt.anitrend.databinding.ActivityProfileBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.navigation.extension.putScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.TutorialUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.activity.base.SettingsActivity
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Created by max on 2017/11/14.
 * Profile activity
 */
class ProfileActivity :
    CommonActivity(),
    View.OnClickListener {

    companion object {
        fun newIntent(context: Context, param: UserScreenParam): Intent = Intent(context, ProfileActivity::class.java).apply {
            putScreenParam(param)
            // Interim boundary: the pager fragments and the profile stats widget still
            // consume the legacy wire keys from the activity extras, so keep them
            // alongside the typed param.
            putExtra(KeyUtil.arg_id, param.userId)
            putExtra(KeyUtil.arg_userName, param.initialName)
        }

        /**
         * Resolves the typed parameter from the intent.
         *
         * The typed parameter is read first. Deep links (injected by
         * [IntentBundleUtil.checkIntentData]) and pre-bridge callers still write the
         * legacy [KeyUtil.arg_id] and [KeyUtil.arg_userName] extras, so those values
         * are bridged here into [UserScreenParam] via [resolve]. The bridge is a
         * scalar conversion point inside the activity, not a parcel path for the
         * user entity.
         */
        fun fromIntent(intent: Intent): UserScreenParam? = resolve(
            typed = intent.screenParam<UserScreenParam>(),
            legacyId = intent.getLongExtra(KeyUtil.arg_id, -1),
            legacyName = intent.getStringExtra(KeyUtil.arg_userName),
        )

        /**
         * Production parsing rule for the profile destination.
         *
         * A present typed parameter wins; it is accepted only when it carries a
         * positive user id or a non-blank name. Otherwise the legacy [KeyUtil.arg_id]
         * and [KeyUtil.arg_userName] extras are bridged when either carries identity.
         * A null result keeps the pre-refactor no-identity behaviour: the pager stays
         * hidden and the error state is rendered in onResume.
         */
        @VisibleForTesting
        internal fun resolve(
            typed: UserScreenParam?,
            legacyId: Long,
            legacyName: String?,
        ): UserScreenParam? {
            typed?.let { param ->
                return if (param.userId > 0L || !param.initialName.isNullOrBlank()) param else null
            }
            return if (legacyId > 0L || !legacyName.isNullOrBlank()) {
                UserScreenParam(userId = if (legacyId > 0L) legacyId else 0L, initialName = legacyName)
            } else {
                null
            }
        }

        /**
         * Resolves the legacy media-list redirect from the launch intent.
         *
         * Deep links (e.g. anilist.co/user/{name}/animelist) inject
         * [KeyUtil.arg_mediaType] before onCreate; when present, the profile
         * forwards its full extras to MediaListActivity instead of rendering
         * profile content. This is a scalar conversion point inside the activity,
         * not part of the identity parameter.
         */
        fun redirectToMediaList(intent: Intent): Boolean = hasMediaListRedirect(intent)

        /**
         * Production parsing rule for the media-list redirect: mirrors the exact
         * legacy `Intent.hasExtra(KeyUtil.arg_mediaType)` semantics. The routing
         * decision depends only on key presence in the extras bundle, never on the
         * stored value's type or nullness: an explicitly present null or a non-String
         * value (e.g. a numeric extra) still triggers the redirect, exactly as the
         * legacy `hasExtra` check did, whereas a `getStringExtra`-based check would
         * have missed those cases.
         */
        @VisibleForTesting
        internal fun hasMediaListRedirect(intent: Intent): Boolean = intent.hasExtra(KeyUtil.arg_mediaType)
    }

    private lateinit var binding: ActivityProfileBinding

    private var model: UserBase? = null
    private var userId: Long = 0
    private var userName: String? = null
    private var mBottomSheet: com.mxt.anitrend.base.custom.sheet.BottomSheetBase<*>? = null

    private val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Process deep links (e.g. anilist.co/user/{name}) so arg_userName/arg_id
        // is injected into the intent before we read it. Previously handled by
        // ActivityBase.onCreate -> IntentBundleUtil.checkIntentData.
        IntentBundleUtil(intent).checkIntentData(this)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.profileIdentityTier.visibility = GONE
        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(
            getCompatDrawable(R.drawable.ic_arrow_back_white_24dp),
        )
        binding.profileBanner.setOnClickListener(this)
        binding.profileErrorRetry.setOnClickListener {
            profileViewModel.load(userId, userName)
        }

        // Resolve the destination through the typed parameter, falling back to the
        // legacy wire keys for deep links and pre-bridge callers. A null result keeps
        // the existing no-identity behaviour: the pager stays hidden and the error
        // state is rendered in onResume.
        fromIntent(intent)?.let { args ->
            userId = args.userId
            userName = args.initialName
        }
        // Legacy deep-link routing: animelist/mangalist profile links forward the
        // full extras to MediaListActivity instead of rendering profile content.
        if (redirectToMediaList(intent)) {
            val intent =
                Intent(this, MediaListActivity::class.java).apply {
                    putExtras(this@ProfileActivity.intent.extras ?: Bundle())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            startActivity(intent)
        }

        observeViewModel()
        if (hasProfileIdentity()) {
            setUpPager()
        } else {
            binding.smartTab.smartTab.visibility = GONE
            binding.pageContainer.pageContainer.visibility = GONE
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.state.collect { state ->
                    when (state) {
                        is ProfileViewModel.UiState.Loading -> showLoadingState()
                        is ProfileViewModel.UiState.Success -> {
                            model = state.user
                            showContentState()
                            updateUI()
                        }
                        is ProfileViewModel.UiState.Error -> {
                            showErrorState(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun setUpPager() {
        val profilePageAdapter =
            ProfilePageAdapter(this, applicationContext).apply {
                params = intent.extras ?: Bundle.EMPTY
            }
        binding.pageContainer.pageContainer.adapter = profilePageAdapter
        binding.pageContainer.pageContainer.offscreenPageLimit = 3
        TabLayoutMediator(
            binding.smartTab.smartTab,
            binding.pageContainer.pageContainer,
        ) { tab, position ->
            tab.text = profilePageAdapter.getPageTitle(position)
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        // The notification overflow item and the settings entry are only
        // relevant for the current signed-in user. Share stays available for
        // any profile. Deep-link handling above already resolved the target.
        if (!isCurrentUser(userId, userName)) {
            menu.findItem(R.id.action_notification).isVisible = false
            menu.findItem(R.id.action_settings).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        val current = model
        return when (item.itemId) {
            R.id.action_notification -> {
                startActivity(Intent(this@ProfileActivity, NotificationActivity::class.java))
                true
            }
            R.id.action_message -> {
                if (current != null) {
                    if (isCurrentUser(current.id)) {
                        startActivity(Intent(this@ProfileActivity, MessageActivity::class.java))
                    } else {
                        mBottomSheet =
                            BottomSheetComposer
                                .Builder()
                                .setUserModel(current)
                                .setRequestMode(KeyUtil.MUT_SAVE_MESSAGE_FEED)
                                .setTitle(R.string.text_message_to)
                                .build()
                        mBottomSheet?.let { sheet ->
                            sheet.show(supportFragmentManager, sheet.tag)
                        }
                    }
                } else {
                    NotifyUtil
                        .makeText(this, R.string.text_activity_loading, Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }
            R.id.action_share -> {
                if (current != null) {
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(
                                Intent.EXTRA_TEXT,
                                String.format(
                                    Locale.getDefault(),
                                    "https://anilist.co/user/%s",
                                    current.name,
                                ),
                            )
                            type = "text/plain"
                        }
                    startActivity(
                        Intent.createChooser(
                            intent,
                            getString(R.string.abc_shareactionprovider_share_with),
                        ),
                    )
                } else {
                    NotifyUtil
                        .makeText(this, R.string.text_activity_loading, Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this@ProfileActivity, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasProfileIdentity()) {
            showErrorState(getString(R.string.profile_error_no_user), retryEnabled = false)
        } else {
            profileViewModel.load(userId, userName)
        }
    }

    private fun hasProfileIdentity(): Boolean = userId > 0L || !userName.isNullOrBlank()

    private fun updateUI() {
        val current = model ?: return
        binding.profileStatsWidget.setParams(intent.extras ?: Bundle())
        lifecycleScope.launch {
            profileViewModel.loadStats(userId, userName)
                .onSuccess { stats ->
                    binding.profileStatsWidget.setStats(stats)
                }
                .onFailure {
                    // stats loading failed, widget shows placeholders
                }
        }
        WideImageView.setImage(binding.profileBanner, current.bannerImage)

        // Identity tier
        binding.profileDisplayName.text = current.name
        current.name?.let { binding.profileHandle.text = "@$it" }
        Glide.with(this)
            .load(current.avatar?.large)
            .apply(RequestOptions.circleCropTransform())
            .apply(RequestOptions.placeholderOf(R.drawable.avatar_placeholder))
            .transition(DrawableTransitionOptions.withCrossFade(150))
            .into(binding.profileAvatar)
        binding.profileIdentityTier.visibility = VISIBLE

        if (isCurrentUser(current.id)) {
            TutorialUtil()
                .setContext(this)
                .setFocalColour(R.color.colorGrey600)
                .setTapTarget(KeyUtil.KEY_NOTIFICATION_TIP)
                .setSettings(settings)
                .showTapTarget(
                    R.string.tip_notifications_title,
                    R.string.tip_notifications_text,
                    R.id.action_notification,
                )
        } else {
            TutorialUtil()
                .setContext(this)
                .setFocalColour(R.color.colorGrey600)
                .setTapTarget(KeyUtil.KEY_MESSAGE_TIP)
                .setSettings(settings)
                .showTapTarget(
                    R.string.tip_compose_message_title,
                    R.string.tip_compose_message_text,
                    R.id.action_message,
                )
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.profile_banner -> {
                model?.let { current ->
                    CompatUtil.imagePreview(
                        view,
                        current.bannerImage,
                        R.string.image_preview_error_profile_banner,
                    )
                }
            }
        }
    }

    private fun isCurrentUser(userId: Long, userName: String? = null): Boolean {
        if (!settings.isAuthenticated) return false
        return profileViewModel.isCurrentUser(userId, userName)
    }

    private fun showLoadingState() {
        binding.profileStateOverlay.visibility = VISIBLE
        binding.profileLoadingState.visibility = VISIBLE
        binding.profileErrorState.visibility = GONE
        binding.profileStateOverlay.contentDescription =
            getString(R.string.profile_loading_content_description)
    }

    private fun showContentState() {
        binding.profileStateOverlay.visibility = GONE
    }

    private fun showErrorState(message: String, retryEnabled: Boolean = true) {
        binding.profileStateOverlay.visibility = VISIBLE
        binding.profileLoadingState.visibility = GONE
        binding.profileErrorState.visibility = VISIBLE
        binding.profileErrorText.text = message
        binding.profileErrorRetry.visibility = if (retryEnabled) VISIBLE else GONE
        binding.profileStateOverlay.contentDescription = message
    }
}
