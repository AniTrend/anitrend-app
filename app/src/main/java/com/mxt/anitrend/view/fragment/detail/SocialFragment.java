package com.mxt.anitrend.view.fragment.detail;

import android.annotation.SuppressLint;
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
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.CommentActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by max on 2018/01/05.
 */

public class SocialFragment extends FragmentBaseList<FeedList, List<FeedList>, BasePresenter> implements BaseConsumer.onRequestModelChange<FeedList>, PublisherListener<Media> {

    private @KeyUtils.ActivityType int requestType;
    private @KeyUtils.MediaType
    int seriesType;
    private long seriesId;

    private Media series;

    public static SocialFragment newInstance(Bundle args) {
        SocialFragment fragment = new SocialFragment();
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
            requestType = getArguments().getInt(KeyUtils.arg_request_type);
            seriesType = getArguments().getInt(KeyUtils.arg_series_type);
        }
        isPager = true; mColumnSize = R.integer.single_list_x1;
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
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        if(seriesId != 0) {
            Bundle bundle = getViewModel().getParams();
            bundle.putLong(KeyUtils.arg_id, seriesId);
            bundle.putInt(KeyUtils.arg_series_type, seriesType);
            bundle.putInt(KeyUtils.arg_page, getPresenter().getCurrentPage());
            bundle.putString(KeyUtils.arg_request_type, KeyUtils.ActivityTypes[requestType]);
            getViewModel().requestData(KeyUtils.SERIES_ACTIVITY_REQ, getContext());
        }
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
        if(!getPresenter().getApplicationPref().isAuthenticated()) {
            NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
            return;
        }
        switch (target.getId()) {
            case R.id.widget_comment:
                intent = new Intent(getActivity(), CommentActivity.class);
                intent.putExtra(KeyUtils.arg_id, data.getId());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
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

                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @SuppressLint("SwitchIntDef")
    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<FeedList> consumer) {
        switch (consumer.getRequestMode()) {
            case KeyUtils.ACTIVITY_CREATE_REQ:
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
                break;
            case KeyUtils.ACTIVITY_EDIT_REQ:
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
                break;
            case KeyUtils.ACTIVITY_DELETE_REQ:
                Optional<IntPair<FeedList>> pairOptional = CompatUtil.findIndexOf(model, consumer.getChangeModel());
                if(pairOptional.isPresent()) {
                    model.remove(pairOptional.get().getFirst());
                    mAdapter.onItemRemoved(pairOptional.get().getFirst());
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
        if(content != null)
            for (FeedList feedList : content)
                feedList.setSeries(series);
        super.onChanged(content);
    }


    /**
     * Responds to published events, be sure to add subscribe annotation
     *
     * @param param passed event
     * @see Subscribe
     */
    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(Media param) {
        if(model == null) {
            seriesId = param.getId();
            series = param;
            makeRequest();
        }
    }
}
