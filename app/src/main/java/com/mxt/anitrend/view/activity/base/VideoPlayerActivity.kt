package com.mxt.anitrend.view.activity.base

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityVideoPlayerBinding
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import timber.log.Timber

class VideoPlayerActivity : ActivityBase<Void, BasePresenter>(), View.OnClickListener {

    private var contentLink: String? = null
    private lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.hasExtra(KeyUtil.arg_model)) {
            contentLink = intent.getStringExtra(KeyUtil.arg_model)
            onActivityReady()
        } else {
            NotifyUtil.makeText(
                this,
                R.string.text_error_request,
                R.drawable.ic_warning_white_18dp,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            setImmersive(true)
        setTransparentStatusBar()
    }

    override fun onBackPressed() {
        runCatching {
            binding.videoPlayer.cancelProgressTimer()
        }.onFailure { Timber.e(it) }
        if (Jzvd.backPress()) {
            NotifyUtil.makeText(this, R.string.text_confirm_exit, Toast.LENGTH_SHORT).show()
            return
        }
        super.onBackPressed()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        val link = contentLink ?: return
        val dataSource = JZDataSource(link)
        binding.videoPlayer.setUp(dataSource, Jzvd.SCREEN_FULLSCREEN)
        binding.videoPlayer.fullscreenButton.setImageResource(R.drawable.jz_shrink)
        binding.videoPlayer.fullscreenButton.setOnClickListener(this)
        updateUI()
    }

    override fun updateUI() {
        binding.videoPlayer.startButton.performClick()
    }

    override fun makeRequest() {
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.back,
            R.id.fullscreen -> onBackPressed()
        }
    }
}
