package com.mxt.anitrend.view.activity.index

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.index.AiringPageAdapter
import com.mxt.anitrend.adapter.pager.index.FeedPageAdapter
import com.mxt.anitrend.adapter.pager.index.HubPageAdapter
import com.mxt.anitrend.adapter.pager.index.MangaPageAdapter
import com.mxt.anitrend.adapter.pager.index.MediaListPageAdapter
import com.mxt.anitrend.adapter.pager.index.ReviewPageAdapter
import com.mxt.anitrend.adapter.pager.index.SeasonPageAdapter
import com.mxt.anitrend.adapter.pager.index.TrendingPageAdapter
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.activity.checkUpdate
import com.mxt.anitrend.base.custom.activity.launchUpdateWorker
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.base.custom.view.image.AvatarIndicatorView
import com.mxt.anitrend.base.custom.view.image.HeaderImageView
import com.mxt.anitrend.base.custom.view.search.MaterialSearchView
import com.mxt.anitrend.base.interfaces.event.BottomSheetChoice
import com.mxt.anitrend.databinding.ActivityMainBinding
import com.mxt.anitrend.extension.LAZY_MODE_UNSAFE
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.extension.requestNotificationsPermission
import com.mxt.anitrend.extension.startNewActivity
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.view.activity.base.AboutActivity
import com.mxt.anitrend.view.activity.base.LoggingActivity
import com.mxt.anitrend.view.activity.base.SettingsActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomSheetMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.Locale

/**
 * Created by max on 2017/10/04.
 * Base main_menu activity to show case template
 */

