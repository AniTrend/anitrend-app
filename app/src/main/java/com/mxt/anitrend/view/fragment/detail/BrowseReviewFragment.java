package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.annimon.stream.IntPair;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.ReviewAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.Settings;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;
import com.mxt.anitrend.view.sheet.BottomReviewReader;

import java.util.Collections;

/**
 * Created by max on 2017/10/30.
 * Media review browse
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
        isPager = true; mColumnSize = R.integer.single_list_x1; isFilterable = true;
        mAdapter = new ReviewAdapter(getContext());
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_genre).setVisible(false);
        menu.findItem(R.id.action_tag).setVisible(false);
        menu.findItem(R.id.action_type).setVisible(false);
        menu.findItem(R.id.action_year).setVisible(false);
        menu.findItem(R.id.action_status).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getContext() != null)
            switch (item.getItemId()) {
                case R.id.action_sort:
                    DialogUtil.createSelection(getContext(), R.string.app_filter_sort, CompatUtil.INSTANCE.getIndexOf(KeyUtil.ReviewSortType,
                            getPresenter().getSettings().getReviewSort()), CompatUtil.INSTANCE.capitalizeWords(KeyUtil.ReviewSortType),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getSettings().setReviewSort(KeyUtil.ReviewSortType[dialog.getSelectedIndex()]);
                            });
                    return true;
                case R.id.action_order:
                    DialogUtil.createSelection(getContext(), R.string.app_filter_order, CompatUtil.INSTANCE.getIndexOf(KeyUtil.SortOrderType,
                            getPresenter().getSettings().getSortOrder()), CompatUtil.INSTANCE.getStringList(getContext(), R.array.order_by_types),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getSettings().saveSortOrder(KeyUtil.SortOrderType[dialog.getSelectedIndex()]);
                            });
                    return true;
            }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        Settings pref = getPresenter().getSettings();
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(true)
                .putVariable(KeyUtil.arg_mediaType, mediaType)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage())
                .putVariable(KeyUtil.arg_sort, pref.getReviewSort() + pref.getSortOrder());

        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_REVIEWS_REQ, getContext());
    }

    @Override
    public void onChanged(@Nullable PageContainer<Review> content) {
        if(content != null) {
            if(content.hasPageInfo())
                getPresenter().setPageInfo(content.getPageInfo());
            if(!content.isEmpty())
                onPostProcessed(content.getPageData());
            else
                onPostProcessed(Collections.emptyList());
        } else
            onPostProcessed(Collections.emptyList());
        if(mAdapter.getItemCount() < 1)
            onPostProcessed(null);
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, IntPair<Review> data) {
        switch (target.getId()) {
            case R.id.series_image:
                MediaBase mediaBase = data.getSecond().getMedia();
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtil.arg_id, mediaBase.getId());
                intent.putExtra(KeyUtil.arg_mediaType, mediaBase.getType());
                CompatUtil.INSTANCE.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.review_read_more:
                mBottomSheet = new BottomReviewReader.Builder()
                        .setReview(data.getSecond())
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
    public void onItemLongClick(View target, IntPair<Review> data) {
        switch (target.getId()) {
            case R.id.series_image:
                if(getPresenter().getSettings().isAuthenticated()) {
                    mediaActionUtil = new MediaActionUtil.Builder()
                            .setId(data.getSecond().getMedia().getId()).build(getActivity());
                    mediaActionUtil.startSeriesAction();
                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
