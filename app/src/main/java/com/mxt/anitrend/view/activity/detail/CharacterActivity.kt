package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.detail.CharacterPageAdapter
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import java.util.Locale

/**
 * Created by max on 2017/12/14.
 * character activity
 */
class CharacterActivity : ActivityBase<CharacterBase, BasePresenter>() {
    private lateinit var binding: ActivityPagerGenericBinding

    private var favouriteWidget: FavouriteToolbarWidget? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagerGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        setPresenter(BasePresenter(this))
        setViewModel(true)
        if (intent.hasExtra(KeyUtil.arg_id)) {
            id = intent.getLongExtra(KeyUtil.arg_id, -1)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        viewModel?.params?.putLong(KeyUtil.arg_id, id)
        onActivityReady()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val isAuth = presenter.settings.isAuthenticated
        menuInflater.inflate(R.menu.custom_menu, menu)
        menu.findItem(R.id.action_favourite).isVisible = isAuth
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
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(
                                Intent.EXTRA_TEXT,
                                String.format(
                                    Locale.getDefault(),
                                    "%s - %s",
                                    model.name?.fullName ?: "",
                                    model.siteUrl ?: "",
                                ),
                            )
                            type = "text/plain"
                        }
                    startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
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

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        val pageAdapter =
            CharacterPageAdapter(this, applicationContext).apply {
                params = viewModel?.params ?: Bundle.EMPTY
            }
        binding.contentMain.pageContainer.adapter = pageAdapter
        binding.contentMain.pageContainer.offscreenPageLimit = offScreenLimit
        TabLayoutMediator(binding.customTab.smartTab, binding.contentMain.pageContainer) { tab, position ->
            tab.text = pageAdapter.getPageTitle(position)
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        if (getModel() == null) {
            makeRequest()
        } else {
            updateUI()
        }
    }

    override fun updateUI() {
        getModel()?.let { model ->
            favouriteWidget?.setModel(model)
        }
    }

    override fun makeRequest() {
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_id, id)
        }
        viewModel?.requestData(KeyUtil.CHARACTER_BASE_REQ, applicationContext)
    }

    override fun onChanged(model: CharacterBase?) {
        super.onChanged(model)
        updateUI()
    }
}
