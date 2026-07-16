package com.mxt.anitrend.view.activity.base

import android.Manifest
import android.app.DownloadManager
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
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityImagePreviewBinding
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import timber.log.Timber
import androidx.core.net.toUri

/**
 * Created by max on 2017/11/14.
 * ImagePreviewActivity
 */
class ImagePreviewActivity : ActivityBase<Void, BasePresenter>() {
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
        val modelUrl = intent.getStringExtra(KeyUtil.arg_model)
        if (!modelUrl.isNullOrEmpty()) {
            imageUri = modelUrl
            Glide.with(this).load(modelUrl).into(binding.previewImage)
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
