package com.mxt.anitrend.base.custom.view.image

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.WidgetAvatarIndicatorBinding
import com.mxt.anitrend.extension.getLayoutInflater

/**
 * Render-only avatar and notification indicator.
 *
 * Authentication state, persistence, and navigation belong to the hosting UI.
 */
class AvatarIndicatorView :
    FrameLayout,
    CustomView,
    View.OnClickListener {

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

    private lateinit var binding: WidgetAvatarIndicatorBinding

    var onAvatarClick: (() -> Unit)? = null

    override fun onInit() {
        if (this::binding.isInitialized) return
        binding = WidgetAvatarIndicatorBinding.inflate(context.getLayoutInflater(), this, true)
        binding.userAvatar.setOnClickListener(this)
    }

    fun render(
        avatar: String?,
        unreadNotificationCount: Int,
    ) {
        onInit()
        binding.userAvatar.setImage(avatar)
        if (unreadNotificationCount > 0) {
            binding.notificationCount.text = unreadNotificationCount.toString()
            showNotificationWidget()
        } else {
            hideNotificationCountWidget()
        }
    }

    fun hideNotificationWidget() {
        onInit()
        hideNotificationCountWidget()
    }

    override fun onViewRecycled() {
        onAvatarClick = null
    }

    private fun showNotificationWidget() {
        binding.notificationCount.visibility = View.VISIBLE
        binding.container.visibility = View.VISIBLE
    }

    private fun hideNotificationCountWidget() {
        binding.notificationCount.visibility = View.GONE
        binding.container.visibility = View.GONE
    }

    override fun onClick(view: View) {
        if (view.id == R.id.user_avatar) onAvatarClick?.invoke()
    }
}
