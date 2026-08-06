package com.mxt.anitrend.view.activity.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.detail.StaffPageAdapter
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetRenderState
import com.mxt.anitrend.databinding.ActivityStaffBinding
import com.mxt.anitrend.domain.model.StaffRecord
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.extension.serializableExtra
import com.mxt.anitrend.navigation.extension.putScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.viewmodel.StaffViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Created by max on 2017/12/14.
 * staff activity
 */
class StaffActivity : CommonActivity() {

    companion object {
        fun newIntent(context: Context, param: StaffScreenParam): Intent = Intent(context, StaffActivity::class.java).apply {
            putScreenParam(param)
        }

        /**
         * Compatibility overload preserving the legacy id-based callers. Bridges into
         * the typed parameter so navigation always uses [StaffScreenParam].
         */
        fun newIntent(context: Context, id: Long): Intent = newIntent(context, StaffScreenParam(staffId = id))

        /**
         * Resolves the typed parameter from the intent.
         *
         * The typed parameter is read first. Deep links (injected by
         * [IntentBundleUtil.checkIntentData]) and pre-bridge callers still write the
         * legacy [KeyUtil.arg_id] extra, so that value is bridged here into
         * [StaffScreenParam] via [resolve]. The bridge is a scalar conversion point
         * inside the activity, not a parcel path for the staff entity. The tri-state
         * [KeyUtil.arg_onList] filter stays on the legacy transitional channel and is
         * read directly by the activity.
         *
         * A null result keeps the pre-refactor invalid-ID behaviour: the activity
         * renders its in-layout error state (via the ViewModel) instead of finishing.
         */
        fun fromIntent(intent: Intent): StaffScreenParam? = resolve(
            typed = intent.screenParam<StaffScreenParam>(),
            legacyId = intent.getLongExtra(KeyUtil.arg_id, -1),
        )

        /**
         * Production parsing rule for the staff destination.
         *
         * A present typed parameter wins; it is accepted only when it carries a
         * positive id. Otherwise the legacy [KeyUtil.arg_id] extra is bridged when
         * positive. Missing or non-positive ids resolve to null so the activity
         * keeps its default (invalid) state.
         */
        @VisibleForTesting
        internal fun resolve(typed: StaffScreenParam?, legacyId: Long): StaffScreenParam? {
            typed?.let { param ->
                return if (param.staffId > 0) param else null
            }
            return if (legacyId > 0) StaffScreenParam(staffId = legacyId) else null
        }
    }

    private lateinit var binding: ActivityStaffBinding

    private var model: StaffRecord? = null
    private var staffId: Long = 0
    private var onList: Boolean? = null

    private var favouriteWidget: FavouriteToolbarWidget? = null

    private var tabMediator: TabLayoutMediator? = null

    private val staffViewModel: StaffViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Process deep links (e.g. anilist.co/staff/{id}) so arg_id is injected
        // into the intent before we read it. Previously handled by
        // ActivityBase.onCreate -> IntentBundleUtil.checkIntentData.
        IntentBundleUtil(intent).checkIntentData(this)

        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.staffErrorRetry.setOnClickListener {
            staffViewModel.load(staffId)
        }

        // Resolve the destination through the typed parameter, falling back to the
        // legacy wire key for deep links and pre-bridge callers. A null result keeps
        // the pre-refactor invalid-ID behaviour: the id stays 0 and the ViewModel
        // renders the in-layout error state. The onList filter is read directly from
        // the legacy transitional channel.
        fromIntent(intent)?.let { args ->
            staffId = args.staffId
        }
        onList = intent.serializableExtra(KeyUtil.arg_onList)

        observeViewModel()
        setUpPager()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                staffViewModel.state.collect { state ->
                    when (state) {
                        is StaffViewModel.UiState.Loading -> showLoadingState()
                        is StaffViewModel.UiState.Success -> {
                            model = state.staff
                            showContentState()
                            updateUI()
                        }
                        is StaffViewModel.UiState.Error -> {
                            showErrorState(state.message)
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe the canonical favourite store through the ViewModel and
                // re-render after every committed mutation (or in-flight loading change).
                combine(
                    staffViewModel.favouriteFlag,
                    staffViewModel.favouriteLoading,
                ) { flag, loading ->
                    FavouriteWidgetRenderState.fromFlag(
                        flag = flag,
                        fallbackIsFavourite = model?.isFavourite ?: false,
                        isLoading = loading,
                    )
                }.collect { renderState ->
                    favouriteWidget?.render(renderState)
                }
            }
        }
    }

