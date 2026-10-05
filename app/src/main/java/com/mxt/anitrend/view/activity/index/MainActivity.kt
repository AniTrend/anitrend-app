package com.mxt.anitrend.view.activity.index

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withResumed
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationBarView
import com.mxt.anitrend.R
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.base.custom.activity.checkUpdate
import com.mxt.anitrend.base.custom.activity.launchUpdateWorker
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.image.AvatarIndicatorView
import com.mxt.anitrend.base.custom.view.search.MaterialSearchView
import com.mxt.anitrend.base.interfaces.event.BottomSheetChoice
import com.mxt.anitrend.base.interfaces.event.ISearchDelegate
import com.mxt.anitrend.databinding.ActivityMainBinding
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.extension.requestNotificationsPermission
import com.mxt.anitrend.navigation.extension.NavigationArgs
import com.mxt.anitrend.navigation.extension.navigateToAbout
import com.mxt.anitrend.navigation.extension.navigateToChangelog
import com.mxt.anitrend.navigation.extension.navigateToComment
import com.mxt.anitrend.navigation.extension.navigateToCharacter
import com.mxt.anitrend.navigation.extension.navigateToLogging
import com.mxt.anitrend.navigation.extension.navigateToMediaBrowse
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.navigateToMessages
import com.mxt.anitrend.navigation.extension.navigateToNotifications
import com.mxt.anitrend.navigation.extension.navigateToProfile
import com.mxt.anitrend.navigation.extension.navigateToMediaList
import com.mxt.anitrend.navigation.extension.navigateToRootMediaList
import com.mxt.anitrend.navigation.extension.navigateToFavourites
import com.mxt.anitrend.navigation.extension.navigateToAnime
import com.mxt.anitrend.navigation.extension.navigateToAiring
import com.mxt.anitrend.navigation.extension.navigateToHub
import com.mxt.anitrend.navigation.extension.navigateToFeed
import com.mxt.anitrend.navigation.extension.navigateToManga
import com.mxt.anitrend.navigation.extension.navigateToReviews
import com.mxt.anitrend.navigation.extension.navigateToSearch
import com.mxt.anitrend.navigation.extension.navigateToSettings
import com.mxt.anitrend.navigation.extension.navigateToSharedContent
import com.mxt.anitrend.navigation.extension.navigateToStudio
import com.mxt.anitrend.navigation.extension.navigateToStaff
import com.mxt.anitrend.navigation.extension.navigateToTrending
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.fragment.detail.SharedContentFragment
import com.mxt.anitrend.view.fragment.list.MediaListFragment
import com.mxt.anitrend.view.fragment.list.MediaListOrigin
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

class MainActivity : CommonActivity() {
    private lateinit var binding: ActivityMainBinding

    // --- Fields carried over from ActivityBase shell ---
    private var mediaActionUtil: MediaActionUtil? = null

    /** @see ActivityBase.showBottomSheet */
    internal var mBottomSheet: BottomSheetBase<*>? = null

    private val mToolbar by lazy(LazyThreadSafetyMode.NONE) {
        binding.appBarMain.customToolbar.toolbar
    }

    /**
     * Whichever navigation surface the current window class inflated: the
     * compact BottomNavigationView or the w600dp NavigationRailView. Both are
     * driven through one [NavigationBarView] binding path.
     */
    private val primaryNavigation: NavigationBarView?
        get() = binding.root.findViewById(R.id.primary_navigation)

    private val navController
        get() = (supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment).navController

    private var searchView: MaterialSearchView? = null
    private var isClosing = false

    @IdRes
    private var redirectShortcut: Int = 0

    @IdRes
    private var selectedItem: Int = 0

    @StringRes
    private var selectedTitle: Int = 0

    /**
     * External-entry finish semantics (NFR-003). Armed only while the task is
     * rooted on an external ingress intent and the initial ingress route has
     * not been followed by internal navigation. The initial ingress route
     * keeps the flag, so a direct `/anime/1` or `/activity/1` still finishes
     * the external task on back; a subsequent internal navigation (the
     * Profile -> MediaList push of a `/user/<name>/animelist` chain) clears
     * it, and returning to the ingress destination later never re-arms it. A
     * warm intent delivered to the existing task (onNewIntent) never arms it:
     * the task already has a prior destination beneath the delivered route,
     * so back returns there instead of finishing the task.
     */
    @Suppress("CommentOverPrivateProperty") // The NFR-003 entry-state contract is materially useful here.
    private var shouldFinishExternalTaskOnBack: Boolean = false

