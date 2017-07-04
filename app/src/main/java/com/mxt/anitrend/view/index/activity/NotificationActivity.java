package com.mxt.anitrend.view.index.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.user.UserNotificationAdapter;
import com.mxt.anitrend.api.structure.UserNotification;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.view.StatefulRecyclerView;
import com.mxt.anitrend.event.NotificationClickListener;
import com.mxt.anitrend.presenter.index.NotificationPresenter;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.util.ErrorHandler;
import com.mxt.anitrend.view.detail.activity.AnimeActivity;
import com.mxt.anitrend.view.detail.activity.UserReplyActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends DefaultActivity implements Callback<List<UserNotification>>, SwipeRefreshLayout.OnRefreshListener, NotificationClickListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.content_notification) ProgressLayout progressLayout;
    @BindView(R.id.notification_pull_refresh) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.notification_recycler) StatefulRecyclerView recyclerView;
    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    private List<UserNotification> mListNotifications;
    private ActionBar mActionBar;
    private NotificationPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if ((mActionBar = getSupportActionBar()) != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        startInit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void startInit() {
        refreshLayout.setOnRefreshListener(this);
        progressLayout.showLoading();
        GridLayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), getResources().getInteger(R.integer.list_col_size));
        recyclerView.setLayoutManager(mLayoutManager);
        mPresenter = new NotificationPresenter(this);
        mPresenter.beginAsync(this);
    }

    @Override
    protected void updateUI() {
        RecyclerViewAdapter mAdapter = new UserNotificationAdapter(mListNotifications, getApplicationContext(), mPresenter.getAppPrefs(),this);
        recyclerView.setAdapter(mAdapter);
        progressLayout.showContent();
        /*
        Testing notifications
        ArrayList<UserNotification> range_filter = new ArrayList<>(mListNotifications.subList(0, 3));
        NotificationDispatcher.createNotification(getApplicationContext(), range_filter);
        */
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call
     * @param response
     */
    @Override
    public void onResponse(Call<List<UserNotification>> call, Response<List<UserNotification>> response) {
        if(!isDestroyed() || !isFinishing()) {
            if(response.isSuccessful() && response.body() != null) {
                mListNotifications = response.body();
                updateUI();
            } else
                progressLayout.showError(ContextCompat.getDrawable(getApplicationContext(), R.drawable.request_error),
                        ErrorHandler.getError(response).toString(), getString(R.string.try_again), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.beginAsync(NotificationActivity.this);
                    }
                });
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call
     * @param t
     */
    @Override
    public void onFailure(Call<List<UserNotification>> call, Throwable t) {
        if(!isDestroyed() || !isFinishing()) {
            progressLayout.showError(ContextCompat.getDrawable(getApplicationContext(), R.drawable.request_error), t.getLocalizedMessage(), getString(R.string.try_again), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mPresenter != null) {
                        progressLayout.showLoading();
                        mPresenter.beginAsync(NotificationActivity.this);
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.setParcelable(recyclerView.onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPresenter.getSavedParse() != null)
            recyclerView.onRestoreInstanceState(mPresenter.getSavedParse());
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(false);
        if(mPresenter == null) return;
        mPresenter.beginAsync(this);
        progressLayout.showLoading();
    }

    @Override
    public void onNotificationClick(int index) {
        UserNotification notification = mListNotifications.get(index);
        Intent starter;
        switch (notification.getObject_type()) {
            case NotificationClickListener.TYPE_AIRING:
                starter = new Intent(getApplicationContext(), AnimeActivity.class);
                starter.putExtra(AnimeActivity.MODEL_BANNER_KEY, notification.getSeries().getImage_url_banner());
                starter.putExtra(AnimeActivity.MODEL_ID_KEY, notification.getSeries().getId());
                starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(starter);
                break;
            case NotificationClickListener.TYPE_COMMENT_FORUM:
                if(notification.getComment() != null) {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), notification.getComment().getComment());
                } else {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), String.valueOf(notification.getMeta_value()));
                }
                break;
            case NotificationClickListener.TYPE_LIKE_FORUM:
                if(notification.getComment() != null) {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), notification.getComment().getComment());
                } else {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), String.valueOf(notification.getMeta_value()));
                }
                break;
            case NotificationClickListener.TYPE_LIKE_ACTIVITY:
                starter = new Intent(this, UserReplyActivity.class);
                starter.putExtra(UserReplyActivity.USER_ACTIVITY_NOTIFICATION_EXTRA, notification.getObject_id());
                startActivity(starter);
                break;
            case NotificationClickListener.TYPE_REPLY_ACTIVITY:
                starter = new Intent(this, UserReplyActivity.class);
                starter.putExtra(UserReplyActivity.USER_ACTIVITY_NOTIFICATION_EXTRA, notification.getObject_id());
                startActivity(starter);
                break;
            case NotificationClickListener.TYPE_REPLY_FORUM:
                if(notification.getComment() != null) {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), notification.getComment().getComment());
                } else {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), String.valueOf(notification.getMeta_value()));
                }
                break;
            case NotificationClickListener.TYPE_FOLLOW_ACTIVITY:
                starter = new Intent(this, UserProfileActivity.class);
                starter.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, notification.getUser());
                startActivity(starter);
                break;
            case NotificationClickListener.TYPE_DIRECT_MESSAGE:
                starter = new Intent(this, MessageActivity.class);
                startActivity(starter);
                break;
            case NotificationClickListener.TYPE_LIKE_ACTIVITY_REPLY:
                starter = new Intent(this, UserReplyActivity.class);
                starter.putExtra(UserReplyActivity.USER_ACTIVITY_NOTIFICATION_EXTRA, notification.getObject_id());
                startActivity(starter);
                break;
            case NotificationClickListener.TYPE_MENTION_ACTIVITY:
                starter = new Intent(this, UserReplyActivity.class);
                starter.putExtra(UserReplyActivity.USER_ACTIVITY_NOTIFICATION_EXTRA, notification.getObject_id());
                startActivity(starter);
                break;
            case NotificationClickListener.TYPE_LIKE_FORUM_COMMENT:
                //Change this when forum support is finished
                if(notification.getComment() != null) {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), notification.getComment().getComment());
                } else {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), String.valueOf(notification.getMeta_value()));
                }
                break;
            default:
                if(notification.getComment() != null) {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), notification.getComment().getComment());
                } else {
                    new DialogManager(NotificationActivity.this).createDialogMessage(notification.getUser().getDisplay_name(), String.valueOf(notification.getMeta_value()));
                }
                break;
        }
    }
}