    private fun setUpPager() {
        val params = buildPagerParams()
        val pageAdapter =
            StaffPageAdapter(this, applicationContext).apply {
                this.params = params
            }
        binding.contentMain.pageContainer.adapter = pageAdapter
        binding.contentMain.pageContainer.offscreenPageLimit = 3
        attachTabs(pageAdapter)
    }

    private fun buildPagerParams(): Bundle = Bundle().apply {
        putLong(KeyUtil.arg_id, staffId)
        putSerializable(KeyUtil.arg_onList, onList)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val isAuth = KoinExt.get(Settings::class.java).isAuthenticated
        menuInflater.inflate(R.menu.staff_menu, menu)
        menu.findItem(R.id.action_favourite).isVisible = isAuth
        menu.findItem(R.id.action_on_my_list).isVisible = isAuth
        if (isAuth) {
            val favouriteMenuItem = menu.findItem(R.id.action_favourite)
            favouriteWidget = favouriteMenuItem.actionView as? FavouriteToolbarWidget
            if (favouriteWidget == null) {
                favouriteMenuItem.isVisible = false
            } else {
                favouriteWidget?.setOnToggleAction {
                    staffViewModel.toggleFavouriteStaff(staffId)
                }
                // The widget is created after the observeViewModel collectors start, so
                // render once with the current values and let the collector re-render on
                // any subsequent store or loading change.
                renderFavouriteWidget()
            }
        }
        return true
    }

    private fun renderFavouriteWidget() {
        favouriteWidget?.render(
            FavouriteWidgetRenderState.fromFlag(
                flag = staffViewModel.favouriteFlag.value,
                fallbackIsFavourite = model?.isFavourite ?: false,
                isLoading = staffViewModel.favouriteLoading.value,
            ),
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        val current = model
        if (current != null) {
            when (item.itemId) {
                R.id.action_share -> {
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(
                                Intent.EXTRA_TEXT,
                                String.format(
                                    Locale.getDefault(),
                                    "%s - %s",
                                    current.name ?: "",
                                    current.siteUrl ?: "",
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
                    return true
                }
                R.id.action_on_my_list -> {
                    val selectedIndex =
                        when (onList) {
                            null -> 0
                            false -> 1
                            true -> 2
                        }
                    DialogUtil.createSelection(
                        this,
                        R.string.app_filter_on_list,
                        selectedIndex,
                        CompatUtil.getStringList(this, R.array.on_list_values),
                    ) { dialog, _ ->
                        onList =
                            when (dialog.selectedIndex) {
                                0 -> null
                                1 -> false
                                else -> true
                            }
                        reloadViewPager()
                    }
                    return true
                }
            }
        } else {
            NotifyUtil
                .makeText(
                    applicationContext,
                    R.string.text_activity_loading,
                    Toast.LENGTH_SHORT,
                ).show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        staffViewModel.load(staffId)
    }

    private fun updateUI() {
        model?.let { current ->
            binding.staffDisplayName.text = current.name
            binding.staffIdentityTier.visibility = VISIBLE
        }
    }

    private fun showLoadingState() {
        binding.staffStateOverlay.visibility = VISIBLE
        binding.staffLoadingState.visibility = VISIBLE
        binding.staffErrorState.visibility = GONE
        binding.staffStateOverlay.contentDescription =
            getString(R.string.staff_loading_content_description)
    }

    private fun showContentState() {
        binding.staffStateOverlay.visibility = GONE
    }

    private fun showErrorState(message: String) {
        binding.staffStateOverlay.visibility = VISIBLE
        binding.staffLoadingState.visibility = GONE
        binding.staffErrorState.visibility = VISIBLE
        binding.staffErrorText.text = message
        binding.staffStateOverlay.contentDescription = message
    }

    private fun reloadViewPager() {
        val params = buildPagerParams()
        val adapter = StaffPageAdapter(this, applicationContext)
        adapter.params = params

        val currentItem = binding.contentMain.pageContainer.currentItem
        binding.contentMain.pageContainer.adapter = adapter
        attachTabs(adapter)
        binding.contentMain.pageContainer.setCurrentItem(currentItem, false)
    }

    private fun attachTabs(adapter: StaffPageAdapter) {
        tabMediator?.detach()
        tabMediator =
            TabLayoutMediator(binding.customTab.smartTab, binding.contentMain.pageContainer) { tab, position ->
                tab.text = adapter.getPageTitle(position)
            }
        tabMediator?.attach()
    }

    override fun onDestroy() {
        favouriteWidget?.setOnToggleAction(null)
        super.onDestroy()
    }
}
