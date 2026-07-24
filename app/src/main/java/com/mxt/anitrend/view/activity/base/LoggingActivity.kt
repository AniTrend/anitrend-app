package com.mxt.anitrend.view.activity.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.LogEntryAdapter
import com.mxt.anitrend.databinding.ActivityLoggingBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.model.entity.log.LogFilter
import com.mxt.anitrend.model.entity.log.LogUiState
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.viewmodel.LoggingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class LoggingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoggingBinding

    private val progressLayout get() = binding.contentLogging.stateLayout
    private val logRecycler get() = binding.contentLogging.logRecycler
    private val filterGroup get() = binding.contentLogging.filterGroup

    private val logAdapter by lazy { LogEntryAdapter(this) }

    @VisibleForTesting
    internal val loggingViewModel: LoggingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Replicates ActivityBase.configureActivity() theme behaviour via
        // ConfigurationUtil.onCreateAttach. Must run before super.onCreate() so
        // the correct theme resource is locked in before setContentView().
        val settings = KoinExt.get(Settings::class.java)
        val themeRes = when (settings.theme) {
            KeyUtil.THEME_DARK -> R.style.AppThemeDark
            KeyUtil.THEME_BLACK -> R.style.AppThemeBlack
            else -> R.style.AppThemeLight
        }
        setTheme(themeRes)
        super.onCreate(savedInstanceState)

        binding = ActivityLoggingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        configureRecycler()
        configureFilterChips()
        observeViewModel()
        bindMetadataCard()
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
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loggingViewModel.state.collect { state ->
                    when (state) {
                        is LogUiState.Loading -> progressLayout.showLoading()
                        is LogUiState.Success -> {
                            logAdapter.onItemsInserted(state.entries)
                            progressLayout.showContent()
                        }
                        is LogUiState.Error -> progressLayout.showContent()
                    }
                }
            }
        }
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
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            R.id.action_clear_log -> {
                loggingViewModel.clear()
            }
            R.id.action_save_log -> {
                if (requestWritePermission()) {
                    // Permission already granted — save immediately.
                    performSaveToDownloads()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            performSaveToDownloads()
        }
    }

    /**
     * Copies the log file to Downloads and notifies the user on completion.
     * Extracted so both the already-granted branch and the permission-result
     * callback can invoke it without duplicating the coroutine + notification block.
     */
    private fun performSaveToDownloads() {
        // TODO (API 29+): migrate from WRITE_EXTERNAL_STORAGE / Environment to
        // scoped storage (MediaStore / SAF). The current path is deprecated and
        // may stop working on future SDK levels.
        lifecycleScope.launch {
            loggingViewModel.saveToDownloads()
                .onFailure { Timber.e(it) }
                .onSuccess {
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

    override fun onResume() {
        super.onResume()
        if (!loggingViewModel.isLogLoadComplete) {
            loggingViewModel.load()
        }
    }

    private fun requestWritePermission(): Boolean = if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        true
    } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION,
        )
        false
    } else {
        false
    }

    companion object {
        private const val REQUEST_PERMISSION = 102
    }
}
