package com.mxt.anitrend.view.activity.base

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.VisibleForTesting
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

    /**
     * Navigation args for [GiphyPreviewActivity]. Use [newIntent] to build and
     * [fromIntent] to read. Wire format key: [KeyUtil.arg_model].
     */
    data class Args(val modelUrl: String)

    companion object {
        /**
         * Builds an [Intent] for [GiphyPreviewActivity] with the image URL as the
         * [KeyUtil.arg_model] extra.
         */
        fun newIntent(context: Context, modelUrl: String): Intent =
            Intent(context, GiphyPreviewActivity::class.java).apply {
                putExtra(KeyUtil.arg_model, modelUrl)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

        /**
         * Reads [Args] from an intent that was built with [newIntent].
         * Returns `null` for missing or empty model URLs (preserving the existing
         * graceful-degrade behaviour).
         */
        fun fromIntent(intent: Intent): Args? = parseArgs(intent.getStringExtra(KeyUtil.arg_model))

        /**
         * Pure parsing helper: converts a raw model URL string to [Args], returning
         * `null` for null / empty input. Extracted so tests can exercise the
         * production parsing logic without needing a real [Intent].
         */
        @VisibleForTesting
        internal fun parseArgs(raw: String?): Args? {
            return if (!raw.isNullOrEmpty()) Args(raw) else null
        }
    }

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
        val args = fromIntent(intent)
        if (args != null) {
            Glide
                .with(this)
                .load(args.modelUrl)
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