    private val mainViewModel: MainViewModel by viewModel()

    /** Opens the comment destination for [param]. */
    fun navigateToComment(param: CommentScreenParam) {
        navController.navigateToComment(param)
    }

    /** Toolbar menu currently attached; hosts the account and update action views. */
    private var menuItems: Menu? = null

    /** The update worker observer registers once per activity instance. */
    private var updateWorkerAttached = false

    /** Suppresses re-dispatch when syncing the bar selection from navigation state. */
    private var suppressPrimarySelection = false

    /**
     * Toolbar-owned account avatar (action view of `action_account`). The
     * drawer header used to own this view; its data source and click behavior
     * are unchanged.
     */
    private val mUserAvatar: AvatarIndicatorView?
        get() = menuItems
            ?.findItem(R.id.action_account)
            ?.actionView
            ?.findViewById(R.id.account_avatar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        primaryNavigation?.let(::bindPrimaryNavigation)
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val isSharedContent = destination.id == R.id.sharedContentFragment
            binding.appBarMain.customToolbar.root.isVisible = !isSharedContent
            // The shell owns the navigation surface inset: hiding the bar or
            // rail together with the toolbar keeps the content host full-bleed
            // on the full-screen shared content destination.
            primaryNavigation?.isVisible = !isSharedContent
            // Navigation owns the toolbar title. Graph labels are applied for every destination,
            // including root switches and restored back-stack entries, so a previous title cannot
            // leak into the next root landing.
            mToolbar.title = destination.label
            if (destination.id == R.id.searchFragment) searchView?.closeSearch()
            if (isTopLevelDestination(destination.id)) {
                // Top-level roots have no up action; the single registered
                // back listener stays dormant without the icon.
                mToolbar.navigationIcon = null
            } else {
                // Pushed and detail destinations show the back affordance;
                // its click is owned by the once-registered listener above.
                mToolbar.setNavigationIcon(R.drawable.ic_msv_arrow_back)
            }
            syncPrimarySelection(primaryMenuItemFor(destination.id, arguments))
            invalidateOptionsMenu()
        }
        searchView = binding.appBarMain.customToolbar.searchView
        val searchDelegate = object : ISearchDelegate {
            override fun onQueryChanged(query: String?) {
                applySearchToAllListFragments(query)
            }

            override fun onSearchSubmitted(query: String?) {
                if (!query.isNullOrEmpty()) {
                    navController.navigateToSearch(query)
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
        // Single-owner toolbar back contract: AppCompat's setSupportActionBar
        // wrapper owns the button's click listener, so the production back
        // policy is registered once here; destination changes only swap the
        // navigation icon.
        mToolbar.setNavigationOnClickListener {
            navigateBackFromDestination()
        }

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
            shouldFinishExternalTaskOnBack = resolveExternalEntry(isTaskRoot, intent.action, intent.data != null)
        } else {
            shouldFinishExternalTaskOnBack = savedInstanceState.getBoolean(KEY_EXTERNAL_ENTRY)
        }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Warm delivery: singleTop routes the intent to the existing task, so
        // this is never a cold task-root ingress and must not arm the external
        // finish semantics (NFR-003). The task already has a prior destination
        // beneath the delivered route, so back from the delivered destination
        // returns there instead of finishing the task. Only the onCreate entry
        // path evaluates resolveExternalEntry.
        dispatchExternalIntentRoute(intent)
        shouldFinishExternalTaskOnBack = false
    }

    @Suppress("ComplexCondition") // The menu remains explicit to match the root destination set.
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (navController.currentDestination?.id == R.id.animeFragment ||
            navController.currentDestination?.id == R.id.mangaFragment ||
            navController.currentDestination?.id == R.id.mediaListFragment ||
            navController.currentDestination?.id == R.id.airingFragment ||
            navController.currentDestination?.id == R.id.hubFragment ||
            navController.currentDestination?.id == R.id.feedFragment ||
            navController.currentDestination?.id == R.id.reviewFragment ||
            navController.currentDestination?.id == R.id.trendingFragment
        ) {
            menuInflater.inflate(R.menu.main_menu, menu)
            val searchItem = menu.findItem(R.id.action_search)
            searchView?.setMenuItem(searchItem)
            menuItems = menu
            bindToolbarActionViews(menu)
            applyAccountMenuVisibility(menu)
            if (!updateWorkerAttached) {
                updateWorkerAttached = true
                makeRequest()
            }
        }
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
                navController.navigateToAbout()
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
                navController.navigateToSettings()
                return true
            }
            R.id.action_discord -> {
                val invite = getString(R.string.link_anitrend_discord)
                intent = Intent(Intent.ACTION_VIEW, invite.toUri())
                startActivity(intent)
                return true
            }
            R.id.action_report -> {
                navController.navigateToLogging()
                return true
            }
            R.id.action_account -> {
                onAvatarClicked()
                return true
            }
            R.id.nav_trending,
            R.id.nav_reviews,
            R.id.nav_airing,
            R.id.nav_hub,
            R.id.nav_sign_in,
            R.id.nav_sign_out,
            R.id.nav_check_update,
            -> {
                onNavigate(item.itemId)
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
                        startupNavigationItem()
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
        syncPrimarySelection(selectedItem.takeIf { isPrimaryNavigationItem(it) })
        if (navController.currentDestination?.id == R.id.animeFragment) {
            onNavigate(selectedItem)
        }
        shouldFinishExternalTaskOnBack = externalEntryAfterDispatch(
            shouldFinishExternalTaskOnBack,
            dispatchExternalIntentRoute(intent),
        )
        requestCurrentUser()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KeyUtil.arg_redirect, redirectShortcut)
        outState.putBoolean(KEY_EXTERNAL_ENTRY, shouldFinishExternalTaskOnBack)
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (searchView?.isSearchOpen == true) {
            searchView?.closeSearch()
            return
        }
        if (!isTopLevelDestination(navController.currentDestination?.id)) {
            navigateBackFromDestination()
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
        // The NavController's OnBackPressedDispatcher callback pops the nav
        // stack instead of finishing, so the exit-confirm completes with an
        // explicit finish: a root media list sits above the start
        // destination, and the second back press still has to exit the task.
        finish()
    }

