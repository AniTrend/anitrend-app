package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.adapter.pager.detail.FavouritePageAdapter
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.presenter.base.BasePresenter

/**
 * Created by max on 2017/12/14.
 */
class FavouriteActivity : ActivityBase<Favourite, BasePresenter>() {
    private lateinit var binding: ActivityPagerGenericBinding

    private lateinit var pageAdapter: FavouritePageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagerGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mSearchView = binding.customToolbar.searchView
        setSupportActionBar(binding.customToolbar.toolbar)
        setPresenter(BasePresenter(this))
        setViewModel(true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        onActivityReady()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        pageAdapter =
            FavouritePageAdapter(this, applicationContext).apply {
                params = intent.extras ?: Bundle.EMPTY
            }
        updateUI()
    }

    override fun updateUI() {
        binding.contentMain.pageContainer.adapter = pageAdapter
        binding.contentMain.pageContainer.offscreenPageLimit = offScreenLimit
        TabLayoutMediator(binding.customTab.smartTab, binding.contentMain.pageContainer) { tab, position ->
            tab.text = pageAdapter.getPageTitle(position)
        }.attach()
    }

    override fun makeRequest() {
    }
}
