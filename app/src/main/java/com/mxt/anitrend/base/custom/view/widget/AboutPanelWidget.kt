package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetProfileAboutPanelBinding
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.WidgetState
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.view.activity.detail.FavouriteActivity
import com.mxt.anitrend.view.sheet.BottomSheetListUsers

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
    CustomView,
    View.OnClickListener {

    private lateinit var binding: WidgetProfileAboutPanelBinding
    private var lifecycle: Lifecycle? = null
    private var userId: Long = 0L
    private val tag = AboutPanelWidget::class.java.simpleName

    private var lastSynced: Long = 0L

    private var followers: StatState = StatState.NotLoaded
    private var following: StatState = StatState.NotLoaded
    private var favourites: StatState = StatState.NotLoaded
    private var lastAppliedFollowState: Boolean? = null

    private var bottomSheet: BottomSheetBase<*>? = null
    private var fragmentManager: FragmentManager? = null

    private val placeHolder = ".."

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
        binding = WidgetProfileAboutPanelBinding.inflate(LayoutInflater.from(context), this, true)
        binding.userFollowingContainer.setOnClickListener(this)
        binding.userFollowersContainer.setOnClickListener(this)
        binding.userFavouritesContainer.setOnClickListener(this)
    }

    fun setUserId(
        userId: Long,
        lifecycle: Lifecycle,
    ) {
        this.userId = userId
        this.lifecycle = lifecycle

        if (DateUtil.timeDifferenceSatisfied(KeyUtil.TIME_UNIT_MINUTES, lastSynced, 5)) {
            binding.userFavouritesCount.text = placeHolder
            binding.userFollowersCount.text = placeHolder
            binding.userFollowingCount.text = placeHolder
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
            binding.userFollowersCount.text = WidgetState.valueFormatter(state.total)
        }
        (following as? StatState.Loaded)?.let { state ->
            binding.userFollowingCount.text = WidgetState.valueFormatter(state.total)
        }
        (favourites as? StatState.Loaded)?.let { state ->
            binding.userFavouritesCount.text = WidgetState.valueFormatter(state.total)
        }
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        fragmentManager = null
        bottomSheet = null
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.user_favourites_container -> {
                if (favourites.resolveStatClick() == StatClickAction.Open) {
                    val intent =
                        Intent(context, FavouriteActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra(KeyUtil.arg_id, userId)
                        }
                    context.startActivity(intent)
                } else {
                    NotifyUtil.makeText(context, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.user_followers_container -> {
                if (followers.resolveStatClick() == StatClickAction.Open) {
                    val loaded = followers as StatState.Loaded
                    val manager = fragmentManager ?: return
                    bottomSheet =
                        BottomSheetListUsers
                            .Builder()
                            .setUserId(userId)
                            .setModelCount(loaded.total)
                            .setRequestType(KeyUtil.USER_FOLLOWERS_REQ)
                            .setTitle(R.string.title_bottom_sheet_followers)
                            .build()
                    bottomSheet?.show(manager, bottomSheet?.tag)
                } else {
                    NotifyUtil.makeText(context, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.user_following_container -> {
                if (following.resolveStatClick() == StatClickAction.Open) {
                    val loaded = following as StatState.Loaded
                    val manager = fragmentManager ?: return
                    bottomSheet =
                        BottomSheetListUsers
                            .Builder()
                            .setUserId(userId)
                            .setModelCount(loaded.total)
                            .setRequestType(KeyUtil.USER_FOLLOWING_REQ)
                            .setTitle(R.string.title_bottom_sheet_following)
                            .build()
                    bottomSheet?.show(manager, bottomSheet?.tag)
                } else {
                    NotifyUtil.makeText(context, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                }
            }
        }
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