    private fun navigateBackFromDestination() {
        if (shouldFinishExternalTaskOnBack) {
            finish()
        } else {
            navController.navigateUp()
        }
    }

    private fun isTopLevelDestination(@IdRes destinationId: Int?): Boolean = when (destinationId) {
        R.id.animeFragment,
        R.id.mangaFragment,
        R.id.airingFragment,
        R.id.hubFragment,
        R.id.feedFragment,
        R.id.reviewFragment,
        R.id.trendingFragment,
        -> true
        // NFR-002: the media list is top-level only for the root primary
        // producer; pushed producers keep caller-back semantics.
        R.id.mediaListFragment -> isCurrentDestinationRootMediaList()
        else -> false
    }

    /**
     * NFR-002: reads the explicit route-origin contract from the current
     * destination arguments. The legacy ARG_UNIFIED_DESTINATION flag is not the
     * origin contract: it is written for root and pushed routes alike and
     * cannot distinguish them.
     */
    @Suppress("CommentOverPrivateFunction") // The NFR-002 route-origin contract is materially useful here.
    private fun isCurrentDestinationRootMediaList(): Boolean = NavigationArgs.resolveMediaListOrigin(
        navController.currentBackStackEntry?.arguments?.getString(MediaListFragment.ARG_MEDIA_LIST_ORIGIN),
    ) == MediaListOrigin.ROOT

    override fun onPause() {
        super.onPause()
        mediaActionUtil?.onPause(null)
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
        updateUI()
        requestCurrentUser()
    }

    /**
     * One binding path for both navigation surfaces. Item selection routes
     * through the shared production mapping in [onPrimaryNavigationItemSelected];
     * a reselect of the active item is a no-op (the legacy `selectedItem` guard).
     */
    private fun bindPrimaryNavigation(view: NavigationBarView) {
        view.setOnItemSelectedListener { item -> onPrimaryNavigationItemSelected(item.itemId) }
        view.setOnItemReselectedListener { /* Intentionally empty: reselect must not re-dispatch. */ }
    }

