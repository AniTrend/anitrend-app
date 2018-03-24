package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesReviewAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.sheet.BottomReviewReader;

/**
 * Created by max on 2017/12/28.
 * Reviews for a given series
 */

public class ReviewFragment extends FragmentBaseList<Review, PageContainer<Review>, BasePresenter> {

    private @KeyUtils.MediaType String mediaType;
    private long mediaId;

    public static ReviewFragment newInstance(Bundle args) {
        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(args);
        return reviewFragment;
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
            mediaId = getArguments().getLong(KeyUtils.arg_id);
            mediaType = getArguments().getString(KeyUtils.arg_mediaType);
        }
        mColumnSize = R.integer.single_list_x1; isPager = true;
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
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
        if(mediaId == 0)
            return;
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtils.arg_mediaId, mediaId)
                .putVariable(KeyUtils.arg_mediaType, mediaType)
                .putVariable(KeyUtils.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtils.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtils.MEDIA_REVIEWS_REQ, getContext());
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
        Intent intent;
        switch (target.getId()) {
            case R.id.series_image:
                MediaBase mediaBase = data.getMedia();
                intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtils.arg_id, mediaBase.getId());
                intent.putExtra(KeyUtils.arg_mediaType, mediaBase.getType());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.user_avatar:
                if(getPresenter().getApplicationPref().isAuthenticated()) {
                    intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(KeyUtils.arg_id, data.getUser().getId());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                } else
                    NotifyUtil.makeText(getActivity(), R.string.info_login_req, R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
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
                            .setModel(data.getMedia()).build(getActivity());
                    seriesActionUtil.startSeriesAction();
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