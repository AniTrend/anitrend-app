package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.ReviewAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.view.sheet.BottomReviewReader;

/**
 * Created by max on 2017/10/30.
 * Media review browse
 * // TODO: 2018/03/23 Add review sorting
 */

public class BrowseReviewFragment extends FragmentBaseList<Review, PageContainer<Review>, BasePresenter> {

    private @KeyUtil.MediaType String mediaType;

    public static BrowseReviewFragment newInstance(@KeyUtil.MediaType String mediaType) {
        Bundle args = new Bundle();
        args.putString(KeyUtil.arg_mediaType, mediaType);
        BrowseReviewFragment fragment = new BrowseReviewFragment();
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
            mediaType = getArguments().getString(KeyUtil.arg_mediaType);
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
            mAdapter = new ReviewAdapter(model, getContext());
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(true)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage())
                .putVariable(KeyUtil.arg_mediaType, mediaType);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_REVIEWS_REQ, getContext());
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
                mediaActionUtil = new MediaActionUtil.Builder()
                        .setModel(data.getMedia()).build(getActivity());
                mediaActionUtil.startSeriesAction();
            } else
                NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onChanged(@Nullable PageContainer<Review> content) {
        if(content != null) {
            if (content.hasPageInfo())
                pageInfo = content.getPageInfo();
            if (!content.isEmpty())
                onPostProcessed(content.getPageData());
        }
    }
}
