package com.mxt.anitrend.base.custom.view.image;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetAvatarIndicatorBinding;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.NotificationActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.activity.index.LoginActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by max on 2017/11/30.
 * avatar image view which will be capable of displaying
 * current notification count.
 */

public class AvatarIndicatorView extends FrameLayout implements CustomView, View.OnClickListener, BaseConsumer.onRequestModelChange<User> {

    private WidgetAvatarIndicatorBinding binding;
    private BasePresenter presenter;
    private User currentUser;
    private long mLastSynced;

    public AvatarIndicatorView(Context context) {
        super(context);
        onInit();
    }

    public AvatarIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public AvatarIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @Override
    public void onInit() {
        presenter = new WidgetPresenter<>(getContext());
        binding = WidgetAvatarIndicatorBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
        binding.setOnClickListener(this);
        checkLastSyncTime();
    }

    private void checkLastSyncTime() {
        if(presenter.getApplicationPref().isAuthenticated()) {
            if ((currentUser = presenter.getDatabase().getCurrentUser()) != null) {
                if (currentUser.getUnreadNotificationCount() < 1)
                    hideNotificationCountWidget();
                else
                    showNotificationWidget();
            } else {
                hideNotificationCountWidget();
            }
            if (DateUtil.timeDifferenceSatisfied(KeyUtil.TIME_UNIT_MINUTES, mLastSynced, 2))
                mLastSynced = System.currentTimeMillis();
            AvatarImageView.setImage(binding.userAvatar, currentUser.getAvatar());

        }
    }

    @Override
    public void onViewRecycled() {
        if(presenter != null)
            presenter.onDestroy();
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<User> consumer) {
        if(consumer.getRequestMode() == KeyUtil.USER_CURRENT_REQ) {
            presenter.getDatabase().saveCurrentUser(consumer.getChangeModel());
            checkLastSyncTime();
        }
    }

    private void showNotificationWidget() {
        binding.notificationCount.setText(String.valueOf(currentUser.getUnreadNotificationCount()));
        binding.container.setVisibility(VISIBLE);
    }

    private void hideNotificationCountWidget() {
        binding.notificationCount.setText(String.valueOf(currentUser.getUnreadNotificationCount()));
        binding.container.setVisibility(GONE);
    }

    private void resetCounter() {
        currentUser.setUnreadNotificationCount(0);
        hideNotificationCountWidget();
    }

    @Override
    public void onClick(View view) {
        if(presenter.getApplicationPref().isAuthenticated()) {
            if (view.getId() == R.id.user_avatar) {
                Intent intent;
                if (currentUser.getUnreadNotificationCount() > 0) {
                    resetCounter();
                    intent = new Intent(getContext(), NotificationActivity.class);
                } else {
                    intent = new Intent(getContext(), ProfileActivity.class);
                    intent.putExtra(KeyUtil.arg_userName, currentUser.getName());
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        } else {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }
}
