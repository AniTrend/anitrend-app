package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.data.DatabaseHelper;
import com.mxt.anitrend.databinding.WidgetFollowStateBinding;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.base.UserBase_;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/11/16.
 * widget that represents the state of an
 * external user, either following or not
 */

public class FollowStateWidget extends FrameLayout implements CustomView, View.OnClickListener, RetroCallback<ResponseBody> {

    private UserBase model;
    private boolean following;
    private WidgetFollowStateBinding binding;
    private WidgetPresenter<ResponseBody> presenter;

    public FollowStateWidget(Context context) {
        super(context);
        onInit();
    }

    public FollowStateWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public FollowStateWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        binding = WidgetFollowStateBinding.inflate(CompatUtil.getLayoutInflater(getContext()),this, true);
        presenter = new WidgetPresenter<>(getContext());
        binding.setOnClickListener(this);
    }

    public void setUserModel(UserBase model) {
        this.model = model;
        if(presenter.getApplicationPref().isAuthenticated())
            if(!presenter.isCurrentUser(model))
                setControlText();
            else
                setVisibility(GONE);
        else
            setVisibility(GONE);
    }

    private void setControlText() {
        following = presenter.getDatabase().getBoxStore(UserBase.class)
                .query().equal(UserBase_.id, model.getId())
                .build().count() > 0;
        if (following) {
            binding.userFollowStateContainer.setCardBackgroundColor(CompatUtil.getColor(getContext(), R.color.colorAccentDark));
            binding.userFollowStateText.setText(R.string.following);
        } else {
            binding.userFollowStateContainer.setCardBackgroundColor(CompatUtil.getColor(getContext(), R.color.colorAccent));
            binding.userFollowStateText.setText(R.string.follow);
        }
        resetFlipperState();
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        setVisibility(VISIBLE);
        if(presenter != null)
            presenter.onDestroy();
        resetFlipperState();
    }

    private void resetFlipperState() {
        if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.setDisplayedChild(WidgetPresenter.CONTENT_STATE);
    }

            @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.widget_flipper:
                if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext();
                    presenter.getParams().putLong(KeyUtils.arg_id, model.getId());
                    presenter.requestData(KeyUtils.ACTION_FOLLOW_TOGGLE_REQ, getContext(), this);
                }
                else
                    NotifyUtil.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean isAlive() {
        return getContext() != null && binding != null;
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
    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
        try {
            if(response.isSuccessful()) {
                DatabaseHelper database = presenter.getDatabase();
                if(following) database.removeUser(model);
                else database.addUser(model);
                if(database.getCurrentUser() != null)
                    presenter.notifyAllListeners(new BaseConsumer<>(KeyUtils.ACTION_FOLLOW_TOGGLE_REQ,
                            database.getCurrentUser()), false);
                if(isAlive())
                    setControlText();
            }
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
    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
        try {
            if (isAlive()) {
                throwable.printStackTrace();
                setControlText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
