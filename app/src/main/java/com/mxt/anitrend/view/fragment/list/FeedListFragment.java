package com.mxt.anitrend.view.fragment.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.FeedAdapter;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.NotifyUtil;
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

public class FeedListFragment extends FragmentBaseList<FeedList, PageContainer<FeedList>, BasePresenter> implements BaseConsumer.onRequestModelChange<FeedList> {

    protected QueryContainerBuilder queryContainer;

    public static FeedListFragment newInstance(Bundle params, QueryContainerBuilder queryContainerBuilder) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtil.arg_graph_params, queryContainerBuilder);
        FeedListFragment fragment = new FeedListFragment();
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
            queryContainer = getArguments().getParcelable(KeyUtil.arg_graph_params);
        isPager = true; isFeed = true; mColumnSize = R.integer.single_list_x1;
        hasSubscriber = true;
        mAdapter = new FeedAdapter(getContext());
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_post:
                mBottomSheet = new BottomSheetComposer.Builder()
                        .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
                        .setTitle(R.string.menu_title_new_activity_post)
                        .build();
                showBottomSheet();
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
        if(!TapTargetUtil.isActive(KeyUtil.KEY_POST_TYPE_TIP) && isFeed) {
            if (getPresenter().getSettings().shouldShowTipFor(KeyUtil.KEY_POST_TYPE_TIP)) {
                TapTargetUtil.buildDefault(getActivity(), R.string.tip_status_post_title, R.string.tip_status_post_text, R.id.action_post)
                        .setPromptStateChangeListener((prompt, state) -> {
                            if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                                getPresenter().getSettings().disableTipFor(KeyUtil.KEY_POST_TYPE_TIP);
                            if (state == MaterialTapTargetPrompt.STATE_DISMISSED)
                                TapTargetUtil.setActive(KeyUtil.KEY_POST_TYPE_TIP, true);
                        }).show();
                TapTargetUtil.setActive(KeyUtil.KEY_POST_TYPE_TIP, false);
            }
        }
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        queryContainer.putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.FEED_LIST_REQ, getContext());
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<FeedList> consumer) {
        Optional<IntPair<FeedList>> pairOptional;
        int pairIndex;
        switch (consumer.getRequestMode()) {
            case KeyUtil.MUT_SAVE_TEXT_FEED:
                if(consumer.getChangeModel() == null) {
                    swipeRefreshLayout.setRefreshing(true);
                    onRefresh();
                } else {
                    pairOptional = CompatUtil.INSTANCE.findIndexOf(mAdapter.getData(), consumer.getChangeModel());
                    if(pairOptional.isPresent()) {
                        pairIndex = pairOptional.get().getFirst();
                        mAdapter.onItemChanged(consumer.getChangeModel(), pairIndex);
                    }
                }
                break;
            case KeyUtil.MUT_SAVE_MESSAGE_FEED:
                if(consumer.getChangeModel() == null) {
                    swipeRefreshLayout.setRefreshing(true);
                    onRefresh();
                } else {
                    pairOptional = CompatUtil.INSTANCE.findIndexOf(mAdapter.getData(), consumer.getChangeModel());
                    if (pairOptional.isPresent()) {
                        pairIndex = pairOptional.get().getFirst();
                        mAdapter.onItemChanged(consumer.getChangeModel(), pairIndex);
                    }
                }
                break;
            case KeyUtil.MUT_DELETE_FEED:
                pairOptional = CompatUtil.INSTANCE.findIndexOf(mAdapter.getData(), consumer.getChangeModel());
                if(pairOptional.isPresent()) {
                    pairIndex = pairOptional.get().getFirst();
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
                getPresenter().setPageInfo(content.getPageInfo());
            if(!content.isEmpty())
                onPostProcessed(GraphUtil.INSTANCE.filterFeedList(getPresenter(), content.getPageData()));
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
    public void onItemClick(View target, IntPair<FeedList> data) {
        Intent intent;
        switch (target.getId()) {
            case R.id.series_image:
                MediaBase series = data.getSecond().getMedia();
                intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtil.arg_id, series.getId());
                intent.putExtra(KeyUtil.arg_mediaType, series.getType());
                CompatUtil.INSTANCE.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.widget_comment:
                intent = new Intent(getActivity(), CommentActivity.class);
                intent.putExtra(KeyUtil.arg_model, data.getSecond());
                CompatUtil.INSTANCE.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.widget_edit:
                mBottomSheet = new BottomSheetComposer.Builder().setUserActivity(data.getSecond())
                        .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
                        .setTitle(R.string.edit_status_title)
                        .build();
                showBottomSheet();
                break;
            case R.id.widget_users:
                List<UserBase> likes = data.getSecond().getLikes();
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
                if(data.getSecond().getUser() != null) {
                    intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(KeyUtil.arg_id, data.getSecond().getUser().getId());
                    CompatUtil.INSTANCE.startRevealAnim(getActivity(), target, intent);
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
    public void onItemLongClick(View target, IntPair<FeedList> data) {
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
