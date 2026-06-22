package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.mxt.anitrend.R
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.adapter.pager.detail.StaffPageAdapter
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding
import com.mxt.anitrend.extension.serializableExtra
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import co.anitrend.retrofit.graphql.model.request.QueryContainerBuilder
import java.util.Locale

/**
 * Created by max on 2017/12/14.
 * staff activity
 */
class StaffActivity : ActivityBase<StaffBase, BasePresenter>() {

    private lateinit var binding: ActivityPagerGenericBinding

    private var onList: Boolean? = null

    private var favouriteWidget: FavouriteToolbarWidget? = null

    private var tabMediator: TabLayoutMediator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagerGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mSearchView = binding.customToolbar.searchView
        setSupportActionBar(binding.customToolbar.toolbar)
        setPresenter(BasePresenter(this))
        setViewModel(true)
        id = intent.getLongExtra(KeyUtil.arg_id, -1)
        onList = intent.serializableExtra(KeyUtil.arg_onList)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        viewModel?.params?.putLong(KeyUtil.arg_id, id)
        viewModel?.params?.putSerializable(KeyUtil.arg_onList, onList)
        onActivityReady()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val isAuth = presenter.settings.isAuthenticated
        menuInflater.inflate(R.menu.staff_menu, menu)
        menu.findItem(R.id.action_favourite).isVisible = isAuth
        menu.findItem(R.id.action_on_my_list).isVisible = isAuth
        if (isAuth) {
            val favouriteMenuItem = menu.findItem(R.id.action_favourite)
            favouriteWidget = favouriteMenuItem.actionView as? FavouriteToolbarWidget
            if (favouriteWidget == null) {
                favouriteMenuItem.isVisible = false
            } else {
                getModel()?.let { model ->
                    favouriteWidget?.setModel(model)
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val model = getModel()
        if (model != null) {
            when (item.itemId) {
                R.id.action_share -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(
                            Intent.EXTRA_TEXT,
                            String.format(
                                Locale.getDefault(),
                                "%s - %s",
                                model.name?.fullName.orEmpty(),
                                model.siteUrl.orEmpty()
                            )
                        )
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
                }
                R.id.action_on_my_list -> {
                    val selectedIndex = when (onList) {
                        null -> 0
                        false -> 1
                        true -> 2
                    }
                    DialogUtil.createSelection(
                        this,
                        R.string.app_filter_on_list,
                        selectedIndex,
                        CompatUtil.getStringList(this, R.array.on_list_values)
                    ) { dialog, which ->
                        if (which == DialogAction.POSITIVE) {
                            onList = when (dialog.selectedIndex) {
                                0 -> null
                                1 -> false
                                else -> true
                            }
                            reloadViewPager()
                        }
                    }
                }
            }
        } else {
            NotifyUtil.makeText(
                applicationContext,
                R.string.text_activity_loading,
                Toast.LENGTH_SHORT
            ).show()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        val pageAdapter = StaffPageAdapter(this, applicationContext).apply {
            params = viewModel?.params ?: Bundle.EMPTY
        }
        binding.contentMain.pageContainer.adapter = pageAdapter
        binding.contentMain.pageContainer.offscreenPageLimit = offScreenLimit
        attachTabs(pageAdapter)
    }

    override fun onResume() {
        super.onResume()
        if (getModel() == null)
            makeRequest()
        else
            updateUI()
    }

    override fun updateUI() {
        getModel()?.let { model ->
            favouriteWidget?.setModel(model)
        }
    }

    override fun makeRequest() {
        val queryContainer: QueryContainerBuilder = GraphUtil.getDefaultQuery(false)
            .putVariable(KeyUtil.arg_id, id)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.STAFF_BASE_REQ, applicationContext)
    }

    override fun onChanged(model: StaffBase?) {
        super.onChanged(model)
        updateUI()
    }

    private fun reloadViewPager() {
        val adapter = StaffPageAdapter(this, applicationContext)

        viewModel?.params?.putLong(KeyUtil.arg_id, id)
        viewModel?.params?.putSerializable(KeyUtil.arg_onList, onList)
        adapter.params = viewModel?.params ?: Bundle.EMPTY

        val currentItem = binding.contentMain.pageContainer.currentItem
        binding.contentMain.pageContainer.adapter = adapter
        attachTabs(adapter)
        binding.contentMain.pageContainer.setCurrentItem(currentItem, false)
    }

    private fun attachTabs(adapter: StaffPageAdapter) {
        tabMediator?.detach()
        tabMediator = TabLayoutMediator(binding.customTab.smartTab, binding.contentMain.pageContainer) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }
        tabMediator?.attach()
    }
}
