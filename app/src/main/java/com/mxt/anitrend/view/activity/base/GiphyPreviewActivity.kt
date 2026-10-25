package com.mxt.anitrend.view.activity.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.navigation.extension.putScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.currentAniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.view.activity.CommonActivity

class GiphyPreviewActivity : CommonActivity() {

    companion object {
        fun newIntent(context: Context, param: GiphyPreviewScreenParam): Intent = Intent(context, GiphyPreviewActivity::class.java).apply { putScreenParam(param) }

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
            intent.screenParam<GiphyPreviewScreenParam>(),
            intent.extras?.getString(KeyUtil.arg_model),
        )

        /**
         * Production parsing rule for the giphy preview destination.
         *
         * A present typed parameter wins; it is accepted only when it carries a
         * non-empty url. Otherwise the legacy [KeyUtil.arg_model] extra is bridged
         * via [parseUrl]. Blank strings remain valid, preserving the pre-refactor
         * `isNullOrEmpty` contract.
         */
        @VisibleForTesting
        internal fun resolve(typed: GiphyPreviewScreenParam?, legacyUrl: String?): GiphyPreviewScreenParam? {
            if (typed != null) return if (typed.url.isNotEmpty()) typed else null
            return parseUrl(legacyUrl)
        }

        @VisibleForTesting
        internal fun parseUrl(raw: String?): GiphyPreviewScreenParam? = if (!raw.isNullOrEmpty()) GiphyPreviewScreenParam(url = raw) else null
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
        super.onCreate(savedInstanceState)

        val args = fromIntent(intent)
        if (args == null) {
            NotifyUtil.makeText(
                this,
                R.string.layout_empty_response,
                Toast.LENGTH_SHORT,
            ).show()
        }

        val creditsRes =
            if (!CompatUtil.isLightTheme(settings)) {
                R.drawable.powered_by_giphy_light
            } else {
                R.drawable.powered_by_giphy_dark
            }

        setContent {
            AniTrendTheme(currentAniTrendTheme()) {
                GiphyPreviewScreen(
                    url = args?.url,
                    creditsRes = creditsRes,
                    onCloseClick = { finish() },
                )
            }
        }
    }
}

/** Fullscreen GIF preview with close affordance and attribution credits. */
@Composable
private fun GiphyPreviewScreen(
    url: String?,
    creditsRes: Int,
    onCloseClick: () -> Unit,
) {
    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (url != null) {
            AniTrendImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(48.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close_grey_600_24dp),
                contentDescription = stringResource(R.string.Close),
                tint = Color.White,
            )
        }
        Image(
            painter = painterResource(creditsRes),
            contentDescription = null,
            modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
        )
    }
}
