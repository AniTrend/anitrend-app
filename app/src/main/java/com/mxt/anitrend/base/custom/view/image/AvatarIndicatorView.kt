package com.mxt.anitrend.base.custom.view.image

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.WidgetAvatarIndicatorBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.detail.NotificationActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.activity.index.LoginActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by max on 2017/11/30.
 * avatar image view which will be capable of displaying
 * current notification count.
 */

class AvatarIndicatorView :
    FrameLayout,
    CustomView,
    View.OnClickListener,
    KoinComponent {
    constructor(context: Context) :
        super(context) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet) :
        super(context, attrs) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        onInit()
    }

    private val boxQuery: BoxQuery by inject()
    private val settings: Settings by inject()

    private val currentUser: User?
        get() = boxQuery.currentUser

    private lateinit var binding: WidgetAvatarIndicatorBinding

    override fun onInit() {
        binding = WidgetAvatarIndicatorBinding.inflate(context.getLayoutInflater(), this, true)
        binding.userAvatar.setOnClickListener(this)
        checkLastSyncTime()
    }

    private fun checkLastSyncTime() {
        if (settings.isAuthenticated == true) {
            if (currentUser != null) {
                binding.userAvatar.setImage(currentUser?.avatar)
                if ((currentUser?.unreadNotificationCount ?: 0) > 0) {
                    binding.notificationCount.text = currentUser?.unreadNotificationCount.toString()
                    showNotificationWidget()
                } else {
                    hideNotificationCountWidget()
                }
            } else {
                hideNotificationCountWidget()
            }
        }
    }

    override fun onViewRecycled() = Unit

    private fun showNotificationWidget() {
        binding.notificationCount.visibility = View.VISIBLE
        binding.container.visibility = View.VISIBLE
    }

    private fun hideNotificationCountWidget() {
        binding.notificationCount.visibility = View.GONE
        binding.container.visibility = View.GONE
    }

    override fun onClick(view: View) {
        if (settings.isAuthenticated && currentUser != null) {
            if (view.id == R.id.user_avatar) {
                val intent: Intent
                if ((currentUser?.unreadNotificationCount ?: 0) > 0) {
                    intent = Intent(context, NotificationActivity::class.java)
                    hideNotificationCountWidget()
                } else {
                    intent = Intent(context, ProfileActivity::class.java)
                    intent.putExtra(KeyUtil.arg_userName, currentUser?.name)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        } else {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}
