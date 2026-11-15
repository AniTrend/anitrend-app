package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.profile.AboutPanelStats
import com.mxt.anitrend.ui.profile.AboutPanelStatsRow
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.WidgetState
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.view.sheet.BottomSheetListUsers
import com.mxt.anitrend.navigation.model.UserScreenParam

/**
 * Created by max on 2017/11/27.
 * following, followers & favourites
 */
class AboutPanelWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView {

    private var userId: Long = 0L

    private var lastSynced: Long = 0L

    private var followers: StatState = StatState.NotLoaded
    private var following: StatState = StatState.NotLoaded
    private var favourites: StatState = StatState.NotLoaded

    var onFavouritesRequested: ((Long) -> Unit)? = null
    var onUserRequested: ((UserScreenParam) -> Unit)? = null

    private var bottomSheet: BottomSheetBase<*>? = null
    private var fragmentManager: FragmentManager? = null

    private val placeHolder = ".."
    private var followingCount by mutableStateOf(placeHolder)
    private var followersCount by mutableStateOf(placeHolder)
    private var favouritesCount by mutableStateOf(placeHolder)

    /**
     * Loaded-state contract for a profile stat container (Favourite, Following, Followers).
     *
     * The widget must distinguish not-yet-loaded, failed, and loaded counts so a legitimate
     * zero count never falls back to the loading toast: loaded counts (including zero) open
     * the normal destination or list sheet, while not-yet-loaded and failed counts keep the
     * loading toast.
     */
    sealed interface StatState {
        data object NotLoaded : StatState
        data object Failed : StatState
        data class Loaded(val total: Int) : StatState
    }

    /**
     * Resolves the click behavior of a profile stat container from its loaded state.
     * Loaded counts (including zero) open the destination or list sheet; not-yet-loaded
     * and failed counts keep the loading toast.
     */
    enum class StatClickAction {
        Open,
        ShowLoading,
    }

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        val composeView =
            ComposeView(context).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
                setContent {
                    AniTrendTheme {
                        AboutPanelStatsRow(
                            stats =
                            AboutPanelStats(
                                followingCount = followingCount,
                                followersCount = followersCount,
                                favouritesCount = favouritesCount,
                            ),
                            onFollowingClick = ::onFollowingClick,
                            onFollowersClick = ::onFollowersClick,
                            onFavouritesClick = ::onFavouritesClick,
                        )
                    }
                }
            }
        addView(
            composeView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
    }

    fun setUserId(userId: Long) {
        this.userId = userId

        if (DateUtil.timeDifferenceSatisfied(KeyUtil.TIME_UNIT_MINUTES, lastSynced, 5)) {
            followersCount = placeHolder
            followingCount = placeHolder
            favouritesCount = placeHolder
            followers = StatState.NotLoaded
            following = StatState.NotLoaded
            favourites = StatState.NotLoaded

            lastSynced = System.currentTimeMillis()
        }
    }

    fun setStats(
        followers: StatState,
        following: StatState,
        favourites: StatState,
    ) {
        this.followers = followers
        this.following = following
        this.favourites = favourites
        (followers as? StatState.Loaded)?.let { state ->
            followersCount = WidgetState.valueFormatter(state.total)
        }
        (following as? StatState.Loaded)?.let { state ->
            followingCount = WidgetState.valueFormatter(state.total)
        }
        (favourites as? StatState.Loaded)?.let { state ->
            favouritesCount = WidgetState.valueFormatter(state.total)
        }
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        fragmentManager = null
        bottomSheet = null
        onFavouritesRequested = null
        onUserRequested = null
    }

    private fun onFavouritesClick() {
        handleStatClick(favourites) { onFavouritesRequested?.invoke(userId) }
    }

    private fun onFollowersClick() {
        handleStatClick(followers) {
            showUserList(
                state = followers as StatState.Loaded,
                requestType = KeyUtil.USER_FOLLOWERS_REQ,
                title = R.string.title_bottom_sheet_followers,
            )
        }
    }

    private fun onFollowingClick() {
        handleStatClick(following) {
            showUserList(
                state = following as StatState.Loaded,
                requestType = KeyUtil.USER_FOLLOWING_REQ,
                title = R.string.title_bottom_sheet_following,
            )
        }
    }

    private fun handleStatClick(
        state: StatState,
        onOpen: () -> Unit,
    ) {
        when (state.resolveStatClick()) {
            StatClickAction.Open -> onOpen()
            StatClickAction.ShowLoading ->
                NotifyUtil.makeText(context, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUserList(
        state: StatState.Loaded,
        requestType: Int,
        title: Int,
    ) {
        val manager = fragmentManager ?: return
        bottomSheet =
            BottomSheetListUsers
                .Builder()
                .setUserId(userId)
                .setModelCount(state.total)
                .setRequestType(requestType)
                .setOnUserClick { param -> onUserRequested?.invoke(param) }
                .setTitle(title)
                .build()
        bottomSheet?.show(manager, bottomSheet?.tag)
    }

    fun setFragmentActivity(activity: FragmentActivity?) {
        fragmentManager = activity?.supportFragmentManager
    }
}

/**
 * Resolves the click behavior of a profile stat container from its loaded state.
 * Loaded counts (including zero) open the normal destination or list sheet;
 * not-yet-loaded and failed counts keep the loading toast.
 */
internal fun AboutPanelWidget.StatState.resolveStatClick(): AboutPanelWidget.StatClickAction = if (this is AboutPanelWidget.StatState.Loaded) {
    AboutPanelWidget.StatClickAction.Open
} else {
    AboutPanelWidget.StatClickAction.ShowLoading
}
