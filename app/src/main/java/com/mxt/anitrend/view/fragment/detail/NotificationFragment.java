package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.NotificationAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.general.Notification;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.CommentActivity;
import com.mxt.anitrend.view.activity.detail.MessageActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.activity.detail.SeriesActivity;
import java.util.List;

/**
 * Created by max on 2017/12/06.
 */

public class NotificationFragment extends FragmentBaseList<Notification, List<Notification>, BasePresenter> {

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
        mColumnSize = R.integer.single_list_x1; inflateMenu = R.menu.shared_menu;
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new NotificationAdapter(model, getContext());
        injectAdapter();

        //Testing notifications by forcing the notification dispatcher
        /*for (int i = 0; i < 3; i++)
            NotificationDispatcher.createNotification(getContext(), new ArrayList<>(model.subList(i, i + 1)));*/
        // NotificationDispatcher.createNotification(getContext(), new ArrayList<>(model.subList(5, 6)));
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
                if(model != null) {
                    Stream.of(model).forEach(notification -> {
                        notification.setRead(true);
                        getPresenter().getDatabase().saveNotifications(notification);
                    });
                    if (mAdapter != null)
                        mAdapter.notifyDataSetChanged();
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

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        getViewModel().requestData(KeyUtils.USER_NOTIFICATION_REQ, getContext());
    }

    private void setReadItems(Notification data) {
        Stream.of(model).filter(value -> value.getObject_id() == data.getObject_id() || value.getId() == data.getId())
                .forEach(notification -> {
                    notification.setRead(true); getPresenter().getDatabase().saveNotifications(notification);
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
    public void onItemClick(View target, Notification data) {
        Intent intent;
        setReadItems(data);
        if(target.getId() == R.id.notification_img && data.getObject_type() != KeyUtils.NOTIFICATION_AIRING) {
            intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra(KeyUtils.arg_user_name, data.getUser().getDisplay_name());
            CompatUtil.startRevealAnim(getActivity(), target, intent);
        }
        else
            switch (data.getObject_type()) {
                case KeyUtils.NOTIFICATION_AIRING:
                    intent = new Intent(getActivity(), SeriesActivity.class);
                    intent.putExtra(KeyUtils.arg_id, data.getSeries().getId());
                    intent.putExtra(KeyUtils.arg_series_type, KeyUtils.SeriesTypes[KeyUtils.ANIME]);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.NOTIFICATION_COMMENT_FORUM:
                    if(data.getComment() != null) {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), data.getComment().getComment());
                    } else {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), String.valueOf(data.getMeta_value()));
                    }
                    break;
                case KeyUtils.NOTIFICATION_LIKE_FORUM:
                    if(data.getComment() != null) {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), data.getComment().getComment());
                    } else {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), String.valueOf(data.getMeta_value()));
                    }
                    break;
                case KeyUtils.NOTIFICATION_LIKE_ACTIVITY:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtils.arg_id, data.getObject_id());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.NOTIFICATION_REPLY_ACTIVITY:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtils.arg_id, data.getObject_id());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.NOTIFICATION_REPLY_FORUM:
                    if(data.getComment() != null) {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), data.getComment().getComment());
                    } else {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), String.valueOf(data.getMeta_value()));
                    }
                    break;
                case KeyUtils.NOTIFICATION_FOLLOW_ACTIVITY:
                    intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra(KeyUtils.arg_user_name, data.getUser().getDisplay_name());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.NOTIFICATION_DIRECT_MESSAGE:
                    intent = new Intent(getActivity(), MessageActivity.class);
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.NOTIFICATION_LIKE_ACTIVITY_REPLY:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtils.arg_id, data.getObject_id());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.NOTIFICATION_MENTION_ACTIVITY:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtils.arg_id, data.getObject_id());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.NOTIFICATION_LIKE_FORUM_COMMENT:
                    if(data.getComment() != null) {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), data.getComment().getComment());
                    } else {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), String.valueOf(data.getMeta_value()));
                    }
                    break;
                default:
                    if(data.getComment() != null) {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), data.getComment().getComment());
                    } else {
                        DialogUtil.createMessage(getContext(), data.getUser().getDisplay_name(), String.valueOf(data.getMeta_value()));
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
    public void onItemLongClick(View target, Notification data) {
        if(data.getObject_type() == KeyUtils.NOTIFICATION_AIRING) {
            setReadItems(data);
            if(getPresenter().getApplicationPref().isAuthenticated()) {
                seriesActionUtil = new SeriesActionUtil.Builder()
                        .setModel(data.getSeries()).build(getActivity());
                seriesActionUtil.startSeriesAction();
            } else
                NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
        }
    }
}
