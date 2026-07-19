package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.adapter.pager.detail.MessagePageAdapter
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/12/07.
 * MessageActivity
 */
class MessageActivity : ActivityBase<FeedList, BasePresenter>() {
    private lateinit var binding: ActivityPagerGenericBinding

    private lateinit var messagePageAdapter: MessagePageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagerGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        setPresenter(BasePresenter(this))
        setViewModel(true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        presenter.database.currentUser?.id?.let { userId ->
            viewModel?.params?.putLong(KeyUtil.arg_userId, userId)
        }
        onActivityReady()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        messagePageAdapter =
            MessagePageAdapter(this, applicationContext).apply {
                params = viewModel?.params ?: Bundle.EMPTY
            }
        updateUI()
    }

    override fun updateUI() {
        binding.contentMain.pageContainer.adapter = messagePageAdapter
        binding.contentMain.pageContainer.offscreenPageLimit = offScreenLimit
        TabLayoutMediator(binding.customTab.smartTab, binding.contentMain.pageContainer) { tab, position ->
            tab.text = messagePageAdapter.getPageTitle(position)
        }.attach()
    }

    override fun makeRequest() {
    }
}
