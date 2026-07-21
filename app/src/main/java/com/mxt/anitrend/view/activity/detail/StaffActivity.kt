package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.detail.StaffPageAdapter
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.extension.serializableExtra
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.StaffModel
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.viewmodel.StaffViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Created by max on 2017/12/14.
 * staff activity
 */
class StaffActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPagerGenericBinding

    private var model: StaffBase? = null
    private var staffId: Long = 0
    private var onList: Boolean? = null

    private var favouriteWidget: FavouriteToolbarWidget? = null

    private var tabMediator: TabLayoutMediator? = null

    private lateinit var staffViewModel: StaffViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // Preserve configured theme (was previously handled by ActivityBase.configureActivity).
        val settings = KoinExt.get(Settings::class.java)
        val themeRes = when (settings.theme) {
            KeyUtil.THEME_DARK -> R.style.AppThemeDark
            KeyUtil.THEME_BLACK -> R.style.AppThemeBlack
            else -> R.style.AppThemeLight
        }
        setTheme(themeRes)
        super.onCreate(savedInstanceState)

        // Process deep links (e.g. anilist.co/staff/{id}) so arg_id is injected
        // into the intent before we read it. Previously handled by
        // ActivityBase.onCreate -> IntentBundleUtil.checkIntentData.
        IntentBundleUtil(intent).checkIntentData(this)

        binding = ActivityPagerGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra(KeyUtil.arg_id)) {
            staffId = intent.getLongExtra(KeyUtil.arg_id, -1)
        }
        onList = intent.serializableExtra(KeyUtil.arg_onList)

        staffViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    StaffViewModel(
                        staffService = WebFactory.createService(
                            StaffModel::class.java,
                            applicationContext,
                        ),
                    ) as T
            },
        )[StaffViewModel::class.java]

        observeViewModel()
        setUpPager()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                staffViewModel.state.collect { state ->
                    when (state) {
                        is StaffViewModel.UiState.Loading -> { /* content loads below */ }
                        is StaffViewModel.UiState.Success -> {
                            model = state.staff
                            updateUI()
                        }
                        is StaffViewModel.UiState.Error -> {
                            NotifyUtil.makeText(
                                this@StaffActivity,
                                state.message,
                                R.drawable.ic_warning_white_18dp,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
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

    private fun buildPagerParams(): Bundle {
        return Bundle().apply {
            putLong(KeyUtil.arg_id, staffId)
            putSerializable(KeyUtil.arg_onList, onList)
        }
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
                model?.let { m ->
                    favouriteWidget?.setModel(m)
                }
            }
        }
        return true
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
                                    current.name?.fullName.orEmpty(),
                                    current.siteUrl.orEmpty(),
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
            favouriteWidget?.setModel(current)
            supportActionBar?.title = current.name?.fullName
        }
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
}
