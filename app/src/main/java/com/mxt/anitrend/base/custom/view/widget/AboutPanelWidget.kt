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
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetProfileAboutPanelBinding
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.view.activity.detail.FavouriteActivity
import com.mxt.anitrend.view.sheet.BottomSheetListUsers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

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
    View.OnClickListener,
    BaseConsumer.onRequestModelChange<UserBase> {
    private lateinit var binding: WidgetProfileAboutPanelBinding
    private var lifecycle: Lifecycle? = null
    private var userId: Long = 0L
    private val tag = AboutPanelWidget::class.java.simpleName

    private var lastSynced: Long = 0L

    private var followers: PageInfo? = null
    private var following: PageInfo? = null
    private var favourites: Int = 0
    private var lastAppliedFollowState: Boolean? = null

    private var usersPresenter: WidgetPresenter<PageContainer<UserBase>>? = null
    private var favouritePresenter: WidgetPresenter<ConnectionContainer<Favourite>>? = null

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
            requestFavourites()
            requestFollowers()
            requestFollowing()
        }
    }

    private fun requestFollowers() {
        val presenter = WidgetPresenter<PageContainer<UserBase>>(context)
        presenter.params.apply {
            putLong(KeyUtil.arg_id, userId)
            putInt(KeyUtil.arg_page_limit, 1)
        }
        presenter.requestData(
            KeyUtil.USER_FOLLOWERS_REQ,
            context,
            object : RetroCallback<PageContainer<UserBase>> {
                override fun onResponse(
                    call: Call<PageContainer<UserBase>>,
                    response: Response<PageContainer<UserBase>>,
                ) {
                    if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != true) {
                        return
                    }
                    val pageContainer = response.body()
                    if (response.isSuccessful && pageContainer?.hasPageInfo() == true) {
                        followers = pageContainer.pageInfo
                        binding.userFollowersCount.text =
                            WidgetPresenter.valueFormatter(pageContainer.pageInfo.total)
                    }
                }

                override fun onFailure(
                    call: Call<PageContainer<UserBase>>,
                    throwable: Throwable,
                ) {
                    if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) {
                        Timber.e(throwable)
                    }
                }
            },
        )
        usersPresenter = presenter
    }

    private fun requestFollowing() {
        val presenter = WidgetPresenter<PageContainer<UserBase>>(context)
        presenter.params.apply {
            putLong(KeyUtil.arg_id, userId)
            putInt(KeyUtil.arg_page_limit, 1)
        }
        presenter.requestData(
            KeyUtil.USER_FOLLOWING_REQ,
            context,
            object : RetroCallback<PageContainer<UserBase>> {
                override fun onResponse(
                    call: Call<PageContainer<UserBase>>,
                    response: Response<PageContainer<UserBase>>,
                ) {
                    if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != true) {
                        return
                    }
                    val pageContainer = response.body()
                    if (response.isSuccessful && pageContainer?.hasPageInfo() == true) {
                        following = pageContainer.pageInfo
                        binding.userFollowingCount.text =
                            WidgetPresenter.valueFormatter(pageContainer.pageInfo.total)
                    }
                }

                override fun onFailure(
                    call: Call<PageContainer<UserBase>>,
                    throwable: Throwable,
                ) {
                    if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) {
                        Timber.tag(tag).e(throwable)
                    }
                }
            },
        )
        usersPresenter = presenter
    }

    private fun requestFavourites() {
        val presenter = WidgetPresenter<ConnectionContainer<Favourite>>(context)
        presenter.params.apply {
            putLong(KeyUtil.arg_id, userId)
            putInt(KeyUtil.arg_page_limit, 1)
        }
        presenter.requestData(
            KeyUtil.USER_FAVOURITES_COUNT_REQ,
            context,
            object : RetroCallback<ConnectionContainer<Favourite>> {
                override fun onResponse(
                    call: Call<ConnectionContainer<Favourite>>,
                    response: Response<ConnectionContainer<Favourite>>,
                ) {
                    if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != true) {
                        return
                    }
                    val connection = response.body()?.connection
                    if (response.isSuccessful && connection != null) {
                        favourites =
                            listOfNotNull(
                                connection.anime?.pageInfo?.total,
                                connection.manga?.pageInfo?.total,
                                connection.characters?.pageInfo?.total,
                                connection.staff?.pageInfo?.total,
                                connection.studios?.pageInfo?.total,
                            ).sum()
                        binding.userFavouritesCount.text =
                            WidgetPresenter.valueFormatter(favourites)
                    }
                }

                override fun onFailure(
                    call: Call<ConnectionContainer<Favourite>>,
                    throwable: Throwable,
                ) {
                    if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) {
                        Timber.e(throwable)
                    }
                }
            },
        )
        favouritePresenter = presenter
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        favouritePresenter?.onDestroy()
        favouritePresenter = null
        usersPresenter?.onDestroy()
        usersPresenter = null
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
                if (following == null || following?.total ?: 0 < 1) {
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

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    override fun onModelChanged(consumer: BaseConsumer<UserBase>) {
        if (consumer.requestMode == KeyUtil.MUT_TOGGLE_FOLLOW) {
            val totalInfo = followers
            val isStarted = lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true
            if (totalInfo != null) {
                val isFollowing = consumer.changeModel?.isFollowing == true
                if (lastAppliedFollowState == isFollowing) {
                    return
                }
                totalInfo.total = totalInfo.total + if (isFollowing) 1 else -1
                lastAppliedFollowState = isFollowing
                if (isStarted) {
                    binding.userFollowersCount.text =
                        WidgetPresenter.valueFormatter(totalInfo.total)
                }
            } else if (isStarted) {
                requestFollowing()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDetachedFromWindow()
    }
}
