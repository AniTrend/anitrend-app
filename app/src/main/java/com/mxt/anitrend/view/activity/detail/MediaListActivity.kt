package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import android.view.Menu
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.index.MediaListPageAdapter
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/12/14.
 * users anime / manga list impl
 */
class MediaListActivity : ActivityBase<User, BasePresenter>() {
    private lateinit var binding: ActivityPagerGenericBinding

    private lateinit var pageAdapter: MediaListPageAdapter

    private var mediaType: String? = null

    private val bundle: Bundle?
        get() = intent.extras

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagerGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        mediaType = bundle?.getString(KeyUtil.arg_mediaType)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        bundle?.let {
            setTitle(
                if (CompatUtil.equals(mediaType, KeyUtil.ANIME)) {
                    R.string.title_anime_list
                } else {
                    R.string.title_manga_list
                },
            )
        }
        onActivityReady()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.action_settings).isVisible = false
        menu.findItem(R.id.action_extra).isVisible = false
        menu.findItem(R.id.action_share).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        pageAdapter =
            MediaListPageAdapter(this, applicationContext).apply {
                params = bundle ?: Bundle.EMPTY
            }
        updateUI()
    }

    override fun updateUI() {
        binding.contentMain.pageContainer.adapter = pageAdapter
        binding.contentMain.pageContainer.offscreenPageLimit = offScreenLimit + 2
        TabLayoutMediator(binding.customTab.smartTab, binding.contentMain.pageContainer) { tab, position ->
            tab.text = pageAdapter.getPageTitle(position)
        }.attach()
    }

    override fun makeRequest() {
    }
}
