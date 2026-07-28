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
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
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

    private var followers: PageInfo? = null
    private var following: PageInfo? = null
    private var favourites: Int = 0
    private var lastAppliedFollowState: Boolean? = null

    private var bottomSheet: BottomSheetBase<*>? = null
    private var fragmentManager: FragmentManager? = null

    private val placeHolder = ".."

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

            lastSynced = System.currentTimeMillis()
        }
    }

    fun setStats(
        followersTotal: Int?,
        followingTotal: Int?,
        favouritesTotal: Int?,
    ) {
        if (followersTotal != null) {
            binding.userFollowersCount.text =
                WidgetState.valueFormatter(followersTotal)
        }
        if (followingTotal != null) {
            binding.userFollowingCount.text =
                WidgetState.valueFormatter(followingTotal)
        }
        if (favouritesTotal != null) {
            binding.userFavouritesCount.text =
                WidgetState.valueFormatter(favouritesTotal)
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
                if (favourites < 1) {
                    NotifyUtil.makeText(context, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                } else {
                    val intent =
                        Intent(context, FavouriteActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra(KeyUtil.arg_id, userId)
                        }
                    context.startActivity(intent)
                }
            }
            R.id.user_followers_container -> {
                if (followers == null || followers?.total ?: 0 < 1) {
                    NotifyUtil.makeText(context, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                } else {
                    val manager = fragmentManager ?: return
                    bottomSheet =
                        BottomSheetListUsers
                            .Builder()
                            .setUserId(userId)
                            .setModelCount(followers?.total ?: 0)
                            .setRequestType(KeyUtil.USER_FOLLOWERS_REQ)
                            .setTitle(R.string.title_bottom_sheet_followers)
                            .build()
                    bottomSheet?.show(manager, bottomSheet?.tag)
                }
            }
            R.id.user_following_container -> {
                if (following == null || (following?.total ?: 0) < 1) {
                    NotifyUtil.makeText(context, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                } else {
                    val manager = fragmentManager ?: return
                    bottomSheet =
                        BottomSheetListUsers
                            .Builder()
                            .setUserId(userId)
                            .setModelCount(following?.total ?: 0)
                            .setRequestType(KeyUtil.USER_FOLLOWING_REQ)
                            .setTitle(R.string.title_bottom_sheet_following)
                            .build()
                    bottomSheet?.show(manager, bottomSheet?.tag)
                }
            }
        }
    }

    fun setFragmentActivity(activity: FragmentActivity?) {
        fragmentManager = activity?.supportFragmentManager
    }
}
