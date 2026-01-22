package com.mxt.anitrend.view.activity.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.afollestad.materialdialogs.DialogAction;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.detail.StaffPageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget;
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.graphql.GraphUtil;

import java.util.Locale;

import io.github.wax911.library.model.request.QueryContainerBuilder;

/**
 * Created by max on 2017/12/14.
 * staff activity
 */

public class StaffActivity extends ActivityBase<StaffBase, BasePresenter> {

    private ActivityPagerGenericBinding binding;

    private Boolean onList;

    private FavouriteToolbarWidget favouriteWidget;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPagerGenericBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mSearchView = binding.customToolbar.searchView;
        setSupportActionBar(binding.customToolbar.toolbar);
        setPresenter(new BasePresenter(this));
        setViewModel(true);
        id = getIntent().getLongExtra(KeyUtil.arg_id, -1);
        onList = (Boolean) getIntent().getSerializableExtra(KeyUtil.arg_onList);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getViewModel().getParams().putLong(KeyUtil.arg_id, id);
        getViewModel().getParams().putSerializable(KeyUtil.arg_onList, onList);
        onActivityReady();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isAuth = getPresenter().getSettings().isAuthenticated();
        getMenuInflater().inflate(R.menu.staff_menu, menu);
        menu.findItem(R.id.action_favourite).setVisible(isAuth);
        menu.findItem(R.id.action_on_my_list).setVisible(isAuth);
        if(isAuth) {
            MenuItem favouriteMenuItem = menu.findItem(R.id.action_favourite);
            favouriteWidget = (FavouriteToolbarWidget) favouriteMenuItem.getActionView();
            StaffBase model = getModel();
            if(model != null)
                favouriteWidget.setModel(model);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        StaffBase model = getModel();
        if(model != null) {
            switch (item.getItemId()) {
                case R.id.action_share:
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(),
                            "%s - %s", model.getName().getFullName(), model.getSiteUrl()));
                    intent.setType("text/plain");
                    startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)));
                    break;
                case R.id.action_on_my_list:
                    DialogUtil.createSelection(this, R.string.app_filter_on_list,
                            onList == null ? 0 : !onList ? 1 : 2,
                            CompatUtil.INSTANCE.getStringList(this, R.array.on_list_values),
                            (dialog, which) -> {
                                if (which == DialogAction.POSITIVE) {
                                    switch (dialog.getSelectedIndex()) {
                                        case 0:
                                            onList = null;
                                            break;
                                        case 1:
                                            onList = false;
                                            break;
                                        case 2:
                                            onList = true;
                                            break;
                                    }
                                    reloadViewPager();
                                }
                            });
                    break;
            }
        } else
            NotifyUtil.INSTANCE.makeText(getApplicationContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        StaffPageAdapter pageAdapter = new StaffPageAdapter(getSupportFragmentManager(), getApplicationContext());
        pageAdapter.setParams(getViewModel().getParams());
        binding.contentMain.pageContainer.setAdapter(pageAdapter);
        binding.contentMain.pageContainer.setOffscreenPageLimit(offScreenLimit);
        binding.customTab.smartTab.setViewPager(binding.contentMain.pageContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getModel() == null)
            makeRequest();
        else
            updateUI();
    }

    @Override
    protected void updateUI() {
        StaffBase model = getModel();
        if(model != null)
            if(favouriteWidget != null)
                favouriteWidget.setModel(model);
    }

    @Override
    protected void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, id);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.STAFF_BASE_REQ, getApplicationContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable StaffBase model) {
        super.onChanged(model);
        updateUI();
    }

    private void reloadViewPager() {
        StaffPageAdapter adapter = new StaffPageAdapter(getSupportFragmentManager(), getApplicationContext());

        // Update params if necessary
        getViewModel().getParams().putLong(KeyUtil.arg_id, id);
        getViewModel().getParams().putSerializable(KeyUtil.arg_onList, onList);
        adapter.setParams(getViewModel().getParams());

        // Re-set adapter while preserving currently selected item
        int currentItem = binding.contentMain.pageContainer.getCurrentItem();
        binding.contentMain.pageContainer.setAdapter(adapter);
        binding.contentMain.pageContainer.setCurrentItem(currentItem);
    }
}
