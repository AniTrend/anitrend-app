package com.mxt.anitrend.view.activity.base

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.setContent
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.mxt.anitrend.R
import com.mxt.anitrend.navigation.extension.putScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.ImagePreviewScreenParam
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.currentAniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.view.activity.CommonActivity
import timber.log.Timber

class ImagePreviewActivity : CommonActivity() {

    companion object {
        fun newIntent(context: Context, param: ImagePreviewScreenParam): Intent = Intent(context, ImagePreviewActivity::class.java).apply {
            putScreenParam(param)
            // Interim boundary: keep the legacy wire key alongside the typed param
            // until all pre-bridge callers and fixtures migrate.
            putExtra(KeyUtil.arg_model, param.url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        /**
         * Compatibility overload preserving the legacy URL-based callers. Bridges
         * into the typed parameter so navigation always uses [ImagePreviewScreenParam].
         */
        fun newIntent(context: Context, modelUrl: String): Intent = newIntent(context, ImagePreviewScreenParam(url = modelUrl))

        /**
         * Resolves the typed parameter from the intent.
         *
         * The typed parameter is read first. Pre-bridge callers still write the
         * legacy [KeyUtil.arg_model] extra, so that value is bridged here via
         * [resolve]. The bridge is a scalar conversion point inside the activity,
         * not a parcel path for any remote model.
         */
        fun fromIntent(intent: Intent): ImagePreviewScreenParam? = resolve(
            typed = intent.screenParam<ImagePreviewScreenParam>(),
            legacyUrl = intent.getStringExtra(KeyUtil.arg_model),
        )

        /**
         * Production parsing rule for the image preview destination.
         *
         * A present typed parameter wins; it is accepted only when it carries a
         * non-empty url. Otherwise the legacy [KeyUtil.arg_model] extra is bridged
         * via [parseUrl]. Blank strings remain valid, preserving the pre-refactor
         * `isNullOrEmpty` contract.
         */
        @VisibleForTesting
        internal fun resolve(typed: ImagePreviewScreenParam?, legacyUrl: String?): ImagePreviewScreenParam? {
            typed?.let { param ->
                return if (param.url.isNotEmpty()) param else null
            }
            return parseUrl(legacyUrl)
        }

        @VisibleForTesting
        internal fun parseUrl(raw: String?): ImagePreviewScreenParam? = if (!raw.isNullOrEmpty()) ImagePreviewScreenParam(url = raw) else null

        private const val REQUEST_PERMISSION = 102
    }

    private var imageUri: String? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
        super.onCreate(savedInstanceState)

        val args = fromIntent(intent)
        if (args != null) {
            imageUri = args.url
        } else {
            NotifyUtil.makeText(
                this,
                R.string.layout_empty_response,
                Toast.LENGTH_SHORT,
            ).show()
        }

        setContent {
            AniTrendTheme(currentAniTrendTheme()) {
                ImagePreviewScreen(
                    imageUri = imageUri,
                    onDownloadClick = { onDownloadRequested() },
                    onShareClick = { shareImage() },
                    onLinkClick = { openLink() },
                )
            }
        }
    }

    private fun onDownloadRequested() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            null
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
        if (permission == null || requestWritePermission(permission)) {
            downloadAttachment()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            DialogUtil.createMessage(
                this,
                R.string.title_permission_write,
                R.string.text_permission_write,
            ) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    REQUEST_PERMISSION,
                )
            }
        }
    }

    private fun shareImage() {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, imageUri)
            }
        startActivity(Intent.createChooser(intent, resources.getText(R.string.image_preview_share)))
    }

    private fun openLink() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(imageUri) }
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
            NotifyUtil.makeText(this, R.string.text_unknown_error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            permissions.isNotEmpty() &&
            permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            downloadAttachment()
        }
    }

    private fun requestWritePermission(permission: String): Boolean = if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
        true
    } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION)
        false
    } else {
        false
    }

    private fun downloadAttachment() {
        val currentUri = imageUri ?: return
        val imageUri = currentUri.toUri()
        val request =
            DownloadManager.Request(imageUri).apply {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imageUri.lastPathSegment)
                }
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager != null) {
            downloadManager.enqueue(request)
            NotifyUtil.createAlerter(
                this,
                R.string.title_download_info,
                R.string.text_download_info,
                R.drawable.ic_cloud_download_white_24dp,
                R.color.colorStateGreen,
                KeyUtil.DURATION_SHORT,
            )
        } else {
            NotifyUtil.createAlerter(
                this,
                R.string.title_download_info,
                R.string.text_unknown_error,
                R.drawable.ic_cloud_download_white_24dp,
                R.color.colorStateRed,
                KeyUtil.DURATION_SHORT,
            )
        }
    }
}

/**
 * Fullscreen zoomable image preview with a control overlay: tapping the image
 * toggles the download/share/link controls, pinch gestures zoom and pan.
 */
@Composable
private fun ImagePreviewScreen(
    imageUri: String?,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    onLinkClick: () -> Unit,
) {
    var controlsVisible by remember { mutableStateOf(true) }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { controlsVisible = !controlsVisible }
            },
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        if (imageUri != null) {
            AniTrendImage(
                model = imageUri,
                contentDescription = null,
                modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val nextScale = (scale * zoom).coerceIn(1f, 6f)
                            scale = nextScale
                            offset = if (nextScale > 1f) offset + pan else Offset.Zero
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
            )
        }
        if (controlsVisible) {
            Row(
                modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(onClick = onDownloadClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cloud_download_white_24dp),
                        contentDescription = stringResource(R.string.image_preview_download),
                        tint = Color.White,
                    )
                }
                IconButton(onClick = onShareClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share_white_24dp),
                        contentDescription = stringResource(R.string.image_preview_share),
                        tint = Color.White,
                    )
                }
                IconButton(onClick = onLinkClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_link_white_24dp),
                        contentDescription = stringResource(R.string.image_preview_link),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
