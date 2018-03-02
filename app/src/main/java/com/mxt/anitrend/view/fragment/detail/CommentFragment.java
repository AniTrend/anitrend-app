package com.mxt.anitrend.view.fragment.detail;

import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.CommentAdapter;
import com.mxt.anitrend.adapter.recycler.index.StatusFeedAdapter;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseComment;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.entity.anilist.UserActivity;
import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.general.UserActivityReply;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.activity.detail.SeriesActivity;
import com.mxt.anitrend.view.sheet.BottomSheetEmoticon;
import com.mxt.anitrend.view.sheet.BottomSheetGiphy;
import com.mxt.anitrend.view.sheet.BottomSheetUsers;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/11/16.
 * Comment fragment
 */

public class CommentFragment extends FragmentBaseComment implements RetroCallback<UserActivity>, BaseConsumer.onRequestModelChange<UserActivityReply> {

    private StatusFeedAdapter feedAdapter;

    public static CommentFragment newInstance(Bundle params) {
        CommentFragment fragment = new CommentFragment();
        fragment.setArguments(params);
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
            if (getArguments().containsKey(KeyUtils.arg_model))
                userActivity = getArguments().getParcelable(KeyUtils.arg_model);
            if (getArguments().containsKey(KeyUtils.arg_id))
                userActivityId = getArguments().getInt(KeyUtils.arg_id);
        }
        mColumnSize = R.integer.single_list_x1;
        setPresenter(new WidgetPresenter<>(getContext()));
        setViewModel(true);
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to Activity.onStart of the containing Activity's lifecycle.
     * In this current context the Event bus is automatically registered for you
     *
     * @see EventBus
     */
    @Override
    public void onStart() {
        composerWidget.setLifecycle(getLifecycle());
        composerWidget.setItemClickListener(new ItemClickListener<Object>() {
            @Override
            public void onItemClick(View target, Object data) {
                switch (target.getId()) {
                    case R.id.insert_emoticon:
                        mBottomSheet = new BottomSheetEmoticon.Builder()
                                .setTitle(R.string.text_emoticons_scroll)
                                .build();
                        showBottomSheet();
                        break;
                    case R.id.insert_gif:
                        mBottomSheet = new BottomSheetGiphy.Builder()
                                .setTitle(R.string.title_bottom_sheet_giphy)
                                .build();

                        showBottomSheet();
                        break;
                    case R.id.widget_flipper:
                        CompatUtil.hideKeyboard(getActivity());
                        break;
                    default:
                        DialogUtil.createDialogAttachMedia(target.getId(), composerWidget.getEditor(), getContext());
                        break;
                }
            }

            @Override
            public void onItemLongClick(View target, Object data) {

            }
        });
        if(userActivity != null) {
            userActivityId = userActivity.getId();
            initExtraComponents();
        }
        else {
            Bundle params = getPresenter().getParams();
            params.putInt(KeyUtils.arg_id, userActivityId);
            getPresenter().requestData(KeyUtils.USER_ACTIVITY_BY_ID_REQ, getContext(), this);
        }
        super.onStart();
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new CommentAdapter(model, getContext());
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        Bundle bundle = getViewModel().getParams();
        bundle.putInt(KeyUtils.arg_id, userActivityId);
        getViewModel().requestData(KeyUtils.USER_ACTIVITY_REPLIES_BY_ID_REQ, getContext());
    }

