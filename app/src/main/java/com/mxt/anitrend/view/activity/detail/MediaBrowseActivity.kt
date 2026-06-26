package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.view.fragment.list.MediaBrowseFragment

/**
 * Created by max on 2018/01/27.
 * browse activity for rankings, tags and genres.
 */
class MediaBrowseActivity : ActivityBase<MediaBase, MediaPresenter>() {
    private lateinit var binding: ActivityFrameGenericBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrameGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mSearchView = binding.customToolbar.searchView
        setSupportActionBar(binding.customToolbar.toolbar)
        setViewModel(true)
        setPresenter(MediaPresenter(this))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        intent.getStringExtra(KeyUtil.arg_activity_tag)?.let { tag ->
            val activityTitle = MarkDownUtil.convert(this, tag)
            mActionBar?.title = activityTitle
        }
        onActivityReady()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        makeRequest()
    }

    override fun updateUI() {
        mFragment = MediaBrowseFragment.newInstance(intent.extras ?: Bundle.EMPTY)
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        mFragment?.let { fragment ->
            fragmentTransaction.replace(R.id.content_frame, fragment, fragment.TAG)
            fragmentTransaction.commit()
        }
    }

    override fun makeRequest() {
        updateUI()
    }
}
