package com.mxt.anitrend.view.activity.base

import android.os.Bundle
import android.support.v7.widget.Toolbar
import butterknife.ButterKnife
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.view.fragment.detail.AboutFragment

class AboutActivity : ActivityBase<Void, BasePresenter>() {

    private val toolbar by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Toolbar?>(R.id.toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame_generic)
        setSupportActionBar(toolbar)
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
        mFragment = AboutFragment.newInstance()
        updateUI()
    }

    override fun updateUI() {
        if (mFragment != null) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.content_frame, mFragment, mFragment.TAG)
            fragmentTransaction.commit()
        }
    }

    override fun makeRequest() {

    }
}
