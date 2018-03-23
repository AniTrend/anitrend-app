package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.StatusFeedAdapter;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.base.interfaces.event.PublisherListener;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.presenter.activity.ProfilePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.CommentActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.activity.detail.MediaActivity;
import com.mxt.anitrend.view.sheet.BottomSheetComposer;
import com.mxt.anitrend.view.sheet.BottomSheetUsers;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by max on 2017/11/26.
 * user profile targeted feeds
 */

public class UserFeedFragment extends FragmentBaseList<FeedList, List<FeedList>, ProfilePresenter> implements PublisherListener<User>, BaseConsumer.onRequestModelChange<FeedList> {

    @KeyUtils.ActivityType int requestType;
    private UserBase userBase;

    public static UserFeedFragment newInstance(@KeyUtils.ActivityType int requestType) {
        Bundle args = new Bundle();
        args.putInt(KeyUtils.arg_request_type, requestType);
        UserFeedFragment fragment = new UserFeedFragment();
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
            requestType = getArguments().getInt(KeyUtils.arg_request_type);
        isPager = true; isMenuDisabled = true;
        mColumnSize = R.integer.single_list_x1;
        setPresenter(new ProfilePresenter(getContext()));
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
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        if(requestType == KeyUtils.MESSAGE)
            userBase = getPresenter().getDatabase().getCurrentUser();
        if(userBase != null) {
            Bundle bundle = getViewModel().getParams();
            bundle.putInt(KeyUtils.arg_page, getPresenter().getCurrentPage());
            bundle.putString(KeyUtils.arg_user_name, userBase.getName());
            bundle.putString(KeyUtils.arg_request_type, KeyUtils.ActivityTypes[requestType]);
            getViewModel().requestData(KeyUtils.USER_ACTIVITY_REQ, getContext());
        }
    }

    /**
     * Responds to published events, be sure to add subscribe annotation
     *
     * @param param passed event
     * @see Subscribe
     */
    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(User param) {
        if(userBase == null) {
            userBase = param;
            onRefresh();
        } else
            updateUI();
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
                intent.putExtra(KeyUtils.arg_media_type, series.getSeries_type());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.widget_comment:
                intent = new Intent(getActivity(), CommentActivity.class);
                intent.putExtra(KeyUtils.arg_model, data);
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.widget_edit:
                mBottomSheet = new BottomSheetComposer.Builder().setUserActivity(data)
                        .setRequestMode(KeyUtils.ACTIVITY_EDIT_REQ)
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
            case KeyUtils.ACTIVITY_CREATE_REQ:
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
                break;
            case KeyUtils.ACTIVITY_EDIT_REQ:
                pairOptional = CompatUtil.findIndexOf(model, consumer.getChangeModel());
                if(pairOptional.isPresent()) {
                    pairIndex = pairOptional.get().getFirst();
                    model.set(pairIndex, consumer.getChangeModel());
                    mAdapter.onItemChanged(consumer.getChangeModel(), pairIndex);
                }
                break;
            case KeyUtils.ACTIVITY_DELETE_REQ:
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
    public void onChanged(@Nullable List<FeedList> content) {
        super.onChanged(FilterProvider.getUserActivityFilter(getPresenter(), content));
    }
}