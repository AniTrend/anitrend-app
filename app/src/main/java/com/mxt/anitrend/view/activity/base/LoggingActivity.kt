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
import com.mxt.anitrend.extension.launchCatching
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.NotifyUtil
import com.nguyenhoanglam.progresslayout.ProgressLayout
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader

class LoggingActivity : ActivityBase<Void, BasePresenter>(), CoroutineScope by MainScope() {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.report_display)
    lateinit var reportLogTextView: AppCompatTextView

    @BindView(R.id.application_version)
    lateinit var applicationVersionTextViwe: SingleLineTextView

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
        applicationVersionTextViwe.text = getString(
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
                if (requestPermissionIfMissing(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    try {
                        val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                "AniTrend Logcat.txt")
                        FileWriter(root).use {
                            it.append(log.toString())
                        }
                        NotifyUtil.makeText(applicationContext, R.string.bug_report_saved, Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        Timber.tag(TAG).e(e)
                        e.printStackTrace()
                    }
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
        reportLogTextView.text = log.toString()
    }

    override fun makeRequest() {
        launchCatching (coroutineContext = Dispatchers.IO) {
            val process = Runtime.getRuntime().exec("logcat -d -v threadtime com.mxt.anitrend:*")
            InputStreamReader(process.inputStream).useLines { sequence ->
                sequence.iterator().forEach {
                    log.append(it).append("\n")
                }
            }
            withContext(Dispatchers.Main) {
                updateUI()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binder?.unbind()
        cancel()
    }
}
