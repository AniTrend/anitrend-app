package com.mxt.anitrend.view.activity.base

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.VisibleForTesting
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.LogEntryAdapter
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityLoggingBinding
import com.mxt.anitrend.extension.logFile
import com.mxt.anitrend.model.entity.log.LogFilter
import com.mxt.anitrend.model.entity.log.LogUiState
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.viewmodel.LoggingViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class LoggingActivity : ActivityBase<Void, BasePresenter>() {

    private lateinit var binding: ActivityLoggingBinding

    private val progressLayout get() = binding.contentLogging.stateLayout
    private val logRecycler get() = binding.contentLogging.logRecycler
    private val filterGroup get() = binding.contentLogging.filterGroup

    private val logAdapter by lazy { LogEntryAdapter(this) }

    @VisibleForTesting
    internal lateinit var loggingViewModel: LoggingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoggingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)

        loggingViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = LoggingViewModel(
                    logFileProvider = { applicationContext.logFile() },
                    metadataProvider = { buildSupportMetadata() },
                ) as T
            },
        )[LoggingViewModel::class.java]

        configureRecycler()
        configureFilterChips()
        observeViewModel()
    }

    private fun configureRecycler() {
        logRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            /* reverseLayout = */ true,
        )
        logRecycler.adapter = logAdapter
        logRecycler.setHasFixedSize(true)
    }

    private fun configureFilterChips() {
        // Check "All" chip by default
        binding.contentLogging.filterAll.isChecked = true

        filterGroup.setOnCheckedStateChangeListener { group, _ ->
            val filter = when (group.checkedChipId) {
                R.id.filter_error -> LogFilter.Error
                R.id.filter_warning -> LogFilter.Warning
                R.id.filter_info -> LogFilter.Info
                R.id.filter_debug -> LogFilter.Debug
                else -> LogFilter.All
            }
            loggingViewModel.setFilter(filter)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            loggingViewModel.state.collect { state ->
                when (state) {
                    is LogUiState.Loading -> progressLayout.showLoading()
                    is LogUiState.Success -> {
                        logAdapter.onItemsInserted(state.entries)
                        updateUI()
                    }
                    is LogUiState.Error -> updateUI()
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        bindMetadataCard()
    }

    private fun bindMetadataCard() {
        binding.contentLogging.supportVersion.text = buildSupportVersion()
        binding.contentLogging.supportDevice.text =
            "${Build.MANUFACTURER} ${Build.MODEL}"
        binding.contentLogging.supportAndroid.text =
            "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    private fun buildSupportVersion(): String = "v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})"

    private fun buildSupportMetadata(): String {
        val version = buildSupportVersion()
        val device = "${Build.MANUFACTURER} ${Build.MODEL}"
        val android = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        return buildString {
            appendLine("# $version")
            appendLine("# $device")
            appendLine("# $android")
            appendLine()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.logging_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_log -> {
                loggingViewModel.clear()
            }
            R.id.action_save_log -> {
                if (requestPermissionIfMissing(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    runCatching {
                        val root = File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS,
                            ),
                            "AniTrend Logcat.txt",
                        )
                        applicationContext.logFile().copyTo(root, true)
                    }.onFailure {
                        Timber.e(it)
                    }.onSuccess {
                        NotifyUtil.createAlerter(
                            this@LoggingActivity,
                            R.string.text_post_information,
                            R.string.bug_report_saved,
                            R.drawable.ic_insert_emoticon_white_24dp,
                            R.color.colorStateGreen,
                        )
                    }
                }
            }
            R.id.action_share_log -> {
                lifecycleScope.launch {
                    val shareFile = loggingViewModel.buildShareFile()
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(
                            Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(
                                applicationContext,
                                "${applicationContext.packageName}.provider",
                                shareFile,
                            ),
                        )
                        type = "text/plain"
                    }
                    startActivity(
                        Intent.createChooser(
                            intent,
                            getString(R.string.abc_shareactionprovider_share_with),
                        ),
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        onActivityReady()
    }

    override fun onActivityReady() {
        loggingViewModel.load()
    }

    override fun updateUI() {
        progressLayout.showContent()
    }

    override fun makeRequest() {
        // No-op: ViewModel owns the load lifecycle
    }

    override fun onDestroy() {
        super.onDestroy()
        val shareFile = File(
            applicationContext.logFile().parentFile,
            LoggingViewModel.SHARE_FILE_NAME,
        )
        if (shareFile.exists()) shareFile.delete()
    }
}
