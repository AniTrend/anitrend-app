package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.fragment.detail.StudioMediaFragment
import co.anitrend.retrofit.graphql.model.request.QueryContainerBuilder
import java.util.Locale

/**
 * Created by max on 2017/12/14.
 * StudioActivity
 */
class StudioActivity : ActivityBase<StudioBase, BasePresenter>() {

    private lateinit var binding: ActivityFrameGenericBinding

    private var model: StudioBase? = null

    private var favouriteWidget: FavouriteToolbarWidget? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrameGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mSearchView = binding.customToolbar.searchView
        setSupportActionBar(binding.customToolbar.toolbar)
        setViewModel(true)
        setPresenter(BasePresenter(this))
        if (intent.hasExtra(KeyUtil.arg_id))
            id = intent.getLongExtra(KeyUtil.arg_id, -1)
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
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val current = model
        if (current != null) {
            when (item.itemId) {
                R.id.action_share -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(
                            Intent.EXTRA_TEXT,
                            String.format(
                                Locale.getDefault(),
                                "%s - %s",
                                current.name,
                                current.siteUrl
                            )
                        )
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
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

    override fun onResume() {
        super.onResume()
        if (model == null)
            makeRequest()
        else
            updateUI()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        mFragment = StudioMediaFragment.newInstance(intent.extras ?: Bundle.EMPTY)
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        mFragment?.let { fragment ->
            fragmentTransaction.replace(R.id.content_frame, fragment, fragment.TAG)
            fragmentTransaction.commit()
        }
    }

    override fun updateUI() {
        model?.let { current ->
            favouriteWidget?.setModel(current)
            mActionBar?.title = current.name
        }
    }

    override fun makeRequest() {
        val queryContainer: QueryContainerBuilder = GraphUtil.getDefaultQuery(false)
            .putVariable(KeyUtil.arg_id, id)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.STUDIO_BASE_REQ, applicationContext)
    }

    override fun onChanged(model: StudioBase?) {
        super.onChanged(model)
        this.model = model
        updateUI()
    }
}
