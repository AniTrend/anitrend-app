package com.mxt.anitrend.view.activity.base

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityGiphyPreviewBinding
import com.mxt.anitrend.navigation.extension.putScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.view.activity.CommonActivity

class GiphyPreviewActivity :
    CommonActivity(),
    RequestListener<Drawable> {

    companion object {
        fun newIntent(context: Context, param: GiphyPreviewScreenParam): Intent = Intent(context, GiphyPreviewActivity::class.java).apply {
            putScreenParam(param)
            // Interim boundary: keep the legacy wire key alongside the typed param
            // until all pre-bridge callers and fixtures migrate.
            putExtra(KeyUtil.arg_model, param.url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        /**
         * Compatibility overload preserving the legacy URL-based callers. Bridges
         * into the typed parameter so navigation always uses [GiphyPreviewScreenParam].
         */
        fun newIntent(context: Context, modelUrl: String): Intent = newIntent(context, GiphyPreviewScreenParam(url = modelUrl))

        /**
         * Resolves the typed parameter from the intent.
         *
         * The typed parameter is read first. Pre-bridge callers still write the
         * legacy [KeyUtil.arg_model] extra, so that value is bridged here via
         * [resolve]. The bridge is a scalar conversion point inside the activity,
         * not a parcel path for any remote model.
         */
        fun fromIntent(intent: Intent): GiphyPreviewScreenParam? = resolve(
            typed = intent.screenParam<GiphyPreviewScreenParam>(),
            legacyUrl = intent.getStringExtra(KeyUtil.arg_model),
        )

        /**
         * Production parsing rule for the Giphy preview destination.
         *
         * A present typed parameter wins; it is accepted only when it carries a
         * non-empty url. Otherwise the legacy [KeyUtil.arg_model] extra is bridged
         * via [parseUrl]. Blank strings remain valid, preserving the pre-refactor
         * `isNullOrEmpty` contract.
         */
        @VisibleForTesting
        internal fun resolve(typed: GiphyPreviewScreenParam?, legacyUrl: String?): GiphyPreviewScreenParam? {
            typed?.let { param ->
                return if (param.url.isNotEmpty()) param else null
            }
            return parseUrl(legacyUrl)
        }

        @VisibleForTesting
        internal fun parseUrl(raw: String?): GiphyPreviewScreenParam? = if (!raw.isNullOrEmpty()) GiphyPreviewScreenParam(url = raw) else null
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
        binding.previewClose.setOnClickListener { finish() }

        val args = fromIntent(intent)
        if (args != null) {
            Glide
                .with(this)
                .load(args.url)
                .listener(this)
                .into(binding.previewImage)
        } else {
            NotifyUtil.makeText(
                this,
                R.string.layout_empty_response,
                R.drawable.ic_warning_white_18dp,
                Toast.LENGTH_SHORT,
            ).show()
            showStatus(R.string.layout_empty_response)
        }

        binding.previewCredits.setImageResource(
            if (!CompatUtil.isLightTheme(settings)) {
                R.drawable.powered_by_giphy_light
            } else {
                R.drawable.powered_by_giphy_dark
            },
        )
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Drawable>,
        isFirstResource: Boolean,
    ): Boolean {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            showStatus(R.string.text_unknown_error)
        }
        return false
    }

    override fun onResourceReady(
        resource: Drawable,
        model: Any,
        target: Target<Drawable>,
        dataSource: DataSource,
        isFirstResource: Boolean,
    ): Boolean {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            binding.previewLoadingContainer.isVisible = false
            binding.previewStatusCard.isVisible = false
        }
        return false
    }

    private fun showStatus(messageRes: Int) {
        binding.previewLoadingContainer.isVisible = false
        binding.previewStatusMessage.setText(messageRes)
        binding.previewStatusCard.isVisible = true
    }
}
