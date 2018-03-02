package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesReviewAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.sheet.BottomReviewReader;

import java.util.List;

/**
 * Created by max on 2017/10/30.
 * Series review type
 */

public class SeriesReviewFragment extends FragmentBaseList<Review, List<Review>, BasePresenter> {

    private int id;
    private @KeyUtils.SeriesReviewType int reviewType;

    public static SeriesReviewFragment newInstance(@KeyUtils.SeriesReviewType int reviewType, int id) {
        Bundle args = new Bundle();
        args.putInt(KeyUtils.arg_id, id);
        args.putInt(KeyUtils.arg_request_type, reviewType);
        SeriesReviewFragment fragment = new SeriesReviewFragment();
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
        if(getArguments() != null) {
            id = getArguments().getInt(KeyUtils.arg_id);
            @KeyUtils.SeriesReviewType int reviewType = getArguments().getInt(KeyUtils.arg_request_type);
            this.reviewType = reviewType;
        }
        isPager = true; mColumnSize = R.integer.single_list_x1;
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overriden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new SeriesReviewAdapter(model, getContext());
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        Bundle bundle = getViewModel().getParams();
        bundle.putInt(KeyUtils.arg_id, id);
        getViewModel().requestData(reviewType, getContext());
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, Review data) {
        switch (target.getId()) {
            case R.id.series_image:
                break;
            case R.id.review_read_more:
                mBottomSheet = new BottomReviewReader.Builder()
                        .setReview(data)
                        .setTitle(R.string.drawer_title_reviews)
                        .build();
                showBottomSheet();
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
    public void onItemLongClick(View target, Review data) {
        switch (target.getId()) {
            case R.id.series_image:
            if(getPresenter().getApplicationPref().isAuthenticated()) {
                seriesActionUtil = new SeriesActionUtil.Builder()
                        .setModel(data.getAnime() != null? data.getAnime() : data.getManga()).build(getActivity());
                seriesActionUtil.startSeriesAction();
            } else
                NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