    private fun onPrimaryNavigationItemSelected(@IdRes itemId: Int): Boolean {
        if (suppressPrimarySelection) return true
        if (selectedItem != itemId || !isTopLevelDestination(navController.currentDestination?.id)) {
            onNavigate(itemId)
        }
        return true
    }

    /**
     * Applies the bar/rail checked state from navigation state without
     * re-dispatching navigation (guarded by [suppressPrimarySelection]).
     * Destinations without a primary owner clear the selection, leaving the
     * bar unselected until a primary root is entered again.
     */
    private fun syncPrimarySelection(@IdRes itemId: Int?) {
        val view = primaryNavigation ?: return
        suppressPrimarySelection = true
        if (itemId != null && view.menu.findItem(itemId)?.isVisible == true) {
            view.selectedItemId = itemId
        } else {
            for (index in 0 until view.menu.size()) {
                view.menu.getItem(index).isChecked = false
            }
        }
        suppressPrimarySelection = false
    }

    private fun isPrimaryNavigationItem(@IdRes menu: Int): Boolean = when (menu) {
        R.id.nav_home_feed,
        R.id.nav_anime,
        R.id.nav_manga,
        R.id.nav_myanime,
        R.id.nav_mymanga,
        -> true
        else -> false
    }

    /**
     * Maps a destination and its route arguments to the primary menu item that
     * owns it, or null when the destination has no primary owner (secondary
     * roots, pushed media lists, details and settings). A root media list maps
     * by its freshly selected media type (NFR-007); pushed media lists never
     * claim a primary item (NFR-002).
     */
    @VisibleForTesting
    internal fun primaryMenuItemFor(
        @IdRes destinationId: Int,
        mediaListOrigin: String?,
        mediaType: String?,
    ): Int? = when (destinationId) {
        R.id.feedFragment -> R.id.nav_home_feed
        R.id.animeFragment -> R.id.nav_anime
        R.id.mangaFragment -> R.id.nav_manga
        R.id.mediaListFragment -> when {
            NavigationArgs.resolveMediaListOrigin(mediaListOrigin) != MediaListOrigin.ROOT -> null
            mediaType == KeyUtil.ANIME -> R.id.nav_myanime
            mediaType == KeyUtil.MANGA -> R.id.nav_mymanga
            else -> null
        }
        else -> null
    }

    private fun primaryMenuItemFor(@IdRes destinationId: Int, arguments: Bundle?): Int? = primaryMenuItemFor(
        destinationId,
        arguments?.getString(MediaListFragment.ARG_MEDIA_LIST_ORIGIN),
        arguments?.getString(KeyUtil.arg_mediaType),
    )

