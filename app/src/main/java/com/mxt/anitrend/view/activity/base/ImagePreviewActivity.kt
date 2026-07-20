package com.mxt.anitrend.view.activity.base

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityImagePreviewBinding
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import timber.log.Timber

/**
 * Created by max on 2017/11/14.
 * ImagePreviewActivity
 */
class ImagePreviewActivity : ActivityBase<Void, BasePresenter>() {

    /**
     * Navigation args for [ImagePreviewActivity]. Use [newIntent] to build and
     * [fromIntent] to read. Wire format key: [KeyUtil.arg_model].
     */
    data class Args(val modelUrl: String)

    companion object {
        /**
         * Builds an [Intent] for [ImagePreviewActivity] with the image URL as the
         * [KeyUtil.arg_model] extra. Includes [Intent.FLAG_ACTIVITY_NEW_TASK] so
         * callers from non-Activity contexts (custom views, widgets) work correctly.
         */
        fun newIntent(context: Context, modelUrl: String): Intent =
            Intent(context, ImagePreviewActivity::class.java).apply {
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

    private lateinit var binding: ActivityImagePreviewBinding

    private var imageUri: String? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarPreviewImage)
        supportActionBar?.title = ""

        binding.previewImage.setOnClickListener {
            binding.toolbarPreviewImage
                .animate()
                .alpha(if (binding.toolbarPreviewImage.alpha == 1f) 0f else 1f)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val args = fromIntent(intent)
        if (args != null) {
            imageUri = args.modelUrl
            Glide.with(this).load(args.modelUrl).into(binding.previewImage)
        } else {
            NotifyUtil
                .makeText(
                    this,
                    R.string.layout_empty_response,
                    R.drawable.ic_warning_white_18dp,
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!imageUri.isNullOrEmpty()) {
            menuInflater.inflate(R.menu.image_preview_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.image_preview_download -> {
                val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    null // Scoped storage doesn't require WRITE_EXTERNAL_STORAGE
                } else {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                }

                if (permission == null || requestPermissionIfMissing(permission)) {
                    downloadAttachment()
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permission,
                    )
                ) {
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
                return true
            }
            R.id.image_preview_share,
            R.id.action_share,
            -> {
                val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, imageUri)
                    }
                startActivity(Intent.createChooser(intent, resources.getText(R.string.image_preview_share)))
                return true
            }
            R.id.image_preview_link -> {
                return try {
                    val intent =
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(imageUri)
                        }
                    startActivity(intent)
                    true
                } catch (e: Exception) {
                    Timber.e(e.localizedMessage)
                    NotifyUtil.makeText(this, R.string.text_unknown_error, Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        updateUI()
    }

    override fun updateUI() {
    }

    override fun makeRequest() {
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

    override fun onPermissionGranted(permission: String) {
        super.onPermissionGranted(permission)
        val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (permission == writePermission) {
            downloadAttachment()
        }
    }
}