    /**
     * Informs parent activity if on back can continue to super method or not
     * @return true to inform parent activity to
     */
    @Override
    public boolean onBackPress() {
        if(composerWidget != null)
            if(composerWidget.editBoxHasFocus(true))
                return true;
        return super.onBackPress();
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, UserActivityReply data) {
        Intent intent;
        switch (target.getId()) {
            case R.id.series_image:
                SeriesBase series = userActivity.getSeries();
                intent = new Intent(getActivity(), SeriesActivity.class);
                intent.putExtra(KeyUtils.arg_id, series.getId());
                intent.putExtra(KeyUtils.arg_series_type, series.getSeries_type());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.widget_mention:
                composerWidget.mentionUserFrom(data);
                break;
            case R.id.widget_edit:
                composerWidget.setModel(data);
                composerWidget.setRequestMode(KeyUtils.ACTIVITY_REPLY_EDIT_REQ);
                composerWidget.setText(data.getReply());
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
                intent = new Intent(getActivity(), ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(KeyUtils.arg_id, data.getUser().getId());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
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
    public void onItemLongClick(View target, UserActivityReply data) {

    }

    private void initExtraComponents() {
        composerWidget.setModel(userActivity);
        composerWidget.setRequestMode(KeyUtils.ACTIVITY_REPLY_REQ);

        if(feedAdapter == null) {
            feedAdapter = new StatusFeedAdapter(Collections.singletonList(userActivity), getContext());
            feedAdapter.setClickListener(new ItemClickListener<UserActivity>() {
                @Override
                public void onItemClick(View target, UserActivity data) {
                    Intent intent;
                    switch (target.getId()) {
                        case R.id.series_image:
                            intent = new Intent(getActivity(), SeriesActivity.class);
                            intent.putExtra(KeyUtils.arg_id, data.getSeries().getId());
                            intent.putExtra(KeyUtils.arg_series_type, data.getSeries().getSeries_type());
                            CompatUtil.startRevealAnim(getActivity(), target, intent);
                            break;
                        case R.id.widget_users:
                            List<UserBase> likes = data.getLikes();
                            if (likes.size() > 0) {
                                mBottomSheet = new BottomSheetUsers.Builder()
                                        .setModel(likes)
                                        .setTitle(R.string.title_bottom_sheet_likes)
                                        .build();
                                showBottomSheet();
                            } else
                                NotifyUtil.makeText(getActivity(), R.string.text_no_likes, Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.user_avatar:
                            intent = new Intent(getActivity(), ProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(KeyUtils.arg_id, data.getUsers().get(0).getId());
                            CompatUtil.startRevealAnim(getActivity(), target, intent);
                            break;
                    }
                }

                @Override
                public void onItemLongClick(View target, UserActivity data) {
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
            });
        }
        if(originRecycler.getAdapter() == null)
            originRecycler.setAdapter(feedAdapter);
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call     the origination requesting object
     * @param response the response from the network
     */
    @Override
    public void onResponse(@NonNull Call<UserActivity> call, @NonNull Response<UserActivity> response) {
        if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            if(response.isSuccessful() && response.body() != null) {
                userActivity = response.body();
                initExtraComponents();
            } else {
                NotifyUtil.createAlerter(getActivity(), getString(R.string.text_error_request),
                        ErrorUtil.getError(response), R.drawable.ic_warning_white_18dp, R.color.colorStateOrange);
            }
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call      the origination requesting object
     * @param throwable contains information about the error
     */
    @Override
    public void onFailure(@NonNull Call<UserActivity> call, @NonNull Throwable throwable) {
        if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            throwable.printStackTrace();
            NotifyUtil.createAlerter(getActivity(), getString(R.string.text_error_request),
                    throwable.getLocalizedMessage(), R.drawable.ic_warning_white_18dp, R.color.colorStateRed);
        }
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<UserActivityReply> consumer) {
        Optional<IntPair<UserActivityReply>> pairOptional;
        int pairIndex;
        switch (consumer.getRequestMode()) {
            case KeyUtils.ACTIVITY_REPLY_REQ:
                if(model != null && !model.isEmpty())
                    swipeRefreshLayout.setRefreshing(true);
                onRefresh();
                break;
            case KeyUtils.ACTIVITY_REPLY_EDIT_REQ:
                pairOptional = CompatUtil.findIndexOf(model, consumer.getChangeModel());
                if(pairOptional.isPresent()) {
                    pairIndex = pairOptional.get().getFirst();
                    model.set(pairIndex, consumer.getChangeModel());
                    mAdapter.onItemChanged(consumer.getChangeModel(), pairIndex);
                }
                break;
            case KeyUtils.ACTIVITY_REPLY_DELETE_REQ:
                pairOptional = CompatUtil.findIndexOf(model, consumer.getChangeModel());
                if(pairOptional.isPresent()) {
                    pairIndex = pairOptional.get().getFirst();
                    model.remove(pairIndex);
                    mAdapter.onItemRemoved(pairIndex);
                }
                break;
        }
        // resetting our components state after each request, this is important because the edit invocation sets its
        // own request type and we want the default in this class to be just a normal post
        initExtraComponents();
    }

    @Override
    public void onDestroyView() {
        if(composerWidget != null)
            composerWidget.onViewRecycled();
        super.onDestroyView();
    }
}
