package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.detail.ProfilePageAdapter
import com.mxt.anitrend.base.custom.view.image.WideImageView
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.databinding.ActivityProfileBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.TutorialUtil
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Created by max on 2017/11/14.
 * Profile activity
 */
class ProfileActivity :
    AppCompatActivity(),
    View.OnClickListener {

    private lateinit var binding: ActivityProfileBinding

    private var model: UserBase? = null
    private var userId: Long = 0
    private var userName: String? = null
    private var mBottomSheet: com.mxt.anitrend.base.custom.sheet.BottomSheetBase<*>? = null

    private val profileViewModel: ProfileViewModel by viewModel()

    private val settings: Settings by inject()
    private val boxQuery: BoxQuery by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Preserve configured theme (was previously handled by ActivityBase.configureActivity).
        val themeRes = when (settings.theme) {
            KeyUtil.THEME_DARK -> R.style.AppThemeDark
            KeyUtil.THEME_BLACK -> R.style.AppThemeBlack
            else -> R.style.AppThemeLight
        }
        setTheme(themeRes)
        super.onCreate(savedInstanceState)

        // Process deep links (e.g. anilist.co/user/{name}) so arg_userName/arg_id
        // is injected into the intent before we read it. Previously handled by
        // ActivityBase.onCreate -> IntentBundleUtil.checkIntentData.
        IntentBundleUtil(intent).checkIntentData(this)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(
            getCompatDrawable(R.drawable.ic_arrow_back_white_24dp),
        )
        binding.profileBanner.setOnClickListener(this)

        if (intent.hasExtra(KeyUtil.arg_id)) {
            userId = intent.getLongExtra(KeyUtil.arg_id, -1)
        }
        if (intent.hasExtra(KeyUtil.arg_userName)) {
            userName = intent.getStringExtra(KeyUtil.arg_userName)
        }
        if (intent.hasExtra(KeyUtil.arg_mediaType)) {
            val intent =
                Intent(this, MediaListActivity::class.java).apply {
                    putExtras(this@ProfileActivity.intent.extras ?: Bundle())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            startActivity(intent)
        }

        observeViewModel()
        setUpPager()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.state.collect { state ->
                    when (state) {
                        is ProfileViewModel.UiState.Loading -> { /* content loads below */ }
                        is ProfileViewModel.UiState.Success -> {
                            model = state.user
                            updateUI()
                        }
                        is ProfileViewModel.UiState.Error -> {
                            NotifyUtil.makeText(
                                this@ProfileActivity,
                                state.message,
                                R.drawable.ic_warning_white_18dp,
                                Toast.LENGTH_LONG,
                            ).show()
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
        if (!isCurrentUser(userId, userName)) {
            menu.findItem(R.id.action_notification).isVisible = false
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (userId == -1L && userName == null) {
            NotifyUtil.createAlerter(
                this,
                R.string.text_user_model,
                R.string.layout_empty_response,
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateRed,
            )
        } else {
            profileViewModel.load(userId, userName)
        }
    }

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
        val currentUser = boxQuery.currentUser ?: return false
        return if (userName != null) {
            currentUser.name == userName
        } else {
            userId != 0L && currentUser.id == userId
        }
    }
}
