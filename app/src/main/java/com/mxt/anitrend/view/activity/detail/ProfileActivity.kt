package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.detail.ProfilePageAdapter
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.custom.view.image.WideImageView
import com.mxt.anitrend.databinding.ActivityProfileBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.TutorialUtil
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import java.util.Locale

/**
 * Created by max on 2017/11/14.
 * Profile activity
 */
class ProfileActivity :
    ActivityBase<UserBase, BasePresenter>(),
    View.OnClickListener {
    private lateinit var binding: ActivityProfileBinding
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPresenter(BasePresenter(applicationContext))
        setSupportActionBar(binding.toolbar.toolbar)
        disableToolbarTitle()
        binding.profileBanner.setOnClickListener(this)
        setViewModel(true)
        if (intent.hasExtra(KeyUtil.arg_id)) {
            id = intent.getLongExtra(KeyUtil.arg_id, -1)
        }
        if (intent.hasExtra(KeyUtil.arg_userName)) {
            userName = intent.getStringExtra(KeyUtil.arg_userName)
        }
        if (intent.hasExtra(KeyUtil.arg_mediaType)) {
            val intent =
                Intent(this, MediaListActivity::class.java).apply {
                    putExtras(this@ProfileActivity.intent.extras ?: Bundle())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            startActivity(intent)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mActionBar?.setHomeAsUpIndicator(
            getCompatDrawable(R.drawable.ic_arrow_back_white_24dp),
        )
        val profilePageAdapter =
            ProfilePageAdapter(this, applicationContext).apply {
                params = intent.extras ?: Bundle.EMPTY
            }
        binding.pageContainer.pageContainer.adapter = profilePageAdapter
        binding.pageContainer.pageContainer.offscreenPageLimit = offScreenLimit
        TabLayoutMediator(binding.smartTab.smartTab, binding.pageContainer.pageContainer) { tab, position ->
            tab.text = profilePageAdapter.getPageTitle(position)
        }.attach()
    }

    override fun onPostResume() {
        super.onPostResume()
        if (getModel() == null) {
            onActivityReady()
        } else {
            updateUI()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        if (!presenter.isCurrentUser(id, userName)) {
            menu.findItem(R.id.action_notification).isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val model = getModel()
        return when (item.itemId) {
            R.id.action_notification -> {
                startActivity(Intent(this@ProfileActivity, NotificationActivity::class.java))
                true
            }
            R.id.action_message -> {
                if (model != null) {
                    if (presenter.isCurrentUser(model.id)) {
                        startActivity(Intent(this@ProfileActivity, MessageActivity::class.java))
                    } else {
                        mBottomSheet =
                            BottomSheetComposer
                                .Builder()
                                .setUserModel(model)
                                .setRequestMode(KeyUtil.MUT_SAVE_MESSAGE_FEED)
                                .setTitle(R.string.text_message_to)
                                .build()
                        mBottomSheet?.let { sheet ->
                            sheet.show(supportFragmentManager, sheet.tag)
                        }
                    }
                } else {
                    NotifyUtil.makeText(this, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_share -> {
                if (model != null) {
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(
                                Intent.EXTRA_TEXT,
                                String.format(Locale.getDefault(), "https://anilist.co/user/%s", model.name),
                            )
                            type = "text/plain"
                        }
                    startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
                } else {
                    NotifyUtil.makeText(this, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        if (id == -1L && userName == null) {
            NotifyUtil.createAlerter(
                this,
                R.string.text_user_model,
                R.string.layout_empty_response,
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateRed,
            )
        } else {
            makeRequest()
        }
    }

    override fun updateUI() {
        binding.profileStatsWidget.setParams(intent.extras ?: Bundle())
        val model = getModel() ?: return
        WideImageView.setImage(binding.profileBanner, model.bannerImage)
        if (presenter.isCurrentUser(model.id)) {
            TutorialUtil()
                .setContext(this)
                .setFocalColour(R.color.colorGrey600)
                .setTapTarget(KeyUtil.KEY_NOTIFICATION_TIP)
                .setSettings(presenter.settings)
                .showTapTarget(
                    R.string.tip_notifications_title,
                    R.string.tip_notifications_text,
                    R.id.action_notification,
                )
        } else {
            TutorialUtil()
                .setContext(this)
                .setFocalColour(R.color.colorGrey600)
                .setTapTarget(KeyUtil.KEY_MESSAGE_TIP)
                .setSettings(presenter.settings)
                .showTapTarget(
                    R.string.tip_compose_message_title,
                    R.string.tip_compose_message_text,
                    R.id.action_message,
                )
        }

        presenter.notifyAllListeners(BaseConsumer(KeyUtil.USER_BASE_REQ, model), false)
    }

    override fun makeRequest() {
        viewModel?.params?.apply {
            putString(KeyUtil.arg_userName, userName)
            if (id > 0) {
                putLong(KeyUtil.arg_id, id)
            } else {
                remove(KeyUtil.arg_id)
            }
        }
        viewModel?.requestData(KeyUtil.USER_BASE_REQ, applicationContext)
    }

    override fun onChanged(model: UserBase?) {
        super.onChanged(model)
        if (model != null) {
            id = model.id
            updateUI()
        } else {
            NotifyUtil.createAlerter(
                this,
                R.string.text_user_model,
                R.string.layout_empty_response,
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateRed,
            )
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.profile_banner -> {
                val model = getModel()
                if (model != null) {
                    CompatUtil.imagePreview(
                        view,
                        model.bannerImage,
                        R.string.image_preview_error_profile_banner,
                    )
                }
            }
        }
    }
}
