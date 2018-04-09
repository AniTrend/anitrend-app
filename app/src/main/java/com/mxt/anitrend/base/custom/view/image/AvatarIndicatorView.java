package com.mxt.anitrend.base.custom.view.image;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetAvatarIndicatorBinding;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.activity.detail.NotificationActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.activity.index.LoginActivity;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/11/30.
 * avatar image view which will be capable of displaying
 * current notification count.
 */

public class AvatarIndicatorView extends FrameLayout implements CustomView, RetroCallback<Integer>, View.OnClickListener {

    private WidgetAvatarIndicatorBinding binding;
    private WidgetPresenter<Integer> presenter;
    private Integer notificationCount;
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
    }

    public void setImageSrc(String url) {
        checkLastSyncTime();
        AvatarImageView.setImage(binding.userAvatar, url);
    }

    public void checkLastSyncTime() {
        if(presenter.getApplicationPref().isAuthenticated())
            if(DateUtil.timeDifferenceSatisfied(KeyUtil.TIME_UNIT_MINUTES, mLastSynced, 2)) {
                mLastSynced = System.currentTimeMillis();
                presenter.requestData(KeyUtil.USER_CURRENT_REQ, getContext(), this);
            }
    }

    @Override
    public void onViewRecycled() {
        if(presenter != null)
            presenter.onDestroy();
    }

    private void showSetCounter() {
        binding.notificationCount.setText(String.valueOf(notificationCount));
        binding.container.setVisibility(VISIBLE);
    }

    private void hideSetCounter() {
        binding.notificationCount.setText(String.valueOf(notificationCount));
        binding.container.setVisibility(GONE);
    }

    public void resetCounter() {
        notificationCount = 0;
        hideSetCounter();
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
    public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
        try {
            if(response.isSuccessful() && (notificationCount = response.body()) != null) {
                if(notificationCount > 0) {
                    showSetCounter();
                    presenter.notifyAllListeners(new BaseConsumer<>(KeyUtil.USER_CURRENT_REQ, notificationCount), false);
                } else
                    hideSetCounter();
            } else
                Log.e(this.toString(), ErrorUtil.getError(response));
        } catch (Exception e) {
            e.printStackTrace();
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
    public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable throwable) {
        try {
            throwable.printStackTrace();
            Log.e(this.toString(), throwable.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if(presenter.getApplicationPref().isAuthenticated()) {
            if (view.getId() == R.id.user_avatar) {
                Intent intent;
                if (notificationCount != null && notificationCount > 0) {
                    resetCounter();
                    intent = new Intent(getContext(), NotificationActivity.class);
                } else {
                    intent = new Intent(getContext(), ProfileActivity.class);
                    intent.putExtra(KeyUtil.arg_userName, presenter.getDatabase().getCurrentUser().getName());
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
}