    @Suppress("ComplexCondition") // Navigation item routing remains explicit and centralized.
    private fun onNavigate(
        @IdRes menu: Int,
    ) {
        if (menu == R.id.nav_home_feed ||
            menu == R.id.nav_anime ||
            menu == R.id.nav_manga ||
            menu == R.id.nav_myanime ||
            menu == R.id.nav_mymanga ||
            menu == R.id.nav_airing ||
            menu == R.id.nav_hub ||
            menu == R.id.nav_reviews ||
            menu == R.id.nav_trending
        ) {
            selectedItem = menu
            when (menu) {
                R.id.nav_home_feed -> navController.navigateToFeed()
                R.id.nav_anime -> navController.navigateToAnime()
                R.id.nav_manga -> navController.navigateToManga()
                R.id.nav_myanime -> navController.navigateToRootMediaList(
                    UserScreenParam(
                        userId = mainViewModel.currentUser()?.id ?: 0L,
                        initialName = mainViewModel.currentUser()?.name,
                    ),
                    KeyUtil.ANIME,
                )
                R.id.nav_mymanga -> navController.navigateToRootMediaList(
                    UserScreenParam(
                        userId = mainViewModel.currentUser()?.id ?: 0L,
                        initialName = mainViewModel.currentUser()?.name,
                    ),
                    KeyUtil.MANGA,
                )
                R.id.nav_airing -> navController.navigateToAiring()
                R.id.nav_hub -> navController.navigateToHub()
                R.id.nav_reviews -> navController.navigateToReviews()
                R.id.nav_trending -> navController.navigateToTrending()
            }
            return
        }
        when (menu) {
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
        val authenticated = settings.isAuthenticated
        menuItems?.let { menu ->
            applyAccountMenuVisibility(menu)
            renderAccountAction(menu)
        }
        primaryNavigation?.menu?.let { items ->
            items.findItem(R.id.nav_myanime)?.isVisible = authenticated
            items.findItem(R.id.nav_mymanga)?.isVisible = authenticated
        }
        if (authenticated) {
            setupUserItems()
        }
        checkUpdatedVersion()
    }

    fun makeRequest() {
        menuItems?.let { launchUpdateWorker(it) }
    }

    /**
     * Wires the toolbar-owned account and update action views. The update
     * action view keeps its flavor-specific data source; clicking it dispatches
     * the same route as the update menu action.
     */
    private fun bindToolbarActionViews(menu: Menu) {
        renderAccountAction(menu)
        menu.findItem(R.id.nav_check_update)
            ?.actionView
            ?.findViewById<View>(R.id.app_update_info)
            ?.setOnClickListener {
                onNavigate(R.id.nav_check_update)
            }
    }

    /** Applies sign-in/sign-out visibility to the current toolbar menu. */
    private fun applyAccountMenuVisibility(menu: Menu) {
        menu.findItem(R.id.nav_sign_in)?.isVisible = !settings.isAuthenticated
        menu.findItem(R.id.nav_sign_out)?.isVisible = settings.isAuthenticated
    }

    /**
     * Renders the current user state into the toolbar account action view.
     * Data source and click behavior are unchanged from the legacy drawer
     * header rendering.
     */
    private fun renderAccountAction(menu: Menu) {
        val avatar = menu
            .findItem(R.id.action_account)
            ?.actionView
            ?.findViewById<AvatarIndicatorView>(R.id.account_avatar)
            ?: return
        avatar.onAvatarClick = ::onAvatarClicked
        val user = if (settings.isAuthenticated) mainViewModel.currentUser() else null
        avatar.render(user?.avatar?.large, user?.unreadNotificationCount ?: 0)
    }

    private fun checkUpdatedVersion() {
        if (settings.isUpdated) {
            navController.navigateToChangelog()
            settings.setUpdated()
        }
    }

    /**
     * Dispatches an ingress intent (deep link, share, or EXTRA_ROUTE shortcut)
     * into the nav graph. Returns the number of navigations dispatched: 0 when
     * nothing was dispatched, 1 for the initial ingress route, and 2 for an
     * ingress followed by internal navigation (the `/user/<name>/animelist`
     * chain). Callers use the count to keep external finish semantics through
     * the initial ingress route and clear them after a follow-up (NFR-003).
     */
    @Suppress("CommentOverPrivateFunction", "LongMethod") // Ingress routing is intentionally centralized.
    private fun dispatchExternalIntentRoute(intent: Intent): Int {
        IntentBundleUtil(intent).checkIntentData(this)
        if (intent.action == Intent.ACTION_SEND) {
            navController.navigateToSharedContent(SharedContentFragment.arguments(intent))
            intent.action = null
            return 1
        }
        return when (intent.getStringExtra(EXTRA_ROUTE)) {
            ROUTE_SETTINGS -> {
                intent.removeExtra(EXTRA_ROUTE)
                navController.navigateToSettings()
                1
            }
            ROUTE_NOTIFICATIONS -> {
                intent.removeExtra(EXTRA_ROUTE)
                navController.navigateToNotifications()
                1
            }
            ROUTE_MESSAGES -> {
                intent.removeExtra(EXTRA_ROUTE)
                navController.navigateToMessages()
                1
            }
            ROUTE_LOGGING -> {
                intent.removeExtra(EXTRA_ROUTE)
                navController.navigateToLogging()
                1
            }
            ROUTE_COMMENT -> {
                val param = intent.extras?.screenParam<CommentScreenParam>()
                    ?: intent.getLongExtra(KeyUtil.arg_id, 0L).takeIf { it > 0L }?.let(::CommentScreenParam)
                intent.removeExtra(EXTRA_ROUTE)
                if (param != null) {
                    navController.navigateToComment(param)
                    1
                } else {
                    0
                }
            }
            ROUTE_STUDIO -> {
                val param = intent.extras?.screenParam<StudioScreenParam>()
                    ?: intent.getLongExtra(KeyUtil.arg_id, 0L).takeIf { it > 0L }?.let(::StudioScreenParam)
                intent.removeExtra(EXTRA_ROUTE)
                if (param != null) {
                    navController.navigateToStudio(param)
                    1
                } else {
                    0
                }
            }
            ROUTE_CHARACTER -> {
                val param = intent.extras?.screenParam<CharacterScreenParam>()
                    ?: intent.getLongExtra(KeyUtil.arg_id, 0L).takeIf { it > 0L }?.let(::CharacterScreenParam)
                intent.removeExtra(EXTRA_ROUTE)
                if (param != null) {
                    navController.navigateToCharacter(param)
                    1
                } else {
                    0
                }
            }
            ROUTE_STAFF -> {
                val param = intent.extras?.screenParam<StaffScreenParam>()
                    ?: intent.getLongExtra(KeyUtil.arg_id, 0L).takeIf { it > 0L }?.let(::StaffScreenParam)
                intent.removeExtra(EXTRA_ROUTE)
                if (param != null) {
                    navController.navigateToStaff(param)
                    1
                } else {
                    0
                }
            }
            ROUTE_PROFILE -> {
                val param = intent.extras?.screenParam<UserScreenParam>()
                    ?: UserScreenParam(
                        userId = intent.getLongExtra(KeyUtil.arg_id, 0L),
                        initialName = intent.getStringExtra(KeyUtil.arg_userName),
                    ).takeIf { it.userId > 0L || !it.initialName.isNullOrBlank() }
                intent.removeExtra(EXTRA_ROUTE)
                if (param != null) {
                    navController.navigateToProfile(param)
                    1
                } else {
                    0
                }
            }
            // NFR-002: ROUTE_MEDIA_LIST (and the media-list shortcuts that
            // carry it) pushes with the default PUSHED origin. The post-login
            // shortcut continuation inherits this contract: nothing in the
            // ingress path re-routes it as root, and back returns to the
            // caller beneath the list.
            ROUTE_MEDIA_LIST -> {
                val param = intent.extras?.screenParam<UserScreenParam>()
                    ?: UserScreenParam(
                        userId = intent.getLongExtra(KeyUtil.arg_id, 0L),
                        initialName = intent.getStringExtra(KeyUtil.arg_userName),
                    ).takeIf { it.userId > 0L || !it.initialName.isNullOrBlank() }
                val mediaType = intent.getStringExtra(KeyUtil.arg_mediaType)
                intent.removeExtra(EXTRA_ROUTE)
                if (param != null) {
                    navController.navigateToMediaList(param, mediaType)
                    1
                } else {
                    0
                }
            }
            ROUTE_FAVOURITES -> {
                val param = intent.extras?.screenParam<UserScreenParam>()
                    ?: UserScreenParam(
                        userId = intent.getLongExtra(KeyUtil.arg_id, 0L),
                        initialName = intent.getStringExtra(KeyUtil.arg_userName),
                    ).takeIf { it.userId > 0L || !it.initialName.isNullOrBlank() }
                intent.removeExtra(EXTRA_ROUTE)
                if (param != null) {
                    navController.navigateToFavourites(param)
                    1
                } else {
                    0
                }
            }
            ROUTE_SEARCH -> {
                val query = intent.getStringExtra(KeyUtil.arg_search)
                intent.removeExtra(EXTRA_ROUTE)
                navController.navigateToSearch(query)
                1
            }
            ROUTE_MEDIA -> {
                val param = intent.extras?.screenParam<MediaScreenParam>()
                    ?: intent.getLongExtra(KeyUtil.arg_id, 0L).takeIf { it > 0L }?.let {
                        MediaScreenParam(it, intent.getStringExtra(KeyUtil.arg_mediaType))
                    }
                intent.removeExtra(EXTRA_ROUTE)
                if (param != null) {
                    navController.navigateToMedia(param)
                    1
                } else {
                    0
                }
            }
            ROUTE_FEED -> {
                intent.removeExtra(EXTRA_ROUTE)
                navController.navigateToFeed()
                1
            }
            ROUTE_AIRING -> {
                intent.removeExtra(EXTRA_ROUTE)
                navController.navigateToAiring()
                1
            }
            ROUTE_TRENDING -> {
                intent.removeExtra(EXTRA_ROUTE)
                navController.navigateToTrending()
                1
            }
            ROUTE_MEDIA_BROWSE -> {
                val arguments = Bundle(intent.extras ?: Bundle.EMPTY).apply {
                    remove(EXTRA_ROUTE)
                }
                intent.removeExtra(EXTRA_ROUTE)
                navController.navigateToMediaBrowse(arguments)
                intent.getStringExtra(KeyUtil.arg_activity_tag)?.let { tag ->
                    mToolbar.title = MarkDownUtil.convert(this, tag)
                }
                1
            }
            null -> dispatchExternalUriRoute(intent)
            else -> 0
        }
    }

    /**
     * URI-path ingress for the manifest deep-link family. Returns the number
     * of navigations dispatched: 1 for a direct destination landing and 2 for
     * the `/user/<name>/animelist|mangalist` chain, whose profile landing is
     * followed by an internal media-list push (NFR-001, NFR-003).
     */
    @Suppress("CommentOverPrivateFunction") // URI ingress semantics are documented at this boundary.
    private fun dispatchExternalUriRoute(intent: Intent): Int {
        if (intent.data?.path?.startsWith("/activity") == true) {
            val activityId = intent.getLongExtra(KeyUtil.arg_id, 0L)
            if (activityId > 0L) {
                navController.navigateToComment(CommentScreenParam(activityId))
                return 1
            }
        } else if (intent.data?.path?.startsWith("/studio") == true) {
            val studioId = intent.getLongExtra(KeyUtil.arg_id, 0L)
            if (studioId > 0L) {
                navController.navigateToStudio(StudioScreenParam(studioId))
                return 1
            }
        } else if (intent.data?.path?.startsWith("/character") == true) {
            val characterId = intent.getLongExtra(KeyUtil.arg_id, 0L)
            if (characterId > 0L) {
                navController.navigateToCharacter(CharacterScreenParam(characterId))
                return 1
            }
        } else if (intent.data?.path?.startsWith("/staff") == true || intent.data?.path?.startsWith("/actor") == true) {
            val staffId = intent.getLongExtra(KeyUtil.arg_id, 0L)
            if (staffId > 0L) {
                navController.navigateToStaff(StaffScreenParam(staffId))
                return 1
            }
        } else if (intent.data?.path?.startsWith("/user") == true) {
            val decision = resolveUserRoute(
                userId = intent.getLongExtra(KeyUtil.arg_id, 0L),
                userName = intent.getStringExtra(KeyUtil.arg_userName),
                mediaType = intent.getStringExtra(KeyUtil.arg_mediaType),
            )
            if (decision != null) {
                val mediaListType = decision.mediaListType
                navController.navigateToProfile(decision.profile)
                mediaListType?.let { mediaType ->
                    navController.navigateToMediaList(decision.profile, mediaType)
                }
                return if (mediaListType == null) 1 else 2
            }
        } else if (intent.data?.path?.startsWith("/anime") == true ||
            intent.data?.path?.startsWith("/manga") == true
        ) {
            val mediaId = intent.getLongExtra(KeyUtil.arg_id, 0L)
            if (mediaId > 0L) {
                navController.navigateToMedia(MediaScreenParam(mediaId, intent.getStringExtra(KeyUtil.arg_mediaType)))
                return 1
            }
        }
        return 0
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

    private fun startupNavigationItem(): Int = when (settings.startupPage) {
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
        val user = mainViewModel.currentUser()
        if (user != null) {
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
            koinOf<ISupportAnalytics>().setCrashAnalyticUser(user.name.orEmpty())
        }
    }

    private fun onAvatarClicked() {
        val user = mainViewModel.currentUser()
        if (!settings.isAuthenticated || user == null) {
            onNavigate(R.id.nav_sign_in)
            return
        }
        if (user.unreadNotificationCount > 0) {
            mUserAvatar?.hideNotificationWidget()
            navController.navigateToNotifications()
        } else {
            navController.navigateToProfile(UserScreenParam(userId = 0L, initialName = user.name))
        }
    }

    override fun onDestroy() {
        mUserAvatar?.onViewRecycled()
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
        private const val KEY_EXTERNAL_ENTRY = "key_external_entry"

        /**
         * The legacy shortcut redirect channel is an int nav-item id targeting a
         * navigation menu item; it is launch state, not entity identity, so it
         * stays a scalar wire value instead of a
         * [com.mxt.anitrend.navigation.model.ScreenParam].
         */
        const val NO_REDIRECT = 0
        const val EXTRA_ROUTE = "extra_main_route"
        const val ROUTE_SETTINGS = "settings"
        const val ROUTE_NOTIFICATIONS = "notifications"
        const val ROUTE_MESSAGES = "messages"
        const val ROUTE_LOGGING = "logging"
        const val ROUTE_COMMENT = "comment"
        const val ROUTE_STUDIO = "studio"
        const val ROUTE_CHARACTER = "character"
        const val ROUTE_STAFF = "staff"
        const val ROUTE_PROFILE = "profile"
        const val ROUTE_MEDIA_LIST = "media_list"
        const val ROUTE_FAVOURITES = "favourites"
        const val ROUTE_SEARCH = "search"
        const val ROUTE_MEDIA = "media"
        const val ROUTE_FEED = "feed"
        const val ROUTE_AIRING = "airing"
        const val ROUTE_TRENDING = "trending"
        const val ROUTE_MEDIA_BROWSE = "media_browse"

        /**
         * Reads the legacy shortcut redirect target from the launch intent.
         *
         * Legacy launcher shortcuts (and pre-update persisted shortcut intents)
         * write [KeyUtil.arg_redirect] with a navigation menu-item id. The value is
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

        /**
         * NFR-001: route decision for the `/user` deep-link family. A plain user
         * link lands on the profile; an `animelist`/`mangalist` link lands on the
         * profile and then pushes a typed media list so Profile stays beneath the
         * list. Unknown media types are ignored so the profile-only landing is
         * preserved for malformed inputs.
         */
        @VisibleForTesting
        internal data class UserRouteDecision(
            val profile: UserScreenParam,
            val mediaListType: String?,
        )

        @VisibleForTesting
        internal fun resolveUserRoute(
            userId: Long,
            userName: String?,
            mediaType: String?,
        ): UserRouteDecision? {
            val profile = UserScreenParam(userId, userName).takeIf {
                it.userId > 0L || !it.initialName.isNullOrBlank()
            } ?: return null
            val listType = mediaType?.takeIf { it == KeyUtil.ANIME || it == KeyUtil.MANGA }
            return UserRouteDecision(profile, listType)
        }

        /**
         * NFR-003: an entry is external only when the task is rooted and the
         * launch intent is a VIEW deep link or an ACTION_SEND share. Kept pure so
         * the ingress detection is unit-testable.
         */
        @VisibleForTesting
        internal fun resolveExternalEntry(
            isTaskRoot: Boolean,
            action: String?,
            hasData: Boolean,
        ): Boolean = isTaskRoot && ((action == Intent.ACTION_VIEW && hasData) || action == Intent.ACTION_SEND)

        /**
         * NFR-003: the initial ingress route preserves external finish
         * semantics, and only a subsequent internal navigation clears them.
         * [dispatchCount] is the number of navigations dispatched by one
         * external-intent pass: 0 keeps the current state, 1 (a direct
         * `/anime/1` or `/activity/1` landing) keeps the flag so back finishes
         * the external task, and 2 (the `/user/<name>/animelist` chain:
         * profile landing followed by the media-list push) clears it. The
         * flag is never re-armed by returning to the ingress destination.
         */
        @VisibleForTesting
        internal fun externalEntryAfterDispatch(
            externalEntry: Boolean,
            dispatchCount: Int,
        ): Boolean = externalEntry && dispatchCount < 2
    }
}
