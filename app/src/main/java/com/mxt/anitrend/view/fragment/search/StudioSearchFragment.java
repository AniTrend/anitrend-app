package com.mxt.anitrend.view.fragment.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.base.interfaces.event.PublisherListener;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.activity.detail.StudioActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by max on 2017/12/20.
 * studio search fragment
 */

public class StudioSearchFragment extends FragmentBaseList<StudioBase, List<StudioBase>, BasePresenter> implements PublisherListener<Favourite> {

    private String searchQuery;

    public static StudioSearchFragment newInstance(Bundle args) {
        StudioSearchFragment fragment = new StudioSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            searchQuery = getArguments().getString(KeyUtils.arg_search_query);
        setPresenter(new BasePresenter(getContext()));
        mColumnSize = R.integer.grid_list_x2;  isPager = false;
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new StudioAdapter(model, getContext());
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        if(TextUtils.isEmpty(searchQuery))
            return;
        Bundle bundle = getViewModel().getParams();
        bundle.putString(KeyUtils.arg_search_query, searchQuery);
        bundle.putInt(KeyUtils.arg_page, getPresenter().getCurrentPage());
        getViewModel().requestData(KeyUtils.STUDIO_SEARCH_REQ, getContext());
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, StudioBase data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), StudioActivity.class);
                intent.putExtra(KeyUtils.arg_id, data.getId());
                startActivity(intent);
                break;
        }
    }

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long click index
     */
    @Override
    public void onItemLongClick(View target, StudioBase data) {

    }

    /**
     * Responds to published events, be sure to add subscribe annotation
     *
     * @param param passed event
     * @see Subscribe
     */
    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(Favourite param) {
        onChanged(param.getStudio());
    }
}
