package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.annimon.stream.IntPair;
import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.NotificationAdapter;
import com.mxt.anitrend.base.custom.async.ThreadPool;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Notification;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.base.NotificationHistory;
import com.mxt.anitrend.model.entity.base.NotificationHistory_;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.CommentActivity;
import com.mxt.anitrend.view.activity.detail.MediaActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;

import java.util.Collections;
import java.util.List;

/**
 * Created by max on 2017/12/06.
 * NotificationFragment
 */

public class NotificationFragment extends FragmentBaseList<Notification, PageContainer<Notification>, BasePresenter> {

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColumnSize = R.integer.single_list_x1; isPager = true;
        setInflateMenu(R.menu.notification_menu);
        mAdapter = new NotificationAdapter(getContext());
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        long historyItems = getPresenter().getDatabase().getBoxStore(NotificationHistory.class).count();
        if(historyItems < 1)
            markAllNotificationsAsRead();
        injectAdapter();

        User currentUser = getPresenter().getDatabase().getCurrentUser();
        currentUser.setUnreadNotificationCount(0);
        getPresenter().getDatabase().saveCurrentUser(currentUser);

        //Testing notifications by forcing the notification dispatcher
        /*for (int i = 0; i < 3; i++)
            NotificationUtil.createNotification(getContext(), new ArrayList<>(model.subList(i, i + 1)));*/
        // NotificationUtil.createNotification(getContext(), new ArrayList<>(model.subList(5, 6)));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_mark_all).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_mark_all:
                if(mAdapter.getItemCount() > 0) {
                    new ThreadPool.Builder()
                            .build().execute(this::markAllNotificationsAsRead);
                } else
                    NotifyUtil.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChanged(@Nullable PageContainer<Notification> content) {
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
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage())
                .putVariable(KeyUtil.arg_resetNotificationCount, true);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.USER_NOTIFICATION_REQ, getContext());
    }

    /**
     * Ran on a background thread to assure we don't skip frames
     * @see ThreadPool
     */
    private void setItemAsRead(final Notification data) {
        new ThreadPool.Builder().build()
                .execute(() -> {
                    boolean isNotificationRead = getPresenter().getDatabase().getBoxStore(NotificationHistory.class)
                            .query().equal(NotificationHistory_.id, data.getId()).build().count() != 0;
                    if (!isNotificationRead) {
                        List<NotificationHistory> dismissibleNotifications = Stream.of(mAdapter.getData())
                                .filter(item -> item.getActivityId() != 0 && item.getActivityId() == data.getActivityId())
                                .map(item -> new NotificationHistory(item.getId()))
                                .toList();

                        if (!CompatUtil.isEmpty(dismissibleNotifications))
                            getPresenter().getDatabase().getBoxStore(NotificationHistory.class)
                                    .put(dismissibleNotifications);
                        else
                            getPresenter().getDatabase().getBoxStore(NotificationHistory.class)
                                    .put(new NotificationHistory(data.getId()));
                    }
                });

    }

    /**
     * Ran on a background thread to assure we don't skip frames
     * @see ThreadPool
     */
    private void markAllNotificationsAsRead() {
        List<NotificationHistory> notificationHistories = Stream.of(mAdapter.getData())
                .map(notification -> new NotificationHistory(notification.getId()))
                .toList();

        getPresenter().getDatabase().getBoxStore(NotificationHistory.class)
                .put(notificationHistories);

        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                if(mAdapter != null)
                    mAdapter.notifyDataSetChanged();
            });
    }


    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, IntPair<Notification> data) {
        Intent intent;
        setItemAsRead(data.getSecond());
        if(target.getId() == R.id.notification_img && !CompatUtil.equals(data.getSecond().getType(), KeyUtil.AIRING)) {
            intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra(KeyUtil.arg_id, data.getSecond().getUser().getId());
            CompatUtil.startRevealAnim(getActivity(), target, intent);
        }
        else
            switch (data.getSecond().getType()) {
                case KeyUtil.ACTIVITY_MESSAGE:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtil.arg_id, data.getSecond().getActivityId());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtil.FOLLOWING:
                    intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra(KeyUtil.arg_id, data.getSecond().getUser().getId());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtil.ACTIVITY_MENTION:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtil.arg_id, data.getSecond().getActivityId());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtil.THREAD_COMMENT_MENTION:
                    DialogUtil.createMessage(getContext(), data.getSecond().getUser().getName(), data.getSecond().getContext());
                    break;
                case KeyUtil.THREAD_SUBSCRIBED:
                    DialogUtil.createMessage(getContext(), data.getSecond().getUser().getName(), data.getSecond().getContext());
                    break;
                case KeyUtil.THREAD_COMMENT_REPLY:
                    DialogUtil.createMessage(getContext(), data.getSecond().getUser().getName(), data.getSecond().getContext());
                    break;
                case KeyUtil.AIRING:
                    intent = new Intent(getActivity(), MediaActivity.class);
                    intent.putExtra(KeyUtil.arg_id, data.getSecond().getMedia().getId());
                    intent.putExtra(KeyUtil.arg_mediaType, data.getSecond().getMedia().getType());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtil.ACTIVITY_LIKE:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtil.arg_id, data.getSecond().getActivityId());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtil.ACTIVITY_REPLY:
                case KeyUtil.ACTIVITY_REPLY_SUBSCRIBED:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtil.arg_id, data.getSecond().getActivityId());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtil.ACTIVITY_REPLY_LIKE:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtil.arg_id, data.getSecond().getActivityId());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtil.THREAD_LIKE:
                    DialogUtil.createMessage(getContext(), data.getSecond().getUser().getName(), data.getSecond().getContext());
                    break;
                case KeyUtil.THREAD_COMMENT_LIKE:
                    DialogUtil.createMessage(getContext(), data.getSecond().getUser().getName(), data.getSecond().getContext());
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
    public void onItemLongClick(View target, IntPair<Notification> data) {
        if(CompatUtil.equals(data.getSecond().getType(), KeyUtil.AIRING)) {
            setItemAsRead(data.getSecond());
            if(getPresenter().getApplicationPref().isAuthenticated()) {
                mediaActionUtil = new MediaActionUtil.Builder()
                        .setId(data.getSecond().getMedia().getId()).build(getActivity());
                mediaActionUtil.startSeriesAction();
            } else
                NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
        }
    }
}