class MainActivity :
    ActivityBase<User, BasePresenter>(),
    View.OnClickListener,
    BaseConsumer.onRequestModelChange<User>,
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    private val mToolbar by lazy(LazyThreadSafetyMode.NONE) {
        binding.appBarMain.customToolbar.toolbar
    }
    private val mViewPager by lazy(LazyThreadSafetyMode.NONE) {
        binding.appBarMain.contentMain.pageContainer
    }
    private val mNavigationTabStrip by lazy(LazyThreadSafetyMode.NONE) {
        binding.appBarMain.customTab.smartTab
    }
    private val coordinatorLayout by lazy(LazyThreadSafetyMode.NONE) {
        binding.appBarMain.coordinator
    }
    private val mDrawerLayout by lazy(LazyThreadSafetyMode.NONE) {
        binding.drawerLayout
    }
    private val mNavigationView by lazy(LazyThreadSafetyMode.NONE) {
        binding.navView
    }

    private val mDrawerToggle by lazy(LAZY_MODE_UNSAFE) {
        ActionBarDrawerToggle(
            this@MainActivity,
            mDrawerLayout,
            mToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close,
        )
    }

    private var searchView: MaterialSearchView? = null
    private var isClosing = false

    @IdRes
    private var redirectShortcut: Int = 0

    @IdRes
    private var selectedItem: Int = 0

    @StringRes
    private var selectedTitle: Int = 0

    private var mPageIndex: Int = 0

    private var tabMediator: TabLayoutMediator? = null

    private var hasCheckedInstallation = false

    private lateinit var menuItems: Menu

    private lateinit var mHomeFeed: MenuItem
    private lateinit var mAccountLogin: MenuItem
    private lateinit var mSignOutProfile: MenuItem
    private lateinit var mManageMenu: MenuItem

    private val headerContainer by lazy(LAZY_MODE_UNSAFE) {
        mNavigationView.getHeaderView(0)
    }

    private val mHeaderView by lazy(LAZY_MODE_UNSAFE) {
        headerContainer.findViewById<HeaderImageView>(R.id.drawer_banner)
    }
    private val mUserName by lazy(LAZY_MODE_UNSAFE) {
        headerContainer.findViewById<TextView>(R.id.drawer_app_name)
    }
    private val mUserAvatar by lazy(LAZY_MODE_UNSAFE) {
        headerContainer.findViewById<AvatarIndicatorView>(R.id.drawer_avatar_indicator)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        searchView = binding.appBarMain.customToolbar.searchView
        val searchDelegate = object : com.mxt.anitrend.base.interfaces.event.ISearchDelegate {
            override fun onQueryChanged(query: String?) {
                presenter.notifyAllListeners(query?.lowercase(Locale.getDefault()).orEmpty(), false)
            }

            override fun onSearchSubmitted(query: String?) {
                if (!query.isNullOrEmpty()) {
                    val intent = Intent(this@MainActivity, SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra(KeyUtil.arg_search, query)
                    searchView?.let { sv ->
                        CompatUtil.startRevealAnim(this@MainActivity, sv, intent)
                    } ?: startActivity(intent)
                } else {
                    NotifyUtil.makeText(this@MainActivity, R.string.text_search_empty, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onSearchClosed() {
                presenter.notifyAllListeners("", false)
            }
        }
        searchView?.apply {
            setVoiceSearch(false)
            setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchDelegate.onSearchSubmitted(query)
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    searchDelegate.onQueryChanged(newText)
                    return false
                }
            })
            setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
                override fun onSearchViewShown() {
                    searchDelegate.onSearchShown()
                }
                override fun onSearchViewClosed() {
                    searchDelegate.onSearchClosed()
                }
            })
        }
        setSupportActionBar(mToolbar)
        setPresenter(BasePresenter(applicationContext))
        setViewModel(true)
        if (savedInstanceState == null) {
            redirectShortcut = intent.getIntExtra(KeyUtil.arg_redirect, 0)
        }
        mNavigationView.itemBackground = getCompatDrawable(R.drawable.nav_background)
        mNavigationView.setNavigationItemSelectedListener(this)
        mViewPager.offscreenPageLimit = offScreenLimit
        mPageIndex = DateUtil.menuSelect
        menuItems = mNavigationView.menu
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.Main) {
            withResumed {
                requestNotificationsPermission()
            }
        }
        onActivityReady()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView?.setMenuItem(searchItem)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.action_donate -> {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.patreon.com/wax911"))
                startActivity(intent)
                return true
            }
            R.id.action_about -> {
                startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                return true
            }
            R.id.action_share -> {
                intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.campaign_link))
                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
                return true
            }
            R.id.action_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
            R.id.action_discord -> {
                val invite = getString(R.string.link_anitrend_discord)
                intent = Intent(Intent.ACTION_VIEW, invite.toUri())
                startActivity(intent)
                return true
            }
            R.id.action_report -> {
                startActivity(Intent(this@MainActivity, LoggingActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        if (selectedItem == 0) {
            selectedItem =
                if (presenter.settings.isAuthenticated) {
                    if (redirectShortcut == 0) {
                        presenter.getNavigationItem()
                    } else {
                        redirectShortcut
                    }
                } else {
                    if (redirectShortcut == 0) {
                        R.id.nav_anime
                    } else {
                        redirectShortcut
                    }
                }
        }
        mNavigationView.setCheckedItem(selectedItem)
        onNavigate(selectedItem)
        makeRequest()
        requestCurrentUser()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KeyUtil.arg_redirect, redirectShortcut)
        outState.putInt(KeyUtil.key_navigation_selected, selectedItem)
        outState.putInt(KeyUtil.key_navigation_title, selectedTitle)
        val text = searchView?.findViewById<TextView>(R.id.searchTextView)?.text
        if (!text.isNullOrEmpty()) {
            outState.putCharSequence(KEY_SEARCH_VIEW_QUERY, text)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        redirectShortcut = savedInstanceState.getInt(KeyUtil.arg_redirect)
        selectedItem = savedInstanceState.getInt(KeyUtil.key_navigation_selected)
        selectedTitle = savedInstanceState.getInt(KeyUtil.key_navigation_title)
        if (savedInstanceState.containsKey(KEY_SEARCH_VIEW_QUERY)) {
            searchView?.setQuery(savedInstanceState.getCharSequence(KEY_SEARCH_VIEW_QUERY).toString(), false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val searchWord = matches?.firstOrNull()
            if (!searchWord.isNullOrEmpty()) {
                searchView?.setQuery(searchWord, false)
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
            return
        }
        if (searchView?.isSearchOpen == true) {
            searchView?.closeSearch()
            return
        }
        if (!isClosing) {
            NotifyUtil.makeText(
                this,
                R.string.text_confirm_exit,
                R.drawable.ic_home_white_24dp,
                Toast.LENGTH_SHORT,
            ).show()
            isClosing = true
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        mDrawerLayout.removeDrawerListener(mDrawerToggle)
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are *not* resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * [.onResumeFragments].
     */
    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
        updateUI()
        requestCurrentUser()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        @IdRes val menu = item.itemId
        if (selectedItem != menu) {
            onNavigate(menu)
        }
        if (menu != R.id.nav_sign_in) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        }
        return true
    }

    private fun onNavigate(
        @IdRes menu: Int,
    ) {
        when (menu) {
            R.id.nav_home_feed -> {
                mToolbar.title = getString(R.string.drawer_title_home)
                selectedItem = menu
                val adapter = FeedPageAdapter(this, applicationContext)
                mViewPager.adapter = adapter
                attachTabs(adapter)
            }
            R.id.nav_anime -> {
                mToolbar.title = getString(R.string.drawer_title_anime)
                selectedItem = menu
                val adapter = SeasonPageAdapter(this, applicationContext)
                mViewPager.adapter = adapter
                attachTabs(adapter)
                mViewPager.setCurrentItem(mPageIndex, false)
            }
            R.id.nav_manga -> {
                mToolbar.title = getString(R.string.drawer_title_manga)
                selectedItem = menu
                val adapter = MangaPageAdapter(this, applicationContext)
                mViewPager.adapter = adapter
                attachTabs(adapter)
            }
            R.id.nav_trending -> {
                mToolbar.title = getString(R.string.drawer_title_trending)
                selectedItem = menu
                val adapter = TrendingPageAdapter(this, applicationContext)
                mViewPager.adapter = adapter
                attachTabs(adapter)
            }
            R.id.nav_airing -> {
                mToolbar.title = getString(R.string.drawer_title_airing)
                val adapter = AiringPageAdapter(this, applicationContext)
                mViewPager.adapter = adapter
                attachTabs(adapter)
                selectedItem = menu
            }
            R.id.nav_myanime -> {
                val animeParams = Bundle()
                animeParams.putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                animeParams.putString(KeyUtil.arg_userName, presenter.database.currentUser?.name)
                animeParams.putLong(KeyUtil.arg_id, presenter.database.currentUser?.id ?: 0)

                val animeListPageAdapter =
                    MediaListPageAdapter(this, applicationContext)
                animeListPageAdapter.params = animeParams

                mToolbar.title = getString(R.string.drawer_title_myanime)
                mViewPager.adapter = animeListPageAdapter
                attachTabs(animeListPageAdapter)
                selectedItem = menu
            }
            R.id.nav_mymanga -> {
                val mangaParams = Bundle()
                mangaParams.putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                mangaParams.putString(KeyUtil.arg_userName, presenter.database.currentUser?.name)
                mangaParams.putLong(KeyUtil.arg_id, presenter.database.currentUser?.id ?: 0)

                val mangaListPageAdapter =
                    MediaListPageAdapter(this, applicationContext)
                mangaListPageAdapter.params = mangaParams

                mToolbar.title = getString(R.string.drawer_title_mymanga)
                mViewPager.adapter = mangaListPageAdapter
                attachTabs(mangaListPageAdapter)
                selectedItem = menu
            }
            R.id.nav_hub -> {
                mToolbar.title = getString(R.string.drawer_title_hub)
                val adapter = HubPageAdapter(this, applicationContext)
                mViewPager.adapter = adapter
                attachTabs(adapter)
                selectedItem = menu
            }
            R.id.nav_reviews -> {
                mToolbar.title = getString(R.string.drawer_title_reviews)
                selectedItem = menu
                val adapter = ReviewPageAdapter(this, applicationContext)
                mViewPager.adapter = adapter
                attachTabs(adapter)
            }
            R.id.nav_sign_in -> startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            R.id.nav_sign_out -> {
                mBottomSheet =
                    BottomSheetMessage
                        .Builder()
                        .setText(R.string.drawer_signout_text)
                        .setTitle(R.string.drawer_signout_title)
                        .setPositiveText(R.string.Yes)
                        .setNegativeText(R.string.No)
                        .buildWithCallback(
                            object : BottomSheetChoice {
                                override fun onPositiveButton() {
                                    WebTokenRequest.invalidateInstance(applicationContext)
                                    val intent = Intent(this@MainActivity, SplashActivity::class.java)
                                    finish()
                                    startActivity(intent)
                                }

                                override fun onNegativeButton() {
                                }
                            },
                        )
                showBottomSheet()
            }
            R.id.nav_check_update -> checkUpdate()
            else -> { }
        }
    }

    private fun attachTabs(adapter: BaseStatePageAdapter) {
        tabMediator?.detach()
        tabMediator =
            TabLayoutMediator(mNavigationTabStrip, mViewPager) { tab, position ->
                tab.text = adapter.getPageTitle(position)
            }
        tabMediator?.attach()
    }

    /**
     * Called for each of the requested permissions as they are granted
     *
     * @param permission the current permission granted
     */
    override fun onPermissionGranted(permission: String) {
        super.onPermissionGranted(permission)
        try {
            if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                onNavigate(R.id.nav_check_update)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun updateUI() {
        headerContainer
            .findViewById<View>(R.id.banner_clickable)
            .setOnClickListener(this)

        mHomeFeed = menuItems.findItem(R.id.nav_home_feed)
        mAccountLogin = menuItems.findItem(R.id.nav_sign_in)
        mSignOutProfile = menuItems.findItem(R.id.nav_sign_out)
        mManageMenu = menuItems.findItem(R.id.nav_header_manage)

        if (presenter.settings.isAuthenticated) {
            setupUserItems()
        } else {
            mHeaderView.setImageResource(R.drawable.reg_bg)
        }

        checkNewInstallation()
    }

    override fun makeRequest() {
        launchUpdateWorker(menuItems)
    }

    private fun checkNewInstallation() {
        if (hasCheckedInstallation) return
        hasCheckedInstallation = true
        if (presenter.settings.isFreshInstall) {
            presenter.settings.isFreshInstall = false
            mBottomSheet =
                BottomSheetMessage
                    .Builder()
                    .setText(R.string.app_intro_guide)
                    .setTitle(R.string.app_intro_title)
                    .setNegativeText(R.string.Ok)
                    .build()
            showBottomSheet()
            return
        }
        if (presenter.settings.isUpdated) {
            DialogUtil.createChangeLog(this)
            presenter.settings.setUpdated()
        }
    }

    private fun requestCurrentUser() {
        if (presenter.settings.isAuthenticated) {
            presenter.updateUserLastSyncTimeStampIf(intervalInMinutes = 5) {
                viewModel?.params?.apply {
                    putBoolean(KeyUtil.arg_asHtml, false)
                }
                viewModel?.requestData(KeyUtil.USER_CURRENT_REQ, this)
            }
        }
    }

    private fun setupUserItems() {
        presenter.database.currentUser?.apply {
            mUserName.text = name.orEmpty()
            mUserAvatar.onInit()
            mHeaderView.setImage(bannerImage.orEmpty())
            if (presenter.settings.shouldShowTipFor(KeyUtil.KEY_LOGIN_TIP)) {
                NotifyUtil.createLoginToast(this@MainActivity, this)
                presenter.settings.disableTipFor(KeyUtil.KEY_LOGIN_TIP)
                mBottomSheet =
                    BottomSheetMessage
                        .Builder()
                        .setText(R.string.login_message)
                        .setTitle(R.string.login_title)
                        .setNegativeText(R.string.Ok)
                        .build()
                showBottomSheet()
            }
            koinOf<ISupportAnalytics>().setCrashAnalyticUser(name.orEmpty())
        }
        mAccountLogin.isVisible = false

        mSignOutProfile.isVisible = true
        mManageMenu.isVisible = true
        mHomeFeed.isVisible = true
    }

    override fun onClick(view: View) {
        if (view.id == R.id.banner_clickable) {
            if (presenter.settings.isAuthenticated) {
                val user = presenter.database.currentUser
                if (user != null) {
                    startNewActivity<ProfileActivity>(
                        Bundle().apply {
                            putString(KeyUtil.arg_userName, presenter.database.currentUser?.name)
                        },
                    )
                } else {
                    NotifyUtil
                        .makeText(
                            applicationContext,
                            R.string.text_error_login,
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            } else {
                onNavigate(R.id.nav_sign_in)
            }
        }
    }

    override fun onDestroy() {
        mUserAvatar.onViewRecycled()
        super.onDestroy()
    }

    override fun onChanged(model: User?) {
        if (model != null && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            presenter.database.currentUser = model
            updateUI()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    override fun onModelChanged(consumer: BaseConsumer<User>) {
        if (consumer.requestMode == KeyUtil.USER_CURRENT_REQ &&
            consumer.changeModel != null &&
            consumer.changeModel.unreadNotificationCount > 0
        ) {
            NotifyUtil.createAlerter(
                this,
                R.string.notification_alert_title,
                R.string.notification_alert_text,
                R.drawable.ic_notifications_active_white_24dp,
                R.color.colorAccent,
            )
        }
    }

    companion object {
        private const val KEY_SEARCH_VIEW_QUERY = "SEARCHVIEW_QUERY"
    }
}
