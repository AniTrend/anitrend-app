package com.mxt.anitrend.view.activity.base

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.nguyenhoanglam.progresslayout.ProgressLayout
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

class LoggingActivity : ActivityBase<Void, BasePresenter>(), CoroutineScope by MainScope() {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.report_display)
    lateinit var reportLogTextView: AppCompatTextView

    @BindView(R.id.application_version)
    lateinit var applicationVersionTextView: SingleLineTextView

    @BindView(R.id.stateLayout)
    lateinit var progressLayout: ProgressLayout

    private var binder: Unbinder? = null

    private val log = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logging)
        binder = ButterKnife.bind(this)
        setSupportActionBar(toolbar)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        applicationVersionTextView.text = getString(
                R.string.text_about_appication_version,
                BuildConfig.VERSION_NAME
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.logging_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save_log -> {
                if (!progressLayout.isLoading) {
                    progressLayout.showLoading()
                    if (requestPermissionIfMissing(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        async (Dispatchers.IO) {
                            val root = File(
                                    Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_DOWNLOADS
                                    ),
                                    "AniTrend Logcat.txt"
                            )
                            FileWriter(root).use {
                                it.write(log.toString())
                            }
                            withContext(Dispatchers.Main) {
                                progressLayout.showContent()
                                NotifyUtil.makeText(applicationContext, R.string.bug_report_saved, Toast.LENGTH_SHORT).show()
                            }
                        }.invokeOnCompletion {
                            it?.printStackTrace()
                        }
                    }
                } else {
                    NotifyUtil.createAlerter(this,
                            R.string.title_activity_logging,
                            R.string.busy_please_wait,
                            R.drawable.ic_bug_report_grey_600_24dp,
                            R.color.colorStateBlue,
                            KeyUtil.DURATION_SHORT
                    )
                }
            }
            R.id.action_share_log -> {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, log.toString())
                    type = "text/plain"
                }
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are *not* resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * [.onResumeFragments].
     */
    override fun onResume() {
        super.onResume()
        onActivityReady()
    }

    override fun onActivityReady() {
        progressLayout.showLoading()
        makeRequest()
    }

    override fun updateUI() {
        progressLayout.showContent()
    }

    private fun printLog(logHistory: String) {
        updateUI()
        reportLogTextView.text = logHistory
    }

    override fun makeRequest() {
        async (Dispatchers.IO) {
            val process = Runtime.getRuntime().exec("logcat -d -v threadtime com.mxt.anitrend:*")
            InputStreamReader(process.inputStream).use { inputStream ->
                log.append(inputStream.readText())
                        .append("\n")
            }
            val logHistory = log.toString()
            withContext(Dispatchers.Main) {
                printLog(logHistory)
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binder?.unbind()
        cancel()
    }
}
