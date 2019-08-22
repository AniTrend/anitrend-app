package com.mxt.anitrend.view.activity.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.view.fragment.detail.StudioMediaFragment;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.wax911.library.model.request.QueryContainerBuilder;

/**
 * Created by max on 2017/12/14.
 * StudioActivity
 */

public class StudioActivity extends ActivityBase<StudioBase, BasePresenter> {

    protected @BindView(R.id.toolbar)
    Toolbar toolbar;

    private StudioBase model;

    private FavouriteToolbarWidget favouriteWidget;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_generic);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setViewModel(true);
        setPresenter(new BasePresenter(this));
        if(getIntent().hasExtra(KeyUtil.arg_id))
            id = getIntent().getLongExtra(KeyUtil.arg_id, -1);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getViewModel().getParams().putLong(KeyUtil.arg_id, id);
        onActivityReady();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isAuth = getPresenter().getSettings().isAuthenticated();
        getMenuInflater().inflate(R.menu.custom_menu, menu);
        menu.findItem(R.id.action_favourite).setVisible(isAuth);
        if(isAuth) {
            MenuItem favouriteMenuItem = menu.findItem(R.id.action_favourite);
            favouriteWidget = (FavouriteToolbarWidget) favouriteMenuItem.getActionView();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(model != null) {
            switch (item.getItemId()) {
                case R.id.action_share:
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(),
                            "%s - %s", model.getName(), model.getSiteUrl()));
                    intent.setType("text/plain");
                    startActivity(intent);
                    break;
            }
        } else
            NotifyUtil.INSTANCE.makeText(getApplicationContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(model == null)
            makeRequest();
        else
            updateUI();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        mFragment = StudioMediaFragment.newInstance(getIntent().getExtras());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, mFragment, mFragment.TAG);
        fragmentTransaction.commit();
    }

    @Override
    protected void updateUI() {
        if(model != null) {
            if (favouriteWidget != null)
                favouriteWidget.setModel(model);
            mActionBar.setTitle(model.getName());
        }
    }

    @Override
    protected void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, id);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.STUDIO_BASE_REQ, getApplicationContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable StudioBase model) {
        super.onChanged(model);
        this.model = model;
        updateUI();
    }
}