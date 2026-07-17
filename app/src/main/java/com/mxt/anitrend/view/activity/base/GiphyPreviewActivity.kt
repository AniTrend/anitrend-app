package com.mxt.anitrend.view.activity.base

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityGiphyPreviewBinding
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil

/**
 * Created by max on 2017/12/22.
 * giphy preview activity
 */
class GiphyPreviewActivity :
    ActivityBase<Void, BasePresenter>(),
    RequestListener<Drawable> {
    private lateinit var binding: ActivityGiphyPreviewBinding

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
        super.onCreate(savedInstanceState)
        binding = ActivityGiphyPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPresenter(BasePresenter(this))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val modelUrl = intent.getStringExtra(KeyUtil.arg_model)
        if (!modelUrl.isNullOrEmpty()) {
            Glide
                .with(this)
                .load(modelUrl)
                .listener(this)
                .into(binding.previewImage)
        } else {
            NotifyUtil
                .makeText(
                    this,
                    R.string.layout_empty_response,
                    R.drawable.ic_warning_white_18dp,
                    Toast.LENGTH_SHORT,
                ).show()
        }
        onActivityReady()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        binding.previewCredits.setImageResource(
            if (!CompatUtil.isLightTheme(presenter.settings)) {
                R.drawable.powered_by_giphy_light
            } else {
                R.drawable.powered_by_giphy_dark
            },
        )
        updateUI()
    }

    override fun updateUI() {
    }

    override fun makeRequest() {
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Drawable>,
        isFirstResource: Boolean,
    ): Boolean = false

    override fun onResourceReady(
        resource: Drawable,
        model: Any,
        target: Target<Drawable>,
        dataSource: DataSource,
        isFirstResource: Boolean,
    ): Boolean {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            binding.previewProgress.visibility = View.GONE
        }
        return false
    }
}
