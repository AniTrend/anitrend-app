package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.view.fragment.detail.CommentFragment

/**
 * Created by max on 2017/11/15.
 * Comment activity for progress & feeds
 */
class CommentActivity : ActivityBase<FeedList, BasePresenter>() {
    private lateinit var binding: ActivityFrameGenericBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrameGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mSearchBar = binding.customToolbar.searchBar
        setSupportActionBar(binding.customToolbar.toolbar)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setPresenter(BasePresenter(this))
        onActivityReady()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        mFragment = CommentFragment.newInstance(intent.extras ?: Bundle.EMPTY)
        updateUI()
    }

    override fun updateUI() {
        mFragment?.let { fragment ->
            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.content_frame, fragment, fragment.TAG)
            fragmentTransaction.commit()
        }
    }

    override fun makeRequest() {
    }
}
