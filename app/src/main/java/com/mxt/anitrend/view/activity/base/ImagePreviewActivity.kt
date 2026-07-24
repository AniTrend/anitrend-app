package com.mxt.anitrend.view.activity.base

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityImagePreviewBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import timber.log.Timber

class ImagePreviewActivity : AppCompatActivity() {

    data class Args(val modelUrl: String)

    companion object {
        fun newIntent(context: Context, modelUrl: String): Intent = Intent(context, ImagePreviewActivity::class.java).apply {
            putExtra(KeyUtil.arg_model, modelUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        fun fromIntent(intent: Intent): Args? = parseArgs(intent.getStringExtra(KeyUtil.arg_model))

        @VisibleForTesting
        internal fun parseArgs(raw: String?): Args? = if (!raw.isNullOrEmpty()) Args(raw) else null

        private const val REQUEST_PERMISSION = 102
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
        // Preserve configured theme (previously handled by ActivityBase.configureActivity).
        val settings = KoinExt.get(Settings::class.java)
        val themeRes = when (settings.theme) {
            KeyUtil.THEME_DARK -> R.style.AppThemeDark
            KeyUtil.THEME_BLACK -> R.style.AppThemeBlack
            else -> R.style.AppThemeLight
        }
        setTheme(themeRes)
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

        val args = fromIntent(intent)
        if (args != null) {
            imageUri = args.modelUrl
            Glide.with(this).load(args.modelUrl).into(binding.previewImage)
        } else {
            NotifyUtil.makeText(
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
                    val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(imageUri) }
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
