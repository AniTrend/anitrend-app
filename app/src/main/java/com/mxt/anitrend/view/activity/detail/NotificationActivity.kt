package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.detail.NotificationFragment

/**
 * Created by max on 2017/10/25.
 */
class NotificationActivity : ActivityBase<Void, BasePresenter>() {
    private lateinit var binding: ActivityFrameGenericBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrameGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mSearchView = binding.customToolbar.searchView
        setSupportActionBar(binding.customToolbar.toolbar)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setPresenter(BasePresenter(this))
        onActivityReady()
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        super.onBackPressed()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        mFragment = NotificationFragment.newInstance()
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
