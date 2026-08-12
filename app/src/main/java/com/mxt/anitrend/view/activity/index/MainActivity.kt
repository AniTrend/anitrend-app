package com.mxt.anitrend.view.activity.index

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.mxt.anitrend.base.custom.activity.checkUpdate
import com.mxt.anitrend.base.custom.activity.launchUpdateWorker
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.image.AvatarIndicatorView
import com.mxt.anitrend.base.custom.view.image.HeaderImageView
import com.mxt.anitrend.base.custom.view.search.MaterialSearchView
import com.mxt.anitrend.base.interfaces.event.BottomSheetChoice
import com.mxt.anitrend.base.interfaces.event.ISearchDelegate
import com.mxt.anitrend.databinding.ActivityMainBinding
import com.mxt.anitrend.extension.LAZY_MODE_UNSAFE
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.extension.requestNotificationsPermission
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.activity.base.AboutActivity
import com.mxt.anitrend.view.activity.base.ChangelogActivity
import com.mxt.anitrend.view.activity.base.LoggingActivity
import com.mxt.anitrend.view.activity.base.SettingsActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomSheetMessage
import com.mxt.anitrend.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Created by max on 2017/10/04.
 * Base main_menu activity to show case template
 */

class MainActivity :
    CommonActivity(),
    View.OnClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    // --- Fields carried over from ActivityBase shell ---
    private var mediaActionUtil: MediaActionUtil? = null

    /** @see ActivityBase.showBottomSheet */
    internal var mBottomSheet: BottomSheetBase<*>? = null

    private val offScreenLimit = 3

    private val mToolbar by lazy(LazyThreadSafetyMode.NONE) {
        binding.appBarMain.customToolbar.toolbar
    }
    private val mViewPager by lazy(LazyThreadSafetyMode.NONE) {
        binding.appBarMain.contentMain.pageContainer
    }
    private val mNavigationTabStrip by lazy(LazyThreadSafetyMode.NONE) {
        binding.appBarMain.customTab.smartTab
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

    private val mainViewModel: MainViewModel by viewModel()

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
        val searchDelegate = object : ISearchDelegate {
            override fun onQueryChanged(query: String?) {
                applySearchToAllListFragments(query)
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
                applySearchToAllListFragments(null)
            }
        }
        searchView?.apply {
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.state.collect { state ->
                    when (state) {
                        is MainViewModel.UiState.Loading -> Unit
                        is MainViewModel.UiState.Success -> {
                            updateUI()
                        }
                        is MainViewModel.UiState.Error -> {
                            Timber.e(state.message, "MainViewModel current user fetch failed")
                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                                NotifyUtil.createAlerter(
                                    this@MainActivity,
                                    getString(R.string.text_error_request),
                                    state.message,
                                    R.drawable.ic_warning_white_18dp,
                                    R.color.colorStateOrange,
                                    KeyUtil.DURATION_MEDIUM,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            redirectShortcut = fromIntent(intent)
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
            R.id.action_support -> {
                intent = Intent(Intent.ACTION_VIEW, getString(R.string.patreon).toUri())
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
                intent.setDataAndType(getString(R.string.campaign_link).toUri(), "text/plain")
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
    fun onActivityReady() {
        if (selectedItem == 0) {
            selectedItem =
                if (settings.isAuthenticated) {
                    if (redirectShortcut == 0) {
                        getNavigationItem()
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

    @Deprecated("Deprecated in Java")
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
        mediaActionUtil?.onPause(null)
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
        mediaActionUtil?.onResume(null)
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
                animeParams.putString(KeyUtil.arg_userName, mainViewModel.currentUser()?.name)
                animeParams.putLong(KeyUtil.arg_id, mainViewModel.currentUser()?.id ?: 0)

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
                mangaParams.putString(KeyUtil.arg_userName, mainViewModel.currentUser()?.name)
                mangaParams.putLong(KeyUtil.arg_id, mainViewModel.currentUser()?.id ?: 0)

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

    private fun applySearchToAllListFragments(query: String?) {
        supportFragmentManager.fragments
            .filterIsInstance<FragmentBaseList<*, *>>()
            .forEach { it.applySearchQuery(query) }
    }

    /**
     * Called for each of the requested permissions as they are granted
     *
     * @param permission the current permission granted
     */
    private fun onPermissionGranted(permission: String) {
        try {
            if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                onNavigate(R.id.nav_check_update)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun updateUI() {
        headerContainer
            .findViewById<View>(R.id.banner_clickable)
            .setOnClickListener(this)

        mHomeFeed = menuItems.findItem(R.id.nav_home_feed)
        mAccountLogin = menuItems.findItem(R.id.nav_sign_in)
        mSignOutProfile = menuItems.findItem(R.id.nav_sign_out)
        mManageMenu = menuItems.findItem(R.id.nav_header_manage)

        if (settings.isAuthenticated) {
            setupUserItems()
        } else {
            mHeaderView.setImageResource(R.drawable.reg_bg)
        }

        checkUpdatedVersion()
    }

    fun makeRequest() {
        launchUpdateWorker(menuItems)
    }

    private fun checkUpdatedVersion() {
        if (settings.isUpdated) {
            val intent = Intent(this, ChangelogActivity::class.java)
            startActivity(intent)
            settings.setUpdated()
        }
    }

    private fun requestCurrentUser() {
        if (settings.isAuthenticated) {
            refreshCurrentUserIfStale {
                mainViewModel.loadCurrentUser()
            }
        }
    }

    private inline fun refreshCurrentUserIfStale(action: () -> Unit) {
        val lastSyncedAt = settings.lastUserSyncTime
        if (DateUtil.timeDifferenceSatisfied(KeyUtil.TIME_UNIT_MINUTES, lastSyncedAt, 5)) {
            action()
            settings.lastUserSyncTime = System.currentTimeMillis()
        }
    }

    private fun getNavigationItem(): Int = when (settings.startupPage) {
        "0" -> R.id.nav_home_feed
        "1" -> R.id.nav_anime
        "2" -> R.id.nav_manga
        "3" -> R.id.nav_trending
        "4" -> R.id.nav_airing
        "5" -> R.id.nav_myanime
        "6" -> R.id.nav_mymanga
        "7" -> R.id.nav_hub
        "8" -> R.id.nav_reviews
        else -> R.id.nav_airing
    }

    private fun setupUserItems() {
        mainViewModel.currentUser()?.apply {
            mUserName.text = name.orEmpty()
            mUserAvatar.onInit()
            mHeaderView.setImage(bannerImage.orEmpty())
            if (settings.shouldShowTipFor(KeyUtil.KEY_LOGIN_TIP)) {
                settings.disableTipFor(KeyUtil.KEY_LOGIN_TIP)
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
            if (settings.isAuthenticated) {
                val user = mainViewModel.currentUser()
                if (user != null) {
                    // Name-only identity preserves the legacy redirect behaviour:
                    // the profile resolves the current user by name, not by id.
                    startActivity(
                        ProfileActivity.newIntent(
                            this,
                            UserScreenParam(userId = 0L, initialName = user.name),
                        ),
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
        searchView?.apply {
            setOnQueryTextListener(null)
            setOnSearchViewListener(null)
        }
        mediaActionUtil?.onDestroy()
        super.onDestroy()
    }

    /** @see ActivityBase.showBottomSheet */
    internal fun showBottomSheet() {
        mBottomSheet?.let { sheet ->
            sheet.show(supportFragmentManager, sheet.tag)
        }
    }

    // endregion

    companion object {
        private const val KEY_SEARCH_VIEW_QUERY = "SEARCH_VIEW_QUERY"

        /**
         * The legacy shortcut redirect channel is an int nav-item id targeting a
         * drawer tab; it is launch state, not entity identity, so it stays a scalar
         * wire value instead of a [com.mxt.anitrend.navigation.model.ScreenParam].
         */
        const val NO_REDIRECT = 0

        /**
         * Reads the legacy shortcut redirect target from the launch intent.
         *
         * Legacy launcher shortcuts (and pre-update persisted shortcut intents)
         * write [KeyUtil.arg_redirect] with a drawer nav-item id. The value is
         * normalized via [resolveRedirect]; the saved-state channel keeps using the
         * same legacy key and is handled separately in onSaveInstanceState/onCreate.
         */
        fun fromIntent(intent: Intent): Int = resolveRedirect(intent.getIntExtra(KeyUtil.arg_redirect, NO_REDIRECT))

        /**
         * Production parsing rule for the shortcut redirect: only positive nav-item
         * ids are valid redirects. Missing (0) or garbage (non-positive) values
         * resolve to [NO_REDIRECT], preserving the pre-refactor default behaviour
         * for absent extras and guarding against invalid nav ids.
         */
        @VisibleForTesting
        internal fun resolveRedirect(rawRedirect: Int): Int = if (rawRedirect > 0) rawRedirect else NO_REDIRECT
    }
}
