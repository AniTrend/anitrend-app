package com.mxt.anitrend.view.fragment.index;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.StatusFeedAdapter;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainer;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.util.TapTargetUtil;
import com.mxt.anitrend.view.activity.detail.CommentActivity;
import com.mxt.anitrend.view.activity.detail.MediaActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.sheet.BottomSheetComposer;
import com.mxt.anitrend.view.sheet.BottomSheetUsers;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Created by max on 2017/11/07.
 * Home page feed base
 */

public class FeedFragment extends FragmentBaseList<FeedList, PageContainer<FeedList>, BasePresenter> implements BaseConsumer.onRequestModelChange<FeedList> {

    private QueryContainer queryContainer;

    public static FeedFragment newInstance(Bundle params, QueryContainer queryContainer) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtils.arg_graph_params, queryContainer);
        FeedFragment fragment = new FeedFragment();
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
            queryContainer = getArguments().getParcelable(KeyUtils.arg_graph_params);
        isPager = true; isFeed = true; mColumnSize = R.integer.single_list_x1;
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new StatusFeedAdapter(model, getContext());
        injectAdapter();
        if(!TapTargetUtil.isActive(KeyUtils.KEY_POST_TYPE_TIP)) {
            if (getPresenter().getApplicationPref().shouldShowTipFor(KeyUtils.KEY_POST_TYPE_TIP)) {
                TapTargetUtil.buildDefault(getActivity(), R.string.tip_status_post_title, R.string.tip_status_post_text, R.id.action_post)
                        .setPromptStateChangeListener((prompt, state) -> {
                            if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                                getPresenter().getApplicationPref().disableTipFor(KeyUtils.KEY_POST_TYPE_TIP);
                            if (state == MaterialTapTargetPrompt.STATE_DISMISSED)
                                TapTargetUtil.setActive(KeyUtils.KEY_POST_TYPE_TIP, true);
                        }).show();
                TapTargetUtil.setActive(KeyUtils.KEY_POST_TYPE_TIP, false);
            }
        }
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        queryContainer.setVariable(KeyUtils.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtils.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtils.FEED_LIST_REQ, getContext());
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, FeedList data) {
        Intent intent;
        switch (target.getId()) {
            case R.id.series_image:
                MediaBase series = data.getSeries();
                intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtils.arg_id, series.getId());
                intent.putExtra(KeyUtils.arg_media_type, series.getType());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.widget_comment:
                intent = new Intent(getActivity(), CommentActivity.class);
                intent.putExtra(KeyUtils.arg_model, data);
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.widget_edit:
                mBottomSheet = new BottomSheetComposer.Builder().setUserActivity(data)
                        .setRequestMode(KeyUtils.MUT_SAVE_TEXT_FEED)
                        .setTitle(R.string.edit_status_title)
                        .build();
                showBottomSheet();
                break;
            case R.id.widget_users:
                List<UserBase> likes = data.getLikes();
                if(likes.size() > 0) {
                    mBottomSheet = new BottomSheetUsers.Builder()
                            .setModel(likes)
                            .setTitle(R.string.title_bottom_sheet_likes)
                            .build();
                    showBottomSheet();
                } else
                    NotifyUtil.makeText(getActivity(), R.string.text_no_likes, Toast.LENGTH_SHORT).show();
                break;
            case R.id.user_avatar:
                if(data.getUsers() != null) {
                    intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(KeyUtils.arg_id, data.getUsers().get(0).getId());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                }
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
    public void onItemLongClick(View target, FeedList data) {
        switch (target.getId()) {
            case R.id.series_image:
                if(getPresenter().getApplicationPref().isAuthenticated()) {
                    seriesActionUtil = new SeriesActionUtil.Builder()
                            .setModel(data.getSeries()).build(getActivity());
                    seriesActionUtil.startSeriesAction();
                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<FeedList> consumer) {
        Optional<IntPair<FeedList>> pairOptional;
        int pairIndex;
        switch (consumer.getRequestMode()) {
            case KeyUtils.MUT_SAVE_TEXT_FEED:
                if(consumer.getChangeModel() == null) {
                    swipeRefreshLayout.setRefreshing(true);
                    onRefresh();
                } else {
                    pairOptional = CompatUtil.findIndexOf(model, consumer.getChangeModel());
                    if(pairOptional.isPresent()) {
                        pairIndex = pairOptional.get().getFirst();
                        model.set(pairIndex, consumer.getChangeModel());
                        mAdapter.onItemChanged(consumer.getChangeModel(), pairIndex);
                    }
                }
                break;
            case KeyUtils.MUT_DELETE_FEED:
                pairOptional = CompatUtil.findIndexOf(model, consumer.getChangeModel());
                if(pairOptional.isPresent()) {
                    pairIndex = pairOptional.get().getFirst();
                    model.remove(pairIndex);
                    mAdapter.onItemRemoved(pairIndex);
                }
                break;
        }
    }

    /**
     * Called when the model state is changed.
     *
     * @param content The new data
     */
    @Override
    public void onChanged(@Nullable PageContainer<FeedList> content) {
        if(content != null) {
            if(content.hasPageInfo())
                pageInfo = content.getPageInfo();
            if(!content.isEmpty())
                onPostProcessed(content.getPageData());
            else
                onPostProcessed(Collections.emptyList());
        }
        if(model == null)
            onPostProcessed(null);
    }

    /**
     * Called to refresh an action mode's action menu whenever it is invalidated.
     *
     * @param mode ActionMode being prepared
     * @param menu Menu used to populate action buttons
     * @return true if the menu or action mode was updated, false otherwise.
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        menu.findItem(R.id.action_bookmark).setVisible(true);
        return true;
    }

    /**
     * Called to report a user click on an action button.
     *
     * @param mode The current ActionMode
     * @param item The item that was clicked
     * @return true if this callback handled the event, false if the standard MenuItem
     * invocation should continue.
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<FeedList> selected = getActionMode().getSelectedItems();
        switch (item.getItemId()) {
            case R.id.action_bookmark:
                break;
            case R.id.action_delete:
                break;
        }
        return true;
    }
}
