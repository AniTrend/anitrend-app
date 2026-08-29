package com.mxt.anitrend.view.fragment.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.LogEntryAdapter
import com.mxt.anitrend.databinding.ContentLoggingBinding
import com.mxt.anitrend.model.entity.log.LogFilter
import com.mxt.anitrend.model.entity.log.LogUiState
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.viewmodel.LoggingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Navigation 2 destination for the local application log.
 *
 * The ViewModel owns log loading and mutation. This Fragment only binds the
 * immutable state, handles the screen menu, and requests platform permissions.
 */
@Suppress("TooManyFunctions") // Logging lifecycle, filtering, and export actions stay centralized.
class LoggingFragment : Fragment() {

    private var _binding: ContentLoggingBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val logAdapter by lazy { LogEntryAdapter(requireContext()) }

    @VisibleForTesting
    internal val loggingViewModel: LoggingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ContentLoggingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureRecycler()
        configureFilterChips()
        observeViewModel()
        bindMetadataCard()
    }

    private fun configureRecycler() {
        binding.logRecycler.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            /* reverseLayout = */ true,
        )
        binding.logRecycler.adapter = logAdapter
        binding.logRecycler.setHasFixedSize(true)
    }

    private fun configureFilterChips() {
        binding.filterAll.isChecked = true
        binding.filterGroup.setOnCheckedStateChangeListener { group, _ ->
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loggingViewModel.state.collect { state ->
                    when (state) {
                        is LogUiState.Loading -> binding.stateLayout.showLoading()
                        is LogUiState.Success -> {
                            logAdapter.onItemsInserted(state.entries)
                            binding.stateLayout.showContent()
                        }
                        is LogUiState.Error -> binding.stateLayout.showContent()
                    }
                }
            }
        }
    }

    private fun bindMetadataCard() {
        binding.supportVersion.text = buildSupportVersion()
        binding.supportDevice.text = "${Build.MANUFACTURER} ${Build.MODEL}"
        binding.supportAndroid.text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.logging_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_log -> loggingViewModel.clear()
            R.id.action_save_log -> if (requestWritePermission()) {
                performSaveToDownloads()
            }
            R.id.action_share_log -> shareLog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun shareLog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val shareFile = loggingViewModel.buildShareFile()
            val intent = Intent(Intent.ACTION_SEND).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.provider",
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

    private fun performSaveToDownloads() {
        viewLifecycleOwner.lifecycleScope.launch {
            loggingViewModel.saveToDownloads()
                .onFailure { Timber.e(it) }
                .onSuccess {
                    NotifyUtil.createAlerter(
                        requireActivity(),
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
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        true
    } else if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION,
        )
        false
    } else {
        false
    }

    override fun onDestroyView() {
        binding.logRecycler.adapter = null
        _binding = null
        super.onDestroyView()
    }

    /** Saved-state and logging destination helpers. */
    companion object {
        private const val REQUEST_PERMISSION = 102
    }
}
