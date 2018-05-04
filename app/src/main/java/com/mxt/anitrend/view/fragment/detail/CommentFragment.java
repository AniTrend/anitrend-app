package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.CommentAdapter;
import com.mxt.anitrend.adapter.recycler.index.FeedAdapter;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseComment;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.anilist.FeedReply;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.sheet.BottomSheetEmoticon;
import com.mxt.anitrend.view.sheet.BottomSheetGiphy;
import com.mxt.anitrend.view.sheet.BottomSheetUsers;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

/**
 * Created by max on 2017/11/16.
 * Comment fragment
 */

public class CommentFragment extends FragmentBaseComment implements BaseConsumer.onRequestModelChange<FeedReply> {

    private FeedAdapter feedAdapter;

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
            if (getArguments().containsKey(KeyUtil.arg_model))
                feedList = getArguments().getParcelable(KeyUtil.arg_model);
            if (getArguments().containsKey(KeyUtil.arg_id))
                userActivityId = getArguments().getLong(KeyUtil.arg_id);
        }
        mColumnSize = R.integer.single_list_x1; hasSubscriber = true;
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
        if(feedList != null) {
            userActivityId = feedList.getId();
            initExtraComponents();
        }

        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, userActivityId);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.FEED_LIST_REPLY_REQ, getContext());
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
    public void onItemClick(View target, FeedReply data) {
        Intent intent;
        switch (target.getId()) {
            case R.id.series_image:
                MediaBase mediaBase = feedList.getMedia();
                intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtil.arg_id, mediaBase.getId());
                intent.putExtra(KeyUtil.arg_mediaType, mediaBase.getType());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
            case R.id.widget_mention:
                composerWidget.mentionUserFrom(data);
                break;
            case R.id.widget_edit:
                composerWidget.setModel(data, KeyUtil.MUT_SAVE_FEED_REPLY);
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
                intent.putExtra(KeyUtil.arg_id, data.getUser().getId());
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
    public void onItemLongClick(View target, FeedReply data) {

    }

    private void initExtraComponents() {
        composerWidget.setModel(feedList, KeyUtil.MUT_SAVE_FEED_REPLY);

        if(feedAdapter == null) {
            feedAdapter = new FeedAdapter(Collections.singletonList(feedList), getContext());
            feedAdapter.setClickListener(new ItemClickListener<FeedList>() {
                @Override
                public void onItemClick(View target, FeedList data) {
                    Intent intent;
                    switch (target.getId()) {
                        case R.id.series_image:
                            intent = new Intent(getActivity(), MediaActivity.class);
                            intent.putExtra(KeyUtil.arg_id, data.getMedia().getId());
                            intent.putExtra(KeyUtil.arg_mediaType, data.getMedia().getType());
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
                            intent.putExtra(KeyUtil.arg_id, data.getUser().getId());
                            CompatUtil.startRevealAnim(getActivity(), target, intent);
                            break;
                        case R.id.recipient_avatar:
                            intent = new Intent(getActivity(), ProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(KeyUtil.arg_id, data.getRecipient().getId());
                            CompatUtil.startRevealAnim(getActivity(), target, intent);
                            break;
                        case R.id.messenger_avatar:
                            intent = new Intent(getActivity(), ProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(KeyUtil.arg_id, data.getMessenger().getId());
                            CompatUtil.startRevealAnim(getActivity(), target, intent);
                            break;
                    }
                }

                @Override
                public void onItemLongClick(View target, FeedList data) {
                    switch (target.getId()) {
                        case R.id.series_image:
                            if(getPresenter().getApplicationPref().isAuthenticated()) {
                                mediaActionUtil = new MediaActionUtil.Builder()
                                        .setId(data.getMedia().getId()).build(getActivity());
                                mediaActionUtil.startSeriesAction();
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

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<FeedReply> consumer) {
        Optional<IntPair<FeedReply>> pairOptional;
        int pairIndex;
        switch (consumer.getRequestMode()) {
            case KeyUtil.MUT_SAVE_FEED_REPLY:
                if(!consumer.hasModel()) {
                    if (model != null && !model.isEmpty())
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
            case KeyUtil.MUT_DELETE_FEED_REPLY:
                pairOptional = CompatUtil.findIndexOf(model, consumer.getChangeModel());
                if(pairOptional.isPresent()) {
                    pairIndex = pairOptional.get().getFirst();
                    model.remove(pairIndex);
                    mAdapter.onItemRemoved(pairIndex);
                    NotifyUtil.makeText(getContext(), R.string.text_changes_saved, R.drawable.ic_check_circle_white_24dp, Toast.LENGTH_SHORT).show();
                    if(getActivity() != null)
                        getActivity().finish();
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

    @Override
    public void onChanged(@Nullable FeedList content) {
        super.onChanged(content);
        if(content != null) {
            feedList = content;
            initExtraComponents();
        } else
            NotifyUtil.createAlerter(getActivity(), R.string.text_error_request, R.string.layout_empty_response,
                    R.drawable.ic_warning_white_18dp, R.color.colorStateOrange);
    }
}
