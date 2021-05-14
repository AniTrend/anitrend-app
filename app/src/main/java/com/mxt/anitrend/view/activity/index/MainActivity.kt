package com.mxt.anitrend.view.activity.index

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.DialogAction
import com.google.android.material.navigation.NavigationView
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.index.*
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.custom.view.image.AvatarIndicatorView
import com.mxt.anitrend.base.custom.view.image.HeaderImageView
import com.mxt.anitrend.base.interfaces.event.BottomSheetChoice
import com.mxt.anitrend.extension.*
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.VersionBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.service.DownloaderService
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.activity.base.AboutActivity
import com.mxt.anitrend.view.activity.base.LoggingActivity
import com.mxt.anitrend.view.activity.base.SettingsActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomSheetMessage
import com.ogaclejapan.smarttablayout.SmartTabLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by max on 2017/10/04.
 * Base main_menu activity to show case template
 */

class MainActivity : ActivityBase<User, BasePresenter>(), View.OnClickListener,
    BaseConsumer.onRequestModelChange<User>, NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    lateinit var mToolbar: Toolbar
    @BindView(R.id.page_container)
    lateinit var mViewPager: ViewPager
    @BindView(R.id.smart_tab)
    lateinit var mNavigationTabStrip: SmartTabLayout
    @BindView(R.id.coordinator)
    lateinit var coordinatorLayout: CoordinatorLayout
    @BindView(R.id.drawer_layout)
    lateinit var mDrawerLayout: DrawerLayout
    @BindView(R.id.nav_view)
    lateinit var mNavigationView: NavigationView

    private val mDrawerToggle by lazy(LAZY_MODE_UNSAFE) {
        ActionBarDrawerToggle(this@MainActivity, mDrawerLayout, mToolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
    }

    @IdRes
    private var redirectShortcut: Int = 0
    @IdRes
    private var selectedItem: Int = 0
    @StringRes
    private var selectedTitle: Int = 0

    private var mPageIndex: Int = 0

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
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(mToolbar)
        setPresenter(BasePresenter(applicationContext))
        setViewModel(true)
        if (savedInstanceState == null)
            redirectShortcut = intent.getIntExtra(KeyUtil.arg_redirect, 0)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mNavigationView.itemBackground = getCompatDrawable(R.drawable.nav_background)
        mNavigationView.setNavigationItemSelectedListener(this)
        mViewPager.offscreenPageLimit = offScreenLimit
        mPageIndex = DateUtil.menuSelect
        menuItems = mNavigationView.menu
        onActivityReady()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        mSearchView?.setMenuItem(searchItem)
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
                startActivity(intent)
                return true
            }
            R.id.action_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
            R.id.action_discord -> {
                val invite = getString(R.string.link_anitrend_discord)
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(invite))
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
        if (selectedItem == 0)
            selectedItem = if (presenter.settings.isAuthenticated)
                if (redirectShortcut == 0) presenter.getNavigationItem() else redirectShortcut
            else
                if (redirectShortcut == 0) R.id.nav_anime else redirectShortcut
        mNavigationView.setCheckedItem(selectedItem)
        onNavigate(selectedItem)
        makeRequest()
        requestCurrentUser()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KeyUtil.arg_redirect, redirectShortcut)
        outState.putInt(KeyUtil.key_navigation_selected, selectedItem)
        outState.putInt(KeyUtil.key_navigation_title, selectedTitle)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        redirectShortcut = savedInstanceState.getInt(KeyUtil.arg_redirect)
        selectedItem = savedInstanceState.getInt(KeyUtil.key_navigation_selected)
        selectedTitle = savedInstanceState.getInt(KeyUtil.key_navigation_title)
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
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
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
        updateUI()
        requestCurrentUser()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        @IdRes val menu = item.itemId
        if (selectedItem != menu)
            onNavigate(menu)
        if (menu != R.id.nav_sign_in)
            mDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun onNavigate(@IdRes menu: Int) {
        when (menu) {
            R.id.nav_home_feed -> {
                mToolbar.title = getString(R.string.drawer_title_home)
                selectedItem = menu
                mViewPager.adapter = FeedPageAdapter(supportFragmentManager, applicationContext)
                mNavigationTabStrip.setViewPager(mViewPager)
            }
            R.id.nav_anime -> {
                mToolbar.title = getString(R.string.drawer_title_anime)
                selectedItem = menu
                mViewPager.adapter = SeasonPageAdapter(supportFragmentManager, applicationContext)
                mNavigationTabStrip.setViewPager(mViewPager)
                mViewPager.setCurrentItem(mPageIndex, false)
            }
            R.id.nav_manga -> {
                mToolbar.title = getString(R.string.drawer_title_manga)
                selectedItem = menu
                mViewPager.adapter = MangaPageAdapter(supportFragmentManager, applicationContext)
                mNavigationTabStrip.setViewPager(mViewPager)
            }
            R.id.nav_trending -> {
                mToolbar.title = getString(R.string.drawer_title_trending)
                selectedItem = menu
                mViewPager.adapter =
                    TrendingPageAdapter(supportFragmentManager, applicationContext)
                mNavigationTabStrip.setViewPager(mViewPager)
            }
            R.id.nav_airing -> {
                mToolbar.title = getString(R.string.drawer_title_airing)
                mViewPager.adapter = AiringPageAdapter(supportFragmentManager, applicationContext)
                mNavigationTabStrip.setViewPager(mViewPager)
                selectedItem = menu
            }
            R.id.nav_myanime -> {
                val animeParams = Bundle()
                animeParams.putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                animeParams.putString(KeyUtil.arg_userName, presenter.database.currentUser?.name)
                animeParams.putLong(KeyUtil.arg_id, presenter.database.currentUser?.id ?: 0)

                val animeListPageAdapter =
                    MediaListPageAdapter(supportFragmentManager, applicationContext)
                animeListPageAdapter.params = animeParams

                mToolbar.title = getString(R.string.drawer_title_myanime)
                mViewPager.adapter = animeListPageAdapter
                mNavigationTabStrip.setViewPager(mViewPager)
                selectedItem = menu
            }
            R.id.nav_mymanga -> {
                val mangaParams = Bundle()
                mangaParams.putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                mangaParams.putString(KeyUtil.arg_userName, presenter.database.currentUser?.name)
                mangaParams.putLong(KeyUtil.arg_id, presenter.database.currentUser?.id ?: 0)

                val mangaListPageAdapter =
                    MediaListPageAdapter(supportFragmentManager, applicationContext)
                mangaListPageAdapter.params = mangaParams

                mToolbar.title = getString(R.string.drawer_title_mymanga)
                mViewPager.adapter = mangaListPageAdapter
                mNavigationTabStrip.setViewPager(mViewPager)
                selectedItem = menu
            }
            R.id.nav_hub -> {
                mToolbar.title = getString(R.string.drawer_title_hub)
                mViewPager.adapter = HubPageAdapter(supportFragmentManager, applicationContext)
                mNavigationTabStrip.setViewPager(mViewPager)
                selectedItem = menu
            }
            R.id.nav_reviews -> {
                mToolbar.title = getString(R.string.drawer_title_reviews)
                selectedItem = menu
                mViewPager.adapter = ReviewPageAdapter(supportFragmentManager, applicationContext)
                mNavigationTabStrip.setViewPager(mViewPager)
            }
            R.id.nav_sign_in -> startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            R.id.nav_sign_out -> {
                mBottomSheet = BottomSheetMessage.Builder()
                    .setText(R.string.drawer_signout_text)
                    .setTitle(R.string.drawer_signout_title)
                    .setPositiveText(R.string.Yes)
                    .setNegativeText(R.string.No)
                    .buildWithCallback(object : BottomSheetChoice {
                        override fun onPositiveButton() {
                            WebTokenRequest.invalidateInstance(applicationContext)
                            val intent = Intent(this@MainActivity, SplashActivity::class.java)
                            finish()
                            startActivity(intent)
                        }

                        override fun onNegativeButton() {

                        }
                    })
                showBottomSheet()
            }
            R.id.nav_check_update -> when (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
                PERMISSION_GRANTED -> {
                    mBottomSheet = BottomSheetMessage.Builder()
                        .setText(R.string.drawer_update_text)
                        .setTitle(R.string.drawer_update_title)
                        .setPositiveText(R.string.Yes)
                        .setNegativeText(R.string.No)
                        .buildWithCallback(object : BottomSheetChoice {
                            override fun onPositiveButton() {
                                val versionBase = presenter.database.remoteVersion
                                if (versionBase != null && versionBase.isNewerVersion)
                                    DownloaderService.downloadNewVersion(
                                        this@MainActivity,
                                        versionBase
                                    )
                                else presenter.checkForUpdates()
                            }

                            override fun onNegativeButton() {

                            }
                        })
                    showBottomSheet()
                }
                PERMISSION_DENIED -> if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                    DialogUtil.createMessage(
                        this@MainActivity,
                        R.string.title_permission_write,
                        R.string.text_permission_write
                    ) { _, which ->
                        when (which) {
                            DialogAction.POSITIVE -> ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                REQUEST_PERMISSION
                            )
                            DialogAction.NEGATIVE -> NotifyUtil.makeText(
                                this@MainActivity,
                                R.string.canceled_by_user,
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> {}
                        }
                    }
                else
                    requestPermissionIfMissing(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            else -> { }
        }
    }

    /**
     * Called for each of the requested permissions as they are granted
     *
     * @param permission the current permission granted
     */
    override fun onPermissionGranted(permission: String) {
        super.onPermissionGranted(permission)
        try {
            if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE)
                onNavigate(R.id.nav_check_update)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun updateUI() {
         headerContainer
             .findViewById<View>(R.id.banner_clickable).setOnClickListener(this)

        mHomeFeed = menuItems.findItem(R.id.nav_home_feed)
        mAccountLogin = menuItems.findItem(R.id.nav_sign_in)
        mSignOutProfile = menuItems.findItem(R.id.nav_sign_out)
        mManageMenu = menuItems.findItem(R.id.nav_header_manage)

        if (presenter.settings.isAuthenticated)
            setupUserItems()
        else
            mHeaderView.setImageResource(R.drawable.reg_bg)

        checkNewInstallation()
    }

    override fun makeRequest() {
        WorkManager.getInstance(this)
            .getWorkInfosByTagLiveData(
                KeyUtil.WorkUpdaterId
            ).observe(this) { workInfoList ->
                val workInfo = workInfoList.firstOrNull { workInfo ->
                    workInfo.state == WorkInfo.State.SUCCEEDED
                }
                if (workInfo != null)
                    onUpdateChecked()
        }
    }

    private fun requestCurrentUser() {
        // Sync local current user data with remote
        if (presenter.settings.isAuthenticated) {
            viewModel.params.putParcelable(
                KeyUtil.arg_graph_params,
                GraphUtil.getDefaultQuery(false)
            )
            viewModel.requestData(KeyUtil.USER_CURRENT_REQ, this)
        }
    }

    private fun onLatestUpdateInstalled() {
        NotifyUtil.createAlerter(
            this@MainActivity,
            getString(R.string.title_update_infodadat),
            getString(R.string.app_no_date),
            R.drawable.ic_cloud_done_white_24dp,
            R.color.colorStateGreen
        )
    }

    private fun onUpdateChecked() {
        val remoteVersion = presenter.database.remoteVersion

        if (remoteVersion != null) {
            if (remoteVersion.isNewerVersion) {
                // If a new version of the application is available on GitHub
                val mAppUpdateWidget = menuItems.findItem(R.id.nav_check_update)
                    .actionView.findViewById<TextView>(R.id.app_update_info)
                mAppUpdateWidget.text = getString(R.string.app_update, remoteVersion.version)
                mAppUpdateWidget.visibility = View.VISIBLE
            }
        }
    }

    private fun setupUserItems() {
        presenter.database.currentUser?.apply {
            mUserName.text = name
            mUserAvatar.onInit()
            mHeaderView.setImage(bannerImage)
            if (presenter.settings.shouldShowTipFor(KeyUtil.KEY_LOGIN_TIP)) {
                NotifyUtil.createLoginToast(this@MainActivity, this)
                presenter.settings.disableTipFor(KeyUtil.KEY_LOGIN_TIP)
                mBottomSheet = BottomSheetMessage.Builder()
                    .setText(R.string.login_message)
                    .setTitle(R.string.login_title)
                    .setNegativeText(R.string.Ok)
                    .build()
                showBottomSheet()
            }
            koinOf<ISupportAnalytics>().setCrashAnalyticUser(name)
        }
        mAccountLogin.isVisible = false

        mSignOutProfile.isVisible = true
        mManageMenu.isVisible = true
        mHomeFeed.isVisible = true
    }

    /**
     * Checks to see if this instance is a new installation
     */
    private fun checkNewInstallation() {
        if (presenter.settings.isUpdated) {
            DialogUtil.createChangeLog(this)
            presenter.settings.setUpdated()
        }
        if (presenter.settings.isFreshInstall) {
            presenter.settings.isFreshInstall = false
            mBottomSheet = BottomSheetMessage.Builder()
                .setText(R.string.app_intro_guide)
                .setTitle(R.string.app_intro_title)
                .setNegativeText(R.string.Ok).build()
            showBottomSheet()
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.banner_clickable) {
            if (presenter.settings.isAuthenticated) {
                val user = presenter.database.currentUser
                if (user != null) {
                    startNewActivity<ProfileActivity>(
                        Bundle().apply {
                            putString(KeyUtil.arg_userName, presenter.database.currentUser?.name)
                        }
                    )
                } else
                    NotifyUtil.makeText(
                        applicationContext,
                        R.string.text_error_login,
                        Toast.LENGTH_SHORT
                    ).show()
            } else
                onNavigate(R.id.nav_sign_in)
        }
    }

    override fun onDestroy() {
        mUserAvatar.onViewRecycled()
        super.onDestroy()
    }

    override fun onChanged(model: User?) {
        if (isAlive && model != null) {
            presenter.database.currentUser = model
            updateUI()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    override fun onModelChanged(consumer: BaseConsumer<User>) {
        if (consumer.requestMode == KeyUtil.USER_CURRENT_REQ && consumer.changeModel != null && consumer.changeModel.unreadNotificationCount > 0)
            NotifyUtil.createAlerter(
                this, R.string.alerter_notification_title, R.string.alerter_notification_text,
                R.drawable.ic_notifications_active_white_24dp, R.color.colorAccent
            )
    }
}

